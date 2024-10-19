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

import io.questdb.std.CharSequenceObjHashMap;
import io.questdb.std.Misc;
import io.questdb.std.Unsafe;
import io.questdb.std.str.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import static io.questdb.cutlass.mqtt.MqttProperties.*;

// 3.3 PUBLISH - Publish Message
public class PublishPacket implements ControlPacket, Sinkable {

    public DirectUtf8Sequence contentType; // 3.3.2.3.9
    public byte[] correlationData; // 3.3.2.3.6
    public byte dup; // 3.3.1.1
    public long messageExpiryInterval; // 3.3.2.3.3
    public int packetIdentifier = 0;
    public byte payloadFormatIndicator; // 3.3.2.3.2
    public long payloadLength; // 3.3.3
    public long payloadPtr; // 3.3.3
    public byte qos; // 3.3.1.2
    public DirectUtf8Sequence responseTopic; // 3.3.2.3.5
    public byte retain; // 3.3.1.3
    public int subscriptionIdentifier; // 3.3.2.3.8
    public int topicAlias; // 3.3.2.3.4
    public DirectUtf8Sequence topicName;
    public CharSequenceObjHashMap<DirectUtf8Sequence> userProperties = new CharSequenceObjHashMap<>(); // 3.3.2.3.7


    @Override
    public void clear() {
        contentType = null;
        correlationData = null;
        dup = -1;
        messageExpiryInterval = -1;
        packetIdentifier = 0;
        payloadLength = -1;
        payloadPtr = -1;
        qos = -1;
        responseTopic = null;
        retain = -1;
        subscriptionIdentifier = -1;
        topicAlias = -1;
        topicName = null;
        userProperties.clear();
    }

    @Override
    public int getType() {
        return PacketType.PUBLISH;
    }

