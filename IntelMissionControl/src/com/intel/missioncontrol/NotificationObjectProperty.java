/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.custom.MuteObjectProperty;

/** Created by ekorotko on 18.12.2017. */
@Deprecated
public class NotificationObjectProperty<T extends INotificationObject> extends MuteObjectProperty<T> {

    private INotificationObject.ChangeListener listener = event -> fireValueChangedEvent();

    @Override
    public void set(T newValue) {
        INotificationObject old = get();
        if (old != null) {
            old.removeListener(listener);
        }

        super.set(newValue);
        newValue.addListener(listener);
    }
}
