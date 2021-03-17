/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.property;

import gov.nasa.worldwind.avlist.AVList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import org.asyncfx.beans.property.AsyncStringPropertyBase;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.concurrent.Dispatcher;

public class WWAsyncStringProperty extends AsyncStringPropertyBase {

    private final Object bean;
    private final String name;
    private final String propertyName;
    private final Consumer<String> setter;
    private boolean isChanging;

    public WWAsyncStringProperty(
            Object bean,
            String name,
            Dispatcher dispatcher,
            String worldWindPropertyName,
            AVList avList,
            Consumer<String> setter) {
        super(new PropertyMetadata.Builder<String>().dispatcher(dispatcher).create());
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
        final String value = get();

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

    private static class Listener implements PropertyChangeListener {
        private final WeakReference<WWAsyncStringProperty> wref;
        private final AVList avList;

        public Listener(WWAsyncStringProperty ref, AVList avList) {
            this.avList = avList;
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            WWAsyncStringProperty ref = wref.get();
            if (ref == null) {
                avList.removePropertyChangeListener(this);
                return;
            }

            if (ref.isChanging) {
                return;
            } else if (evt.getPropertyName().equals(ref.propertyName)) {
                try {
                    ref.isChanging = true;
                    PropertyHelper.setValueSafe(ref, (String)evt.getNewValue());
                } finally {
                    ref.isChanging = false;
                }
            }
        }
    }

}
