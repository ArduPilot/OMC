/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CPicAreaCorners;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.flightplan.visitors.SectorVisitor;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.Sector;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CPicAreaCorners;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.flightplan.visitors.SectorVisitor;
import eu.mavinci.geo.ISectorReferenced;

import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicBoolean;

public class PicAreaCorners extends CPicAreaCorners implements ISectorReferenced {

    private SectorVisitor secVis;
    private final AtomicBoolean needsRecomputeSector = new AtomicBoolean(false);

    public PicAreaCorners(CPicArea parent) {
        super(parent);
        secVis = new SectorVisitor(this);
    }

    public PicAreaCorners(PicAreaCorners source) {
        for (IFlightplanStatement statement : source.elements) {
            IFlightplanRelatedObject statementCopy = statement.getCopy();
            this.elements.add((IFlightplanStatement)statementCopy);
            statementCopy.setParent(this);
        }

        secVis = new SectorVisitor(this);
    }

    public boolean isAddableToFlightplanContainer(java.lang.Class<? extends IFlightplanStatement> cls) {
        return super.isAddableToFlightplanContainer(cls) && !MapLayer.class.isAssignableFrom(cls);
    }

    private void updateDerived(){
        if (needsRecomputeSector.getAndSet(false)) {
            SectorVisitor secVis = new SectorVisitor(this);
            secVis.startVisit(this);
            this.secVis = secVis;
        }
    }

    @Override
    public Sector getSector() {
        updateDerived();
        return secVis.getSector();
    }

    @Override
    public OptionalDouble getMaxElev() {
        updateDerived();
        return secVis.getMaxElev();
    }

    @Override
    public OptionalDouble getMinElev() {
        updateDerived();
        return secVis.getMinElev();
    }

    @Override
    public Point getFromFlightplanContainer(int i) {
        return (Point)super.getFromFlightplanContainer(i);
    }

    @Override
    public Point getLastElement() {
        return (Point)super.getLastElement();
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new PicAreaCorners(this);
    }

    public void setNeedsRecomputeSector(boolean b) {
        getParent().reinit();
        needsRecomputeSector.set(b);
    }

    @Override
    public void addAfterToFlightplanContainer(IFlightplanStatement addAfterThisOne, IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        super.addAfterToFlightplanContainer(addAfterThisOne, statement);
        setNeedsRecomputeSector(true);
    }

    @Override
    public void addToFlightplanContainer(IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        super.addToFlightplanContainer(statement);
        setNeedsRecomputeSector(true);
    }

    @Override
    public void addToFlightplanContainer(int index, IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        super.addToFlightplanContainer(index, statement);
        setNeedsRecomputeSector(true);
    }

    @Override
    public IFlightplanStatement removeFromFlightplanContainer(int i) {
        IFlightplanStatement tmp = super.removeFromFlightplanContainer(i);
        setNeedsRecomputeSector(true);
        return tmp;
    }

    @Override
    public IFlightplanStatement removeFromFlightplanContainer(IFlightplanStatement statement) {
        IFlightplanStatement tmp = super.removeFromFlightplanContainer(statement);
        setNeedsRecomputeSector(true);
        return tmp;
    }

    @Override
    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        setNeedsRecomputeSector(true);
        super.flightplanStatementChanged(statement);
    }

    @Override
    public void setMute(boolean mute) {
        if (!mute){
            setNeedsRecomputeSector(true);
        }
        super.setMute(mute);
    }

    @Override
    public void setSilentUnmute() {
        setNeedsRecomputeSector(true);
        super.setSilentUnmute();
    }
}
