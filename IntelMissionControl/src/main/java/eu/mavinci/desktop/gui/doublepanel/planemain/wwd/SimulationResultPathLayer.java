/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.desktop.gui.wwext.IWWPickableAdvancedTooltip;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.computation.FPsim;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.awt.Color;
import java.awt.Point;

public class SimulationResultPathLayer extends AColoredTrajectoryLayer implements IRecomputeListener {

    public static final Color COLOR_TOO_LOW = new Color(0xb7, 0x00, 0x00, 0xF3);
    public static final Color COLOR_TOO_HIGH = new Color(0xb7, 0x00, 0x00, 0xF3);
    public static final Color COLOR_OK = new Color(0xDF, 0xFF, 0x00, 0x00);

    private Flightplan fp;

    static ILanguageHelper languageHelper = DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    // it's deprecated but how else ??
    static AirspacesProvidersSettings airspacesProvidersSettings =
        DependencyInjector.getInstance().getInstanceOf(AirspacesProvidersSettings.class);

    static class SimulationResultPathTooltipProvider implements IWWPickableAdvancedTooltip {

        private final Flightplan fp;
        private final IWWMapView mapView;
        private final IWWGlobes globes;

        SimulationResultPathTooltipProvider(Flightplan fp, IWWGlobes globes, IWWMapView mapView) {
            this.fp = fp;
            this.mapView = mapView;
            this.globes = globes;
        }

        @Override
        public String getTooltip(Position position, Point point) {
            FPsim.SimResultData simResult = fp.getFPsim().getSimResult();
            double minGroundDistance =
                fp.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getMinGroundDistance()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue();
            Line line = mapView.computeRayFromScreenPoint(point.x, point.y);

            double distanceBest = Double.POSITIVE_INFINITY;
            FPsim.SimDistance simPosBest = null;
            for (FPsim.SimDistance simPos : simResult.simDistances) {
                Vec4 v = globes.getDefaultGlobe().computePointFromPosition(simPos.positionOverGround);
                double distance = line.distanceTo(v);
                if (distance < distanceBest) {
                    distanceBest = distance;
                    simPosBest = simPos;
                }
            }

            if (simPosBest != null) {
                if (!simPosBest.aoiCollisions.isEmpty()) {
                    String name = null;
                    for (PicArea picArea : simPosBest.aoiCollisions) {
                        if (name == null) {
                            name = picArea.getName();
                        } else {
                            name += ", " + picArea.getName();
                        }
                    }

                    return languageHelper.getString(
                        "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.SimulationResultPathLayer.AOIcollision",
                        name);
                } else if (simPosBest.groundDistanceMeter < minGroundDistance) {
                    return languageHelper.getString(
                        "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.SimulationResultPathLayer.groundIssueTooltip",
                        StringHelper.lengthToIngName(simPosBest.groundDistanceMeter, 2, false, true),
                        StringHelper.lengthToIngName(minGroundDistance, 2, false, true));
                } else if (simPosBest.airspaceDistanceMeter < 0) {
                    return languageHelper.getString(
                        "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.SimulationResultPathLayer.airspaceIssueTooltip",
                        StringHelper.lengthToIngName(-simPosBest.airspaceDistanceMeter, 2, false, true),
                        StringHelper.lengthToIngName(simPosBest.groundDistanceMeter, 2, false, true),
                        StringHelper.lengthToIngName(
                            simPosBest.groundDistanceMeter + simPosBest.airspaceDistanceMeter, 2, false, true),
                        simPosBest.lowestAirspace.getName(),
                        simPosBest.lowestAirspace.getType());
                }
            }

            return null;
        }
    }

    private final Runnable recompRunnable =
        () -> {
            FPsim.SimResultData simResult = fp.getFPsim().getSimResult();
            if (simResult == null) {
                clear();
                notifyRedraw();
                return;
            }

            double minGroundDistance =
                fp.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getMinGroundDistance()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue();

            clear();
            for (int i = 0; i < simResult.simDistances.size(); i++) {
                FPsim.SimDistance simPos = simResult.simDistances.get(i);
                Color color;
                if (airspacesProvidersSettings.isUseAirspaceDataForPlanning()) {
                    if (simPos.groundDistanceMeter < minGroundDistance || !simPos.aoiCollisions.isEmpty()) {
                        color = COLOR_TOO_LOW;
                    } else if (simPos.airspaceDistanceMeter < 0) {
                        color = COLOR_TOO_HIGH;
                    } else {
                        color = COLOR_OK;
                    }
                } else {
                    color = COLOR_OK;
                }

                addPosition(simPos.positionOverGround, color);
            }

            notifyRedraw();
        };

    private final Recomputer pathRecomputer =
        new Recomputer(recompRunnable) {
            @Override
            protected long getDelayBeforeStart() {
                return 10;
            }

            @Override
            public String toString() {
                return "Simulation Path for " + fp.getName() + " " + super.toString();
            }
        };

    public SimulationResultPathLayer(Flightplan fp, IWWGlobes globes, IWWMapView mapView) {
        super(new SimulationResultPathTooltipProvider(fp, globes, mapView), "TrackLayerName", false, true);
        this.fp = fp;
        setPickEnabled(true);
        fp.getFPsim().addRecomputeListener(this);
        pathRecomputer.tryStartRecomp();
    }

    @Override
    protected long getCutIntervalInMs() {
        return -1;
    }

    @Override
    public void recomputeReady(Recomputer recomputer, boolean anotherRecomputeIsWaiting, long runNo) {
        pathRecomputer.tryStartRecomp();
    }

}
