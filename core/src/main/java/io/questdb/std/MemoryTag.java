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

public final class MemoryTag {
    public static final int MMAP_DEFAULT = 0;
    public static final int MMAP_BLOCK_WRITER = MMAP_DEFAULT + 1;
    public static final int MMAP_IMPORT = MMAP_BLOCK_WRITER + 1;
    public static final int MMAP_INDEX_READER = MMAP_IMPORT + 1;
    public static final int MMAP_INDEX_SLIDER = MMAP_INDEX_READER + 1;
    public static final int MMAP_INDEX_WRITER = MMAP_INDEX_SLIDER + 1;
    public static final int MMAP_O3 = MMAP_INDEX_WRITER + 1;
    public static final int MMAP_PARALLEL_IMPORT = MMAP_O3 + 1;
    public static final int MMAP_SEQUENCER_METADATA = MMAP_PARALLEL_IMPORT + 1;
    public static final int MMAP_TABLE_READER = MMAP_SEQUENCER_METADATA + 1;
    public static final int MMAP_TABLE_WAL_READER = MMAP_TABLE_READER + 1;
    public static final int MMAP_TABLE_WAL_WRITER = MMAP_TABLE_WAL_READER + 1;
    public static final int MMAP_TABLE_WRITER = MMAP_TABLE_WAL_WRITER + 1;
    public static final int MMAP_TX_LOG = MMAP_TABLE_WRITER + 1;
    public static final int MMAP_TX_LOG_CURSOR = MMAP_TX_LOG + 1;
    public static final int MMAP_UPDATE = MMAP_TX_LOG_CURSOR + 1;
    public static final int MMAP_PARQUET_PARTITION_CONVERTER = MMAP_UPDATE + 1;
    public static final int MMAP_PARQUET_PARTITION_DECODER = MMAP_PARQUET_PARTITION_CONVERTER + 1;

