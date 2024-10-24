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
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.SqlExecutionContextImpl;
import io.questdb.std.ObjList;
import io.questdb.std.Os;
import io.questdb.std.QuietCloseable;
import io.questdb.std.Rnd;
import io.questdb.std.str.DirectUtf8String;

// this should be owned by the server and injected into each client
public class TableFacade implements QuietCloseable {
    static Rnd rnd = new Rnd();
    private final CairoEngine engine;
    private final int maxCommitLag = 5000;
    private final int minCommitLag = 1000;
    private final int numWals = 4;
    private ObjList<LockedWalWriter> walWriters = new ObjList<>();

    public TableFacade(CairoEngine engine) {
        this.engine = engine;

        // make table if needed
        TableToken tableToken = engine.getTableTokenIfExists("mqtt");
        if (tableToken == null) {
            try (SqlExecutionContext executionContext = new SqlExecutionContextImpl(engine, 1).with(engine.getConfiguration().getFactoryProvider().getSecurityContextFactory().getRootContext(),
                    null,
                    null)) {
                engine.ddl("" +
                                "CREATE TABLE IF NOT EXISTS mqtt ( timestamp TIMESTAMP, " +
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

        for (int i = 0; i < numWals; i++) {
            WalWriter w = engine.getWalWriter(engine.getTableTokenIfExists("mqtt"));
            LockedWalWriter lww = new LockedWalWriter(w, minCommitLag, maxCommitLag);

            walWriters.add(lww);
        }

    }

    public int appendRow(PublishPacket publishPacket, ConnectPacket connectPacket) throws InterruptedException {
        int index = getSlot();

        LockedWalWriter lww = walWriters.getQuick(index);
        WalWriter w = lww.acquire();

        // commit to wal
        long timestamp = Os.currentTimeMicros();
        var row = w.newRow(timestamp);
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
        lww.release();

        // now check one other log
        // todo: clock strategy.. we need to be committing regularly on a timer,
        // not just on next rows
        int otherLogIndex = getSlot();
        if (otherLogIndex != index) {
            lww = walWriters.getQuick(otherLogIndex);
            if (lww.checkForCommit(timestamp)) {
                w = lww.acquire();
                if (lww.needToCommit) {
                    w.commit();
                }
                lww.release();
            }
        }

        return index;
    }

    @Override
    public void close() {
        for (int i = 0; i < walWriters.size(); i++) {
            LockedWalWriter lww = walWriters.getQuick(i);
            lww.acquire();
            lww.close();
            walWriters.remove(i);
        }
    }

    public int getSlot() {
        int slot = rnd.nextInt(walWriters.size());
        slot--;

        if (slot < 0) {
            slot++;
        }
        return slot;
    }

    public LockedWalWriter getWalWriter() {
        return getWalWriter(rnd.nextInt(walWriters.size()));
    }

    public LockedWalWriter getWalWriter(int index) {
        return walWriters.getQuick(index);
    }

}
