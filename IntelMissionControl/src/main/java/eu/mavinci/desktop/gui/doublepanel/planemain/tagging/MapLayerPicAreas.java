/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.visitors.ExtractPicAreasVisitor;
import eu.mavinci.core.flightplan.visitors.IFlightplanVisitor;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.Point;
import eu.mavinci.geo.GeoReferencedHelper;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.util.Iterator;
import java.util.OptionalDouble;
import java.util.logging.Level;

public class MapLayerPicAreas extends MapLayer implements ISectorReferenced, IMatchingRelated, IFlightplanContainer {

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicAreas";

    public static final String KEY_SUFFIX_CropCoverage = ".cropCoverage";

    protected boolean cropCoverage = true;

    public boolean isCropCoverage() {
        return cropCoverage;
    }

    public void setCropCoverage(boolean cropCoverage) {
        if (this.cropCoverage == cropCoverage) {
            return;
        }

        this.cropCoverage = cropCoverage;
        mapLayerStructureChanged(this);
        mapLayerVisibilityChanged(this, isVisible);
    }

    public boolean tryAddPicAreasFromFlightplan(Flightplan fp) {
        boolean addedSomething = false;
        try {
            ExtractPicAreasVisitor vis = new ExtractPicAreasVisitor();
            vis.startVisit(fp);
            for (CPicArea cpicArea : vis.picAreas) {
                PicArea picArea = (PicArea)cpicArea;
                if (!picArea.getPlanType().supportsCoverageComputation()) {
                    continue;
                }

                // to make getHull work corectly
                picArea.computeFlightLinesNotSpreading(true);

                MapLayerPicArea newArea = new MapLayerPicArea((MapLayerMatching)getMatching());
                newArea.setGSD(picArea.getGsd());
                newArea.setName(fp.getName() + " - " + picArea.getName());
                for (LatLon tmp : picArea.getHull()) {
                    Point point = new Point(tmp.latitude.degrees, tmp.longitude.degrees);
                    newArea.addToFlightplanContainer(point);
                }

                // prevent equal doubled picAreas
                boolean isNew = true;
                for (IFlightplanStatement oldArea : getMatching().getPicAreas()) {
                    if (oldArea.equals(newArea)) {
                        isNew = false;
                        break;
                    }
                }

                if (isNew) {
                    addMapLayer(newArea);
                    addedSomething = true;
                }
            }
        } catch (Throwable t) {
            Debug.getLog().log(Level.WARNING, "Could not import PicAreas from Flightplan into new Matching", t);
        }

        return addedSomething;
    }

    public void setTristate(boolean isVisible, boolean cropCoverage) {
        if (this.isVisible != isVisible || this.cropCoverage != cropCoverage) {
            this.cropCoverage = cropCoverage;
            this.isVisible = isVisible;
            mapLayerStructureChanged(this);
            mapLayerVisibilityChanged(this, isVisible);
        }
    }

    AMapLayerMatching matching;

    public MapLayerPicAreas(AMapLayerMatching matching) {
        super(true);
        this.matching = matching;
    }

    @Override
    public OptionalDouble getMaxElev() {
        return GeoReferencedHelper.getMaxElev(subLayers);
    }

    @Override
    public OptionalDouble getMinElev() {
        return GeoReferencedHelper.getMinElev(subLayers);
    }

    @Override
    public Sector getSector() {
        return GeoReferencedHelper.getSector(subLayers);
    }

    @Override
    public AMapLayerMatching getMatching() {
        return matching;
    }

    @Override
    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {}

    @Override
    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {}

    @Override
    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {}

    @Override
    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {}

    @Override
    public CFlightplan getFlightplan() {
        return null;
    }

    @Override
    public void setParent(IFlightplanContainer container) {}

    @Override
    public IFlightplanContainer getParent() {
        return null;
    }

    @Override
    public void reassignIDs() {}

    @Override
    public Iterator<IFlightplanStatement> iterator() {
        return null;
    }

    @Override
    public void addToFlightplanContainer(IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        addMapLayer((IMapLayer)statement);
    }

    @Override
    public void addBeforeToFlightplanContainer(IFlightplanStatement addBeforeThisOne, IFlightplanStatement statement) throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        addMapLayer((IMapLayer)statement);
    }

    @Override
    public void addAfterToFlightplanContainer(IFlightplanStatement addAfterThisOne, IFlightplanStatement statement) {
        addMapLayer((IMapLayer)statement);
    }

    @Override
    public void addToFlightplanContainer(int index, IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        addMapLayer(index, (IMapLayer)statement);
    }

    @Override
    public boolean isAddableToFlightplanContainer(IFlightplanStatement statement) {
        return isAddableToFlightplanContainer(statement.getClass());
    }

    public void addMapLayer(IMapLayer layer) {
        if (layer instanceof MapLayerPicArea) {
            MapLayerPicArea picArea = (MapLayerPicArea)layer;
            if (picArea.getName().isEmpty()) {
                picArea.setName("#" + (sizeMapLayer() + 1));
            }
        }

        super.addMapLayer(layer);
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        return MapLayerPicArea.class.isAssignableFrom(cls);
    }

    @Override
    public IFlightplanStatement getFromFlightplanContainer(int i) {
        return (IFlightplanStatement)getMapLayer(i);
    }

    @Override
    public IFlightplanStatement removeFromFlightplanContainer(IFlightplanStatement statement) {
        removeMapLayer((IMapLayer)statement);
        return statement;
    }

    @Override
    public IFlightplanStatement removeFromFlightplanContainer(int i) {
        return (IFlightplanStatement)removeMapLayer(i);
    }

    @Override
    public int sizeOfFlightplanContainer() {
        return sizeMapLayer();
    }

    @Override
    public boolean applyFpVisitor(IFlightplanVisitor visitor, boolean skipIgnoredPaths) {
        return false;
    }

    @Override
    public boolean applyFpVisitorFlat(IFlightplanVisitor visitor, boolean skipIgnoredPaths) {
        return false;
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        // FIXME This is obviously a bug
        return null;
    }

    public double getArea() {
        double sum = 0;
        for (IMapLayer layer : subLayers) {
            if (layer instanceof MapLayerPicArea) {
                MapLayerPicArea picArea = (MapLayerPicArea)layer;
                if (picArea.isVisible()) {
                    sum += picArea.getArea();
                }
            }
        }

        return sum;
    }

    @Override
    public void mapLayerValuesChanged(IMapLayer layer) {
        super.mapLayerValuesChanged(layer);
        if (layer instanceof MapLayerPicArea) {
            mapLayerValuesChanged(this); // title changed
        }
    }

    @Override
    public void mapLayerVisibilityChanged(IMapLayer layer, boolean newVisibility) {
        super.mapLayerVisibilityChanged(layer, newVisibility);
        if (layer instanceof MapLayerPicArea) {
            mapLayerValuesChanged(this); // title changed
        }
    }

    @Override
    public int getMaxSize() {
        return IFlightplanContainer.MAX_MEMBER;
    }

    @Override
    public <T extends IFlightplanStatement> T getFirstElement(Class<T> elementClass) {
        return (T)getMapLayer(0);
    }

    @Override
    public IFlightplanStatement getLastElement() {
        return (IFlightplanStatement)getMapLayer(sizeOfFlightplanContainer());
    }

}