    // All malloc calls should use NATIVE_* tags
    public static final int NATIVE_PATH = MMAP_PARQUET_PARTITION_DECODER + 1;
    public static final int NATIVE_DEFAULT = NATIVE_PATH + 1;
    public static final int NATIVE_DBG01 = NATIVE_DEFAULT + 1;
    public static final int NATIVE_DBG02 = NATIVE_DBG01 + 1;
    public static final int NATIVE_DBG03 = NATIVE_DBG02 + 1;
    public static final int NATIVE_DBG04 = NATIVE_DBG03 + 1;
    public static final int NATIVE_DBG05 = NATIVE_DBG04 + 1;
    public static final int NATIVE_DBG06 = NATIVE_DBG05 + 1;
    public static final int NATIVE_DBG07 = NATIVE_DBG06 + 1;
    public static final int NATIVE_DBG08 = NATIVE_DBG07 + 1;
    public static final int NATIVE_DBG09 = NATIVE_DBG08 + 1;
    public static final int NATIVE_DBG10 = NATIVE_DBG09 + 1;
    public static final int NATIVE_DBG11 = NATIVE_DBG10 + 1;
    public static final int NATIVE_DBG12 = NATIVE_DBG11 + 1;
    public static final int NATIVE_DBG13 = NATIVE_DBG12 + 1;
    public static final int NATIVE_DBG14 = NATIVE_DBG13 + 1;
    public static final int NATIVE_DBG15 = NATIVE_DBG14 + 1;
    public static final int NATIVE_DBG16 = NATIVE_DBG15 + 1;
    public static final int NATIVE_DBG17 = NATIVE_DBG16 + 1;
    public static final int NATIVE_DBG18 = NATIVE_DBG17 + 1;
    public static final int NATIVE_DBG19 = NATIVE_DBG18 + 1;
    public static final int NATIVE_DBG20 = NATIVE_DBG19 + 1;
    public static final int NATIVE_DBG21 = NATIVE_DBG20 + 1;
    public static final int NATIVE_DBG22 = NATIVE_DBG21 + 1;
    public static final int NATIVE_DBG23 = NATIVE_DBG22 + 1;
    public static final int NATIVE_DBG24 = NATIVE_DBG23 + 1;
    public static final int NATIVE_DBG25 = NATIVE_DBG24 + 1;
    public static final int NATIVE_DBG26 = NATIVE_DBG25 + 1;
    public static final int NATIVE_DBG27 = NATIVE_DBG26 + 1;
    public static final int NATIVE_DBG28 = NATIVE_DBG27 + 1;
    public static final int NATIVE_DBG29 = NATIVE_DBG28 + 1;
    public static final int NATIVE_DBG30 = NATIVE_DBG29 + 1;
    public static final int NATIVE_CB2 = NATIVE_DBG30 + 1;
    public static final int NATIVE_CB3 = NATIVE_CB2 + 1;
    public static final int NATIVE_CB4 = NATIVE_CB3 + 1;
    public static final int NATIVE_CB5 = NATIVE_CB4 + 1;
    public static final int NATIVE_CIRCULAR_BUFFER = NATIVE_CB5 + 1;
    public static final int NATIVE_COMPACT_MAP = NATIVE_CIRCULAR_BUFFER + 1;
    public static final int NATIVE_DIRECT_BYTE_SINK = NATIVE_COMPACT_MAP + 1;
    public static final int NATIVE_DIRECT_CHAR_SINK = NATIVE_DIRECT_BYTE_SINK + 1;
    public static final int NATIVE_DIRECT_UTF8_SINK = NATIVE_DIRECT_CHAR_SINK + 1;
    public static final int NATIVE_FAST_MAP = NATIVE_DIRECT_UTF8_SINK + 1;
    public static final int NATIVE_FAST_MAP_INT_LIST = NATIVE_FAST_MAP + 1;
    public static final int NATIVE_FUNC_RSS = NATIVE_FAST_MAP_INT_LIST + 1;
    public static final int NATIVE_GROUP_BY_FUNCTION = NATIVE_FUNC_RSS + 1;
    public static final int NATIVE_HTTP_CONN = NATIVE_GROUP_BY_FUNCTION + 1;
    public static final int NATIVE_ILP_RSS = NATIVE_HTTP_CONN + 1;
    public static final int NATIVE_IMPORT = NATIVE_ILP_RSS + 1;
    public static final int NATIVE_IO_DISPATCHER_RSS = NATIVE_IMPORT + 1;
    public static final int NATIVE_JIT = NATIVE_IO_DISPATCHER_RSS + 1;
    public static final int NATIVE_JIT_LONG_LIST = NATIVE_JIT + 1;
    public static final int NATIVE_JOIN_MAP = NATIVE_JIT_LONG_LIST + 1;
    public static final int NATIVE_LATEST_BY_LONG_LIST = NATIVE_JOIN_MAP + 1;
    public static final int NATIVE_LOGGER = NATIVE_LATEST_BY_LONG_LIST + 1;
    public static final int NATIVE_LONG_LIST = NATIVE_LOGGER + 1;
    public static final int NATIVE_MIG = NATIVE_LONG_LIST + 1;
    public static final int NATIVE_MIG_MMAP = NATIVE_MIG + 1;
    public static final int NATIVE_O3 = NATIVE_MIG_MMAP + 1;
    public static final int NATIVE_OFFLOAD = NATIVE_O3 + 1;
    public static final int NATIVE_PARALLEL_IMPORT = NATIVE_OFFLOAD + 1;
    public static final int NATIVE_PGW_CONN = NATIVE_PARALLEL_IMPORT + 1;
    public static final int NATIVE_PGW_PIPELINE = NATIVE_PGW_CONN + 1;
    public static final int NATIVE_RECORD_CHAIN = NATIVE_PGW_PIPELINE + 1;
    public static final int NATIVE_REPL = NATIVE_RECORD_CHAIN + 1;
    public static final int NATIVE_ROSTI = NATIVE_REPL + 1;
    public static final int NATIVE_SAMPLE_BY_LONG_LIST = NATIVE_ROSTI + 1;
    public static final int NATIVE_SQL_COMPILER = NATIVE_SAMPLE_BY_LONG_LIST + 1;
    public static final int NATIVE_TABLE_READER = NATIVE_SQL_COMPILER + 1;
    public static final int NATIVE_TABLE_WRITER = NATIVE_TABLE_READER + 1;
    public static final int NATIVE_TEXT_PARSER_RSS = NATIVE_TABLE_WRITER + 1;
    public static final int NATIVE_TLS_RSS = NATIVE_TEXT_PARSER_RSS + 1;
    public static final int NATIVE_TREE_CHAIN = NATIVE_TLS_RSS + 1;
    public static final int NATIVE_UNORDERED_MAP = NATIVE_TREE_CHAIN + 1;
    public static final int NATIVE_INDEX_READER = NATIVE_UNORDERED_MAP + 1;
    public static final int NATIVE_TABLE_WAL_WRITER = NATIVE_INDEX_READER + 1;
    public static final int NATIVE_METADATA_READER = NATIVE_TABLE_WAL_WRITER + 1;
    public static final int NATIVE_BIT_SET = NATIVE_METADATA_READER + 1;
    public static final int NATIVE_PARQUET_PARTITION_DECODER = NATIVE_BIT_SET + 1;
    public static final int NATIVE_PARQUET_PARTITION_UPDATER = NATIVE_PARQUET_PARTITION_DECODER + 1;
    public static final int SIZE = NATIVE_PARQUET_PARTITION_UPDATER + 1;

