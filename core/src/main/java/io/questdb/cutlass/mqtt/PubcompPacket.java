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

public class PubcompPacket implements ControlPacket {
    int packetIdentifier;
    int reasonCode;

    @Override
    public void clear() {

    }

    public int getPropertiesLength() {
        return 0;
    }

    @Override
    public byte getType() {
        return PacketType.PUBREC;
    }

    public void of(int packetIdentifier, int reasonCode) {
        this.packetIdentifier = packetIdentifier;
        this.reasonCode = reasonCode;
    }

    @Override
    public int parse(long ptr) throws MqttException {
        return 0;
    }

    @Override
    public int unparse(long ptr) throws MqttException {
        int pos = 0;
        byte fhb = 0b01110000;

        Unsafe.getUnsafe().putByte(ptr, fhb);
        pos++;

        int remainingLength = getPropertiesLength() + 3 + VariableByteInteger.encodedSize(getPropertiesLength());
        pos += VariableByteInteger.encode(ptr + pos, remainingLength);

        TwoByteInteger.encode(ptr + pos, packetIdentifier);
        pos += 2;

        Unsafe.getUnsafe().putByte(ptr + pos, (byte) reasonCode);
        pos++;

        pos += VariableByteInteger.encode(ptr + pos, getPropertiesLength());

        // other properties todo
        return pos;
    }
}
