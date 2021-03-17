/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingViewOptions;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.common.BindingUtils;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ProjectionType;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import org.asyncfx.beans.property.PropertyPath;

public class MapViewOptionsViewModel extends ViewModelBase {

    public static final double PROJECTION_DISTANCE_MAX = 100.0;
    public static final double PROJECTION_DISTANCE_MIN = 0.0;
    public static final double ELEVATION_OFFSET_MAX = 300.0;
    public static final double ELEVATION_OFFSET_MIN = -300.0;
    public static final double PROJECTION_DISTANCE_DEFAULT = 4.0;
    public static final double PROJECTION_DISTANCE_STEP = 0.1;
    public static final int PROJECTION_DISTANCE_DIGITS = 2;
    public static final double ELEVATION_OFFSET_STEP = 0.1;
    public static final int ELEVATION_OFFSET_DIGITS = 2;

    private static final Quantity<Length> MAX_PROJECTION_DISTANCE = Quantity.of(PROJECTION_DISTANCE_MAX, Unit.METER);
    private static final Quantity<Length> MIN_PROJECTION_DISTANCE = Quantity.of(PROJECTION_DISTANCE_MIN, Unit.METER);
    private static final Quantity<Length> MAX_ELEVATION_OFFSET = Quantity.of(ELEVATION_OFFSET_MAX, Unit.METER);
    private static final Quantity<Length> MIN_ELEVATION_OFFSET = Quantity.of(ELEVATION_OFFSET_MIN, Unit.METER);
    private final BooleanProperty showPreviews = new SimpleBooleanProperty();
    private final BooleanProperty showImageLocations = new SimpleBooleanProperty();
    private final BooleanProperty showAois = new SimpleBooleanProperty();
    private final BooleanProperty showRtkBaseLocation = new SimpleBooleanProperty();
    private final BooleanProperty showDatasetTrack = new SimpleBooleanProperty();
    private final BooleanProperty showCoverage = new SimpleBooleanProperty();
    private final BooleanProperty showAnnotations = new SimpleBooleanProperty();
    private final BooleanProperty rtkAvailable = new SimpleBooleanProperty();
    private final ObjectProperty<ProjectionType> imageProjection =
        new SimpleObjectProperty<>(ProjectionType.INSPECTIONS_3D);
    private final DoubleProperty projectionDistance = new SimpleDoubleProperty();
    private final DoubleProperty elevationOffset = new SimpleDoubleProperty();
    private final QuantityProperty<Length> projectionDistanceQuantity;
    private final QuantityProperty<Length> elevationOffsetQuantity;
    private final ObjectProperty<ImageChannel> imageChannel = new SimpleObjectProperty<>(ImageChannel.RGB);
    private final ListProperty<ImageChannel> availableChannels =
        new SimpleListProperty<>(FXCollections.emptyObservableList());

    private final BooleanProperty showElevationOffset = new SimpleBooleanProperty(true);

    public BooleanProperty showElevationOffsetProperty() {
        return showElevationOffset;
    }

    private final IApplicationContext applicationContext;
    private final ObjectProperty<Matching> currentMatching = new SimpleObjectProperty<>();

    @Inject
    public MapViewOptionsViewModel(ISettingsManager settingsManager, IApplicationContext applicationContext) {
        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.applicationContext = applicationContext;
        // showElevationOffset.bind(generalSettings.operationLevelProperty().isNotEqualTo(OperationLevel.USER));

        projectionDistanceQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(projectionDistanceQuantity, projectionDistance, Unit.METER);
        elevationOffsetQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(elevationOffsetQuantity, elevationOffset, Unit.METER);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        currentMatching.addListener(
            (observable, oldValue, newValue) -> {
                MatchingViewOptions oldViewOptions =
                    Optional.ofNullable(oldValue).map(Matching::getViewOptions).orElse(null);
                MatchingViewOptions currentViewOptions = newValue == null ? null : newValue.getViewOptions();

                BindingUtils.rebindBidirectional(
                    showPreviews, MatchingViewOptions::showPreviewProperty, oldViewOptions, currentViewOptions);
                BindingUtils.rebindBidirectional(
                    showImageLocations,
                    MatchingViewOptions::showImageLocationsProperty,
                    oldViewOptions,
                    currentViewOptions);
                BindingUtils.rebindBidirectional(
                    showAois, MatchingViewOptions::showAoisProperty, oldViewOptions, currentViewOptions);
                BindingUtils.rebindBidirectional(
                    showRtkBaseLocation, MatchingViewOptions::showRtkProperty, oldViewOptions, currentViewOptions);
                BindingUtils.rebindBidirectional(
                    showDatasetTrack, MatchingViewOptions::showTrackProperty, oldViewOptions, currentViewOptions);
                BindingUtils.rebindBidirectional(
                    showCoverage, MatchingViewOptions::showCoverageProperty, oldViewOptions, currentViewOptions);
                BindingUtils.rebindBidirectional(
                    showAnnotations, MatchingViewOptions::showAnnotationProperty, oldViewOptions, currentViewOptions);
                BindingUtils.rebindBidirectional(
                    imageProjection, MatchingViewOptions::imageProjectionProperty, oldViewOptions, currentViewOptions);
                BindingUtils.rebindBidirectional(
                    projectionDistance,
                    MatchingViewOptions::projectionDistanceProperty,
                    oldViewOptions,
                    currentViewOptions);
                BindingUtils.rebindBidirectional(
                    elevationOffset, MatchingViewOptions::elevationOffsetProperty, oldViewOptions, currentViewOptions);
                BindingUtils.rebindBidirectional(
                    imageChannel, MatchingViewOptions::selectedChannelProperty, oldViewOptions, currentViewOptions);
                if (oldViewOptions != null) {
                    availableChannels.unbindContentBidirectional(oldViewOptions.availableChannelsProperty());
                }

                if (currentViewOptions != null) {
                    availableChannels.bindBidirectional(currentViewOptions.availableChannelsProperty());
                }

                BindingUtils.rebind(rtkAvailable, Matching::rtkAvailableProperty, newValue);
            });

        currentMatching.bind(
            PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                .selectReadOnlyObject(Mission::currentMatchingProperty));
    }

    public BooleanProperty showPreviewsProperty() {
        return showPreviews;
    }

    public BooleanProperty showImageLocationsProperty() {
        return showImageLocations;
    }

    public BooleanProperty showAoisProperty() {
        return showAois;
    }

    public BooleanProperty showRtkBaseLocationProperty() {
        return showRtkBaseLocation;
    }

    public BooleanProperty showDatasetTrackProperty() {
        return showDatasetTrack;
    }

    public BooleanProperty showCoverageProperty() {
        return showCoverage;
    }

    public BooleanProperty showAnnotationsProperty() {
        return showAnnotations;
    }

    public ObjectProperty<ProjectionType> imageProjectionProperty() {
        return imageProjection;
    }

    public DoubleProperty projectionDistanceProperty() {
        return projectionDistance;
    }

    public QuantityProperty<Length> projectionDistanceQuantityProperty() {
        return projectionDistanceQuantity;
    }

    public ObjectProperty<ImageChannel> imageChannelProperty() {
        return imageChannel;
    }

    public ListProperty<ImageChannel> availableChannelsProperty() {
        return availableChannels;
    }

    public ReadOnlyBooleanProperty rtkAvailableProperty() {
        return rtkAvailable;
    }

    public QuantityProperty<Length> elevationOffsetQuantityProperty() {
        return elevationOffsetQuantity;
    }
}
