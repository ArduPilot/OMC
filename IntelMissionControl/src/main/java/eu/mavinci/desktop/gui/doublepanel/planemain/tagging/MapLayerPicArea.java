/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.flightplan.AFlightplanContainer;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.IMuteable;
import eu.mavinci.core.flightplan.visitors.IFlightplanVisitor;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.computation.AutoFPhelper;
import eu.mavinci.flightplan.computation.LocalTransformationProvider;
import eu.mavinci.flightplan.visitors.SectorVisitor;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Vector;
import java.util.logging.Level;
import org.apache.commons.lang3.NotImplementedException;

public class MapLayerPicArea extends MapLayer
        implements ISectorReferenced, IMatchingRelated, IFlightplanContainer, IFlightplanStatement, IMuteable {

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicAreas";

    public static final String KEY_GSD = KEY + ".gsd";

    MapLayerMatching matching;

    MapLayerPicAreaFpContainer container;

    String name = "";

    protected double gsd = CPicArea.DEF_GSD; // in meter

    public double getGSD() {
        return gsd;
    }

    public void setGSD(double gsd) {
        if (gsd > CPicArea.MAX_GSD) {
            gsd = CPicArea.MAX_GSD;
        } else if (gsd < CPicArea.MIN_GSD) {
            gsd = CPicArea.MIN_GSD;
        }

        if (this.gsd == gsd) {
            return;
        }

        this.gsd = gsd;
        mapLayerValuesChanged(this);
    }

    Vector<LatLon> corners = null;

    public synchronized Vector<LatLon> getCorners() {
        if (corners == null) {
            corners = new Vector<LatLon>();
            for (IFlightplanStatement fpStatement : this) {
                if (fpStatement instanceof Point) {
                    Point p = (Point)fpStatement;
                    corners.add(p.getLatLon());
                }
            }
        }

        return corners;
    }

    public class MapLayerPicAreaFpContainer extends AFlightplanContainer implements IFlightplanStatement, IMuteable {

        public MapLayerPicArea getMapLayer() {
            return MapLayerPicArea.this;
        }

        @Override
        public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
            if (sizeOfFlightplanContainer() >= getMaxSize()) {
                return false;
            }

            return Point.class.equals(cls);
        }

        @Override
        public void reassignIDs() {}

        @Override
        public void setParent(IFlightplanContainer container) {}

        @Override
        public IFlightplanContainer getParent() {
            return matching.getPicAreasLayer();
        }

        @Override
        public CFlightplan getFlightplan() {
            return null;
        }

        @Override
        public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
            mapLayerStructureChanged(MapLayerPicArea.this);
        }

        @Override
        public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
            mapLayerValuesChanged(MapLayerPicArea.this);
        }

        @Override
        public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
            mapLayerStructureChanged(MapLayerPicArea.this);
        }

        @Override
        public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
            mapLayerStructureChanged(MapLayerPicArea.this);
        }

        @Override
        public IFlightplanRelatedObject getCopy() {
            throw new NotImplementedException("Cant copy dummy container for Dataset area filers");
        }

        @Override
        protected void updateParent(IFlightplanStatement statement) {
            statement.setParent(MapLayerPicArea.this);
        }

        @Override
        public void setMute(boolean mute) {
            MapLayerPicArea.this.setMute(mute);
        }

        @Override
        public void setSilentUnmute() {
            MapLayerPicArea.this.setSilentUnmute();
        }

        @Override
        public boolean isMute() {
            return MapLayerPicArea.this.isMute();
        }
    }

    public synchronized void resetCaches() {
        corners = null;
        sector = null;
    }

    public MapLayerPicArea(final MapLayerMatching matching) {
        super(true);
        this.matching = matching;
        container = new MapLayerPicAreaFpContainer();
    }

    @Override
    public void mapLayerValuesChanged(IMapLayer layer) {
        resetCaches();
        if (mute) {
            return;
        }

        super.mapLayerValuesChanged(layer);
        getMatching().setChanged(true);
    }

    @Override
    public void mapLayerStructureChanged(IMapLayer layer) {
        resetCaches();
        if (mute) {
            return;
        }

        super.mapLayerStructureChanged(layer);
        getMatching().setChanged(true);
    }

    @Override
    public OptionalDouble getMaxElev() {
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble getMinElev() {
        return OptionalDouble.empty();
    }

    Sector sector = null;

    @Override
    public synchronized Sector getSector() {
        if (sector == null) {
            SectorVisitor vis = new SectorVisitor(this);
            sector = vis.getSector();
        }

        return sector;
    }

    @Override
    public AMapLayerMatching getMatching() {
        return matching;
    }

    @Override
    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        container.flightplanStatementChanged(statement);
    }

    @Override
    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
        container.flightplanStatementStructureChanged(statement);
    }

    @Override
    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        container.flightplanStatementAdded(statement);
    }

    @Override
    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        container.flightplanStatementRemove(i, statement);
    }

    @Override
    public CFlightplan getFlightplan() {
        return container.getFlightplan();
    }

    @Override
    public void setParent(IFlightplanContainer container) {
        if (container instanceof IMatchingRelated) {
            IMatchingRelated rel = (IMatchingRelated)container;
            matching = (MapLayerMatching)rel.getMatching();
        }

        container.setParent(container);
    }

    @Override
    public IFlightplanContainer getParent() {
        return container.getParent();
    }

    @Override
    public void reassignIDs() {
        container.reassignIDs();
    }

    @Override
    public Iterator<IFlightplanStatement> iterator() {
        return container.iterator();
    }

    @Override
    public void addToFlightplanContainer(IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        container.addToFlightplanContainer(statement);
    }

    @Override
    public void addBeforeToFlightplanContainer(IFlightplanStatement addBeforeThisOne, IFlightplanStatement statement) throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        container.addBeforeToFlightplanContainer(addBeforeThisOne, statement);
    }

    @Override
    public void addAfterToFlightplanContainer(IFlightplanStatement addAfterThisOne, IFlightplanStatement statement)
            throws FlightplanContainerWrongAddingException, FlightplanContainerFullException {
        container.addAfterToFlightplanContainer(addAfterThisOne, statement);
    }

    @Override
    public void addToFlightplanContainer(int index, IFlightplanStatement statement)
            throws FlightplanContainerFullException, FlightplanContainerWrongAddingException {
        container.addToFlightplanContainer(index, statement);
    }

    @Override
    public boolean isAddableToFlightplanContainer(IFlightplanStatement statement) {
        return container.isAddableToFlightplanContainer(statement);
    }

    @Override
    public IFlightplanStatement getFromFlightplanContainer(int i) {
        return container.getFromFlightplanContainer(i);
    }

    @Override
    public IFlightplanStatement removeFromFlightplanContainer(IFlightplanStatement statement) {
        return container.removeFromFlightplanContainer(statement);
    }

    @Override
    public IFlightplanStatement removeFromFlightplanContainer(int i) {
        return container.removeFromFlightplanContainer(i);
    }

    @Override
    public <T extends IFlightplanStatement> T getFirstElement(Class<T> elementClass) {
        return container.getFirstElement(elementClass);
    }

    @Override
    public IFlightplanStatement getLastElement() {
        return container.getLastElement();
    }

    @Override
    public int sizeOfFlightplanContainer() {
        return container.sizeOfFlightplanContainer();
    }

    @Override
    public boolean applyFpVisitor(IFlightplanVisitor visitor, boolean skipIgnoredPaths) {
        return container.applyFpVisitor(visitor, skipIgnoredPaths);
    }

    @Override
    public boolean applyFpVisitorFlat(IFlightplanVisitor visitor, boolean skipIgnoredPaths) {
        return container.applyFpVisitorFlat(visitor, skipIgnoredPaths);
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        // FIXME This is obviously a bug
        return null;
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        return container.isAddableToFlightplanContainer(cls);
    }

    /**
     * compute size in m² of this polygone
     *
     * @return
     */
    public double getArea() {
        if (corners.size() < 3) {
            return 0;
        }

        LocalTransformationProvider trafo =
            new LocalTransformationProvider(new Position(corners.get(0), 0), Angle.ZERO, 0, 0, false);
        Vector<Vec4> cornersVec = new Vector<Vec4>(corners.size());
        for (LatLon latLon : corners) {
            cornersVec.add(trafo.transformToLocal(latLon));
        }

        return AutoFPhelper.computeArea(cornersVec);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof MapLayerPicArea) {
            MapLayerPicArea other = (MapLayerPicArea)obj;
            if (gsd != other.gsd) {
                return false;
            }

            if (!name.equals(other.name)) {
                return false;
            }

            if (sizeOfFlightplanContainer() != other.sizeOfFlightplanContainer()) {
                return false;
            }

            for (int i = 0; i != sizeOfFlightplanContainer(); i++) {
                if (!getFromFlightplanContainer(i).equals(other.getFromFlightplanContainer(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public MapLayerPicArea clone(MapLayerMatching other) {
        MapLayerPicArea clone = new MapLayerPicArea(other);
        clone.gsd = gsd;
        for (IFlightplanStatement fpStatement : this) {
            if (fpStatement instanceof Point) {
                Point p = (Point)fpStatement;
                try {
                    clone.addToFlightplanContainer(new Point(p));
                } catch (Exception e) {
                    Debug.getLog().log(Level.SEVERE, "could not clone PicAreaLayer", e);
                }
            }
        }

        return clone;
    }

    /**
     * checks if a given polygon has overlap whith this one
     *
     * <p>wont work close to the dateline or at poles
     *
     * @param other
     * @param sector
     * @return
     */
    public boolean intersectsWith(ArrayList<LatLon> other, Sector sector) {
        // System.out.println("sector:" + sector + "corners: "+ other);
        if (sector == null || other == null) {
            return false;
        }

        Sector memberSector = getSector();
        if (memberSector == null) {
            return false;
        }

        Ensure.notNull(memberSector, "memberSector"); // memberSector should not be null, but just checking for klocwork
        if (!memberSector.intersects(sector)) {
            return false;
        }
        // System.out.println("intersect sector!");
        // check if one of the corners is inside this
        for (LatLon corner : other) {
            if (intersectsWith(corner)) {
                return true;
            }
        }
        // or the oposite way around
        for (LatLon point : getCorners()) {
            if (intersectsWith(null, other, point)) {
                return true;
            }
        }

        // System.out.println("not intersecting corners");

        // check for each of the edges if they cross one of this edges
        int max = other.size();
        for (int i = 0; i != max; ++i) {
            if (intersectsOneBorderWith(other.get(i), other.get((i + 1) % max))) {
                return true;
            }
        }
        // System.out.println("not intersecting borders");
        return false;
    }

    /**
     * check if a point is inside this polygon
     *
     * <p>wont work close to the dateline or at poles
     */
    public boolean intersectsWith(LatLon point) {
        Sector sector = getSector();
        Ensure.notNull(sector, "sector");
        return sector.contains(point) && intersectsWith(null, getCorners(), point);
    }

    public static boolean intersectsWith(Sector sector, List<LatLon> corners, LatLon point) {
        if (point == null) {
            return false;
        }

        if (sector != null && !sector.contains(point)) {
            return false;
        }
        // count the numbers of edges of this left of this point, if its odd, the point was inside the polygon
        int noLeftEdges = 0;
        final double x = point.longitude.degrees;
        final double y = point.latitude.degrees;

        final int max = corners.size();
        for (int i = 0; i != max; ++i) {
            final int k = (i + 1) % max;
            final LatLon a = corners.get(i);
            final LatLon b = corners.get(k);

            if ((a.latitude.degrees >= y && b.latitude.degrees <= y)
                    || (b.latitude.degrees >= y && a.latitude.degrees <= y)) { // will
                // cross ray
                // to west
                // OR east
                double tmpx =
                    b.longitude.degrees
                        + (a.longitude.degrees - b.longitude.degrees)
                            * (y - b.latitude.degrees)
                            / (a.latitude.degrees - b.latitude.degrees);
                if (tmpx <= x) {
                    noLeftEdges++;
                }
            }
        }

        return noLeftEdges % 2 == 1;
    }

    public static LatLon meanPoint(List<LatLon> corners) {
        if (corners == null || corners.isEmpty()) {
            return null;
        }

        double lat = 0;
        double lon = 0;
        for (LatLon corner : corners) {
            lat += corner.latitude.degrees;
            lon += corner.longitude.degrees;
        }

        return LatLon.fromDegrees(lat / corners.size(), lon / corners.size());
    }

    /**
     * Checks if a given line intersects with at leas one or two border-line segment of this polygon. in detail: only
     * n-1 borders of this are checked for crossing. if the first one is found, true is returned
     *
     * <p>wont work close to the dateline or at poles
     */
    public boolean intersectsOneBorderWith(LatLon lineStart, LatLon lineEnd) {
        final Vector<LatLon> corners = getCorners();

        final double bx = lineStart.longitude.degrees;
        final double by = lineStart.latitude.degrees;
        final double dbx = lineEnd.longitude.degrees - bx;
        final double dby = lineEnd.latitude.degrees - by;

        final int max = corners.size() - 1;
        for (int i = 0; i != max; ++i) {
            final int k = i + 1;
            final LatLon s = corners.get(i);
            final LatLon e = corners.get(k);

            final double ax = s.longitude.degrees;
            final double ay = s.latitude.degrees;
            final double dax = e.longitude.degrees - ax;
            final double day = e.latitude.degrees - ay;

            // http://de.wikipedia.org/wiki/Regul%C3%A4re_Matrix#Formel_f.C3.BCr_2x2-Matrizen
            double det = dax * dby - dbx * day;
            if (det == 0) {
                continue;
            }

            det = 1 / det;
            final double aMbx = ax - bx;
            final double aMby = ay - by;

            final double alpha = -det * (dby * aMbx - dbx * aMby);
            if (alpha < 0 || alpha > 1) {
                continue;
            }

            final double beta = det * (-day * aMbx + dax * aMby);
            if (beta < 0 || beta > 1) {
                continue;
            }
            // System.out.println("startLat" + lineStart + " lineEnd "+lineEnd);
            // System.out.println("picAreaLine start" + s+ " end "+e);
            // System.out.println("ax="+ax+" ay="+ay+ " dax="+dax+" day="+day);
            // System.out.println("bx="+bx+" by="+by+ " dbx="+dbx+" dby="+dby);
            // System.out.println("aMbx=" + aMbx + " aMby="+aMby);
            // System.out.println("det=" +(1/det));
            // System.out.println("corner i=" + i + " alpha="+alpha + " beta="+beta);
            return true;
        }

        return false;
    }

    @Override
    public int getMaxSize() {
        return IFlightplanContainer.MAX_MEMBER;
    }

    public LatLon getCenter() {
        return meanPoint(getCorners());
    }

    public void setMute(boolean mute) {
        if (mute == this.mute) {
            return;
        }

        this.mute = mute;
        if (!mute) {
            mapLayerValuesChanged(MapLayerPicArea.this);
        }
    }

    public boolean isMute() {
        return mute;
    }

    public void setSilentUnmute() {
        this.mute = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }

        if (name.equals(this.name)) {
            return;
        }

        this.name = name;
        mapLayerValuesChanged(this);
    }
}
