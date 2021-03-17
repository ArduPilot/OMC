/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public interface BidirectionalBindingMarker {

    class MarkedChangeListener<T> implements ChangeListener<T>, BidirectionalBindingMarker {
        private final ChangeListener<T> listener;

        MarkedChangeListener(ChangeListener<T> listener) {
            this.listener = listener;
        }

        @Override
        public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
            listener.changed(observable, oldValue, newValue);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof MarkedChangeListener) {
                return listener.equals(((MarkedChangeListener)obj).listener);
            }

            return listener.equals(obj);
        }
    }

    static <T> ChangeListener<T> add(ChangeListener<T> listener) {
        if (listener instanceof BidirectionalBindingMarker) {
            return listener;
        }

        return new MarkedChangeListener<>(listener);
    }

}
