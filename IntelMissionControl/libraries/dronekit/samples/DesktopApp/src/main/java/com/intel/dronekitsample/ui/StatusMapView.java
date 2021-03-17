package com.intel.dronekitsample.ui;

import com.intel.dronekitsample.model.DroneModel;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import javafx.beans.property.ReadOnlyObjectProperty;

import javax.swing.*;

import static gov.nasa.worldwind.WorldWind.RELATIVE_TO_GROUND;

public class StatusMapView {
    public WorldWindow wwd;
    RenderableLayer layer;
    Box box;

    private DroneModel model;

    public void init(WorldWindow wwd, DroneModel model) {
        this.wwd = wwd;
        this.model = model;
        createLayer();

        setupModelBinding();
    }

    private void setupModelBinding() {
        model.getAttributes().gpsProperty().addListener((i) -> {
            Gps gps = model.getAttributes().gpsProperty().get();
            Altitude alt = model.getAttributes().altitudeProperty().get();

            if (gps == null || alt == null) return;;
            final LatLong p = new LatLong(gps.getPosition());
            final double altitude = alt.getAltitude();
            SwingUtilities.invokeLater(() -> {
                box.setCenterPosition(Position.fromDegrees(p.getLatitude(), p.getLongitude(), altitude));
            });

        });
    }

    void createLayer() {
        layer = new RenderableLayer();
        box = createShape();
        layer.addRenderable(box);
    }

    Box createShape() {
        Position position = new Position(Angle.fromDegrees(-35.363261) ,
                Angle.fromDegrees(149.165230), 100);
        Box box = new Box(position, 100, 100, 100);
        BasicShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setOutlineMaterial(Material.BLACK);
        attributes.setInteriorMaterial(Material.GREEN);
        attributes.setDrawOutline(true);
        box.setAttributes(attributes);
        box.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        return box;
    }

}