    private static final ObjList<String> tagNameMap = new ObjList<>(SIZE);

    public static String nameOf(int tag) {
        return tagNameMap.getQuick(tag);
    }

    static {
        tagNameMap.extendAndSet(MMAP_DEFAULT, "MMAP_DEFAULT");
        tagNameMap.extendAndSet(NATIVE_DEFAULT, "NATIVE_DEFAULT");
        tagNameMap.extendAndSet(NATIVE_DBG01, "NATIVE_DBG01");
        tagNameMap.extendAndSet(NATIVE_DBG02, "NATIVE_DBG02");
        tagNameMap.extendAndSet(NATIVE_DBG03, "NATIVE_DBG03");
        tagNameMap.extendAndSet(NATIVE_DBG04, "NATIVE_DBG04");
        tagNameMap.extendAndSet(NATIVE_DBG05, "NATIVE_DBG05");
        tagNameMap.extendAndSet(NATIVE_DBG06, "NATIVE_DBG06");
        tagNameMap.extendAndSet(NATIVE_DBG07, "NATIVE_DBG07");
        tagNameMap.extendAndSet(NATIVE_DBG08, "NATIVE_DBG08");
        tagNameMap.extendAndSet(NATIVE_DBG09, "NATIVE_DBG09");
        tagNameMap.extendAndSet(NATIVE_DBG10, "NATIVE_DBG10");
        tagNameMap.extendAndSet(NATIVE_DBG11, "NATIVE_DBG11");
        tagNameMap.extendAndSet(NATIVE_DBG12, "NATIVE_DBG12");
        tagNameMap.extendAndSet(NATIVE_DBG13, "NATIVE_DBG13");
        tagNameMap.extendAndSet(NATIVE_DBG14, "NATIVE_DBG14");
        tagNameMap.extendAndSet(NATIVE_DBG15, "NATIVE_DBG15");
        tagNameMap.extendAndSet(NATIVE_DBG16, "NATIVE_DBG16");
        tagNameMap.extendAndSet(NATIVE_DBG17, "NATIVE_DBG17");
        tagNameMap.extendAndSet(NATIVE_DBG18, "NATIVE_DBG18");
        tagNameMap.extendAndSet(NATIVE_DBG19, "NATIVE_DBG19");
        tagNameMap.extendAndSet(NATIVE_DBG20, "NATIVE_DBG20");
        tagNameMap.extendAndSet(NATIVE_DBG21, "NATIVE_DBG21");
        tagNameMap.extendAndSet(NATIVE_DBG22, "NATIVE_DBG22");
        tagNameMap.extendAndSet(NATIVE_DBG23, "NATIVE_DBG23");
        tagNameMap.extendAndSet(NATIVE_DBG24, "NATIVE_DBG24");
        tagNameMap.extendAndSet(NATIVE_DBG25, "NATIVE_DBG25");
        tagNameMap.extendAndSet(NATIVE_DBG26, "NATIVE_DBG26");
        tagNameMap.extendAndSet(NATIVE_DBG27, "NATIVE_DBG27");
        tagNameMap.extendAndSet(NATIVE_DBG28, "NATIVE_DBG28");
        tagNameMap.extendAndSet(NATIVE_DBG29, "NATIVE_DBG29");
        tagNameMap.extendAndSet(NATIVE_DBG30, "NATIVE_DBG30");
        tagNameMap.extendAndSet(MMAP_O3, "MMAP_O3");
        tagNameMap.extendAndSet(NATIVE_O3, "NATIVE_O3");
        tagNameMap.extendAndSet(NATIVE_RECORD_CHAIN, "NATIVE_RECORD_CHAIN");
        tagNameMap.extendAndSet(MMAP_TABLE_WRITER, "MMAP_TABLE_WRITER");
        tagNameMap.extendAndSet(NATIVE_TREE_CHAIN, "NATIVE_TREE_CHAIN");
        tagNameMap.extendAndSet(MMAP_TABLE_READER, "MMAP_TABLE_READER");
        tagNameMap.extendAndSet(NATIVE_COMPACT_MAP, "NATIVE_COMPACT_MAP");
        tagNameMap.extendAndSet(NATIVE_FAST_MAP, "NATIVE_FAST_MAP");
        tagNameMap.extendAndSet(NATIVE_FAST_MAP_INT_LIST, "NATIVE_FAST_MAP_INT_LIST");
        tagNameMap.extendAndSet(NATIVE_UNORDERED_MAP, "NATIVE_UNORDERED_MAP");
        tagNameMap.extendAndSet(NATIVE_HTTP_CONN, "NATIVE_HTTP_CONN");
        tagNameMap.extendAndSet(NATIVE_PGW_CONN, "NATIVE_PGW_CONN");
        tagNameMap.extendAndSet(NATIVE_PGW_PIPELINE, "NATIVE_PGW_PIPELINE");
        tagNameMap.extendAndSet(MMAP_INDEX_READER, "MMAP_INDEX_READER");
        tagNameMap.extendAndSet(MMAP_INDEX_WRITER, "MMAP_INDEX_WRITER");
        tagNameMap.extendAndSet(MMAP_INDEX_SLIDER, "MMAP_INDEX_SLIDER");
        tagNameMap.extendAndSet(MMAP_BLOCK_WRITER, "MMAP_BLOCK_WRITER");
        tagNameMap.extendAndSet(NATIVE_REPL, "NATIVE_REPL");
        tagNameMap.extendAndSet(NATIVE_SAMPLE_BY_LONG_LIST, "NATIVE_SAMPLE_BY_LONG_LIST");
        tagNameMap.extendAndSet(NATIVE_LATEST_BY_LONG_LIST, "NATIVE_LATEST_BY_LONG_LIST");
        tagNameMap.extendAndSet(NATIVE_JIT_LONG_LIST, "NATIVE_JIT_LONG_LIST");
        tagNameMap.extendAndSet(NATIVE_LONG_LIST, "NATIVE_LONG_LIST");
        tagNameMap.extendAndSet(NATIVE_JIT, "NATIVE_JIT");
        tagNameMap.extendAndSet(NATIVE_OFFLOAD, "NATIVE_OFFLOAD");
        tagNameMap.extendAndSet(MMAP_UPDATE, "MMAP_UPDATE");
        tagNameMap.extendAndSet(MMAP_PARQUET_PARTITION_CONVERTER, "MMAP_PARQUET_PARTITION_CONVERTER");
        tagNameMap.extendAndSet(MMAP_PARQUET_PARTITION_DECODER, "MMAP_PARQUET_PARTITION_DECODER");
        tagNameMap.extendAndSet(NATIVE_PATH, "NATIVE_PATH");
        tagNameMap.extendAndSet(NATIVE_TABLE_READER, "NATIVE_TABLE_READER");
        tagNameMap.extendAndSet(NATIVE_TABLE_WRITER, "NATIVE_TABLE_WRITER");
        tagNameMap.extendAndSet(NATIVE_CB2, "NATIVE_CB2");
        tagNameMap.extendAndSet(NATIVE_CB3, "NATIVE_CB3");
        tagNameMap.extendAndSet(NATIVE_CB4, "NATIVE_CB4");
        tagNameMap.extendAndSet(NATIVE_CB5, "NATIVE_CB5");
        tagNameMap.extendAndSet(MMAP_IMPORT, "MMAP_IMPORT");
        tagNameMap.extendAndSet(NATIVE_IMPORT, "NATIVE_IMPORT");
        tagNameMap.extendAndSet(NATIVE_ROSTI, "NATIVE_ROSTI");
        tagNameMap.extendAndSet(MMAP_TABLE_WAL_READER, "MMAP_TABLE_WAL_READER");
        tagNameMap.extendAndSet(MMAP_TABLE_WAL_WRITER, "MMAP_TABLE_WAL_WRITER");
        tagNameMap.extendAndSet(MMAP_SEQUENCER_METADATA, "MMAP_SEQUENCER_METADATA");
        tagNameMap.extendAndSet(MMAP_PARALLEL_IMPORT, "MMAP_PARALLEL_IMPORT");
        tagNameMap.extendAndSet(NATIVE_PARALLEL_IMPORT, "NATIVE_PARALLEL_IMPORT");
        tagNameMap.extendAndSet(NATIVE_JOIN_MAP, "NATIVE_JOIN_MAP");
        tagNameMap.extendAndSet(NATIVE_LOGGER, "NATIVE_LOGGER");
        tagNameMap.extendAndSet(NATIVE_MIG, "NATIVE_MIG");
        tagNameMap.extendAndSet(NATIVE_MIG_MMAP, "NATIVE_MIG_MMAP");
        tagNameMap.extendAndSet(NATIVE_ILP_RSS, "NATIVE_ILP_RSS");
        tagNameMap.extendAndSet(NATIVE_TLS_RSS, "NATIVE_TLS_RSS");
        tagNameMap.extendAndSet(NATIVE_TEXT_PARSER_RSS, "NATIVE_TEXT_PARSER_RSS");
        tagNameMap.extendAndSet(NATIVE_IO_DISPATCHER_RSS, "NATIVE_IO_DISPATCHER_RSS");
        tagNameMap.extendAndSet(NATIVE_FUNC_RSS, "NATIVE_FUNC_RSS");
        tagNameMap.extendAndSet(NATIVE_DIRECT_CHAR_SINK, "NATIVE_DIRECT_CHAR_SINK");
        tagNameMap.extendAndSet(NATIVE_DIRECT_UTF8_SINK, "NATIVE_DIRECT_UTF8_SINK");
        tagNameMap.extendAndSet(NATIVE_DIRECT_BYTE_SINK, "NATIVE_DIRECT_BYTE_SINK");
        tagNameMap.extendAndSet(MMAP_TX_LOG_CURSOR, "MMAP_TX_LOG_CURSOR");
        tagNameMap.extendAndSet(MMAP_TX_LOG, "MMAP_TX_LOG");
        tagNameMap.extendAndSet(NATIVE_SQL_COMPILER, "NATIVE_SQL_COMPILER");
        tagNameMap.extendAndSet(NATIVE_CIRCULAR_BUFFER, "NATIVE_CIRCULAR_BUFFER");
        tagNameMap.extendAndSet(NATIVE_GROUP_BY_FUNCTION, "NATIVE_GROUP_BY_FUNCTION");
        tagNameMap.extendAndSet(NATIVE_INDEX_READER, "NATIVE_INDEX_READER");
        tagNameMap.extendAndSet(NATIVE_TABLE_WAL_WRITER, "NATIVE_TABLE_WAL_WRITER");
        tagNameMap.extendAndSet(NATIVE_METADATA_READER, "NATIVE_METADATA_READER");
        tagNameMap.extendAndSet(NATIVE_BIT_SET, "NATIVE_BIT_SET");
        tagNameMap.extendAndSet(NATIVE_PARQUET_PARTITION_DECODER, "NATIVE_PARQUET_PARTITION_DECODER");
        tagNameMap.extendAndSet(NATIVE_PARQUET_PARTITION_UPDATER, "NATIVE_PARQUET_PARTITION_UPDATER");
    }
}
