/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2024 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.cutlass.mqtt;

import io.questdb.cairo.CairoEngine;
import io.questdb.cairo.TableToken;
import io.questdb.cairo.wal.WalWriter;
import io.questdb.cutlass.auth.SocketAuthenticator;
import io.questdb.cutlass.pgwire.BadProtocolException;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.SqlExecutionContextImpl;
import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.network.*;
import io.questdb.std.MemoryTag;
import io.questdb.std.Misc;
import io.questdb.std.Os;
import io.questdb.std.Unsafe;
import io.questdb.std.str.DirectUtf8String;
import org.jetbrains.annotations.NotNull;

import static io.questdb.network.IODispatcher.DISCONNECT_REASON_UNKNOWN_OPERATION;

public class MqttConnectionContext extends IOContext<MqttConnectionContext> {
    private static Log LOG = LogFactory.getLog(MqttConnectionContext.class);
    private static int recvBufferSize = 4096;
    private static int sendBufferSize = 4096;
    private final ConnackPacket connackPacket = new ConnackPacket();
    private final ConnectPacket connectPacket = new ConnectPacket();
    private final PubackPacket pubackPacket = new PubackPacket();
    private final PublishPacket publishPacket = new PublishPacket();
    private SocketAuthenticator authenticator;
    private boolean connected = false;
    private CairoEngine engine;
    private long recvBuffer;
    private long recvBufferPtr;
    private long recvBufferReadOffset = 0;
    private long recvBufferWriteOffset = 0;
    private long sendBuffer;
    private long sendBufferLimit;
    private long sendBufferPtr;
    private long sendBufferReadOffset = 0;
    private long sendBufferWriteOffset = 0;
    private SuspendEvent suspendEvent;


    public MqttConnectionContext(
            CairoEngine engine,
            MqttServerConfiguration configuration) {
        super(configuration.getFactoryProvider().getMqttSocketFactory(),
                configuration.getNetworkFacade(),
                LOG,
                engine.getMetrics().getMqttMetrics().getConnectionCountGauge());
        this.engine = engine;
    }

    @Override
    public void close() {
        Misc.free(authenticator);
    }

    public boolean handleClientOperation(int operation)
            throws HeartBeatException, PeerIsSlowToReadException, ServerDisconnectException, PeerIsSlowToWriteException, QueryPausedException, PeerDisconnectedException, BadProtocolException, MqttException {
        switch (operation) {
            case IOOperation.READ:
                handleClientRecv();
                break;
            case IOOperation.WRITE:
//                handleClientSend();
                break;
            case IOOperation.HEARTBEAT:
                throw registerDispatcherHeartBeat();
            default:
                throw registerDispatcherDisconnect(DISCONNECT_REASON_UNKNOWN_OPERATION);
        }
        return false; // return useful;
    }

    @Override
    public MqttConnectionContext of(long fd, @NotNull IODispatcher<MqttConnectionContext> dispatcher) {
        super.of(fd, dispatcher);

        if (recvBuffer == 0) {
            this.recvBuffer = Unsafe.malloc(recvBufferSize, MemoryTag.NATIVE_MQTT_CONN);
            this.recvBufferPtr = recvBuffer;
        }
        if (sendBuffer == 0) {
            this.sendBuffer = Unsafe.malloc(sendBufferSize, MemoryTag.NATIVE_MQTT_CONN);
            this.sendBufferLimit = (sendBuffer + sendBufferSize);
            this.sendBufferPtr = sendBuffer;
        }
        authenticator.init(socket, recvBuffer, recvBuffer + recvBufferSize, sendBuffer, sendBufferLimit);
        return this;
    }

