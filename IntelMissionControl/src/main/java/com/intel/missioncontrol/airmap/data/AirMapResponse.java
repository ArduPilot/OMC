package com.intel.missioncontrol.airmap.data;

public class AirMapResponse<T> {
    String status;
    T data;

    public T getData() {
        return data;
    }
}
