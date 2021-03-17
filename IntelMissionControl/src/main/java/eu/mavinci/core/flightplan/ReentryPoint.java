/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public abstract class ReentryPoint implements IReentryPoint, IMuteable {

    public static int INVALID_REENTRYPOINT = -99;

    protected int id = INVALID_REENTRYPOINT;
    private IFlightplanContainer parent = null;
    boolean mute = false;

    protected ReentryPoint(int id) {
        this.id = id;
    }

    public ReentryPoint(IFlightplanContainer parent) {
        this.parent = parent;
        reassignIDs();
    }

    public int getId() {
        return id;
    }

    public void setParent(IFlightplanContainer container) {
        if (parent != container) {
            parent = container;
            if (id == INVALID_REENTRYPOINT) {
                reassignIDs();
            }
        } else {
            parent = container;
        }
    }

    public void setId(int id) {
        if (this.id != id) {
            this.id = id;
            informChangeListener();
        }
    }

    public void reassignIDs() {
        if (parent != null && parent.getFlightplan() != null) {
            id = -1;
            setId(parent.getFlightplan().getUnusedId());
        }
    }

    public ReentryPoint(IFlightplanContainer parent, int id) {
        this.parent = parent;
        this.id = id;
    }

    public void informChangeListener() {
        if (mute) {
            return;
        }

        IFlightplanContainer cont = this.parent;
        if (cont != null) {
            cont.flightplanStatementChanged(this);
        }
    }

    public IFlightplanContainer getParent() {
        return parent;
    }

    public CFlightplan getFlightplan() {
        if (parent == null) {
            return null;
        }

        return parent.getFlightplan();
    }

    @Override
    public void setMute(boolean mute) {
        if (this.mute == mute) {
            return;
        }

        this.mute = mute;
        if (!mute) {
            informChangeListener();
        }
    }

    @Override
    public void setSilentUnmute() {
        this.mute = false;
    }

    @Override
    public boolean isMute() {
        return mute;
    }

}
