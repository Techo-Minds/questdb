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
import io.questdb.griffin.engine.functions.DecimalFunction;
import io.questdb.std.DecimalImpl;

public class DecimalConstant extends DecimalFunction implements ConstantFunction {
    public static final DecimalConstant NULL = new DecimalConstant(DecimalImpl.NULL);
    public static final DecimalConstant ZERO = new DecimalConstant(DecimalImpl.ZERO);
    private final @Decimal long decimal;

    public DecimalConstant(@Decimal long decimal) {
        this.decimal = decimal;
    }

    public static DecimalConstant newInstance(@Decimal long value) {
        if (DecimalImpl.isZero(value)) {
            return ZERO;
        }

        if (DecimalImpl.isNull(value)) {
            return NULL;

        }

        return new DecimalConstant(value);
    }

    @Override
    public long getDecimal(Record rec) {
        return decimal;
    }

    @Override
    public boolean isNullConstant() {
        return DecimalImpl.isNull(decimal);
    }

    public void toPlan(PlanSink sink) {
        sink.val(DecimalImpl.toString(decimal));
    }
}
