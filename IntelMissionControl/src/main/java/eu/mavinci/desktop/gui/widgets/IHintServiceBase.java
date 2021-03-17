/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.widgets;

public interface IHintServiceBase {
    void showHint(String text);

    void showHint(String text, int ttlSeconds);

    void showAlert(String text);

    void showAlert(String text, int ttlSeconds);
}
