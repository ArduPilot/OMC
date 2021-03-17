/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence;

import com.intel.missioncontrol.project.ResourceReference;
import java.io.InputStream;
import java.io.OutputStream;
import org.asyncfx.concurrent.Future;

public interface ResourceResolver {

    Future<InputStream> openInputStreamAsync(ResourceReference reference);

    Future<OutputStream> openOutputStreamAsync(ResourceReference reference);

}
