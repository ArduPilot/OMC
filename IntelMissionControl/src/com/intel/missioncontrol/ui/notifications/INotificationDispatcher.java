/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import com.intel.missioncontrol.concurrent.NamedFuture;
import javafx.beans.property.ReadOnlyListProperty;

public interface INotificationDispatcher {

    ReadOnlyListProperty<NamedFuture<?>> runningFuturesProperty();

    <T> void enqueue(NamedFuture<T> future);

    <T> void enqueue(NamedFuture<T> future, Toast successToast);

    <T> void enqueue(NamedFuture<T> future, Toast successToast, Toast failedToast);

    <T> void enqueue(NamedFuture<T> future, Toast successToast, Toast failedToast, Toast runningToast);

    <T> void enqueue(
            NamedFuture<T> future, Toast succeededToast, Toast failedToast, Toast runningToast, Toast cancelledToast);

}
