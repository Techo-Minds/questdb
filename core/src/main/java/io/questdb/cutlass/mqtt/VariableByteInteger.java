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

/*
    1.5.5 Variable Byte Integer
    To encode:

    do
        encodedByte = X MOD 128
        X = X DIV 128
        // if there are more data to encode, set the top bit of this byte
        if (X > 0)
            encodedByte = encodedByte OR 128
        endif
        'output' encodedByte
    while (X > 0)
    To decode:

    multiplier = 1
    value = 0
    do
        encodedByte = 'next byte from stream'
        value += (encodedByte AND 127) * multiplier
        if (multiplier > 128*128*128)
            throw Error(Malformed Variable Byte Integer)
        multiplier *= 128
    while ((encodedByte AND 128) != 0)
 */
public class VariableByteInteger {

    // return two ints, encoded in a long
    // high bits is the variable integer
    // low bits is the number of bytes used
    public static long decode(long ptr) throws MqttException {
        int m = 1;
        int v = 0;
        byte b;
        int i = 0;
        do {
            b = Unsafe.getUnsafe().getByte(ptr + i);
            v += (b & 127) * m;
            if (m > 128 * 128 * 128) {
                throw new MqttException(); // malformed variable byte integer
            }
            m *= 128;
            i++;
        } while ((b & 128) != 0);
        return (((long) v)) << 32 | (i & 0xffffffffL);
    }

    public static int encode(long ptr, int value) {
        int x = value;
        int i = 0;
        do {
            byte b = (byte) (x % 128);
            x /= 128;
            if (x > 0) {
                b |= (byte) 128;
            }
            Unsafe.getUnsafe().putByte(ptr + i, b);
            i++;
        } while (x > 0);
        return i;
    }

    public static int left(long l) {
        return (int) (l >> 32);
    }

    public static int right(long l) {
        return (int) l;
    }
}
