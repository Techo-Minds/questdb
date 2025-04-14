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

package io.questdb.griffin.engine.functions.finance;

import com.epam.deltix.dfp.Decimal;
import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.engine.functions.DecimalFunction;
import io.questdb.griffin.engine.functions.QuaternaryFunction;
import io.questdb.std.DecimalImpl;
import io.questdb.std.IntList;
import io.questdb.std.ObjList;

public class DecimalWeightedMidPriceFunctionFactory implements FunctionFactory {
    @Override
    public String getSignature() {
        return "wmid(ÆÆÆÆ)";
    }

    @Override
    public Function newInstance(int position, ObjList<Function> args, IntList argPositions,
                                CairoConfiguration configuration, SqlExecutionContext sqlExecutionContext) {
        return new WeightedMidPriceFunction(args.getQuick(0), args.getQuick(1), args.getQuick(2), args.getQuick(3));
    }

    private static class WeightedMidPriceFunction extends DecimalFunction implements QuaternaryFunction {
        private final Function askPrice;
        private final Function askSize;
        private final Function bidPrice;
        private final Function bidSize;

        // Argument order, e.g. `bidSize, bidPrice, askPrice, askSize`, follows the standard order commonly displayed on trading systems.
        public WeightedMidPriceFunction(Function bidSize, Function bidPrice, Function askPrice, Function askSize) {
            this.bidSize = bidSize;
            this.bidPrice = bidPrice;
            this.askPrice = askPrice;
            this.askSize = askSize;
        }

        @Override
        public @Decimal long getDecimal(Record rec) {
            final @Decimal long bs = bidSize.getDecimal(rec);
            final @Decimal long bp = bidPrice.getDecimal(rec);
            final @Decimal long ap = askPrice.getDecimal(rec);
            final @Decimal long as = askSize.getDecimal(rec);

            if (DecimalImpl.isNull(bp) || DecimalImpl.isNull(bs) || DecimalImpl.isNull(ap) || DecimalImpl.isNull(as)) {
                return DecimalImpl.NULL;
            }

            @Decimal long imbalance = DecimalImpl.div(bs, DecimalImpl.add(bs, as));
            @Decimal long lhs = DecimalImpl.mul(ap, imbalance);
            @Decimal long rhs = DecimalImpl.mul(bp, DecimalImpl.sub(DecimalImpl.ONE, imbalance));
            return DecimalImpl.add(lhs, rhs);
        }

        @Override
        public Function getFunc0() {
            return bidSize;
        }

        @Override
        public Function getFunc1() {
            return bidPrice;
        }

        @Override
        public Function getFunc2() {
            return askPrice;
        }

        @Override
        public Function getFunc3() {
            return askSize;
        }

        @Override
        public String getName() {
            return "wmid";
        }
    }
}