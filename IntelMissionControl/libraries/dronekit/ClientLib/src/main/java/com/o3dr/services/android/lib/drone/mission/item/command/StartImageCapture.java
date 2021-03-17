/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

public class StartImageCapture extends MissionItem implements MissionItem.Command, android.os.Parcelable{

    private int numberOfImagesToCapture;
    //TODO: Complete this class by including member variables below if needed (9/9/2018)
    private int durationBetweenConsecPictures;
    private int captureSequency;

    public StartImageCapture(){
        super(MissionItemType.START_IMAGE_CAPTURE);
    }

    public StartImageCapture(StartImageCapture copy){
        super(MissionItemType.START_IMAGE_CAPTURE);
        numberOfImagesToCapture = copy.numberOfImagesToCapture;
    }

    public int getNumberOfImagesToCapture() {
        return numberOfImagesToCapture;
    }

    public void setNumberOfImagesToCapture(int numberOfImagesToCapture) {
        this.numberOfImagesToCapture = numberOfImagesToCapture;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StartImageCapture)) return false;
        if (!super.equals(o)) return false;

        StartImageCapture that = (StartImageCapture) o;
        return Integer.compare(that.numberOfImagesToCapture, numberOfImagesToCapture) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Long.valueOf(numberOfImagesToCapture);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "StartImageCapture{" +
                "numberOfImagesToCapture=" + numberOfImagesToCapture+
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.numberOfImagesToCapture);
    }

    private StartImageCapture(Parcel in) {
        super(in);
        this.numberOfImagesToCapture = in.readInt();
    }

    @Override
    public MissionItem clone() {
        return new StartImageCapture(this);
    }

    public static final Creator<StartImageCapture> CREATOR = new Creator<StartImageCapture>() {
        public StartImageCapture createFromParcel(Parcel source) {
            return new StartImageCapture(source);
        }

        public StartImageCapture[] newArray(int size) {
            return new StartImageCapture[size];
        }
    };
}
