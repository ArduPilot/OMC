/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.common.base.Objects;
import com.intel.missioncontrol.NotificationObject;
import com.intel.missioncontrol.helper.Expect;

class LensConfiguration extends NotificationObject implements ILensConfiguration {

    static final LensConfiguration NONE;

    static {
        NONE = new LensConfiguration(LensDescription.NONE);
        NONE.immutable = true;
    }

    private transient boolean immutable = false;
    private ILensDescription description;

    public LensConfiguration(ILensDescription description) {
        Expect.notNull(description, "description");
        this.description = description;
        registerSubObject(description);
    }

    @Override
    public ILensDescription getDescription() {
        return description;
    }

    @Override
    public void setDescription(ILensDescription description) {
        Expect.notNull(description, "description");
        verifyMutable();
        if (this.description != description) {
            ILensDescription oldValue = this.description;
            unregisterSubObject(oldValue);
            registerSubObject(description);
            this.description = description;
            notifyPropertyChanged(DESCRIPTION_PROPERTY, oldValue, description);
        }
    }

    @Override
    public ILensConfiguration deepCopy() {
        return new LensConfiguration(description);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof LensConfiguration)) {
            return false;
        }

        LensConfiguration other = (LensConfiguration)obj;
        return description.equals(other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(description);
    }

    @Override
    public String toString() {
        return description.getId();
    }

    private void verifyMutable() {
        if (immutable) {
            throw new IllegalStateException("The configuration is immutable and cannot be changed.");
        }
    }

}
