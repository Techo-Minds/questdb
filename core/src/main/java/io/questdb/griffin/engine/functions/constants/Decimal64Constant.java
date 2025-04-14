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

package io.questdb.griffin.engine.functions.constants;

import com.epam.deltix.dfp.Decimal;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.PlanSink;
import io.questdb.griffin.engine.functions.Decimal64Function;
import io.questdb.std.Decimal64Impl;

public class Decimal64Constant extends Decimal64Function implements ConstantFunction {
    public static final Decimal64Constant NULL = new Decimal64Constant(Decimal64Impl.NULL);
    public static final Decimal64Constant ZERO = new Decimal64Constant(Decimal64Impl.ZERO);
    private final @Decimal long decimal;

    public Decimal64Constant(@Decimal long decimal) {
        this.decimal = decimal;
    }

    public static Decimal64Constant newInstance(@Decimal long value) {
        if (Decimal64Impl.isZero(value)) {
            return ZERO;
        }

        if (Decimal64Impl.isNull(value)) {
            return NULL;

        }

        return new Decimal64Constant(value);
    }

    @Override
    public long getDecimal64(Record rec) {
        return decimal;
    }

    @Override
    public boolean isNullConstant() {
        return Decimal64Impl.isNull(decimal);
    }

    public void toPlan(PlanSink sink) {
        sink.val(Decimal64Impl.toString(decimal));
    }
}
