/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.measure.Quantity;
import java.lang.ref.WeakReference;

public interface INotificationObject {

    interface ChangeListener {
        void propertyChange(ChangeEvent event);
    }

    class WeakChangeListener implements ChangeListener {
        private final WeakReference<ChangeListener> wref;

        public WeakChangeListener(ChangeListener listener) {
            wref = new WeakReference<>(listener);
        }

        @Override
        public void propertyChange(ChangeEvent event) {
            ChangeListener listener = wref.get();
            if (listener != null) {
                listener.propertyChange(event);
            } else {
                event.getSender().removeListener(this);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            return obj == wref.get();
        }
    }

    class ChangeEvent {
        private final INotificationObject sender;
        private final INotificationObject source;
        private final String propertyName;
        private final Object oldValue;
        private final Object newValue;

        ChangeEvent(
                INotificationObject sender,
                INotificationObject source,
                String propertyName,
                Object oldValue,
                Object newValue) {
            this.sender = sender;
            this.source = source;
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        ChangeEvent(INotificationObject sender, ChangeEvent other) {
            this.sender = sender;
            this.source = other.source;
            this.propertyName = other.propertyName;
            this.oldValue = other.oldValue;
            this.newValue = other.newValue;
        }

        public INotificationObject getSender() {
            return sender;
        }

        public INotificationObject getSource() {
            return source;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public <T> T getNewValue(Class<T> cls) {
            return cls.cast(newValue);
        }

        public <T> T getOldValue(Class<T> cls) {
            return cls.cast(oldValue);
        }

        @SuppressWarnings("unchecked")
        public <Q extends Quantity<Q>> Quantity<Q> getNewQuantity(Class<Q> quantityType) {
            return (Quantity<Q>)newValue;
        }

        @SuppressWarnings("unchecked")
        public <Q extends Quantity<Q>> Quantity<Q> getOldQuantity(Class<Q> quantityType) {
            return (Quantity<Q>)oldValue;
        }
    }

    void addListener(ChangeListener listener);

    void removeListener(ChangeListener listener);

}
