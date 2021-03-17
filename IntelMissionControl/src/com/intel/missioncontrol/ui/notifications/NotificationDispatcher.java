/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.NamedFuture;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NotificationDispatcher implements INotificationDispatcher {

    private final IApplicationContext applicationContext;
    private final ListProperty<NamedFuture<?>> runningFutures =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    @Inject
    public NotificationDispatcher(IApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ReadOnlyListProperty<NamedFuture<?>> runningFuturesProperty() {
        return runningFutures;
    }

    @Override
    public <T> void enqueue(NamedFuture<T> future) {
        enqueueImpl(future, null, null, null, null);
    }

    @Override
    public <T> void enqueue(NamedFuture<T> future, Toast successToast) {
        enqueueImpl(future, successToast, null, null, null);
    }

    @Override
    public <T> void enqueue(NamedFuture<T> future, Toast successToast, Toast failedToast) {
        enqueueImpl(future, successToast, failedToast, null, null);
    }

    @Override
    public <T> void enqueue(NamedFuture<T> future, Toast successToast, Toast failedToast, Toast runningToast) {
        enqueueImpl(future, successToast, failedToast, runningToast, null);
    }

    @Override
    public <T> void enqueue(
            NamedFuture<T> future, Toast successToast, Toast failedToast, Toast runningToast, Toast cancelledToast) {
        enqueueImpl(future, successToast, failedToast, runningToast, cancelledToast);
    }

    @SuppressWarnings("unchecked")
    private <T> void enqueueImpl(
            NamedFuture<T> future,
            @Nullable Toast succeededToast,
            @Nullable Toast failedToast,
            @Nullable Toast runningToast,
            @Nullable Toast cancelledToast) {
        if (succeededToast != null) {
            future.onSuccess(f -> applicationContext.addToast(succeededToast));
        }

        if (failedToast != null) {
            future.onFailure(f -> applicationContext.addToast(failedToast));
        }

        future.onDone(
            f -> {
                if (cancelledToast != null && f.isCancelled()) {
                    applicationContext.addToast(cancelledToast);
                }

                Dispatcher.postToUI(() -> runningFutures.remove(future));
            });

        Dispatcher.postToUI(() -> runningFutures.add(future));

        if (runningToast != null) {
            applicationContext.addToast(runningToast);
        }
    }

}
