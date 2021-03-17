package com.intel.dronekitsample.ui;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.DoJump;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwindx.examples.util.DirectedPath;
import javafx.geometry.Pos;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static gov.nasa.worldwindx.examples.ApplicationTemplate.insertBeforeCompass;

public class MissionMapView {
    public WorldWindow wwd;

    RenderableLayer layer;
    private MarkerLayer markerLayer;

    public void init(WorldWindow wwd) {
        this.wwd = wwd;
        createLayer();
    }

    void createLayer() {
        layer = new RenderableLayer();
        markerLayer = new MarkerLayer();

        insertBeforeCompass(wwd, markerLayer);
        insertBeforeCompass(wwd, layer);
    }

    public static Position fromLatLongAlt(LatLongAlt lla) {
        return Position.fromDegrees(lla.getLatitude(), lla.getLongitude(), lla.getAltitude());
    }

    public static double computeZoomForExtent(Sector sector) {
        Angle delta = sector.getDeltaLat();
        if (sector.getDeltaLon().compareTo(delta) > 0)
            delta = sector.getDeltaLon();
        double arcLength = delta.radians * Earth.WGS84_EQUATORIAL_RADIUS;
        double fieldOfView = Configuration.getDoubleValue(AVKey.FOV, 45.0);
        return arcLength / (2 * Math.tan(fieldOfView / 2.0));
    }

    private BasicMarkerAttributes markerAttr;
    private BasicMarkerAttributes markerAttr2;

    void initAttr() {
        markerAttr = new BasicMarkerAttributes(Material.CYAN, BasicMarkerShape.SPHERE, 1d, 10, 5);
        markerAttr2 = new BasicMarkerAttributes(Material.MAGENTA, BasicMarkerShape.SPHERE, 1d, 10, 2);

    }
    Timer t;

    public void updateMission(Mission mission) {
        doUpdateMission(mission);

        // wait for terrain to load...
        t = new Timer(2000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> doUpdateMission(mission));
                t.stop();
            }
        });
        t.start();
    }

    private void doUpdateMission(Mission mission) {
        initAttr();

        List<MissionItem> missionItems = mission.getMissionItems();

        ArrayList<Position> pathPositions = new ArrayList<>(missionItems.size());
        ArrayList<Marker> markers = new ArrayList<>();

        Position last = Position.ZERO;
        Position start = Position.ZERO;

        double startElev = 0.0;

        for (MissionItem missionItem : missionItems) {
            Position position = last;
            if (missionItem instanceof MissionItem.SpatialItem) {
                LatLongAlt c = ((MissionItem.SpatialItem) missionItem).getCoordinate();

                if (start == Position.ZERO) {
                    startElev = wwd.getModel().getGlobe().getElevation(
                            Angle.fromDegrees(c.getLatitude()), Angle.fromDegrees(c.getLongitude()));
                    position = Position.fromDegrees(c.getLatitude(), c.getLongitude(), startElev + c.getAltitude());
                    start = position;
                } else {
                    position = Position.fromDegrees(c.getLatitude(), c.getLongitude(), startElev + c.getAltitude());
                }


                pathPositions.add(position);

                BasicMarker marker = new BasicMarker(position, markerAttr2);
                marker.setHeading(Angle.fromDegrees(0));
                marker.setPitch(Angle.fromDegrees(90));
                markers.add(marker);

                last = position;
            } else if (missionItem instanceof DoJump) {
                BasicMarkerAttributes attr = new BasicMarkerAttributes(markerAttr2);
                attr.setMaterial(Material.GREEN);
                attr.setMarkerPixels(15);
                attr.setShapeType(BasicMarkerShape.CONE);
                BasicMarker marker = new BasicMarker(last, attr);
                marker.setHeading(Angle.fromDegrees(0));
                marker.setPitch(Angle.fromDegrees(90));
                markers.add(marker);

                DoJump jump = (DoJump) missionItem;
                LatLongAlt c = ((MissionItem.SpatialItem) missionItems.get(jump.getWaypoint())).getCoordinate();
                position = Position.fromDegrees(c.getLatitude(), c.getLongitude(), startElev + c.getAltitude());
                pathPositions.add(position);

                BasicMarker marker2 = new BasicMarker(position, attr);
                marker2.setHeading(Angle.fromDegrees(0));
                marker2.setPitch(Angle.fromDegrees(90));
                markers.add(marker2);

            } else {


                BasicMarker marker = new BasicMarker(last, markerAttr);
                marker.setHeading(Angle.fromDegrees(0));
                marker.setPitch(Angle.fromDegrees(90));
                markers.add(marker);
            }
        }

        layer.removeAllRenderables();

        Sector boundingSector;
        // create path
        {
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(2d);

            Path path = new DirectedPath(pathPositions);

            // To ensure that the arrowheads resize smoothly, refresh each time the path is drawn.
            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(WorldWind.ABSOLUTE);
            path.setFollowTerrain(false);
            path.setPathType(AVKey.GREAT_CIRCLE);
            layer.addRenderable(path);

            boundingSector = Sector.boundingSector(path.getPositions());
        }



        // add markers
        {
            markerLayer.setMarkers(markers);
        }

        double v = computeZoomForExtent(boundingSector);

        BasicOrbitView view = (BasicOrbitView) wwd.getView();
        view.stopMovement();
        Position center = new Position(boundingSector.getCentroid(), startElev);
//        view.setCenterPosition();
//        view.setZoom(v  + 1.0);
        view.addPanToAnimator(center, Angle.ZERO, Angle.ZERO, v, 1500, true);
    }

    public void setActive() {

    }
}
