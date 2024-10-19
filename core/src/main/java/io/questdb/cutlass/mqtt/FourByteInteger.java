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

import io.questdb.std.Unsafe;

public class FourByteInteger {
    public static int decode(long ptr) {
        int b0 = Unsafe.getUnsafe().getByte(ptr);
        int b1 = Unsafe.getUnsafe().getByte(ptr + 1);
        int b2 = Unsafe.getUnsafe().getByte(ptr + 2);
        int b3 = Unsafe.getUnsafe().getByte(ptr + 3);
        return b0 << 24 | b1 << 16 | b2 << 8 | b3;
    }

    public static void encode(long ptr, int i) {
        byte b0 = (byte) ((i >> 24) & 0xFF);
        byte b1 = (byte) ((i >> 16) & 0xFF);
        byte b2 = (byte) ((i >> 8) & 0xFF);
        byte b3 = (byte) (i & 0xFF);
        Unsafe.getUnsafe().putByte(ptr, b0);
        Unsafe.getUnsafe().putByte(ptr + 1, b1);
        Unsafe.getUnsafe().putByte(ptr + 2, b2);
        Unsafe.getUnsafe().putByte(ptr + 3, b3);
    }
}
