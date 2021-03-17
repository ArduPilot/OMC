/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CWaypointLoop;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.helper.VectorNonEqual;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.flightplan.visitors.SectorVisitor;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.Sector;
import eu.mavinci.core.flightplan.CWaypointLoop;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.helper.VectorNonEqual;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.flightplan.visitors.SectorVisitor;
import eu.mavinci.geo.ISectorReferenced;

import java.util.OptionalDouble;

public class WaypointLoop extends CWaypointLoop implements ISectorReferenced {

    public static final String KEY = "eu.mavinci.flightplan.WaypointLoop";
    public static final String KEY_COUNT = KEY + ".count";
    public static final String KEY_TIME = KEY + ".time";
    public static final String KEY_TO_STRING = KEY + ".toString";

    public WaypointLoop(int count, int time, int id, IFlightplanContainer parent) {
        super(count, time, id, parent);
    }

    public WaypointLoop(int count, int time, IFlightplanContainer parent) {
        super(count, time, parent);
    }

    public WaypointLoop(IFlightplanContainer parent) {
        super(parent);
    }

    public WaypointLoop(WaypointLoop source) {
        super(source.count, source.time, source.id, source.parent);
        VectorNonEqual<IFlightplanStatement> vl = source.elements.clone();
        for (IFlightplanStatement statement : vl) {
            statement.setParent(this);
        }

        this.elements = vl;
        this.body = source.body;
        this.ignore = source.ignore;
    }

    public boolean isAddableToFlightplanContainer(java.lang.Class<? extends IFlightplanStatement> cls) {
        return super.isAddableToFlightplanContainer(cls) && !MapLayer.class.isAssignableFrom(cls);
    }

    SectorVisitor secVis = null;

    void startSecVis() {
        if (secVis != null) {
            return;
        }

        secVis = new SectorVisitor(this);
    }

    @Override
    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        super.flightplanStatementAdded(statement);
        secVis = null;
    }

    @Override
    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        super.flightplanStatementChanged(statement);
        secVis = null;
    }

    @Override
    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        super.flightplanStatementRemove(i, statement);
        secVis = null;
    }

    @Override
    public Sector getSector() {
        startSecVis();
        return secVis.getSector();
    }

    @Override
    public OptionalDouble getMaxElev() {
        startSecVis();
        return secVis.getMaxElev();
    }

    @Override
    public OptionalDouble getMinElev() {
        startSecVis();
        return secVis.getMinElev();
    }

    public String toString() {
        return DependencyInjector.getInstance()
            .getInstanceOf(ILanguageHelper.class)
            .getString(KEY_TO_STRING, count, time);
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new WaypointLoop(this);
    }

}
