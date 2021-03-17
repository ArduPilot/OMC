/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RawJsonAdapter extends TypeAdapter {
    public void write(@Nullable JsonWriter out, @Nullable String value) {
        if (out != null) {
            try {
                out.jsonValue(value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // $FF: synthetic method
    // $FF: bridge method
    public void write(JsonWriter var1, Object var2) {
        this.write(var1, (String)var2);
    }

    @NotNull
    public String read(@Nullable JsonReader reader) {
        String var10000 = (new JsonParser()).parse(reader).toString();
                return var10000;
    }

}
