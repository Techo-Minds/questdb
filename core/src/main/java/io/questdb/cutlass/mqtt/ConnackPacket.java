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

import io.questdb.std.Chars;
import io.questdb.std.ObjObjHashMap;
import io.questdb.std.Unsafe;
import io.questdb.std.str.DirectUtf8Sequence;
import io.questdb.std.str.Utf8String;
import io.questdb.std.str.Utf8s;

// 3.2 CONNACK - Connect acknowledgement
public class ConnackPacket implements ControlPacket {

    public DirectUtf8Sequence assignedClientIdentifier = null;
    public byte[] authenticationData = null;
    public DirectUtf8Sequence authenticationMethod = null;
    public int maximumPacketSize = -1; // 4 byte integer
    public byte maximumQoS = -1;
    public int reasonCode = -1; // connect reason code
    public DirectUtf8Sequence reasonString = null;
    public int receiveMaximum = -1; // 2 byte integer
    public DirectUtf8Sequence responseInformation = null;
    public byte retainAvailable = -1;
    public int serverKeepAlive = -1; // 2 byte integer
    public DirectUtf8Sequence serverReference = null;
    public int sessionExpiryInterval = -1; // 4 byte integer
    public byte sessionPresent = -1;
    public byte sharedSubscriptionAvailable = -1;
    public byte subscriptionIdentifiersAvailable = -1;
    public int topicAliasMaximum = -1; // 2 byte integer
    public ObjObjHashMap.Entry<String, Utf8String>[] userProperties = null;
    public byte wildcardSubscriptionAvailable = -1;

    @Override
    public void clear() {
        reasonCode = -1;
        sessionExpiryInterval = -1;
        sessionPresent = -1;
        receiveMaximum = -1;
        maximumQoS = -1;
        retainAvailable = -1;
        maximumPacketSize = -1;
        assignedClientIdentifier = null;
        topicAliasMaximum = -1;
        reasonString = null;
        wildcardSubscriptionAvailable = -1;
        subscriptionIdentifiersAvailable = -1;
        sharedSubscriptionAvailable = -1;
        serverKeepAlive = -1;
        responseInformation = null;
        serverReference = null;
        authenticationMethod = null;
        authenticationData = null;
    }

    public int getPropertiesLength() {
        int l = 0;
        if (assignedClientIdentifier != null) {
            l += (1 + assignedClientIdentifier.size());
        }
        if (authenticationData != null) {
            l += (1 + authenticationData.length);
        }
        if (authenticationMethod != null) {
            l += (1 + authenticationMethod.size());
        }
        if (maximumPacketSize != -1) {
            l += 1 + 4;
        }
        if (maximumQoS != -1) {
            l += 1 + 1;
        }
        if (reasonString != null) {
            l += 1 + reasonString.size();
        }
        if (receiveMaximum != -1) {
            l += 1 + 2;
        }
        if (responseInformation != null) {
            l += 1 + responseInformation.size();
        }
        if (retainAvailable != -1) {
            l += 1 + 1;
        }
        if (serverKeepAlive != -1) {
            l += 1 + 2;
        }
        if (serverReference != null) {
            l += 1 + serverReference.size();
        }
        if (sessionExpiryInterval != -1) {
            l += 1 + 4;
        }
        if (sharedSubscriptionAvailable != -1) {
            l += 1 + 1;
        }
        if (subscriptionIdentifiersAvailable != -1) {
            l += 1 + 1;
        }
        if (topicAliasMaximum != -1) {
            l += 1 + 2;
        }
        if (userProperties != null) {
            for (int i = 0; i < userProperties.length; i++) {
                l += 2;
                l += userProperties[i].key.length();
                l += 2;
                l += userProperties[i].value.size();
            }
        }
        if (wildcardSubscriptionAvailable != -1) {
            l += 1 + 1;
        }
        return l;
    }

    @Override
    public byte getType() {
        return PacketType.CONNACK;
    }

    // no payload

    @Override
    public int parse(long ptr) throws MqttException {
        return -1;
    }

    public ConnackPacket success() {
        sessionPresent = 0;
        reasonCode = ReasonCode.REASON_SUCCESS;
        receiveMaximum = 1;
        maximumQoS = 2;
        retainAvailable = 0;
        return this;
    }

    // returns size of unparsed packet
    @Override
    public int unparse(long ptr) throws MqttException {
        int pos = 0;
        byte fhb = PacketType.CONNACK << 4;

        // 3.2.1
        Unsafe.getUnsafe().putByte(ptr, fhb);
        pos++;


        int propertiesLength = getPropertiesLength();

        // remaining length
        int remainingLength = propertiesLength + 2 + VariableByteInteger.encodedSize(propertiesLength);
        pos += VariableByteInteger.encode(ptr + pos, remainingLength);

        // 3.2.2

        // 3.2.2.1 Connect Acknowledge Flags
        // Seven 0 bits with last bit corresponding to Session Present flag.
        // If true, server is using a prior session.
        // If Clean Start is true, then must be set to 0
        Unsafe.getUnsafe().putByte(ptr + pos, sessionPresent);
        pos++;

        // 3.2.2.2 Connect Reason Code
        // If 0, success
        Unsafe.getUnsafe().putByte(ptr + pos, (byte) reasonCode);
        pos++;

        // 3.2.2.3 CONNACK Properties
        // 3.2.2.3.1 Properties Length
        pos += VariableByteInteger.encode(ptr + pos, propertiesLength);

        // 3.2.2.3.2 Session Expiry Interval
        if (sessionExpiryInterval != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_SESSION_EXPIRY_INTERVAL);
            pos++;
            FourByteInteger.encode(ptr + pos, sessionExpiryInterval);
            pos += 4;
        }

