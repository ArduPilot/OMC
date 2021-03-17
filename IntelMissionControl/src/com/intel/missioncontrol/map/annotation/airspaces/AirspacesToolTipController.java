/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.annotation.airspaces;

import com.intel.missioncontrol.airmap.data.AirMapAirspaceLegacyConverter;
import com.intel.missioncontrol.airmap.data.AirSpaceObject;
import com.intel.missioncontrol.airmap.layer.AirSpaceObjectFactory;
import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.airspace.IAirspace;
import eu.mavinci.desktop.gui.wwext.SurfacePolygonWithUserData;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwindx.examples.util.ToolTipController;
import java.util.Comparator;
import java.util.Objects;

// TODO: This class doesn't really meaningfully delegate to ToolTipController...
public class AirspacesToolTipController extends ToolTipController {
    private ILanguageHelper languageHelper;
    private AirspacesAnnotationBalloon balloon;

    private IAirspace lastRolloverAirspace;

    public AirspacesToolTipController(WorldWindow wwd, ILanguageHelper languageHelper) {
        super(wwd);
        this.languageHelper = languageHelper;
    }

    // TODO: We should keep explicit reference, this works because WWJ holds ref via addSelectListener...
    public static void install(WorldWindow wwd, ILanguageHelper languageHelper) {
        new AirspacesToolTipController(wwd, languageHelper);
    }

    @Override
    protected void handleRollover(SelectEvent event) {
        IAirspace airspace = getAirspaceObject2(event);
        if (airspace == null) {
            airspace = getAirspaceObject(event);
        }

        if (airspaceBaloonAlreadyRendered()) {
            if (Objects.equals(airspace, lastRolloverAirspace)) {
                return;
            }

            hideBaloon();
            resetRenderedBaloonState();
        } else {
            setRenderedBaloonState(airspace);
            showBaloon(event, airspace);
        }

        // that line causes wwd redraw in a loop
        // wwd.redraw();
    }

    private void setRenderedBaloonState(IAirspace airspace) {
        lastRolloverAirspace = airspace;
    }

    private void resetRenderedBaloonState() {
        lastRolloverAirspace = null;
    }

    private boolean airspaceBaloonAlreadyRendered() {
        return lastRolloverAirspace != null;
    }

    /** Works with new AirSpaceObjects */
    private IAirspace getAirspaceObject2(SelectEvent event) {
        return event.getObjects()
            .stream()
            .map(
                p -> {
                    Object o = p.getObject();
                    if (o instanceof AVList) {
                        AVList avList = ((AVList)o);
                        if (avList.hasKey(AirSpaceObjectFactory.KEY_AIRSPACE_OBJECT)) {
                            Object as = avList.getValue(AirSpaceObjectFactory.KEY_AIRSPACE_OBJECT);
                            if (as instanceof AirSpaceObject) {
                                return AirMapAirspaceLegacyConverter.convert((AirSpaceObject)as);
                            }
                        }
                    }

                    return null;
                })
            .filter(Objects::nonNull)
            .min(Comparator.comparingDouble(this::getBoundingBoxDiagonalDistance))
            .orElse(null);
    }

    private IAirspace getAirspaceObject(SelectEvent event) {
        return event.getObjects()
            .stream()
            .map(PickedObject::getObject)
            .filter(SurfacePolygonWithUserData.class::isInstance)
            .map(SurfacePolygonWithUserData.class::cast)
            .map(SurfacePolygonWithUserData::getUserData)
            .filter(IAirspace.class::isInstance)
            .map(IAirspace.class::cast)
            .sorted(Comparator.comparingDouble(this::getBoundingBoxDiagonalDistance))
            .findFirst()
            .orElse(null);
    }

    private double getBoundingBoxDiagonalDistance(IAirspace airspace) {
        Sector bb = airspace.getBoundingBox();
        LatLon leftBottomCorner = new LatLon(bb.getMinLatitude(), bb.getMaxLongitude());
        LatLon rightTopCorner = new LatLon(bb.getMaxLatitude(), bb.getMinLongitude());

        return LatLon.greatCircleDistance(leftBottomCorner, rightTopCorner).radians;
    }

    private void showBaloon(SelectEvent event, IAirspace airspace) {
        if (airspace != null) {
            if (layer == null) {
                layer = new AnnotationLayer();
                layer.setPickEnabled(false);
                layer.setValue(AVKey.IGNORE, true);
            }

            Position currentPosition = wwd.getCurrentPosition();
            if (currentPosition != null) {
                balloon = new AirspacesAnnotationBalloon(currentPosition, airspace, languageHelper);

                layer.removeAllAnnotations();
                layer.addAnnotation(balloon);
                addLayer(layer);
            }
        }
    }

    private void hideBaloon() {
        if (layer != null) {
            layer.removeAllAnnotations();
            removeLayer(this.layer);
            layer.dispose();
            layer = null;
        }

        if (balloon != null) {
            balloon.dispose();
            balloon = null;
        }
    }
}