    public void setAuthenticator(SocketAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setSuspendEvent(SuspendEvent suspendEvent) {
        this.suspendEvent = suspendEvent;
    }

    private boolean handleClientRecv() throws PeerIsSlowToReadException, PeerIsSlowToWriteException, ServerDisconnectException, MqttException, PeerDisconnectedException {
        boolean busyRecv = true;
        try {
            // this is address of where header ended in our receive buffer
            // we need to being processing request content starting from this address
            long headerEnd = recvBuffer;
            int read = 0;

            read = socket.recv(recvBufferPtr, recvBufferSize);

            LOG.debug().$("recv [fd=").$(getFd()).$(", count=").$(read).I$();
            if (read < 0) {
                throw registerDispatcherDisconnect(DISCONNECT_REASON_UNKNOWN_OPERATION);
            }

            busyRecv = false;

            byte fhb = FirstHeaderByte.decode(recvBufferPtr);
            byte type = FirstHeaderByte.getType(fhb);

            if (type != PacketType.CONNECT && !connected) {
                throw new MqttException(); // protocol error
            }

            switch (type) {
                case PacketType.CONNECT:
                    // parse connect
                    connectPacket.clear();
                    connectPacket.parse(recvBufferPtr);

                    // send connack
                    connackPacket.clear();
                    int connackPacketLength = connackPacket.success().unparse(sendBufferPtr);
                    socket.send(sendBufferPtr, connackPacketLength);

                    connected = true;
                    sendBufferPtr = sendBuffer;
                    recvBufferPtr = recvBuffer;
                    break;
                case PacketType.PUBLISH:
                    // parse publish
                    int publishPacketLength = publishPacket.parse(recvBufferPtr);

                    // make table if needed
                    TableToken tableToken = engine.getTableTokenIfExists("mqtt");
                    if (tableToken == null) {
                        try (SqlExecutionContext executionContext = new SqlExecutionContextImpl(engine, 1).with(engine.getConfiguration().getFactoryProvider().getSecurityContextFactory().getRootContext(),
                                null,
                                null)) {
                            engine.ddl("" +
                                            "CREATE TABLE mqtt ( timestamp TIMESTAMP, " +
                                            "topic VARCHAR, " +
                                            "qos BYTE, " +
                                            "retain BOOLEAN, " +
                                            "clientId VARCHAR, " +
                                            "payloadBinary BINARY, " +
                                            "payloadVarchar VARCHAR) " +
                                            " TIMESTAMP(timestamp) PARTITION BY DAY WAL;",
                                    executionContext);
                        } catch (SqlException ignore) {
                            throw new RuntimeException();
                        }
                        tableToken = engine.getTableTokenIfExists("mqtt");
                        if (tableToken == null) {
                            throw new RuntimeException();
                        }
                    }

                    // commit to wal
                    try (WalWriter walWriter = engine.getWalWriter(tableToken)) {
                        var row = walWriter.newRow(Os.currentTimeMicros());
                        row.putVarchar(1, publishPacket.topicName);
                        row.putByte(2, publishPacket.qos);
                        row.putBool(3, publishPacket.retain == 1);
                        row.putVarchar(4, connectPacket.clientId);
                        if (publishPacket.payloadFormatIndicator == 1) {
                            row.putVarchar(6, new DirectUtf8String().of(publishPacket.payloadPtr, publishPacket.payloadPtr + publishPacket.payloadLength));
                        } else {
                            row.putBin(5, publishPacket.payloadPtr, publishPacket.payloadLength);
                        }
                        row.append();
                        walWriter.commit();
                    }

                    if (publishPacket.qos == 1) {
                        pubackPacket.packetIdentifier = publishPacket.packetIdentifier;
                        pubackPacket.reasonCode = ReasonCode.REASON_SUCCESS;
                        int size = pubackPacket.unparse(sendBufferPtr);
                        socket.send(sendBufferPtr, size);
                        sendBufferPtr = sendBuffer;
                    }
                    break;
                case PacketType.PINGREQ:
                    int length = PingrespPacket.INSTANCE.unparse(sendBufferPtr);
                    socket.send(sendBufferPtr, length);
                    sendBufferPtr = sendBuffer;
                    recvBufferPtr = recvBuffer;
                    break;
                case PacketType.DISCONNECT:
                    close();
                    throw PeerDisconnectedException.INSTANCE;
                    //return false;
            }
        } catch (Exception ex) {
            throw ex;
        }

        return busyRecv;
    }

    private byte[] toByteArray(long ptr, int size) {
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = Unsafe.getUnsafe().getByte(ptr + i);
        }
        return bytes;
    }

}
