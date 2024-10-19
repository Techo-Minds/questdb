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

public class ReasonCode {

    public static final byte REASON_ADMINISTRATIVE_ACTION = (byte) 152;
    public static final byte REASON_BAD_USERNAME_OR_PASSWORD = (byte) 134;
    public static final byte REASON_BANNED = (byte) 138;
    public static final byte REASON_BANNED_AUTHENTICATION_METHOD = (byte) 140;
    public static final byte REASON_CLIENT_IDENTIFIER_NOT_VALID = (byte) 133;
    public static final byte REASON_CONNECTION_RATE_EXCEEDED = (byte) 159;
    public static final byte REASON_CONTINUE_AUTHENTICATION = 24;
    public static final byte REASON_DISCONNECT_WITH_WILL_MESSAGE = 4;
    public static final byte REASON_GRANTED_QOS_1 = 0;
    public static final byte REASON_GRANTED_QOS_2 = 1;
    public static final byte REASON_GRANTED_QOS_3 = 2;
    public static final byte REASON_IMPLEMENTATION_SPECIFIC_ERROR = (byte) 131;
    public static final byte REASON_KEEP_ALIVE_TIMEOUT = (byte) 141;
    public static final byte REASON_MALFORMED_PACKET = (byte) 129;
    public static final byte REASON_MAXIMUM_CONNECT_TIME = (byte) 160;
    public static final byte REASON_MESSAGE_RATE_TOO_HIGH = (byte) 150;
    public static final byte REASON_NORMAL_DISCONNECTION = 0;
    public static final byte REASON_NOT_AUTHORISED = (byte) 135;
    public static final byte REASON_NO_MATCHING_SUBSCRIBERS = 16;
    public static final byte REASON_NO_SUBSCRIPTION_EXISTED = 17;
    public static final byte REASON_PACKET_IDENTIFIER_IN_USE = (byte) 145;
    public static final byte REASON_PACKET_IDENTIFIER_NOT_FOUND = (byte) 146;
    public static final byte REASON_PACKET_TOO_LARGE = (byte) 149;
    public static final byte REASON_PAYLOAD_FORMAT_INVALID = (byte) 153;
    public static final byte REASON_PROTOCOL_ERROR = (byte) 130;
    public static final byte REASON_QOS_NOT_SUPPORTED = (byte) 155;
    public static final byte REASON_QUOTA_EXCEEDED = (byte) 151;
    public static final byte REASON_REAUTHENTICATE = 25;
    public static final byte REASON_RECEIVE_MAXIMUM_EXCEEDED = (byte) 147;
    public static final byte REASON_RETAIN_NOT_SUPPORTED = (byte) 154;
    public static final byte REASON_SERVER_BUSY = (byte) 137;
    public static final byte REASON_SERVER_MOVED = (byte) 157;
    public static final byte REASON_SERVER_SHUTTING_DOWN = (byte) 139;
    public static final byte REASON_SERVER_UNAVAILABLE = (byte) 136;
    public static final byte REASON_SESSION_TAKEN_OVER = (byte) 142;
    public static final byte REASON_SHARED_SUBSCRIPTIONS_NOT_SUPPORTED = (byte) 158;
    public static final byte REASON_SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED = (byte) 161;
    public static final byte REASON_SUCCESS = 0;
    public static final byte REASON_TOPIC_ALIAS_INVALID = (byte) 148;
    public static final byte REASON_TOPIC_FILTERiNVALID = (byte) 143;
    public static final byte REASON_TOPIC_NAME_INVALID = (byte) 144;
    public static final byte REASON_UNSPECIFIED_ERROR = (byte) 128;
    public static final byte REASON_UNSUPPORTED_PROTOCOL_VERSION = (byte) 132;
    public static final byte REASON_USE_ANOTHER_SERVER = (byte) 156;
    public static final byte REASON_WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED = (byte) 162;


}
