/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NotificationObject implements INotificationObject {

    private final List<ChangeListener> listeners = new ArrayList<>();
    private final ChangeListener subObjectListener = this::notifySubObjectPropertyChanged;

    private boolean isIterating;
    private List<ChangeListener> addedWhileIterating;
    private List<ChangeListener> removedWhileIterating;

    public synchronized void addListener(ChangeListener listener) {
        if (isIterating) {
            if (addedWhileIterating == null) {
                addedWhileIterating = new ArrayList<>();
            }

            addedWhileIterating.add(listener);
        } else {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(ChangeListener listener) {
        if (isIterating) {
            if (removedWhileIterating == null) {
                removedWhileIterating = new ArrayList<>();
            }

            removedWhileIterating.add(listener);
        } else {
            listeners.remove(listener);
        }
    }

    protected void registerSubObject(@Nullable INotificationObject notificationObject) {
        if (notificationObject != null) {
            notificationObject.addListener(subObjectListener);
        }
    }

    protected void unregisterSubObject(@Nullable INotificationObject notificationObject) {
        if (notificationObject != null) {
            notificationObject.removeListener(subObjectListener);
        }
    }

    protected void notifyPropertyChanged(String propertyName, Object oldValue, Object newValue) {
        notifyPropertyChanged(new ChangeEvent(this, this, propertyName, oldValue, newValue));
    }

    private synchronized void notifyPropertyChanged(ChangeEvent event) {
        isIterating = true;

        for (ChangeListener listener : listeners) {
            listener.propertyChange(event);
        }

        if (addedWhileIterating != null) {
            listeners.addAll(addedWhileIterating);
            addedWhileIterating = null;
        }

        if (removedWhileIterating != null) {
            listeners.removeAll(removedWhileIterating);
            removedWhileIterating = null;
        }

        isIterating = false;
    }

    private void notifySubObjectPropertyChanged(ChangeEvent event) {
        notifyPropertyChanged(new ChangeEvent(this, event));
    }

}
