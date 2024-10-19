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

/*
        2.1.2 MQTT Control Packet type
        Name         Value   Direction of flow      Description
        Reserved       0     Forbidden              Reserved
        CONNECT        1     Client to Server       Connection request
        CONNACK        2     Server to Client       Connect acknowledgement
        PUBLISH        3     Bidirectional          Publish message
        PUBACK         4     Bidirectional          Publish acknowledgement (QoS 1)
        PUBREC         5     Bidirectional          Publish received (QoS 2 delivery part 1)
        PUBREL         6     Bidirectional          Publish release (QoS 2 delivery part 2)
        PUBCOMP        7     Bidirectional          Publish complete (QoS 2 delivery part 3)
        SUBSCRIBE      8     Client to Server       Subscribe request
        SUBACK         9     Server to Client       Subscribe acknowledgement
        UNSUBSCRIBE    10    Client to Server       Unsubscribe request
        UNSUBACK       11    Server to Client       Unsubscribe acknowledgement
        PINGREQ        12    Client to Server       PING request
        PINGRESP       13    Server to Client       PING response
        DISCONNECT     14    Bidirectional          Disconnect notification
        AUTH           15    Bidirectional          Authentication exchange
     */
public class PacketType {
    public static final byte RESERVED = 0;               // 0
    public static final byte CONNECT = RESERVED + 1;     // 1
    public static final byte CONNACK = CONNECT + 1;      // 2
    public static final byte PUBLISH = CONNACK + 1;      // 3
    public static final byte PUBACK = PUBLISH + 1;       // 4
    public static final byte PUBREC = PUBACK + 1;        // 5
    public static final byte PUBREL = PUBREC + 1;        // 6
    public static final byte PUBCOMP = PUBREL + 1;       // 7
    public static final byte SUBSCRIBE = PUBCOMP + 1;    // 8
    public static final byte SUBACK = SUBSCRIBE + 1;     // 9
    public static final byte UNSUBSCRIBE = SUBACK + 1;   // 10
    public static final byte UNSUBACK = UNSUBSCRIBE + 1; // 11
    public static final byte PINGREQ = UNSUBACK + 1;     // 12
    public static final byte PINGRESP = PINGREQ + 1;     // 13
    public static final byte DISCONNECT = PINGRESP + 1;  // 14
    public static final byte AUTH = DISCONNECT + 1;      // 15

    private static final String[] names = {
            "RESERVED", "CONNECT", "CONNACK", "PUBLISH", "PUBACK", "PUBREC",
            "PUBREL", "PUBCOMP", "SUBSCRIBE", "SUBACK",
            "UNSUBSCRIBE", "UNSUBACK", "PINGREQ", "PINGRESP",
            "DISCONNECT", "AUTH"
    };

    public static String nameOf(byte type) {
        return names[type];
    }
}