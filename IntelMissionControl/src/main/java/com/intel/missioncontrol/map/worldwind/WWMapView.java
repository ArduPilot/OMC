/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.common.PostConstruct;
import com.intel.missioncontrol.map.ViewMode;
import com.intel.missioncontrol.map.worldwind.impl.MView;
import com.intel.missioncontrol.map.worldwind.impl.ViewAdapter;
import com.intel.missioncontrol.map.worldwind.property.WWAsyncDoubleProperty;
import com.intel.missioncontrol.map.worldwind.property.WWAsyncObjectProperty;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.MapRotationStyle;
import eu.mavinci.desktop.gui.wwext.MViewInputHandler;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;
import java.util.OptionalDouble;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.SynchronizationRoot;

public class WWMapView extends ViewAdapter<MView> implements IWWMapView {

    private final SynchronizationRoot syncRoot;
    private final Provider<IWWGlobes> globesProvider;
    private final AsyncObjectProperty<ViewMode> viewMode;
    private final AsyncObjectProperty<MapRotationStyle> mapRotationStyle;
    private final AsyncBooleanProperty flatEarth;
    private final AsyncDoubleProperty zoom;
    private final AsyncObjectProperty<Angle> heading;
    private final AsyncObjectProperty<Position> eyePosition;

    @Inject
    public WWMapView(
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            Provider<IWWGlobes> globesProvider,
            IApplicationContext applicationContext,
            GeneralSettings generalSettings) {
        super(
            new MView(
                PropertyPath.from(applicationContext.currentMissionProperty())
                    .selectReadOnlyObject(Mission::droneProperty),
                syncRoot));
        this.syncRoot = syncRoot;
        this.globesProvider = globesProvider;

        generalSettings
            .mapRotationStyleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    ((MViewInputHandler)getAdaptedView().getViewInputHandler()).setMapRotationStyle(newValue);
                },
                syncRoot);

        ((MViewInputHandler)getAdaptedView().getViewInputHandler())
            .setMapRotationStyle(generalSettings.getMapRotationStyle());

        viewMode =
            new SimpleAsyncObjectProperty<>(
                this,
                new PropertyMetadata.Builder<ViewMode>()
                    .initialValue(ViewMode.DEFAULT)
                    .synchronizationContext(syncRoot)
                    .create()) {
                @Override
                protected void invalidated() {
                    getAdaptedView().setViewMode(get());
                }
            };

        mapRotationStyle =
            new SimpleAsyncObjectProperty<>(
                this, new PropertyMetadata.Builder<MapRotationStyle>().synchronizationContext(syncRoot).create()) {
                @Override
                protected void invalidated() {
                    ((MViewInputHandler)getAdaptedView().getViewInputHandler()).setMapRotationStyle(get());
                }
            };

        flatEarth =
            new SimpleAsyncBooleanProperty(
                this, new PropertyMetadata.Builder<Boolean>().synchronizationContext(syncRoot).create()) {
                @Override
                protected void invalidated() {
                    if (get() && viewMode.get().isPlaneCentered()) {
                        viewMode.set(ViewMode.FOLLOW);
                    }

                    getAdaptedView().setFlatEarth(get());
                }
            };

        zoom =
            new WWAsyncDoubleProperty(
                this,
                "zoom",
                syncRoot,
                MView.ZOOM,
                getAdaptedView(),
                zoom -> {
                    // on setting the zoom explicit from the UI, we can get this runing with animations still active
                    // from the last scroll wheele usage
                    getAdaptedView().stopAnimations();
                    getAdaptedView().setZoom(zoom);
                },
                getAdaptedView().getZoom());

        heading =
            new WWAsyncObjectProperty<>(
                this,
                "heading",
                syncRoot,
                MView.HEADING,
                getAdaptedView(),
                heading -> getAdaptedView().setHeading(heading));

        eyePosition =
            new WWAsyncObjectProperty<>(
                this,
                "eyePosition",
                syncRoot,
                MView.EYE_POSITION,
                getAdaptedView(),
                eyePosition -> getAdaptedView().setEyePosition(eyePosition));
    }

    @PostConstruct
    @SuppressWarnings("unused")
    public void initialize() {
        getAdaptedView().setGlobe(globesProvider.get().getActiveGlobe());
    }

    @Override
    public AsyncObjectProperty<ViewMode> viewModeProperty() {
        return viewMode;
    }

    @Override
    public AsyncDoubleProperty zoomProperty() {
        return zoom;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Angle> headingProperty() {
        return heading;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Position> eyePositionProperty() {
        return eyePosition;
    }

    @Override
    public AsyncObjectProperty<MapRotationStyle> mapRotationStyleProperty() {
        return mapRotationStyle;
    }

    @Override
    public AsyncBooleanProperty flatEarthProperty() {
        return flatEarth;
    }

    @Override
    public Future<Void> goToPositionAsync(Position center) {
        return syncRoot.runAsync(
            () -> {
                if (getViewMode() != ViewMode.DEFAULT) {
                    return;
                }

                getAdaptedView().flyToPositon(center);
            });
    }

    @Override
    public Future<Void> goToSectorAsync(Sector sector, OptionalDouble maxElev) {
        return syncRoot.runAsync(
            () -> {
                if (getViewMode() != ViewMode.DEFAULT) {
                    return;
                }

                getAdaptedView().flyToSector(sector, maxElev);
            });
    }

    @Override
    public boolean isDetectCollisions() {
        return getAdaptedView().isDetectCollisions();
    }

    @Override
    public void setDetectCollisions(boolean detectCollisions) {
        getAdaptedView().setDetectCollisions(detectCollisions);
    }

    @Override
    public boolean hadCollisions() {
        return getAdaptedView().hadCollisions();
    }

    @Override
    public Position getCenterPosition() {
        Vec4 centerPoint = super.getCenterPoint();
        return globesProvider.get().getActiveGlobe().computePositionFromPoint(centerPoint);
    }

    @Override
    public void setCenterPosition(Position center) {
        getAdaptedView().setCenterPosition(center);
    }

    @Override
    public double getZoom() {
        return getAdaptedView().getZoom();
    }

    @Override
    public void setZoom(double zoom) {
        getAdaptedView().setZoom(zoom);
    }

    @Override
    public OrbitViewLimits getOrbitViewLimits() {
        return getAdaptedView().getOrbitViewLimits();
    }

    @Override
    public void setOrbitViewLimits(OrbitViewLimits limits) {
        getAdaptedView().setOrbitViewLimits(limits);
    }

    @Override
    public boolean canFocusOnViewportCenter() {
        return getAdaptedView().canFocusOnViewportCenter();
    }

    @Override
    public void focusOnViewportCenter() {
        getAdaptedView().focusOnViewportCenter();
    }

    @Override
    public void stopMovementOnCenter() {
        getAdaptedView().stopMovementOnCenter();
    }

    @Override
    public DrawContext getDC() {
        return getAdaptedView().getDC();
    }

}
