/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.Collection;
import javafx.collections.ObservableSet;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubObservable;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface SubObservableSet<E> extends ObservableSet<E>, Collection<E>, SubObservable {}
