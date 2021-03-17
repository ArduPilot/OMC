/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

    void serialize(Serializable value, OutputStream stream) throws IOException;

    <T extends Serializable> T deserialize(InputStream stream, Class<T> type) throws IOException;

}
