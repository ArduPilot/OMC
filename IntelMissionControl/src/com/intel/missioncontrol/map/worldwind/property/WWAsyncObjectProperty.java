/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.property;

import com.intel.missioncontrol.beans.property.AsyncObjectPropertyBase;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import gov.nasa.worldwind.avlist.AVList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class WWAsyncObjectProperty<T> extends AsyncObjectPropertyBase<T> {

    private final Object bean;
    private final String name;
    private final String propertyName;
    private final Consumer<T> setter;
    private boolean isChanging;

    public WWAsyncObjectProperty(
            Object bean,
            String name,
            SynchronizationRoot syncRoot,
            String worldWindPropertyName,
            AVList avList,
            Consumer<T> setter) {
        super(new PropertyMetadata.Builder<T>().synchronizationContext(syncRoot).create());
        this.bean = bean;
        this.name = name;
        this.propertyName = worldWindPropertyName;
        this.setter = setter;
        avList.addPropertyChangeListener(new Listener(this, avList));
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void invalidated() {
        super.invalidated();
        final T value = get();

        if (isChanging) {
            return;
        }

        try {
            isChanging = true;
            setter.accept(value);
        } finally {
            isChanging = false;
        }
    }

    private static class Listener<T> implements PropertyChangeListener {
        private final WeakReference<WWAsyncObjectProperty> wref;
        private final AVList avList;

        public Listener(WWAsyncObjectProperty ref, AVList avList) {
            this.avList = avList;
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            WWAsyncObjectProperty ref = wref.get();
            if (ref == null) {
                avList.removePropertyChangeListener(this);
                return;
            }

            if (ref.isChanging) {
                return;
            } else if (evt.getPropertyName().equals(ref.propertyName)) {
                try {
                    ref.isChanging = true;
                    ref.setAsync((T)evt.getNewValue());
                } finally {
                    ref.isChanging = false;
                }
            }
        }
    }

}
