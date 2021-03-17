/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import eu.mavinci.core.obfuscation.IKeepAll;

import java.io.InputStream;

public interface IReaderFromCache extends IKeepAll {

    /**
     * @param is
     * @return true, if stream was successfully parsed
     */
    public boolean readInputFromCache(InputStream is) throws Exception;
}
