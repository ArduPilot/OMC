/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.widgets.i18n;

public interface MComponent {

    public void setKey(String KEY);

    public String getKey();

    public static final String KEY_TOOLTIP = ".tooltip";
    public static final String KEY_TITLE = ".title";

}
