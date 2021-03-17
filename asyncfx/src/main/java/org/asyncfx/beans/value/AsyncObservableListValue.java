/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.value;

import javafx.beans.value.ObservableObjectValue;
import org.asyncfx.PublishSource;
import org.asyncfx.collections.AsyncObservableList;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncObservableListValue<E>
        extends ObservableObjectValue<AsyncObservableList<E>>, AsyncObservableList<E> {}
