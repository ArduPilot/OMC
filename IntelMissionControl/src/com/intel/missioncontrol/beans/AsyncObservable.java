/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans;

import com.intel.missioncontrol.PublishSource;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncObservable extends Observable {

    void addListener(InvalidationListener listener, Executor executor);

}