    @Override
    public void parse(long ptr) throws MqttException {
        /*
            3.1.1 Fixed Header
            Bit        7    6    5    4          3    2    1    0
            byte1 [MQTT Control Packet Type]   [DUP]  [ QoS ]  [ RETAIN ]
            byte2     [           Remaining Length              ]
        */
        int pos = 0;
        byte fhb = Unsafe.getUnsafe().getByte(ptr);

        byte type = (byte) ((fhb & 0xF0) >> 4);

        if (type != PacketType.PUBLISH) {
            throw new UnsupportedOperationException("passed wrong packet type, expected CONNECT");
        }

        dup = (byte) (((fhb & 0b00001000) >> 3) & 1);
        qos = (byte) ((byte) (fhb & 0b00000110) >> 1);
        retain = (byte) ((fhb & 0b00000001) & 1);

        assert qos >= 0 && qos <= 2;

        pos++;

        // 3.3.1.4 Remaining Length
        long l = VariableByteInteger.decode(ptr + pos);
        int remainingLength = VariableByteInteger.left(l);
        int offset = VariableByteInteger.right(l);
        pos += offset;

        // 3.3.2.1 Topic Name

        long topicNameLength = TwoByteInteger.decode(ptr + pos);
        pos += 2;

        topicName = new DirectUtf8String().of(ptr + pos, ptr + pos + topicNameLength);
        pos += (int) topicNameLength;

        // 3.3.2.2 Packet Identifier
        // Exists only if QoS is 1 or 2
        if (qos > 0) {
            packetIdentifier = TwoByteInteger.decode(ptr + pos);
            pos += 2;
        }

        // 3.3.2.3 PUBLISH Properties

        // 3.3.1.4 Property Length
        long l2 = VariableByteInteger.decode(ptr + pos);
        int propertiesLength = VariableByteInteger.left(l2);
        int offset2 = VariableByteInteger.right(l2);
        pos += offset2;

        int propertiesStart = pos;

        while ((pos - propertiesStart) < propertiesLength) {
            byte tag = Unsafe.getUnsafe().getByte(ptr + pos);
            pos++;
            switch (tag) {
                case PROP_PAYLOAD_FORMAT_INDICATOR: // 1
                         /*
                            3.3.2.3.2 Payload Format Indicator
                            A single byte indicating either:
                                0 - the payload is an unspecified blob
                                1 - the payload is UTF-8 encoded
                         */

                    payloadFormatIndicator = Unsafe.getUnsafe().getByte(ptr + pos);
                    pos++;
                    break;
                case PROP_MESSAGE_EXPIRY_INTERVAL: // 2
                        /*
                            3.3.2.3.3 Message Expiry Interval
                            Four byte integer. Protocol Error to include more than once.
                            If present, represents the lifetime of the Will Message in seconds,
                            and is sent as the Publication Expiry Interval when Server
                            publishes the message.
                            If absent, no Message Expiry Interval is sent.
                         */
                    messageExpiryInterval = Unsafe.getUnsafe().getInt(ptr + pos);
                    pos += 4;
                    break;
                case PROP_TOPIC_ALIAS: // 35
                    /*
                        3.3.2.3.4 Topic Alias
                        Two Byte Integer representing the topic alias. This is an integer
                        used to identify the topic instead of using the topic name.
                        A publish packet will be sent with an alias alongside a topic name.
                        The server should store the alias against this name in a map.
                        These mappings last onlyfor the lifetime of the network connection.
                     */
                    topicAlias = Unsafe.getUnsafe().getShort(ptr + pos);
                    pos += 2;
                    break;
                case PROP_RESPONSE_TOPIC: // 8
                    /*
                        3.3.2.3.5 Response Topic
                        UTF-8 Encoded string used as topic name for a response message.
                        If present, the packet is a request.
                     */
                    final int responseTopicSize = Unsafe.getUnsafe().getShort(ptr + pos);
                    ptr += 2;
                    responseTopic = new DirectUtf8String().of(ptr + pos, ptr + pos + responseTopicSize);
                    ptr += responseTopicSize;
                case PROP_CORRELATION_DATA: // 9
                    /*
                        3.3.2.3.6 Correlation Data
                        Binary data, used to identify which request the response message is for when it is received.
                     */
                    final int correlationDataSize = Unsafe.getUnsafe().getShort(ptr + pos);
                    ptr += 2;
                    correlationData = new byte[correlationDataSize];
                    for (int i = 0; i < correlationData.length; i++) {
                        correlationData[i] = Unsafe.getUnsafe().getByte(ptr + pos);
                        pos++;
                    }
                    break;
                case PROP_USER_PROPERTY: // 38
                    // todo:
                    break;
                case PROP_SUBSCRIPTION_IDENTIFIER: // 11
                    /*
                        3.3.2.3.8 Subscription Identifier
                        Variable Byte Integer, with value of 1 to 268,435,455
                     */
                    long l3 = VariableByteInteger.decode(ptr + pos);
                    subscriptionIdentifier = VariableByteInteger.left(l3);
                    int subscriptionIdentifierOffset = VariableByteInteger.right(l3);
                    pos += subscriptionIdentifierOffset;
                    break;
                case PROP_CONTENT_TYPE: // 3
                    /*
                        3.3.2.3.9 Content Type
                        UTF-8 Encoded String describing content of the message.
                     */
                    final int contentTypeSize = Unsafe.getUnsafe().getShort(ptr + pos);
                    ptr += 2;
                    contentType = new DirectUtf8String().of(ptr + pos, ptr + pos + contentTypeSize);
                    ptr += contentTypeSize;
            }
        }

        payloadLength = remainingLength - pos + 2;
        payloadPtr = ptr + pos;
    }

    @Override
    public void toSink(@NotNull CharSink<?> sink) {
        sink.put("MQTT - PUBLISH\n");
        sink.put("dup=").put(dup).put('\n');
        sink.put("qos=").put(qos).put('\n');
        sink.put("retain=").put(retain).put('\n');
        sink.put("topic=").put(topicName).put('\n');
        sink.put("properties=tba").put('\n');
        sink.put("payload=");
        for (int i = 0; i < payloadLength; i++) {
            sink.put((char) Unsafe.getUnsafe().getByte(payloadPtr + i));
        }
        sink.put('\n');
    }

    @TestOnly
    public String toString0() {
        StringSink ss = Misc.getThreadLocalSink();
        toSink(ss);
        String s = ss.toString();
        ss.clear();
        return s;
    }

    @Override
    public int unparse(long ptr) throws MqttException {
        return 0;
    }
}
