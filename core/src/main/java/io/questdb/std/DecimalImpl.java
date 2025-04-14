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

package io.questdb.std;

import com.epam.deltix.dfp.Decimal;
import com.epam.deltix.dfp.Decimal64Utils;
import io.questdb.std.str.CharSink;
import io.questdb.std.str.Utf8Sequence;
import org.jetbrains.annotations.NotNull;

public class DecimalImpl {

    public static @Decimal long NULL = Decimal64Utils.NULL;
    public static @Decimal long ONE = Decimal64Utils.ONE;
    public static @Decimal long ZERO = Decimal64Utils.ZERO;

    public static @Decimal long add(@Decimal long l, @Decimal long r) {
        return Decimal64Utils.add(l, r);
    }

    public static @Decimal long ceiling(@Decimal long decimal) {
        return Decimal64Utils.ceiling(decimal);
    }

    public static @Decimal long div(@Decimal long l, @Decimal long r) {
        return Decimal64Utils.divide(l, r);
    }

    public static @Decimal long divByInt(@Decimal long l, long r) {
        return Decimal64Utils.divideByInteger(l, r);
    }

    public static @Decimal long divByInt(@Decimal long l, int r) {
        return Decimal64Utils.divideByInteger(l, r);
    }

    public static boolean equals(@Decimal long left, @Decimal long right) {
        return Decimal64Utils.equals(left, right);
    }

    public static @Decimal long floor(@Decimal long decimal) {
        return Decimal64Utils.floor(decimal);
    }

    public static @Decimal long fromDouble(double number, double tickSize) {
        return Decimal64Utils.round(Decimal64Utils.fromDouble(number), Decimal64Utils.fromDouble(tickSize));
    }

    public static @Decimal long fromDouble(double number) {
        return Decimal64Utils.fromDouble(number);
    }

    public static @Decimal long fromLong(long number) {
        return Decimal64Utils.fromLong(number);
    }

    public static boolean isNaN(@Decimal long value) {
        return Decimal64Utils.isNaN(value);
    }

    public static boolean isNull(@Decimal long value) {
        return Decimal64Utils.isNull(value);
    }

    public static boolean isZero(@Decimal long value) {
        return Decimal64Utils.isZero(value);
    }

    public static boolean lessThan(@Decimal long left, @Decimal long right) {
        return Decimal64Utils.isLess(left, right);
    }

    public static @Decimal long mul(@Decimal long l, @Decimal long r) {
        return Decimal64Utils.multiply(l, r);
    }

    public static @Decimal long mulByInt(@Decimal long l, long r) {
        return Decimal64Utils.multiplyByInteger(l, r);
    }

    public static @Decimal long mulByInt(@Decimal long l, int r) {
        return Decimal64Utils.multiplyByInteger(l, r);
    }

    public static @Decimal long parse(CharSequence value) throws NumericException {
        try {
            return Decimal64Utils.parse(value);
        } catch (NumberFormatException ex) {
            throw NumericException.INSTANCE;
        }
    }

    public static @Decimal long parse(Utf8Sequence value) throws NumericException {
        try {
            return Decimal64Utils.parse(value.asAsciiCharSequence());
        } catch (NumberFormatException ex) {
            throw NumericException.INSTANCE;
        }
    }

    public static @Decimal long round(@Decimal long decimal) {
        return Decimal64Utils.round(decimal);
    }

    public static @Decimal long sub(@Decimal long l, @Decimal long r) {
        return Decimal64Utils.subtract(l, r);
    }

    public static double toDouble(@Decimal long decimal) {
        if (!DecimalImpl.isNull(decimal)) {
            return Decimal64Utils.toDouble(decimal);
        } else {
            return Double.NaN;
        }
    }

    public static int toInt(@Decimal long decimal) {
        if (!DecimalImpl.isNull(decimal)) {
            return Decimal64Utils.toInt(decimal);
        } else {
            return Numbers.INT_NULL;
        }
    }

    public static long toLong(@Decimal long decimal) {
        if (!DecimalImpl.isNull(decimal)) {
            return Decimal64Utils.toLong(decimal);
        } else {
            return Numbers.LONG_NULL;
        }
    }

    public static void toSink(@Decimal long decimal, @NotNull CharSink<?> sink) {
        sink.put(Decimal64Utils.toString(decimal));
    }

    public static String toString(@Decimal long decimal) {
        return Decimal64Utils.toString(decimal);
    }

}

