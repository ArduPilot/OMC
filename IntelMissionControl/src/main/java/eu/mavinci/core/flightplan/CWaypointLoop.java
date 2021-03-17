/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.flightplan.Point;

public abstract class CWaypointLoop extends AReentryContainer implements IFlightplanIgnoreable, IFlightplanNodeBody {

    public static final int DEFAULT_LOOP_COUNT = 2;
    public static final int DEFAULT_LOOP_TIME = 0; // loop zeit

    protected int count;
    protected int time; // in sec
    protected String body = "";
    protected boolean ignore = false;

    protected CWaypointLoop(IFlightplanContainer parent) {
        super(parent);
        setCount(DEFAULT_LOOP_COUNT);
        setTime(DEFAULT_LOOP_TIME);
    }

    protected CWaypointLoop(int count, int time, IFlightplanContainer parent) {
        super(parent);
        setCount(count);
        setTime(time);
    }

    protected CWaypointLoop(int count, int time, int id, IFlightplanContainer parent) {
        super(id, parent);
        setCount(count);
        setTime(time);
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        if (sizeOfFlightplanContainer() >= getMaxSize()) {
            return false;
        }

        return !Point.class.isAssignableFrom(cls)
            && !IHasStart.class.isAssignableFrom(cls);
    }

    public static final int MAXCOUNT = 100;
    public static final int MINCOUNT = 0;
    public static final int MAXTIME = 3600;

    public void setCount(int count) {
        if (count > MAXCOUNT) {
            count = MAXCOUNT;
        }

        if (count < MINCOUNT) {
            count = MINCOUNT;
        }

        if (this.count != count) {
            this.count = count;
            flightplanStatementChanged(this);
        }
    }

    public void setTime(int time) {
        if (time > MAXTIME) {
            time = MAXTIME;
        }

        if (time < 0) {
            time = 0;
        }

        if (this.time != time) {
            this.time = time;
            flightplanStatementChanged(this);
        }
    }

    public int getCount() {
        return count;
    }

    public int getTime() {
        return time;
    }

    public String toString() {
        return "CWaypointLoop"; // TODO more useful name
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof CWaypointLoop) {
            CWaypointLoop loop = (CWaypointLoop)o;
            return elements.equals(loop.elements)
                && count == loop.count
                && time == loop.time
                && ignore == loop.ignore
                && body.equals(loop.body);
        }

        return false;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        if (body != null && !this.body.equals(body)) {
            this.body = body;
            flightplanStatementChanged(this);
        }
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        if (this.ignore == ignore) {
            return;
        }

        this.ignore = ignore;
        flightplanStatementChanged(this);
    }

}
