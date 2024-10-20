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

public class DisconnectPacket implements ControlPacket {
    public static DisconnectPacket INSTANCE = new DisconnectPacket();


    @Override
    public void clear() {

    }

    @Override
    public int getType() {
        return PacketType.DISCONNECT;
    }

    @Override
    public int parse(long ptr) throws MqttException {
        return -1;
    }

    @Override
    public int unparse(long ptr) throws MqttException {
        int pos = 0;
        byte fhb = (byte) (PacketType.DISCONNECT << 4);

        // 3.2.1
        Unsafe.getUnsafe().putByte(ptr, fhb);
        pos++;

        // remaining length
        pos += VariableByteInteger.encode(ptr + pos, 0);
        return pos;
    }
}
