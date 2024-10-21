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
import io.questdb.std.str.Utf8String;

import static io.questdb.cutlass.mqtt.MqttProperties.*;


public class ConnectPacket implements ControlPacket {

    boolean cleanStart;
    Utf8String clientId = null;
    int keepAlive = -1;
    int maximumPacketSize = -1;
    int messageExpiryInterval = -1;
    boolean passwordFlag;
    byte payloadFormatIndicator = -1;
    short receiveMaximum = -1;
    byte requestProblemInformation = 1;
    int sessionExpiryInterval = -1;
    short topicAliasMaximum = -1;
    boolean userName;
    int willDelayInterval = 0;
    boolean willFlag;
    int willQoS;
    boolean willRetain;

    public void clear() {
        cleanStart = true;
        clientId = null;
        keepAlive = -1;
        maximumPacketSize = -1;
        messageExpiryInterval = -1;
        passwordFlag = false;
        payloadFormatIndicator = -1;
        receiveMaximum = -1;
        requestProblemInformation = 1;
        sessionExpiryInterval = -1;
        topicAliasMaximum = -1;
        userName = false;
        willDelayInterval = 0;
        willFlag = false;
        willQoS = 0;
        willRetain = false;

    }

    public int getType() {
        return PacketType.CONNECT;
    }

