/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

    void serialize(CompositeSerializable value, OutputStream stream) throws IOException;

    void serialize(CompositeSerializable value, SerializationOptions options, OutputStream stream) throws IOException;

    <T extends CompositeSerializable> T deserialize(InputStream stream, Class<T> type) throws IOException;

    <T extends CompositeSerializable> T deserialize(InputStream stream, Class<T> type, SerializationOptions options)
            throws IOException;

}
