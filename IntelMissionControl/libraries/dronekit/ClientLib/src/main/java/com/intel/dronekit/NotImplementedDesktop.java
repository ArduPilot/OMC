package com.intel.dronekit;

public class NotImplementedDesktop extends UnsupportedOperationException {
    public NotImplementedDesktop() {
        super("Not Supported on desktop");
    }
}
