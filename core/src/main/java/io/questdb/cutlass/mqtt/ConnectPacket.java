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
import io.questdb.std.str.Utf8Sequence;

import static io.questdb.cutlass.mqtt.MqttProperties.*;


public class ConnectPacket implements ControlPacket {

    boolean cleanStart;
    Utf8Sequence clientId = null;
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

    }

    public int getType() {
        return PacketType.CONNECT;
    }

    // assume we are at the very start of the packet
    @Override
    public void parse(long ptr) throws MqttException {
          /*
            2.1.1 Fixed Header
            Bit        7    6    5    4          3    2    1    0
            byte1 [MQTT Control Packet Type]  [Flags specific to each MQTT Control Packet]
            byte2     [           Remaining Length              ]
        */
        int pos = 0;
        byte fhb = Unsafe.getUnsafe().getByte(ptr);

        byte type = (byte) ((fhb & 0xF0) >> 4);

        if (type != PacketType.CONNECT) {
            throw new UnsupportedOperationException("passed wrong packet type, expected CONNECT");
        }

        byte flag = (byte) (fhb & 0x0F);

        if (flag != 0b0000) {
            throw new MqttException();
        }

        pos++;
        // 2.1.4
        long l = VariableByteInteger.decode(ptr + pos);
        int remainingLength = VariableByteInteger.left(l);
        int offset = VariableByteInteger.right(l);
        pos += offset;

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
            If set, any existing session must be discarded.
            If not set, then Server should resume the last session,
            or create a new one if there is no existing session.
        */
        cleanStart = ((connectFlags >> 1) & 1) == 1;

        /*
            3.1.2.5 Will Flag
            If set, a Will Message included in the packet must be stored
            on the server. This message is included in the CONNECT payload.
            When the connection ends, the Will Message must be sent unless
            it has been removed by a DISCONNECT packet.

        */
        willFlag = ((connectFlags >> 2) & 1) == 1;

        /*
            3.1.2.6 Will QoS
            If Will Flag == 0, Will QoS must be 0
            If Will Flag == 1, then QoS can be 0, 1 or 2.
        */
        willQoS = (connectFlags & 0b00011000) >> 3;

        if (!(willFlag || willQoS == 0)) {
            throw new MqttException();
        }

        /*
            3.1.2.7 Will Retain
            If set, Will Message should be retained when published.
            If Will Flag == 0, Will Retain must be 0.
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
            If zero, this feature is disabled, and the Client is not obliged
            to send packets on a schedule.
            If Keep Alive is non-zero, and no other packets are being sent,
            the client must send a PINGREQ packet.
            If Server returns a Server Keep Alive in the CONNACK packet,
            the client must use that value instead.
            Clients can send PINGREQ at any time and check for corresponding
            PINGRESP to check that the server and network are available.
            If a PINGRESP is not received after a reasonable amount of time,
            the connection should be closed.
        */
        keepAlive = TwoByteInteger.decode(ptr + ++pos); // b8, b9
        pos += 2;

        /*
            3.1.2.11 CONNECT Properties

            3.1.2.11.1 Property Length
            The length of the properties, as a Variable Byte Integer
        */

        long pl = VariableByteInteger.decode(ptr + pos); // b...
        int propertyLength = VariableByteInteger.left(pl);
        int ploffset = VariableByteInteger.right(l);
        pos += ploffset;

        int connectPropertiesStart = pos;

        while ((pos - connectPropertiesStart) < propertyLength) {
            byte nextTag = Unsafe.getUnsafe().getByte(ptr + pos);
            pos++;
            switch (nextTag) {
                case PROP_SESSION_EXPIRY_INTERVAL: // 17
                    /*
                        3.1.2.11.2 Session Expiry Interval
                        A four byte integer representing the session expiry interval in seconds.
                        If included more than once, it is a Protocol Error.
                        If zero or absent, the Session ends when the Network Connection is closed.
                        If UINT_MAX, the session does not expire.
                        If this is non-zero, the Client and Server must store the Session State.
                    */
                    sessionExpiryInterval = Unsafe.getUnsafe().getInt(ptr + pos);
                    pos += 4;
                    break;
                case PROP_RECEIVE_MAXIMUM: // 33
                    /*
                        3.1.2.11.3 Receive Maximum
                        A two byte integer representing the Receive Maximum value.
                        Cannot be zero.
                    */
                    if (receiveMaximum > 0) {
                        throw new MqttException(); // protocl error, already handled receive maximum
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
                    maximumPacketSize = Unsafe.getUnsafe().getInt(ptr + pos);
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
                        When zero, the Client does not accept Topic Aliases.
                        If absent or zero, the Server must not send any aliases to the client.
                    */
                    break;
                case PROP_REQUEST_PROBLEM_INFORMATION: // 23
                    /*
                        3.1.2.11.7 Request Problem Information
                        A byte of either 0 or 1. Defaults to 1.
                        Client uses this to indicate whether Reason String or
                        User Properties are sent in the case of failures.
                        If 0, the Server may return a Reason String or User Properties
                        on a CONNACK or DISCONNECT packet, but must not send on any packet
                        other than PUBLISH, CONNACK or DISCONNECT.
                        If Client receives these in any other packet, and the value is 0,
                        it uses a DISCONNECT packet with Reason Coed 0x82 (Protocol Error).
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
                        Used to send connection related properties from Client
                        to the Server.
                    */
                    // todo: implement UTF-8 String Pairs and storage for these user properties
                    break;
                case PROP_AUTHENTICATION_METHOD: // 21
                    /*
                        3.1.2.11.9 Authentication Method
                        A UTF-8 Encoded String containing the name of the authentication method
                        used for extended authentication. Protocol Error if included more than once.
                        If absent, extended authentication is not performed.
                    */
                    // todo: authentication method
                    break;
                case PROP_AUTHENTICATION_DATA: // 22
                    /*
                        3.1.2.11.10 Authentication Data
                        Binary Data containing the authentication data. Protocol Error to include
                        if there is no authentication method.
                        Protocol Error to include more than once.
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


        long clientIdLength = TwoByteInteger.decode(ptr + pos);
        pos += 2;

        clientId = new DirectUtf8String().of(ptr + pos, ptr + pos + clientIdLength);
        pos += (int) clientIdLength;


            /*
                3.1.3.2 Will Properties
                If Will Flag is 1, Will Properties is next. This contains Application Message
                properties to be sent alongside the Will Message when it is published.
             */
        if (willFlag) {

            long wfi = VariableByteInteger.decode(ptr + pos);
            int willPropertiesLength = VariableByteInteger.left(wfi);
            int willPropertiesOffset = VariableByteInteger.right(wfi);
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
                            Protocol Error to include more than once.
                            If absent, defaults to 0, and there is no delay.
                         */
                        // todo: sort out protocol errors here
                        willDelayInterval = Unsafe.getUnsafe().getInt(ptr + pos);
                        pos += 4;
                        break;
                    case PROP_PAYLOAD_FORMAT_INDICATOR: // 1
                            /*
                                3.1.3.2.3 Payload Format Indicator
                                Either a 0 or 1 byte.
                                If 0, no format has been sent.
                                If 1, its UTF-8 encoded.
                                Protocol error to include more than once.
                             */
                        payloadFormatIndicator = Unsafe.getUnsafe().getByte(ptr + pos);
                        pos++;
                        break;
                    case PROP_MESSAGE_EXPIRY_INTERVAL: // 2
                            /*
                                3.1.3.2.4 Message Expiry Interval
                                Four byte integer. Protocol Error to include more than once.
                                If present, represents the lifetime of the Will Message in seconds,
                                and is sent as the Publication Expiry Interval when Server
                                publishes the message.
                                If absent, no Message Expiry Interval is sent.
                             */
                        messageExpiryInterval = Unsafe.getUnsafe().getInt(ptr + pos);
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
                                Protocol Error to include more than once.
                             */
                        // todo:
                        break;
                    case PROP_CORRELATION_DATA: // 9
                            /*
                                3.1.3.2.7 Correlation Data
                                Binary data used by Request Message sender to identify
                                which request the received Response Message is associated with.
                                Protoocl Error to include more than once.
                                If not present, the Requester does not require any correlation data.
                             */
                        // todo
                        break;
                    case PROP_USER_PROPERTY: // 38
                              /*
                        3.1.2.11.8 User Property
                        UTF-8 String Pair which can occur multiple times.
                        The same name can appear more than once.
                        Used to send connection related properties from Client
                        to the Server.
                    */
                        // todo: implement UTF-8 String Pairs and storage for these user properties
                        break;
                }
            }

                /*
                    3.1.3.3 Will Topic
                    If Will Flag is 1, next field is Will Topic, a
                    UTF-8 Encoded String.
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
    }

    @Override
    public int unparse(long ptr) throws MqttException {
        return 0;
    }

}
