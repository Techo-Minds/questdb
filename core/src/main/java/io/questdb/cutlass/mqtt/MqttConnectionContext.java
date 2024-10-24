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
import io.questdb.cutlass.auth.SocketAuthenticator;
import io.questdb.cutlass.pgwire.BadProtocolException;
import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.network.HeartBeatException;
import io.questdb.network.IOContext;
import io.questdb.network.IODispatcher;
import io.questdb.network.IOOperation;
import io.questdb.network.NetworkFacade;
import io.questdb.network.PeerDisconnectedException;
import io.questdb.network.PeerIsSlowToReadException;
import io.questdb.network.PeerIsSlowToWriteException;
import io.questdb.network.QueryPausedException;
import io.questdb.network.ServerDisconnectException;
import io.questdb.network.SuspendEvent;
import io.questdb.std.IntIntHashMap;
import io.questdb.std.LongLongHashMap;
import io.questdb.std.MemoryTag;
import io.questdb.std.Misc;
import io.questdb.std.Os;
import io.questdb.std.Unsafe;
import org.jetbrains.annotations.NotNull;

import static io.questdb.network.IODispatcher.DISCONNECT_REASON_PROTOCOL_VIOLATION;
import static io.questdb.network.IODispatcher.DISCONNECT_REASON_UNKNOWN_OPERATION;

public class MqttConnectionContext extends IOContext<MqttConnectionContext> {
    private static Log LOG = LogFactory.getLog(MqttConnectionContext.class);
    private static int recvBufferSize = 4096;
    private static int sendBufferSize = 4096;
    private final ConnackPacket connackPacket = new ConnackPacket();
    private final ConnectPacket connectPacket = new ConnectPacket();
    private final IntIntHashMap packetIdentifierLwwIndexMap = new IntIntHashMap();
    private final LongLongHashMap packetIdentifierTimestampMap = new LongLongHashMap();
    private final PubackPacket pubackPacket = new PubackPacket();
    private final PubcompPacket pubcompPacket = new PubcompPacket();
    private final PublishPacket publishPacket = new PublishPacket();
    private final PubrecPacket pubrecPacket = new PubrecPacket();
    private final PubrelPacket pubrelPacket = new PubrelPacket();
    private final TableFacade tableFacade;
    private SocketAuthenticator authenticator;
    private boolean connected = false;
    private CairoEngine engine;
    private NetworkFacade nf;
    private long recvBuffer;
    private long recvBufferReadOffset = 0;
    private long recvBufferWriteOffset = 0;
    private long sendBuffer;
    private long sendBufferLimit;
    private long sendBufferReadOffset = 0;
    private long sendBufferWriteOffset = 0;
    private SuspendEvent suspendEvent;

