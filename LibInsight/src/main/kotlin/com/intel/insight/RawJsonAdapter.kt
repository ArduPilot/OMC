/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight

import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class RawJsonAdapter: TypeAdapter<String>() {
    override fun write(out: JsonWriter?, value: String?) {
        out?.jsonValue(value)
    }
    override fun read(reader: JsonReader?): String {
        return JsonParser().parse(reader).toString()
    }
}