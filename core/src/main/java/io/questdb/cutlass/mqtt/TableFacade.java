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
import io.questdb.std.Rnd;
import io.questdb.std.str.DirectUtf8String;

import java.util.concurrent.atomic.AtomicBoolean;

public class TableFacade {
    static int numWals = 1;
    static Rnd rnd = new Rnd();
    private static CairoEngine engine;
    private static AtomicBoolean initialised = new AtomicBoolean(false);
    private static ObjList<LockedWalWriter> walWriters = new ObjList<>();

    public static void appendRow(PublishPacket publishPacket, ConnectPacket connectPacket) throws InterruptedException {
        int slot = rnd.nextInt(walWriters.size());
        slot--;

        if (slot < 0) {
            slot++;
        }

        LockedWalWriter lww = walWriters.getQuick(slot);
        WalWriter w = lww.acquire();


        // commit to wal
        var row = w.newRow(Os.currentTimeMicros());
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
    }

    public static LockedWalWriter getWalWriter() {
        rnd.nextInt(walWriters.size());
        return walWriters.getQuick(walWriters.size() - 1);
    }

    public static synchronized void init(CairoEngine engine2) {
        if (initialised.get()) {
            return;
        }

        engine = engine2;

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
            LockedWalWriter lww = new LockedWalWriter(w);

            walWriters.add(lww);
        }

        initialised.set(true);
    }


}
