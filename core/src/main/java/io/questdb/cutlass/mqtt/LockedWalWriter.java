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

import io.questdb.cairo.wal.WalWriter;
import io.questdb.std.Os;
import io.questdb.std.QuietCloseable;
import io.questdb.std.SimpleReadWriteLock;

import java.util.concurrent.TimeUnit;

public class LockedWalWriter implements QuietCloseable {
    private final SimpleReadWriteLock lock = new SimpleReadWriteLock();
    private final int maxCommitLag;
    private final int minCommitLag;
    private final WalWriter walWriter;
    volatile long lastWrite = 0;
    boolean needToCommit;

    public LockedWalWriter(WalWriter walWriter, int minCommitLag, int maxCommitLag) {
        this.walWriter = walWriter;
        this.minCommitLag = minCommitLag;
        this.maxCommitLag = maxCommitLag;
    }

    public WalWriter acquire() {
        lock.writeLock().lock();
        onAcquire();
        return walWriter;
    }

    public boolean checkForCommit(long currentTimestamp) {
        return currentTimestamp - lastWrite > TimeUnit.MILLISECONDS.toMicros(minCommitLag);
    }

    @Override
    public void close() {
        this.walWriter.commit();
        this.walWriter.close();
    }

    public void commit() {
        this.walWriter.commit();
        needToCommit = false;
        lastWrite = Os.currentTimeMicros();
    }

    public void onAcquire() {
        needToCommit = checkForCommit(Os.currentTimeMicros());
    }

    public void release() {
        if (needToCommit) {
            commit();
        }
        lock.writeLock().unlock();
    }
}
