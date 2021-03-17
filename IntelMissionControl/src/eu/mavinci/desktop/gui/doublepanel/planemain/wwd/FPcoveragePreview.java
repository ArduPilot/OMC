/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.visitors.ExtractTypeVisitor;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerCoverage;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PhotoLogLine;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.computation.FPsim;
import eu.mavinci.flightplan.computation.FPsim.SimDistance;
import eu.mavinci.flightplan.computation.FPsim.SimResultData;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import java.util.ArrayList;
import java.util.Vector;

public class FPcoveragePreview extends AMapLayerCoverage implements ComputeCornerData.IAerialPinholeImageContext {

    Flightplan fp;

    FPsim fpSim;

    IRecomputeListener recompListenerFPsim =
        new IRecomputeListener() {

            @Override
            public synchronized void recomputeReady(
                    Recomputer recomputer, boolean anotherRecomputeIsWaiting, long runNo) {
                // System.out.println("recomputeCoverage no " + runNo+ " ready from FPsim: " + fp);
                ArrayList<ComputeCornerData> cornersList = new ArrayList<>();
                SimResultData simResult = fpSim.getSimResult();
                ArrayList<PhotoData> photos = simResult.photos;
                // System.out.println("photos: "+photos.size());
                IPlatformDescription platformDesc = fp.getHardwareConfiguration().getPlatformDescription();
                for (PhotoData photo : photos) {
                    // System.out.println("newPhoto:"+photo + "@ curthread:" + Thread.currentThread());
                    // if (Math.abs( photo.roll) >2 ||Math.abs( photo.pitch) >2) continue;
                    if (Math.abs(photo.camera_roll) > 3 && !platformDesc.isInCopterMode()) {
                        continue;
                    }

                    ComputeCornerData computeCornerData =
                        ComputeCornerData.computeCorners(
                            FPcoveragePreview.this,
                            new PhotoLogLine(photo, FPcoveragePreview.this.getHardwareConfiguration()));
                    if (computeCornerData == null) {
                        continue;
                    }

                    computeCornerData.updateLineNumber(photo.reentrypoint);
                    ArrayList<LatLon> p = computeCornerData.getGroundProjectedCorners();
                    // System.out.println("isMatchAble:"+img.isMatchable()+" "+p);
                    if (!computeCornerData.isElevationDataReady()) {
                        elevationDataAvaliableTmp = false;
                    }

                    if (p == null) {
                        continue;
                    }

                    cornersList.add(computeCornerData);
                }
                // System.out.println("recomputeCoverage no "+ runNo+ " ready from FPsim2: " + fp + " rcomp "+
                // recomputer.hashCode() + " " +
                // recomputer);
                // System.out.println("readySim corners:"+(cornersList==null?"null":(cornersList.size() + " " +
                // cornersList.hashCode() ) ));
                FPcoveragePreview.this.cornersList = cornersList;
                FPcoveragePreview.this.simDistances = simResult.simDistances;
                recomputeCoverage();
            }
        };

    private final INotificationObject.ChangeListener hwConfigChange = event -> recomputeCoverage();

    public FPcoveragePreview(Flightplan fp) {
        super(true);
        fpSim = fp.getFPsim();
        fpSim.addRecomputeListener(recompListenerFPsim);
        this.fp = fp;

        // here we can skip first triggering update of corners, since they are anyway recreated synchroniously
        fp.getHardwareConfiguration().addListener(new INotificationObject.WeakChangeListener(hwConfigChange));
        recomputeCoverage();
    }

    private ArrayList<CornerMask> maskCorners;
    private boolean containsNonCoverageAbleAOIs;
    private ArrayList<ComputeCornerData> cornersList;
    private ArrayList<SimDistance> simDistances;

    @Override
    protected void updateCameraCornersBlocking() {
        // ignore, AerialPinholeImage are anyway recreated synchroniously
    }

    @Override
    public ArrayList<ComputeCornerData> computeCorners() {
        return cornersList;
    }

    @Override
    public ArrayList<CornerMask> computeMaskCorners() {
        maskCorners = new ArrayList<>();
        containsNonCoverageAbleAOIs = false;
        computeMaskCornersSingleFP(fp);
        return maskCorners;
    }

    @Override
    public boolean containsNonCoverageAbleAOIs() {
        return containsNonCoverageAbleAOIs;
    }

    protected void computeMaskCornersSingleFP(Flightplan fp) {
        ExtractTypeVisitor<PicArea> vis = new ExtractTypeVisitor<>(PicArea.class);
        vis.startVisit(fp);
        for (PicArea c : vis.filterResults) {
            if (!c.getPlanType().supportsCoverageComputation()) {
                containsNonCoverageAbleAOIs = true;
                continue;
            }

            CornerMask mask = new CornerMask();
            mask.corners = new Vector<LatLon>(c.getHull());
            mask.gsd = c.getGsd();
            maskCorners.add(mask);
        }
    }

    @Override
    public boolean shouldShowCoverage() {
        return shouldShowCoverageSingleFP(fp);
    }

    private boolean shouldShowCoverageSingleFP(Flightplan childFlightplan) {
        ExtractTypeVisitor<PicArea> vis = new ExtractTypeVisitor<>(PicArea.class);
        vis.startVisit(fp);
        for (PicArea c : vis.filterResults) {
            if (c.getPlanType().supportsCoverageComputation()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public SectorType getSectorType() {
        return SectorType.auto_redInside;
    }

    public static String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FPcoveragePreview";

    @Override
    public String getTooltipAddon() {
        return DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class).getString(KEY + ".tooltipAddon");
    }

    @Override
    public String toString() {
        return "FP Coverage of FP: " + fp + " " + super.toString();
    }

    public Double getStartingElevationOverWgs84() {
        return fpSim.getStartElevOverWGS84();
    }

    public double getProjectionDistance() {
        return 5; // currently this value is not in use... so it doesnt matter
    }

    public IHardwareConfiguration getHardwareConfiguration() {
        return fp.getHardwareConfiguration();
    }

    public LatLon getStartingPosition() {
        return null;
    }

    public Vec4 getRtkOffset() {
        return null;
    }

    @Override
    public double getStartingElevationOverWgs84WithOffset() {
        return getStartingElevationOverWgs84();
    }

    @Override
    public double getElevationOffset() {
        return 0;
    }
}