        // 3.2.2.3.3 Receive Maximum
        if (receiveMaximum != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_RECEIVE_MAXIMUM);
            pos++;
            TwoByteInteger.encode(ptr + pos, receiveMaximum);
            pos += 2;
        }

        // 3.2.2.3.4 Maximum QoS
        if (maximumQoS != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_MAXIMUM_QOS);
            pos++;
            Unsafe.getUnsafe().putByte(ptr + pos, maximumQoS);
            pos++;
        }

        // 3.2.2.3.5 Retain Available
        if (retainAvailable != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_RETAIN_AVAILABLE);
            pos++;
            Unsafe.getUnsafe().putByte(ptr + pos, retainAvailable);
            pos++;
        }

        // 3.2.2.3.6 Maximum Packet Size
        if (maximumPacketSize != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_MAXIMUM_PACKET_SIZE);
            pos++;
            FourByteInteger.encode(ptr + pos, maximumPacketSize);
            pos += 4;
        }

        // 3.2.2.3.7 Assigned Client Identifier
        if (assignedClientIdentifier != null) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_ASSIGNED_CLIENT_IDENTIFIER);
            pos++;
            Utf8s.strCpy(assignedClientIdentifier, assignedClientIdentifier.size(), ptr + pos);
            pos += assignedClientIdentifier.size();
        }

        // 3.2.2.3.8 Topic Alias Maximum
        if (topicAliasMaximum != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_TOPIC_ALIAS_MAXIMUM);
            pos++;
            TwoByteInteger.encode(ptr + pos, topicAliasMaximum);
            pos += 2;
        }

        // 3.2.2.3.9 Reason String
        if (reasonString != null) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_REASON_STRING);
            pos++;
            Utf8s.strCpy(reasonString, reasonString.size(), ptr + pos);
            pos += reasonString.size();
        }

        // 3.2.2.3.10 User Property
        if (userProperties != null) {
            for (int i = 0; i < userProperties.length; i++) {
                Unsafe.getUnsafe().getByte(ptr + pos, MqttProperties.PROP_USER_PROPERTY);
                pos++;
                TwoByteInteger.encode(ptr + pos, userProperties[i].key.length());
                pos += 2;
                Chars.copyStrChars(userProperties[i].key, 0, userProperties[i].key.length(), ptr + pos);
                pos += userProperties[i].key.length();
                TwoByteInteger.encode(ptr + pos, userProperties[i].value.size());
                pos += 2;
                Utf8s.strCpy(userProperties[i].value, userProperties[i].value.size(), ptr + pos);
                pos += userProperties[i].value.size();
            }
        }

        // 3.2.2.3.11 Wildcard Subscription Available
        if (wildcardSubscriptionAvailable != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_WILDCARD_SUBSCRIPTION_AVAILABLE);
            pos++;
            Unsafe.getUnsafe().putByte(ptr + pos, wildcardSubscriptionAvailable);
            pos++;
        }

        // 3.2.2.3.12 Subscription Identifiers Available
        if (subscriptionIdentifiersAvailable != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_SUBSCRIPTION_IDENTIFIERS_AVAILABLE);
            pos++;
            Unsafe.getUnsafe().putByte(ptr + pos, subscriptionIdentifiersAvailable);
            pos++;
        }

        // 3.2.2.3.13 Shared Subscription Available
        if (sharedSubscriptionAvailable != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_SHARED_SUBSCRIPTION_AVAILABLE);
            pos++;
            Unsafe.getUnsafe().putByte(ptr + pos, sharedSubscriptionAvailable);
            pos++;
        }

        // 3.2.2.3.14 Server Keep Alive
        if (serverKeepAlive != -1) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_SERVER_KEEP_ALIVE);
            pos++;
            TwoByteInteger.encode(ptr + pos, serverKeepAlive);
            pos += 2;
        }

        // 3.2.2.3.15 Response Information
        if (responseInformation != null) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_RESPONSE_INFORMATION);
            pos++;
            Utf8s.strCpy(responseInformation, responseInformation.size(), ptr + pos);
            pos += responseInformation.size();
        }

        // 3.2.2.3.16 Server Reference
        if (serverReference != null) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_SERVER_REFERENCE);
            pos++;
            Utf8s.strCpy(serverReference, serverReference.size(), ptr + pos);
            pos += serverReference.size();
        }

        // 3.2.2.3.17 Authentication Method
        if (authenticationMethod != null) {
            Unsafe.getUnsafe().putByte(ptr + pos, MqttProperties.PROP_AUTHENTICATION_METHOD);
            pos++;
            Utf8s.strCpy(authenticationMethod, authenticationMethod.size(), ptr + pos);
            pos += authenticationMethod.size();
        }

        // 3.2.2.3.18 Authentication Data
        if (authenticationData != null) {
            TwoByteInteger.encode(ptr + pos, authenticationData.length);
            pos += 2;
            for (int i = 0; i < authenticationData.length; i++) {
                Unsafe.getUnsafe().putByte(ptr + pos, authenticationData[i]);
                pos++;
            }
        }

        return pos;
    }


}
