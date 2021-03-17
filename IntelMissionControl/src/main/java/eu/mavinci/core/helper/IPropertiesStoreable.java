/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

public interface IPropertiesStoreable {

    public void storeState(IProperties prop, String keyPrefix);

    public void loadState(IProperties prop, String keyPrefix);

}
