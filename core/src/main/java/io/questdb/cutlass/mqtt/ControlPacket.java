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
import io.questdb.std.str.DirectUtf8String;
import io.questdb.std.str.Utf8String;

// https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901018
public interface ControlPacket {

    static byte nextByte(long ptr) {
        return Unsafe.getUnsafe().getByte(ptr);
    }

    static long nextFourByteInteger(long ptr) {
        return FourByteInteger.decode(ptr);
    }

    static int nextTwoByteInteger(long ptr) {
        return TwoByteInteger.decode(ptr);
    }

    static Utf8String nextUtf8String(long ptr) {
        long length = TwoByteInteger.decode(ptr);
        DirectUtf8String direct = new DirectUtf8String().of(ptr + 2, ptr + 2 + length);
        return new Utf8String(String.valueOf(direct));
    }

    static int utf8sDecodeLength(Utf8String s) {
        return s.size() + 2;
    }

    void clear();

    int getType();

    int parse(long ptr) throws MqttException;

    // returns new offset
    int unparse(long ptr) throws MqttException;




    /*
        3.1.2.3 Connect Flags
        Bit     7  6  5  4  3  2  1
        TBA
     */
//    public class ConnectFlags {
//        public static final byte CleanStart = 1 << 1;
//        public static final byte WillFlag = CleanStart << 1;
//        public static final byte WillQoS = (WillFlag << 2
//        public static final byte Reserved = 0;
//    }

    // todo: add all the packet specs and objects
}
