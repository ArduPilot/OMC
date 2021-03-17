/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.value;

import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface SubChangeListener {

    void changed(ObservableValue<?> observable, Object oldValue, Object newValue, boolean subChange);

}