    public MqttConnectionContext(
            CairoEngine engine,
            MqttServerConfiguration configuration,
            TableFacade tableFacade) {
        super(configuration.getFactoryProvider().getMqttSocketFactory(),
                configuration.getNetworkFacade(),
                LOG,
                engine.getMetrics().getMqttMetrics().getConnectionCountGauge());
        this.engine = engine;
        this.nf = configuration.getNetworkFacade();
        this.tableFacade = tableFacade;
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
        }
        if (sendBuffer == 0) {
            this.sendBuffer = Unsafe.malloc(sendBufferSize, MemoryTag.NATIVE_MQTT_CONN);
            this.sendBufferLimit = (sendBuffer + sendBufferSize);
        }
        authenticator.init(socket, recvBuffer, recvBuffer + recvBufferSize, sendBuffer, sendBufferLimit);
        return this;
    }

    public void send(int length) throws PeerDisconnectedException, PeerIsSlowToReadException {
        // disconnected
        int n = socket.send(sendBuffer, length);
        if (n < 0) {
            LOG.error()
                    .$("disconnected [errno=").$(nf.errno())
                    .$(", fd=").$(socket.getFd())
                    .I$();
            throw PeerDisconnectedException.INSTANCE;
        }

        if (n == 0) {
            // test how many times we tried to send before parking up
            throw PeerIsSlowToReadException.INSTANCE;
        }

    }

    public void setAuthenticator(SocketAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setSuspendEvent(SuspendEvent suspendEvent) {
        this.suspendEvent = suspendEvent;
    }

    private boolean handleClientRecv() throws PeerIsSlowToReadException, PeerIsSlowToWriteException, ServerDisconnectException, MqttException, PeerDisconnectedException, BadProtocolException {
        boolean busyRecv = true;
        try {
            // this is address of where header ended in our receive buffer
            // we need to being processing request content starting from this address
            int read = recv();
            int size;

            LOG.debug().$("recv [fd=").$(getFd()).$(", count=").$(read).I$();
            if (read < 0) {
                throw registerDispatcherDisconnect(DISCONNECT_REASON_UNKNOWN_OPERATION);
            }

            byte fhb = FirstHeaderByte.decode(recvBuffer);
            byte type = FirstHeaderByte.getType(fhb);

            if (type != PacketType.CONNECT && !connected) {
                throw registerDispatcherDisconnect(DISCONNECT_REASON_PROTOCOL_VIOLATION);
            }

            switch (type) {
                case PacketType.CONNECT:
                    // parse connect
                    connectPacket.clear();
                    connectPacket.parse(recvBuffer);

                    // send connack
                    connackPacket.clear();
                    int connackPacketLength = connackPacket.success().unparse(sendBuffer);
                    send(connackPacketLength);

                    connected = true;
                    break;
                case PacketType.PUBLISH:
                    // parse publish
                    int publishPacketLength = publishPacket.parse(recvBuffer);

                    int lwwIndex = tableFacade.appendRow(publishPacket, connectPacket);


                    switch (publishPacket.qos) {
                        case 1:
                            pubackPacket.of(publishPacket.packetIdentifier, ReasonCode.REASON_SUCCESS);
                            size = pubackPacket.unparse(sendBuffer);
                            send(size);
                            break;
                        case 2:
                            packetIdentifierLwwIndexMap.put(publishPacket.packetIdentifier, lwwIndex);
                            packetIdentifierTimestampMap.put(publishPacket.packetIdentifier, Os.currentTimeMicros());

                            pubrecPacket.of(publishPacket.packetIdentifier, ReasonCode.REASON_SUCCESS);
                            size = pubrecPacket.unparse(sendBuffer);
                            send(size);
                            break;
                    }


                    break;
                case PacketType.PUBREL:
                    int pubrelPacketLength = pubrelPacket.parse(recvBuffer);

                    int lwwLoc = packetIdentifierLwwIndexMap.get(publishPacket.packetIdentifier);
                    LockedWalWriter lww = tableFacade.getWalWriter(lwwLoc);


                    long submitTime = packetIdentifierTimestampMap.get(publishPacket.packetIdentifier);

                    while (!lww.checkForCommit(Os.currentTimeMicros())) {
                        Thread.sleep(20);
                    }

                    lww.acquire();

                    long sequencerTxn = lww.getSequencerTxn();

                    if (lww.lastWrite.get() < submitTime) {
                        lww.commit();
                    }

                    lww.release();

//
//                    // todo: review whole pattern
//                    while (lww.lastWrite.get() < submitTime) {
//                        Thread.sleep(10);
//                    }

                    packetIdentifierLwwIndexMap.remove(publishPacket.packetIdentifier);
                    packetIdentifierTimestampMap.remove(publishPacket.packetIdentifier);

                    pubcompPacket.of(pubrelPacket.packetIdentifier, ReasonCode.REASON_SUCCESS);
                    size = pubcompPacket.unparse(sendBuffer);
                    send(size);
                    break;

                // todo: check that buffer has been flushed. if so, get the sequencerTxn so they know when data will be ready
                case PacketType.PINGREQ:
                    int length = PingrespPacket.INSTANCE.unparse(sendBuffer);
                    send(length);
                    break;
                case PacketType.DISCONNECT:
                    close();
                    throw PeerDisconnectedException.INSTANCE;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {

        }

        return true;
    }

    private byte[] toByteArray(long ptr, int size) {
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = Unsafe.getUnsafe().getByte(ptr + i);
        }
        return bytes;
    }

    int recv() throws PeerDisconnectedException, PeerIsSlowToWriteException, BadProtocolException {
        int n = socket.recv(recvBuffer, recvBufferSize);
        LOG.debug().$("recv [n=").$(n).I$();
        if (n < 0) {
            LOG.info().$("disconnected on read [code=").$(n).I$();
            throw PeerDisconnectedException.INSTANCE;
        }
        if (n == 0) {
            // The socket is not ready for read.
            throw PeerIsSlowToWriteException.INSTANCE;
        }

        return n;
    }

}
