/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.airplane;

import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.IAirplane;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FlightplanAirplanePair implements IFlightplanChangeListener, IRecomputeListener {

    private Flightplan fp;
    private final IAirplane plane;

    /**
     * please use this class to define what has to be checked in the preflight section the flight plan here is referring
     * to the currently selected one. please make sure you are calling setFlightplan on every dropdown selection change
     * (so even before pressing launch) if no flight plan is selected, please set it to NULL in this API
     */
    public FlightplanAirplanePair(@Nullable Flightplan fp, IAirplane plane) {
        // flight plan CAN be null!!! in case their is no flight plan selected currently!
        this.plane = plane;
        setFlightplan(fp);
    }

    public FlightplanAirplanePair(IAirplane plane) {
        this.plane = plane;
    }

    public IAirplane getPlane() {
        return this.plane;
    }

    /**
     * setting the currently in the UI selected flight plan which might be sent to the drone soon.. if no one is
     * selected, please set it to NULL
     */
    public void setFlightplan(Flightplan fp) {
        if (this.fp == fp) {
            return;
        }

        if (this.fp != null) {
            this.fp.removeFPChangeListener(this);
        }

        this.fp = fp;
        if (this.fp != null) {
            this.fp.addFPChangeListener(this);
            flightplanStructureChanged(fp);
        }
    }

    public Flightplan getFlightplan() {
        return fp;
    }

    private WeakListenerList<IFlightplanChangeListener> listenersFP = new WeakListenerList<>("FlightplanListeners");
    private WeakListenerList<IRecomputeListener> listenersRecompute =
        new WeakListenerList<>("FlightplanRecomputeListeners");

    public void addListener(@NonNull IFlightplanChangeListener listener) {
        listenersFP.add(listener);
    }

    public void removeListener(@NonNull IFlightplanChangeListener listener) {
        listenersFP.remove(listener);
    }

    public void addListener(@NonNull IRecomputeListener listener) {
        listenersRecompute.add(listener);
    }

    public void removeListener(@NonNull IRecomputeListener listener) {
        listenersRecompute.remove(listener);
    }

    @Override
    public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
        for (IFlightplanChangeListener listener : listenersFP) {
            listener.flightplanStructureChanged(fp);
        }
    }

    @Override
    public void flightplanValuesChanged(IFlightplanRelatedObject fp) {
        for (IFlightplanChangeListener listener : listenersFP) {
            listener.flightplanValuesChanged(fp);
        }
    }

    @Override
    public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {
        for (IFlightplanChangeListener listener : listenersFP) {
            listener.flightplanElementRemoved(fp, i, statement);
        }
    }

    @Override
    public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {
        for (IFlightplanChangeListener listener : listenersFP) {
            listener.flightplanElementAdded(fp, statement);
        }
    }

    @Override
    public void recomputeReady(Recomputer recomputer, boolean anotherRecomputeIsWaiting, long runNo) {
        for (IRecomputeListener listener : listenersRecompute) {
            listener.recomputeReady(recomputer, anotherRecomputeIsWaiting, runNo);
        }
    }
}
