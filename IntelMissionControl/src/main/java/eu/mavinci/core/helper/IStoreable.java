/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

public interface IStoreable {
    public void saveState();

    public void loadState();

    public boolean isLoading();

    public boolean isSaving();
}
