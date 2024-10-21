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

public class MqttProperties {
    public static final byte PROP_ASSIGNED_CLIENT_IDENTIFIER = 0x12;
    public static final byte PROP_AUTHENTICATION_DATA = 0x16;
    public static final byte PROP_AUTHENTICATION_METHOD = 0x15;
    public static final byte PROP_CONTENT_TYPE = 0x03;
    public static final byte PROP_CORRELATION_DATA = 0x09;
    public static final byte PROP_MAXIMUM_PACKET_SIZE = 0x27;
    public static final byte PROP_MAXIMUM_QOS = 0x24;
    public static final byte PROP_MESSAGE_EXPIRY_INTERVAL = 0x02;
    public static final byte PROP_PAYLOAD_FORMAT_INDICATOR = 0x01;
    public static final byte PROP_REASON_STRING = 0x1F;
    public static final byte PROP_RECEIVE_MAXIMUM = 0x21;
    public static final byte PROP_REQUEST_PROBLEM_INFORMATION = 0x17;
    public static final byte PROP_RESPONSE_INFORMATION = 0x1A;
    public static final byte PROP_RESPONSE_TOPIC = 0x08;
    public static final byte PROP_RETAIN_AVAILABLE = 0x25;
    public static final byte PROP_SERVER_KEEP_ALIVE = 0x13;
    public static final byte PROP_SERVER_REFERENCE = 0x1C;
    public static final byte PROP_SESSION_EXPIRY_INTERVAL = 0x11;
    public static final byte PROP_SHARED_SUBSCRIPTION_AVAILABLE = 0x2A;
    public static final byte PROP_SUBSCRIPTION_IDENTIFIER = 0x0B;
    public static final byte PROP_SUBSCRIPTION_IDENTIFIERS_AVAILABLE = 0x29;
    public static final byte PROP_TOPIC_ALIAS = 0x23;
    public static final byte PROP_TOPIC_ALIAS_MAXIMUM = 0x22;
    public static final byte PROP_USER_PROPERTY = 0x26;
    public static final byte PROP_WILDCARD_SUBSCRIPTION_AVAILABLE = 0x28;
    public static final byte PROP_WILL_DELAY_INTERVAL = 0x18;
    public static boolean initialised = false;
    public static String[] propNames = new String[256];

    public static String getPropName(byte prop) {
        assert initialised;
        return propNames[prop & 0xFF];
    }

    public static synchronized void init() {
        if (initialised) {
            return;
        }

        setPropName(PROP_ASSIGNED_CLIENT_IDENTIFIER, "Assigned Client Identifier");
        setPropName(PROP_AUTHENTICATION_DATA, "Authentication Data");
        setPropName(PROP_AUTHENTICATION_METHOD, "Authentication Method");
        setPropName(PROP_CONTENT_TYPE, "Content Type");
        setPropName(PROP_CORRELATION_DATA, "Correlation Data");
        setPropName(PROP_MAXIMUM_PACKET_SIZE, "Maximum Packet Size");
        setPropName(PROP_MAXIMUM_QOS, "Maximum QoS");
        setPropName(PROP_MESSAGE_EXPIRY_INTERVAL, "Message Expiry Interval");
        setPropName(PROP_PAYLOAD_FORMAT_INDICATOR, "Payload Format Indicator");
        setPropName(PROP_REASON_STRING, "Reason String");
        setPropName(PROP_RECEIVE_MAXIMUM, "Receive Maximum");
        setPropName(PROP_REQUEST_PROBLEM_INFORMATION, "Request Problem Information");
        setPropName(PROP_RESPONSE_INFORMATION, "Response Information");
        setPropName(PROP_RESPONSE_TOPIC, "Response Topic");
        setPropName(PROP_RETAIN_AVAILABLE, "Retain Available");
        setPropName(PROP_SERVER_KEEP_ALIVE, "Server Keep Alive");
        setPropName(PROP_SERVER_REFERENCE, "Server Reference");
        setPropName(PROP_SESSION_EXPIRY_INTERVAL, "Session Expiry Interval");
        setPropName(PROP_SHARED_SUBSCRIPTION_AVAILABLE, "Shared Subscription Available");
        setPropName(PROP_SUBSCRIPTION_IDENTIFIER, "Subscription Identifier");
        setPropName(PROP_SUBSCRIPTION_IDENTIFIERS_AVAILABLE, "Subscription Identifiers Available");
        setPropName(PROP_TOPIC_ALIAS, "Topic Alias");
        setPropName(PROP_TOPIC_ALIAS_MAXIMUM, "Topic Alias Maximum");
        setPropName(PROP_USER_PROPERTY, "User Property");
        setPropName(PROP_WILDCARD_SUBSCRIPTION_AVAILABLE, "Wildcard Subscription Available");
        setPropName(PROP_WILL_DELAY_INTERVAL, "Will Delay Interval");

        initialised = true;
    }

    private static void setPropName(byte prop, String name) {
        propNames[prop & 0xFF] = name;
    }
}




