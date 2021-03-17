/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwind.javafx;

import java.util.EventListener;

interface RedrawListener extends EventListener {

    void redrawRequested(int millis);

}
