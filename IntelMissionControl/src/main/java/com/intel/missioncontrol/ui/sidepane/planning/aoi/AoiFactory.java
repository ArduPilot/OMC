/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.BladesComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.FlightDirectionComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.FlightDirectionWidgetView;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.GsdComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.GsdWidgetViewModel;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.HeightComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.NoFlyComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.PowerPoleComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.RadiusComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.RadiusHeightComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.RadiusHeightHubComponent;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.RadiusHeightWidgetView;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.WidthComponent;
import de.saxsys.mvvmfx.Context;
import eu.mavinci.core.flightplan.PlanType;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;

/** Create area of interest objects. */
public class AoiFactory {

    static final BiFunction<AreaOfInterest, Context, ? extends Parent> GSD_COMPONENT =
        (areaOfInterest, context) -> {
            final GsdComponent gsdComponent = new GsdComponent(context);
            final GsdWidgetViewModel viewModel = gsdComponent.getViewModel();
            viewModel.altitudeProperty().bindBidirectional(areaOfInterest.altProperty());
            viewModel.gsdProperty().bindBidirectional(areaOfInterest.gsdProperty());
            viewModel.aoiTypeProperty().bind(areaOfInterest.typeProperty());
            return gsdComponent;
        };
    static final BiFunction<AreaOfInterest, Context, ? extends Parent> RADIUS_HEIGHT_HUB_COMPONENT =
        (areaOfInterest, context) -> {
            RadiusHeightHubComponent radiusHeightHubComponent = new RadiusHeightHubComponent(context);
            radiusHeightHubComponent
                .getViewModel()
                .radiusProperty()
                .bindBidirectional(areaOfInterest.hubDiameterProperty());
            radiusHeightHubComponent
                .getViewModel()
                .heightProperty()
                .bindBidirectional(areaOfInterest.hubLengthProperty());
            return radiusHeightHubComponent;
        };
    static final BiFunction<AreaOfInterest, Context, ? extends Parent> BLADES_COMPONENT =
        (areaOfInterest, context) -> {
            BladesComponent bladesComponent = new BladesComponent(context);
            bladesComponent
                .getViewModel()
                .bladeLengthProperty()
                .bindBidirectional(areaOfInterest.bladeLengthProperty());
            bladesComponent
                .getViewModel()
                .bladeDiameterProperty()
                .bindBidirectional(areaOfInterest.bladeDiameterProperty());
            bladesComponent
                .getViewModel()
                .bladeThinRadiusProperty()
                .bindBidirectional(areaOfInterest.bladeThinRadiusProperty());
            bladesComponent
                .getViewModel()
                .bladeCoverLengthProperty()
                .bindBidirectional(areaOfInterest.bladeCoverLengthProperty());
            bladesComponent.getViewModel().bladePitchProperty().bindBidirectional(areaOfInterest.bladePitchProperty());
            bladesComponent
                .getViewModel()
                .bladeStartRotationProperty()
                .bindBidirectional(areaOfInterest.bladeStartRotationProperty());
            bladesComponent
                .getViewModel()
                .numberOfBladesProperty()
                .bindBidirectional(areaOfInterest.numberOfBladesProperty());
            return bladesComponent;
        };
    static final BiFunction<AreaOfInterest, Context, ? extends Parent> FLIGHT_DIRECTION_COMPONENT =
        (areaOfInterest, context) -> {
            FlightDirectionComponent component = new FlightDirectionComponent(context);
            component.getViewModel().flightDirectionProperty().bindBidirectional(areaOfInterest.yawProperty());
            component.getViewModel().aoiTypeProperty().bind(areaOfInterest.typeProperty());
            component.getViewModel().aoiProperty().set(areaOfInterest);
            if (areaOfInterest.getType() == PlanType.WINDMILL) {
                FlightDirectionWidgetView flightDirectionWidgetView = component.getView();
                flightDirectionWidgetView.spinnerFlightDirectionLabel.setText(
                    flightDirectionWidgetView.languageHelper.getString(
                        "flightDirectionWidget.labelFlightDirectionWindmill"));
            }

            return component;
        };
    private static final Map<PlanType, List<BiFunction<AreaOfInterest, Context, ? extends Parent>>> VIEW_CONSTRUCTORS =
        new EnumMap<>(PlanType.class);
    private static final BiFunction<AreaOfInterest, Context, ? extends Parent> RADIUS_COMPONENT =
        (areaOfInterest, context) -> {
            RadiusComponent radiusComponent = new RadiusComponent(context);
            radiusComponent.getViewModel().radiusProperty().bindBidirectional(areaOfInterest.widthProperty());
            return radiusComponent;
        };
    private static final BiFunction<AreaOfInterest, Context, ? extends Parent> HEIGHT_COMPONENT =
        (areaOfInterest, context) -> {
            HeightComponent heightComponent = new HeightComponent(context);
            heightComponent.getViewModel().heightProperty().bindBidirectional(areaOfInterest.heightProperty());
            return heightComponent;
        };
    private static final BiFunction<AreaOfInterest, Context, ? extends Parent> RADIUS_HEIGHT_COMPONENT =
        (areaOfInterest, context) -> {
            RadiusHeightComponent radiusHeightComponent = new RadiusHeightComponent(context);
            radiusHeightComponent.getViewModel().radiusProperty().bindBidirectional(areaOfInterest.widthProperty());
            radiusHeightComponent.getViewModel().heightProperty().bindBidirectional(areaOfInterest.heightProperty());
            if (areaOfInterest.getType() == PlanType.WINDMILL) {
                RadiusHeightWidgetView radiusHeightWidgetView = radiusHeightComponent.getView();
                radiusHeightWidgetView.spinnerRadiusLabel.setText(
                    radiusHeightWidgetView.languageHelper.getString("radiusHeightWidget.labelRadiusWindmill"));
                radiusHeightWidgetView.spinnerHeightLabel.setText(
                    radiusHeightWidgetView.languageHelper.getString("radiusHeightWidget.labelHeightWindmill"));
            }

            return radiusHeightComponent;
        };
    private static final BiFunction<AreaOfInterest, Context, ? extends Parent> INSPECTION_POINTS_COMPONENT =
        (areaOfInterest, context) -> {
            PowerPoleComponent powerPoleComponent = new PowerPoleComponent(context);
            powerPoleComponent.getViewModel().areaOfInterest = areaOfInterest;
            powerPoleComponent
                .getViewModel()
                .numberOfPointsProperty()
                .bind(areaOfInterest.cornerListProperty().sizeProperty());
            powerPoleComponent
                .getViewModel()
                .numberOfActivePointsProperty()
                .bind(
                    Bindings.createIntegerBinding(
                        () -> {
                            return areaOfInterest
                                .cornerListProperty()
                                .filtered((point) -> point.triggerImageProperty().get())
                                .size();
                        },
                        areaOfInterest.cornerListProperty()));
            return powerPoleComponent;
        };
    private static final BiFunction<AreaOfInterest, Context, ? extends Parent> WIDTH_COMPONENT =
        (areaOfInterest, context) -> {
            WidthComponent widthComponent = new WidthComponent(context);
            widthComponent.getViewModel().widthProperty().bindBidirectional(areaOfInterest.widthProperty());
            return widthComponent;
        };
    private static final BiFunction<AreaOfInterest, Context, ? extends Parent> NO_FLY_COMPONENT =
        (areaOfInterest, context) -> {
            NoFlyComponent noFlyComponent = new NoFlyComponent(context);
            noFlyComponent.getViewModel().aoiProperty().set(areaOfInterest);
            return noFlyComponent;
        };