    // assume we are at the very start of the packet
    @Override
    public int parse(long ptr) throws MqttException {
          /*
            2.1.1 Fixed Header
            Bit        7    6    5    4          3    2    1    0
            byte1 [MQTT Control Packet Type]  [Flags specific to each MQTT Control Packet]
            byte2     [           Remaining Length              ]
        */
        int pos = 0;
        byte firstHeaderByte = Unsafe.getUnsafe().getByte(ptr);

        byte type = (byte) ((firstHeaderByte & 0xF0) >> 4);

        if (type != PacketType.CONNECT) {
            throw new UnsupportedOperationException("passed wrong packet type, expected CONNECT");
        }

        byte flag = (byte) (firstHeaderByte & 0x0F);

        if (flag != 0b0000) {
            throw new MqttException();
        }

        pos++;
        // 2.1.4
        long messageLengthTuple = VariableByteInteger.decode(ptr + pos);
        int messageLength = VariableByteInteger.left(messageLengthTuple);
        int messageLengthOffset = VariableByteInteger.right(messageLengthTuple);
        pos += messageLengthOffset;

        /*
            3.1.2 CONNECT VARIABLE HEADER
            Contains Protocol Name, Protocol Level, Connect Flags, Keep Alive, Properties.
         */

        /*
            3.1.2.1 Protocol Name
            UTF8 Encoded 'MQTT'
         */

        byte b0 = Unsafe.getUnsafe().getByte(ptr + pos);
        byte b1 = Unsafe.getUnsafe().getByte(ptr + ++pos);
        byte b2 = Unsafe.getUnsafe().getByte(ptr + ++pos);
        byte b3 = Unsafe.getUnsafe().getByte(ptr + ++pos);
        byte b4 = Unsafe.getUnsafe().getByte(ptr + ++pos);
        byte b5 = Unsafe.getUnsafe().getByte(ptr + ++pos);

        if (!(
                b0 == 0 && b1 == 4 && b2 == 'M' && b3 == 'Q' && b4 == 'T' && b5 == 'T'
        )) {
            throw new MqttException();
        }

        /*
            3.1.2.2 Protocol Version
            Byte with version number.
        */
        byte protocolVersion = Unsafe.getUnsafe().getByte(ptr + ++pos); // b6
        if (protocolVersion != 5) {
            throw new MqttException();
        }

        /*
            3.1.2.3 Connect Flags
            Contains flags specifying MQTT connection behaviours.
        */
        byte connectFlags = Unsafe.getUnsafe().getByte(ptr + ++pos); // b7

        /*
            3.1.2.4 Clean Start
            1 -> discard existing session.
            0 or not set -> resume last session, or create a new one.
        */
        cleanStart = ((connectFlags >> 1) & 1) == 1;

        /*
            3.1.2.5 Will Flag
            True -> store will message on server, contained in payload.
            On disconnect, send will message unless handled by DISCONNECT.

        */
        willFlag = ((connectFlags >> 2) & 1) == 1;

        /*
            3.1.2.6 Will QoS
            Will Flag == 0 -> Will QoS == 0
            Will Flag == 1 -> Will QoS == 0 or 1 or 2
        */
        willQoS = (connectFlags & 0b00011000) >> 3;

        if (!(willFlag || willQoS == 0)) {
            throw new MqttException();
        }

        /*
            3.1.2.7 Will Retain
            True -> retain will message when published.
            False -> don't.
            If Will Flag == 0, Will Retain == 0
        */

        willRetain = ((connectFlags >> 5) & 1) == 1;

        if (!(willFlag || !willRetain)) {
            throw new MqttException();
        }

        /*
            3.1.2.8 User Name Flag
            If set, a User Name must be present in the payload,
            and vice versa.
        */

        userName = ((connectFlags >> 6) & 1) == 1;
        /*
            3.1.2.9 Password Flag
            If set, a Password must be present in the payload,
            and vice versa.
        */
        passwordFlag = ((connectFlags >> 7) & 1) == 1;

        /*
            3.1.2.10 Keep Alive
            2 Byte Integer, time interval in seconds.
            Maximum time interval between Client finishes sending
            a Control Packet and the time it starts sending the next.
            0 -> disabled.
            non-zero -> clients must send PINGREQ and server can respond with PINGRESP.
            Close connection if PINGRESP not received etc.
        */
        keepAlive = TwoByteInteger.decode(ptr + ++pos); // b8, b9
        pos += 2;

        /*
            3.1.2.11 CONNECT Properties

            3.1.2.11.1 Property Length
            The length of the properties, as a Variable Byte Integer
        */

        long propertyLengthTuple = VariableByteInteger.decode(ptr + pos); // b...
        int propertyLength = VariableByteInteger.left(propertyLengthTuple);
        int propertyLengthOffset = VariableByteInteger.right(propertyLengthTuple);
        pos += propertyLengthOffset;

        int connectPropertiesStart = pos;

        while ((pos - connectPropertiesStart) < propertyLength) {
            byte nextTag = Unsafe.getUnsafe().getByte(ptr + pos);
            pos++;
            switch (nextTag) {
                case PROP_SESSION_EXPIRY_INTERVAL: // 17
                    /*
                        3.1.2.11.2 Session Expiry Interval
                        A four byte integer representing the session expiry interval in seconds.
                        0 -> session ends with network connection closes.
                        UINT_MAX -> session doesn't expire.
                        non-zero -> client and server must store the session state.
                    */
                    sessionExpiryInterval = FourByteInteger.decode(ptr + pos);
                    pos += 4;
                    break;
                case PROP_RECEIVE_MAXIMUM: // 33
                    /*
                        3.1.2.11.3 Receive Maximum
                        A two byte integer representing the Receive Maximum value.
                        Must be non-zero.
                    */
                    if (receiveMaximum > 0) {
                        throw new MqttException(); // protocol error, already handled receive maximum
                    }
                    receiveMaximum = Unsafe.getUnsafe().getShort(ptr + pos);
                    pos += 2;
                    if (receiveMaximum == 0) {
                        throw new MqttException(); // protocol error, cannot be 0
                    }
                    break;
                case PROP_MAXIMUM_PACKET_SIZE: // 39
                    /*
                        3.1.2.11.4 Maximum Packet Size
                        A four byte integer representing the Maximum Packet Size the client
                        will accept.
                        If not present, then there is no limit besides that of the
                        remaining length and header encodings.
                    */
                    if (maximumPacketSize > 0) {
                        throw new MqttException(); // protocol error, already handled receive maximum
                    }
                    maximumPacketSize = FourByteInteger.decode(ptr + pos);
                    pos += 4;
                    if (maximumPacketSize == 0) {
                        throw new MqttException(); // protocol error, cannot be 0
                    }
                    break;
                case PROP_TOPIC_ALIAS_MAXIMUM: // 34
                    topicAliasMaximum = Unsafe.getUnsafe().getShort(ptr + pos);
                    pos += 2;
                    /*
                        3.1.2.11.5 Topic Alias Maximum
                        A two byte integer indicating the highest value the Client will
                        accept as a Topic Alias. The Server must not send any Topic Alias
                        to the Client greater than this number.
                        0 -> client does not accept aliases
                        if 0 or absent, server should not send aliases.
                    */
                    break;
                case PROP_REQUEST_PROBLEM_INFORMATION: // 23
                    /*
                        3.1.2.11.7 Request Problem Information
                        A byte of either 0 or 1. Defaults to 1.
                        Client uses this to indicate whether Reason String or
                        User Properties are sent in the case of failures.
                        0 -> server may return a reason string or user properties on a CONNACK
                        or DISCONNECT packet.
                    */
                    requestProblemInformation = Unsafe.getUnsafe().getByte(ptr + pos);
                    assert requestProblemInformation == 0 || requestProblemInformation == 1;
                    pos++;
                    break;
                case PROP_USER_PROPERTY: // 38
                    /*
                        3.1.2.11.8 User Property
                        UTF-8 String Pair which can occur multiple times.
                        The same name can appear more than once.
                    */
                    // todo: implement UTF-8 String Pairs and storage for these user properties
                    break;
                case PROP_AUTHENTICATION_METHOD: // 21
                    /*
                        3.1.2.11.9 Authentication Method
                        A UTF-8 Encoded String containing the name of the authentication method
                        used for extended authentication.
                        If absent, extended authentication is not performed.
                    */
                    // todo: authentication method
                    break;
                case PROP_AUTHENTICATION_DATA: // 22
                    /*
                        3.1.2.11.10 Authentication Data
                        Binary Data containing the authentication data.
                    */
                    // todo: authentication data
                    break;
            }
        }

        /*
            3.1.3 CONNECT Payload
            The Payload contains one or more length prefixed fields, the
            presence of which is determined by flags in the Variable Header.
            If present, the fields must appear in the following order:
            Client Identifier, Will Properties, Will Topic, Will Payload, User Name, Password

            3.1.3.1 Client Identifier (ClientID)
            Must be the first field in the payload.
            Is used to identify state held for the session between Client and Server.
            Must be a UTF-8 Encoded String
            Must support 1-23 Alphanumeric (caps included) characters.
            May support more.
            Server may allow Client to give a 0 length id. Server must then provide
            a unique id in response.
         */


        clientId = ControlPacket.nextUtf8String(ptr + pos);
        pos += ControlPacket.utf8sDecodeLength(clientId);

        /*
            3.1.3.2 Will Properties
            If Will Flag is 1, Will Properties is next.
        */
        if (willFlag) {

            long willPropertiesTuple = VariableByteInteger.decode(ptr + pos);
            int willPropertiesLength = VariableByteInteger.left(willPropertiesTuple);
            int willPropertiesOffset = VariableByteInteger.right(willPropertiesTuple);
            pos += willPropertiesOffset;

            int willPropertiesStart = pos;

            while ((pos - willPropertiesStart) < willPropertiesLength) {
                byte nextWillTag = Unsafe.getUnsafe().getByte(ptr + pos);
                pos++;
                switch (nextWillTag) {
                    case PROP_WILL_DELAY_INTERVAL: // 24
                         /*
                            3.1.3.2.2 Will Delay Interval
                            A four byte integer representing the Will Delay Interval in seconds.
                            If absent, defaults to 0, and there is no delay.
                         */
                        // todo: sort out protocol errors here
                        willDelayInterval = FourByteInteger.decode(ptr + pos);
                        pos += 4;
                        break;
                    case PROP_PAYLOAD_FORMAT_INDICATOR: // 1
                            /*
                                3.1.3.2.3 Payload Format Indicator
                                Either a 0 or 1 byte.
                                If 0, no format has been sent.
                                If 1, its UTF-8 encoded.
                             */
                        payloadFormatIndicator = Unsafe.getUnsafe().getByte(ptr + pos);
                        pos++;
                        break;
                    case PROP_MESSAGE_EXPIRY_INTERVAL: // 2
                            /*
                                3.1.3.2.4 Message Expiry Interval
                                Four byte integer representing lifetime of will message.
                             */
                        messageExpiryInterval = FourByteInteger.decode(ptr + pos);
                        pos += 4;
                    case PROP_CONTENT_TYPE: // 3
                            /*
                                3.1.3.2.5 Content Type
                                UTF-8 Encoded String describing the content of the Will Message.
                             */
                        // todo: utf8 reading
                        break;
                    case PROP_RESPONSE_TOPIC: // 8
                            /*
                                3.1.3.2.6 Response Topic
                                UTF-8 Encoded String used for the Topic Name for a response message.
                             */
                        // todo:
                        break;
                    case PROP_CORRELATION_DATA: // 9
                    /*
                        3.1.3.2.7 Correlation Data
                        Binary data used by requester to correlate a PUBACK response.
                     */
                        // todo
                        break;
                    case PROP_USER_PROPERTY: // 38
                    /*
                        3.1.2.11.8 User Property
                        UTF-8 String Pair, can appear multiples times.
                    */
                        // todo: implement UTF-8 String Pairs and storage for these user properties
                        break;
                }
            }
                /*
                    3.1.3.3 Will Topic
                 */
            // todo

                /*
                    3.1.3.4 Will Payload
                */
            // todo

                /*
                    3.1.3.5 User Name
                 */
            // todo

                /*
                    3.1.3.6 Password
                */
            // todo
        }

        return pos;
    }

    @Override
    public int unparse(long ptr) throws MqttException {
        return 0;
    }

}
