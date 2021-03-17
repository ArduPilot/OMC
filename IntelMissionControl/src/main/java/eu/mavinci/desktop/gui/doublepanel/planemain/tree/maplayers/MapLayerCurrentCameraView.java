/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.networking.INetworkInformation;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeCurrentView;
import eu.mavinci.geo.ISectorReferenced;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import java.util.OptionalDouble;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

public class MapLayerCurrentCameraView extends MapLayer
        implements ISectorReferenced, IMapLayerWW, INotificationObject.ChangeListener {

    AerialPinholeCurrentView image;
    private final ChangeListener<Boolean> networkBecomesAvailableListener =
        new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    image.recomputeCornersSync();
                }
            }
        };

    public MapLayerCurrentCameraView(IAirplane plane) {
        super(true);
        image = new AerialPinholeCurrentView(plane);
        plane.getHardwareConfiguration().addListener(new INotificationObject.WeakChangeListener(this));

        StaticInjector.getInstance(INetworkInformation.class)
            .networkAvailableProperty()
            .addListener(new WeakChangeListener<>(networkBecomesAvailableListener));
    }

    @Override
    public Layer getWWLayer() {
        return image.getWWLayer();
    }

    @Override
    public Sector getSector() {
        return image.getSector();
    }

    @Override
    public OptionalDouble getMaxElev() {
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble getMinElev() {
        return OptionalDouble.empty();
    }

    @Override
    public void propertyChange(INotificationObject.ChangeEvent propertyChangeEvent) {
        // TODO: PERF - This seems to be a very heavy-weight event handler, it probably should work asynchronously.
        image.recomputeCornersSync();
    }
}