    static {
        VIEW_CONSTRUCTORS.put(PlanType.POLYGON, consistsOf(GSD_COMPONENT, FLIGHT_DIRECTION_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.POINT_OF_INTEREST, consistsOf(RADIUS_HEIGHT_COMPONENT, GSD_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.PANORAMA, consistsOf(HEIGHT_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.CITY, consistsOf(GSD_COMPONENT, FLIGHT_DIRECTION_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.SPIRAL, consistsOf(RADIUS_COMPONENT, GSD_COMPONENT, FLIGHT_DIRECTION_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.SEARCH, consistsOf(RADIUS_COMPONENT, GSD_COMPONENT, FLIGHT_DIRECTION_COMPONENT));
        VIEW_CONSTRUCTORS.put(
            PlanType.TOWER, consistsOf(RADIUS_HEIGHT_COMPONENT, GSD_COMPONENT, FLIGHT_DIRECTION_COMPONENT));
        VIEW_CONSTRUCTORS.put(
            PlanType.WINDMILL,
            consistsOf(
                RADIUS_HEIGHT_COMPONENT,
                GSD_COMPONENT,
                FLIGHT_DIRECTION_COMPONENT,
                RADIUS_HEIGHT_HUB_COMPONENT,
                BLADES_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.INSPECTION_POINTS, consistsOf(INSPECTION_POINTS_COMPONENT));

        VIEW_CONSTRUCTORS.put(
            PlanType.BUILDING, consistsOf(HEIGHT_COMPONENT, GSD_COMPONENT, FLIGHT_DIRECTION_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.FACADE, consistsOf(HEIGHT_COMPONENT, GSD_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.CORRIDOR, consistsOf(WIDTH_COMPONENT, GSD_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.MANUAL, consistsOf(HEIGHT_COMPONENT));
        // just put something in it
        VIEW_CONSTRUCTORS.put(PlanType.COPTER3D, consistsOf(GSD_COMPONENT, FLIGHT_DIRECTION_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.NO_FLY_ZONE_CIRC, consistsOf(NO_FLY_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.GEOFENCE_CIRC, consistsOf(NO_FLY_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.GEOFENCE_POLY, consistsOf(NO_FLY_COMPONENT));
        VIEW_CONSTRUCTORS.put(PlanType.NO_FLY_ZONE_POLY, consistsOf(NO_FLY_COMPONENT));
        // VIEW_CONSTRUCTORS.put(PlanType.POWERPOLE, consistsOf(NO_FLY_COMPONENT));

    }

    private static List<BiFunction<AreaOfInterest, Context, ? extends Parent>> consistsOf(
            BiFunction<AreaOfInterest, Context, ? extends Parent>... components) {
        return Arrays.asList(components);
    }

    public static List<BiFunction<AreaOfInterest, Context, ? extends Parent>> getWidgetBuilders(PlanType planType) {
        Expect.notNull(planType, "planType");
        List<BiFunction<AreaOfInterest, Context, ? extends Parent>> ctors = VIEW_CONSTRUCTORS.get(planType);
        if (ctors != null) {
            return Collections.unmodifiableList(ctors);
        } else {
            throw new IllegalStateException("ctors is null");
        }
    }

}
