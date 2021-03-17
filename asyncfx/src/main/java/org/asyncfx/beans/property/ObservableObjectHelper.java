/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import javafx.beans.InvalidationListener;
import org.asyncfx.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class ObservableObjectHelper {

    public static AsyncProperty[] getProperties(ObservableObject obj) {
        return obj.getProperties();
    }

    public static void addListener(ObservableObject obj, InvalidationListener listener) {
        obj.addListener(listener);
    }

    public static void removeListener(ObservableObject obj, InvalidationListener listener) {
        obj.removeListener(listener);
    }

}
