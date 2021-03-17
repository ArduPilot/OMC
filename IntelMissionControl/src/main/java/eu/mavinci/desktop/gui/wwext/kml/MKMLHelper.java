/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.kml;

import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Position.PositionList;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLAbstractObject;
import gov.nasa.worldwind.ogc.kml.KMLBoundary;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.KMLData;
import gov.nasa.worldwind.ogc.kml.KMLExtendedData;
import gov.nasa.worldwind.ogc.kml.KMLLinearRing;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLPoint;
import gov.nasa.worldwind.ogc.kml.KMLPolygon;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import eu.mavinci.desktop.main.debug.Debug;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;

public class MKMLHelper {
    public static final KMLTraversalContext tc = new KMLTraversalContext();
    public static final String ns = KMLConstants.KML_2dot2_NAMESPACE;

    public static void setBoundary(KMLPlacemark place, ArrayList<Position> listP)
            throws ReflectiveOperationException, IllegalArgumentException {
        PositionList list = new PositionList(listP);

        KMLLinearRing line = new KMLLinearRing(ns);
        line.setField("coordinates", list);

        KMLBoundary bounds = new KMLBoundary(ns);
        bounds.setField("LinearRing", line);

        KMLPolygon pol = new KMLPolygon(ns);
        pol.setField("outerBoundaryIs", bounds);

        setGeometry(place, pol);
    }

    public static void setExtendedDataByName(KMLAbstractFeature kmlFeature, String name, String value) {
        KMLExtendedData extData = kmlFeature.getExtendedData();
        if (extData == null) {
            extData = new KMLExtendedData(MKMLHelper.ns);
            extData.setParent(kmlFeature);
            kmlFeature.setField("ExtendedData", extData);
        }

        for (KMLData data : extData.getData()) {
            if (data.getName().equals(name)) {
                data.setField("value", value);
                return;
            }
        }

        KMLData data = new KMLData(ns);
        data.setField("name", name);
        data.setField("value", value);
        data.setParent(extData);
        extData.getData().add(data);
    }

    public static String getExtendedDataByName(KMLAbstractFeature kmlFeature, String name) {
        KMLExtendedData extData = kmlFeature.getExtendedData();
        if (extData == null) {
            return null;
        }

        for (KMLData data : extData.getData()) {
            if (data.getName().equals(name)) {
                return data.getValue();
            }
        }

        return null;
    }

    public static boolean setCoordinatesOfPoint(KMLPoint point, Position newPosition) {
        ArrayList<Position> list = new ArrayList<Position>();
        list.add(newPosition);
        PositionList l = new PositionList(list);
        // point.setCoordinates(l);
        try {
            Method m = point.getClass().getDeclaredMethod("setCoordinates", PositionList.class);
            m.setAccessible(true);
            m.invoke(point, l);
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not set coordinates on KMLPoint", e);
            return false;
        }

        return true;
    }

    public static void initGeometry(KMLPlacemark place)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
                    InvocationTargetException {
        // the following method will do the initialisation...
        // place.initializeGeometry(tc, place.getGeometry());
        // unfortunately its protected... ARGH.. use reflection instead
        Method m =
            place.getClass()
                .getDeclaredMethod("initializeGeometry", KMLTraversalContext.class, KMLAbstractGeometry.class);
        m.setAccessible(true);
        m.invoke(place, tc, place.getGeometry());
    }

    public static void setGeometry(KMLPlacemark place, KMLAbstractGeometry geom)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
                    InvocationTargetException {

        // the following method will do the initialisation...
        // place.setGeometry(pol);;
        // unfortunately its protected... ARGH.. use reflection instead
        Method m = place.getClass().getDeclaredMethod("setGeometry", KMLAbstractGeometry.class);
        m.setAccessible(true);
        m.invoke(place, geom);
        // place.setGeometry(geom);
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Position> getBoundary(KMLPlacemark place) {
        return (ArrayList<Position>)((KMLPolygon)place.getGeometry()).getOuterBoundary().getCoordinates().list;
    }

    public static void setName(KMLAbstractFeature feature, String name) {
        feature.setField("name", name);
    }

    public static void fireChange(KMLAbstractObject feature) {
        if (feature instanceof KMLPlacemark) {
            feature.onChange(new Message(KMLAbstractObject.MSG_GEOMETRY_CHANGED, feature));
        } else {
            feature.onChange(new Message(KMLAbstractObject.MSG_BOX_CHANGED, feature));
        }
    }
}
