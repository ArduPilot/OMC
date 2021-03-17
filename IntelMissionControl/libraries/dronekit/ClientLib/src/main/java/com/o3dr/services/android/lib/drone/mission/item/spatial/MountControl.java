/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

public class MountControl extends BaseSpatialItem implements android.os.Parcelable {

    private double pitchAngle;
    private double yawAngle;
    private double rollAngle = (0);
    //TODO: Include enum for MAV_MOUNT_MODE if needed 9/9/2018. Also check how LatLonAlt values will be used.
    //private double rollAngle;


    public MountControl(){
        super(MissionItemType.START_IMAGE_CAPTURE.MOUNT_CONTROL);
    }

    public MountControl(MountControl copy){
        super(copy);
        pitchAngle = copy.pitchAngle;
        yawAngle = copy.yawAngle;
        rollAngle = copy.rollAngle;
    }

    public double getPitchAngle() {
        return pitchAngle;
    }

    public void setPitchAngle(double pitchAngle) {
        this.pitchAngle = pitchAngle;
    }

    public double getYawAngle() {
        return yawAngle;
    }

    public void setYawAngle(double yawAngle) {
        this.yawAngle = yawAngle;
    }

    public double getRollAngle() {
        return rollAngle;
    }

    public void setRollAngle(double rollAngle) {
        this.rollAngle = rollAngle;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.pitchAngle);
        dest.writeDouble(this.yawAngle);
    }

    private MountControl(Parcel in) {
        super(in);
        this.pitchAngle= in.readDouble();
        this.yawAngle = in.readDouble();
    }

    @Override
    public String toString() {
        return "MoundControl{" +
                "pitchAngle=" + pitchAngle +
                ", yawAngle=" + yawAngle +
                ", " + super.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MountControl)) return false;
        if (!super.equals(o)) return false;

        MountControl moundControl = (MountControl) o;

        if (Double.compare(moundControl.pitchAngle, pitchAngle) != 0) return false;
        return (Double.compare(moundControl.yawAngle, yawAngle) != 0);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(pitchAngle);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(yawAngle);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public MissionItem clone() {
        return new MountControl(this);
    }

    public static final Creator<MountControl> CREATOR = new Creator<MountControl>() {
        public MountControl createFromParcel(Parcel source) {
            return new MountControl(source);
        }

        public MountControl[] newArray(int size) {
            return new MountControl[size];
        }
    };
}
