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

public class PubrelPacket implements ControlPacket {
    int packetIdentifier;
    int reasonCode;
    VariableByteInteger vbi = new VariableByteInteger();

    @Override
    public void clear() {

    }

    public int getPropertiesLength() {
        return 0;
    }

    @Override
    public byte getType() {
        return PacketType.PUBREL;
    }

    public void of(int packetIdentifier, int reasonCode) {
        this.packetIdentifier = packetIdentifier;
        this.reasonCode = reasonCode;
    }

    @Override
    public int parse(long ptr) throws MqttException {
        int pos = 0;
        byte firstHeaderByte = Unsafe.getUnsafe().getByte(ptr + pos);
        byte type = FirstHeaderByte.getType(firstHeaderByte);

        guardWrongPacket(type);
        byte flag = FirstHeaderByte.getFlag(firstHeaderByte);
        

        pos++;

        // 2.1.4
        vbi.decode(ptr + pos);
        int messageLength = vbi.value;
        pos += vbi.length;

        packetIdentifier = TwoByteInteger.decode(ptr + pos);
        pos += 2;

        reasonCode = Unsafe.getUnsafe().getByte(ptr + pos);
        pos++;

        // 3.3.1.4 Property Length
        vbi.decode(ptr + pos);
        int propertiesLength = vbi.value;
        pos += vbi.length;

        // todo
        return pos;
    }

    @Override
    public int unparse(long ptr) throws MqttException {
        int pos = 0;
        byte fhb = 0b0110;

        Unsafe.getUnsafe().putByte(ptr, fhb);
        pos++;

        int remainingLength = getPropertiesLength() + 4;
        pos += VariableByteInteger.encode(ptr + pos, remainingLength);

        TwoByteInteger.encode(ptr + pos, packetIdentifier);
        pos += 2;

        Unsafe.getUnsafe().putByte(ptr + pos, (byte) reasonCode);
        pos++;

        Unsafe.getUnsafe().putByte(ptr + pos, (byte) getPropertiesLength());
        pos++;

        // other properties todo
        return pos;
    }
}
