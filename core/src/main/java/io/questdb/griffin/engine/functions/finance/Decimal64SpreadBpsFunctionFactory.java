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
import io.questdb.griffin.engine.functions.BinaryFunction;
import io.questdb.griffin.engine.functions.Decimal64Function;
import io.questdb.std.Decimal64Impl;
import io.questdb.std.IntList;
import io.questdb.std.ObjList;

public class Decimal64SpreadBpsFunctionFactory implements FunctionFactory {

    // (bid, ask)
    @Override
    public String getSignature() {
        return "spread_bps(ÆÆ)";
    }

    @Override
    public Function newInstance(int position, ObjList<Function> args, IntList argPositions, CairoConfiguration configuration, SqlExecutionContext sqlExecutionContext) {
        return new SpreadBpsFunction(args.getQuick(0), args.getQuick(1));
    }

    private static class SpreadBpsFunction extends Decimal64Function implements BinaryFunction {
        private final Function ask;
        private final Function bid;

        public SpreadBpsFunction(Function bid, Function ask) {
            this.bid = bid;
            this.ask = ask;
        }

        @Override
        public @Decimal long getDecimal64(Record rec) {
            final @Decimal long b = bid.getDecimal64(rec);
            final @Decimal long a = ask.getDecimal64(rec);

            @Decimal long spread = FinanceUtils.spread(b, a);
            @Decimal long mid = FinanceUtils.mid(b, a);
            @Decimal long interim = Decimal64Impl.div(spread, mid);
            @Decimal long result = Decimal64Impl.mulByInt(interim, 10_000);

            return result;
        }

        @Override
        public Function getLeft() {
            return bid;
        }

        @Override
        public String getName() {
            return "spread_bps";
        }

        @Override
        public Function getRight() {
            return ask;
        }
    }
}
