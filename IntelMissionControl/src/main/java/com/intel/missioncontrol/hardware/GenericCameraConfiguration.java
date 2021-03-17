/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.common.base.Objects;
import com.intel.missioncontrol.NotificationObject;
import com.intel.missioncontrol.helper.Expect;
import org.checkerframework.checker.nullness.qual.Nullable;

class GenericCameraConfiguration extends NotificationObject implements IGenericCameraConfiguration {

    static final GenericCameraConfiguration NONE;

    static {
        NONE = new GenericCameraConfiguration(GenericCameraDescription.NONE, LensConfiguration.NONE);
        NONE.immutable = true;
    }

    private transient boolean immutable = false;
    private IGenericCameraDescription description;
    private ILensConfiguration lens;

    public GenericCameraConfiguration(IGenericCameraDescription description, @Nullable ILensConfiguration lens) {
        Expect.notNull(description, "description");
        this.description = description;
        this.lens = lens;
        registerSubObject(description);
        registerSubObject(lens);
    }

    @Override
    public IGenericCameraDescription getDescription() {
        return description;
    }

    @Override
    public void setDescription(IGenericCameraDescription description) {
        Expect.notNull(description, "description");
        verifyMutable();
        if (this.description != description) {
            IGenericCameraDescription oldValue = this.description;
            unregisterSubObject(oldValue);
            registerSubObject(description);
            this.description = description;
            notifyPropertyChanged(DESCRIPTION_PROPERTY, oldValue, description);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ILensConfiguration> T getLens(Class<T> lensClass) {
        if (lens == null) {
            throw new HardwareConfigurationException("No lens available.");
        }

        if (!lens.getClass().isAssignableFrom(lensClass)) {
            throw new HardwareConfigurationException(
                "The requested lens does not exist (requested: "
                    + lensClass.getName()
                    + ", actual: "
                    + lens.getClass().getName()
                    + ").");
        }

        return (T)lens;
    }

    @Override
    @Nullable
    public ILensConfiguration getLens() {
        return lens;
    }

    @Override
    public void setLens(ILensConfiguration lens) {
        Expect.notNull(lens, "lens");
        verifyMutable();
        if (!this.lens.equals(lens)) {
            ILensConfiguration oldValue = this.lens;
            unregisterSubObject(oldValue);
            registerSubObject(lens);
            this.lens = lens;
            notifyPropertyChanged(LENS_PROPERTY, oldValue, lens);
        }
    }

    @Override
    public IPayloadConfiguration deepCopy() {
        return new GenericCameraConfiguration(description, lens.deepCopy());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof GenericCameraConfiguration)) {
            return false;
        }

        GenericCameraConfiguration other = (GenericCameraConfiguration)obj;
        return description.equals(other.description) && lens.equals(other.lens);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(description, lens);
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
