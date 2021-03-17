/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.VisibilityTracker;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.WWElevationModel;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerWW;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.ComputeCornerData;
import eu.mavinci.desktop.gui.wwext.IWWPickableAdvancedTooltip;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import eu.mavinci.desktop.gui.wwext.MAnalyticSurface;
import eu.mavinci.desktop.gui.wwext.MAnalyticSurface.GridPointAttributes;
import eu.mavinci.desktop.helper.ColorHelper;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.desktop.helper.IRecomputeRunnable;
import eu.mavinci.desktop.helper.IRecomputerListenerManager;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwindx.examples.analytics.AnalyticSurfaceAttributes;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Vector;
import java.util.logging.Level;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.SynchronizationRoot;

public abstract class AMapLayerCoverage extends MapLayer
        implements ISectorReferenced, IMapLayerWW, IWWPickableAdvancedTooltip, IRecomputerListenerManager, IKeepAll {

    private static final IElevationModel elevationModel =
        DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);

    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);
    private static final SynchronizationRoot syncRoot =
        DependencyInjector.getInstance().getInstanceOf(SynchronizationRoot.class);
    private static final double THRESHOLD = 5;
    private static final double PADDING = 0.2;
    public static final int MIN_RELATION = 10;

    private final ChangeListener<Boolean> networkBecomesAvailableChangeListener =
        new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (elevationDataAvaliable) {
                    return;
                }

                if (newValue) {
                    AMapLayerCoverage.this.updateCameraCorners();
                }
            }
        };
    private final ChangeListener<OperationLevel> operationLevelChangeListener;
    private final InvalidationListener updateCameraCornersListener;

    private class AnalyticSurfaceTooltipable extends MAnalyticSurface implements IWWRenderableWithUserData {

        @Override
        protected AnalyticSurfaceObject createClampToGroundSurface() {
            AnalyticSurfaceObject o = super.createClampToGroundSurface();
            o.setEnableBatchPicking(false);
            return o;
        }

        @Override
        protected AnalyticSurfaceObject createShadowSurface() {
            AnalyticSurfaceObject o = super.createShadowSurface();
            o.setEnableBatchPicking(false);
            return o;
        }

        @Override
        public Object getUserData() {
            return AMapLayerCoverage.this;
        }

        @Override
        public boolean isDraggable() {
            return false;
        }

        @Override
        public boolean isPopupTriggering() {
            return false;
        }

        @Override
        public boolean isSelectable() {
            return true;
        }

        @Override
        public void setDraggable(boolean isDraggable) {}

        @Override
        public void setPopupTriggering(boolean isPopupTriggering) {}

        @Override
        public void setSelectable(boolean isSelectable) {}

        @Override
        public void setUserData(Object o) {}

        boolean hasTooltip = true;

        @Override
        public boolean hasTooltip() {
            return hasTooltip;
        }

        @Override
        public void setHasTooltip(boolean hasTooltip) {
            this.hasTooltip = hasTooltip;
        }

        @Override
        public synchronized void preRender(DrawContext dc) {
            try {
                super.preRender(dc);
            } catch (RuntimeException | Error e) {
                Debug.getLog()
                    .log(
                        Level.SEVERE,
                        "Problems PreRender MAnalyticSurface of: "
                            + AMapLayerCoverage.this
                            + " m_height:"
                            + m_height
                            + " m_width:"
                            + m_width
                            + " m_sector:"
                            + m_sector,
                        e);
                throw e;
            }
        }

        boolean isSelectableWhileAddNewPoints = false;

        @Override
        public boolean isSelectableWhileAddNewPoints() {
            return isSelectableWhileAddNewPoints;
        }

        @Override
        public void setSelectableWhileAddNewPoints(boolean isSelectableWhileAddNewPoints) {
            this.isSelectableWhileAddNewPoints = isSelectableWhileAddNewPoints;
        }

        boolean isHighlightable = false;

        @Override
        public boolean isHighlightableEvenWithoutSelectability() {
            return isHighlightable;
        }

        @Override
        public void setHighlightableEvenWithoutSelectability(boolean isHighlightable) {
            this.isHighlightable = isHighlightable;
        }

    }

    boolean m_hasAltitudeWarnings;

    public boolean hasAltitudeWarnings() {
        return m_hasAltitudeWarnings;
    }

    private MinMaxPair m_gsdMissmatchRange;

    public MinMaxPair getGsdMissmatchRange() {
        return m_gsdMissmatchRange;
    }

    public static class GridPointSummary implements GridPointAttributes {
        public final int n;
        public final double eigenAbsRatio;
        public final int clusterNo;

        public GridPointSummary(int n, double eigenAbsRatio, int clusterNo) {
            this.n = n;
            this.eigenAbsRatio = eigenAbsRatio;
            this.clusterNo = clusterNo;
        }

        @Override
        public Color getColor() {
            if (isOutside()) {
                return col_nothing;
            } else if (isEmpty()) {
                return col_really_bad;
            } else if (isBad()) {
                return col_bad;
            } else if (isMedium()) {
                return col_medium;
            } else {
                return col_ok;
            }
        }

        @Override
        public double getValue() {
            return 0; // altitude of this point
        }

        public boolean isMedium() {
            return clusterNo != 0 && n > 1 && (n < 4 || eigenAbsRatio > 20.0);
        }

        public boolean isOK() {
            return clusterNo != 0 && n >= 4 && eigenAbsRatio <= 20.0;
        }

        public boolean isBad() {
            return n == 1 && clusterNo != 0;
        }

        public boolean isEmpty() {
            return n < 1 && clusterNo != 0;
        }

        public boolean isOutside() {
            return n <= 0 || clusterNo == 0;
        }

        public String getName() {
            if (isOutside()) {
                return null;
            } else if (isEmpty()) {
                return languageHelper.getString(KEY + ".empty");
            } else if (isBad()) {
                return languageHelper.getString(KEY + ".bad");
            } else if (isMedium()) {
                return languageHelper.getString(KEY + ".medium");
            } else {
                return languageHelper.getString(KEY + ".ok");
            }
        }
    }

    public static class GridPoint implements GridPointAttributes {

        int clusterNo = -1;

        int n = 0; // number of photos seeing this GridPoint

        double[] sampleMean = new double[3];
        double[][] sampleCov = new double[3][3];
        double[] eigenVal = new double[3];
        double eigenAbsRatio = 0.0;

        Vec4 pixelPoint; // (x, y, z) for point
        Vec4 normal; // Ax + By + Cz + D = 0 plane, (A, B, C) normal vector

        double gsdTarget = -1;
        double gsdReal = -1;

        public GridPoint() {}

        public GridPoint(int n) {
            this();
            this.n = n;
        }

        @Override
        public Color getColor() {
            if (isOutside()) {
                return col_nothing;
            } else if (isEmpty()) {
                return col_really_bad;
            } else if (isBad()) {
                return col_bad;
            } else if (isMedium()) {
                return col_medium;
            } else {
                return col_ok;
            }
        }

        public String getName() {
            if (isOutside()) {
                return null;
            } else if (isEmpty()) {
                return languageHelper.getString(KEY + ".empty");
            } else if (isBad()) {
                return languageHelper.getString(KEY + ".bad");
            } else if (isMedium()) {
                return languageHelper.getString(KEY + ".medium");
            } else {
                return languageHelper.getString(KEY + ".ok");
            }
        }

        @Override
        public double getValue() {
            return 0; // altitude of this point
        }

        public boolean isMedium() {
            return clusterNo != 0 && n > 1 && (n < 4 || eigenAbsRatio > 20.0);
        }

        public boolean isOK() {
            return clusterNo != 0 && n >= 4 && eigenAbsRatio <= 20.0;
        }

        public boolean isBad() {
            return n == 1 && clusterNo != 0;
        }

        public boolean isEmpty() {
            return n < 1 && clusterNo != 0;
        }

        public boolean isOutside() {
            return n <= 0 || clusterNo == 0;
        }

        private void ensureSampleCovIsSymmetric() {
            double avg = (sampleCov[0][1] + sampleCov[1][0]) / 2.0;
            sampleCov[0][1] = avg;
            sampleCov[1][0] = avg;

            avg = (sampleCov[0][2] + sampleCov[2][0]) / 2.0;
            sampleCov[0][2] = avg;
            sampleCov[2][0] = avg;

            avg = (sampleCov[1][2] + sampleCov[2][1]) / 2.0;
            sampleCov[1][2] = avg;
            sampleCov[2][1] = avg;
        }

        private void prepareEigenValues() {
            for (int i = 0; i < 3; i++) {
                eigenVal[i] = Math.abs(eigenVal[i]);
            }

            double temp; // unrolled sort 3 elements
            if (eigenVal[0] < eigenVal[1]) {
                temp = eigenVal[0];
                eigenVal[0] = eigenVal[1];
                eigenVal[1] = temp;
            }

            if (eigenVal[1] < eigenVal[2]) {
                temp = eigenVal[1];
                eigenVal[1] = eigenVal[2];
                eigenVal[2] = temp;
            }

            if (eigenVal[0] < eigenVal[1]) {
                temp = eigenVal[0];
                eigenVal[0] = eigenVal[1];
                eigenVal[1] = temp;
            }
        }

        double angCutoffCos = Math.cos(Math.toRadians(180.0 - 80.0)); // TODO: select optimum value or make a parameter

        public void updateAllStep(Vec4 vecCam, Vec4 cameraDirectionNormal, double distToGsdMultiplier) {
            // first project sample onto this pixel's tangent plane
            Vec4 groundToCam = pixelPoint.subtract3(vecCam);
            double cosNormAng = groundToCam.dot3(normal) / groundToCam.getLength3();
            if (cosNormAng > angCutoffCos) { // check if seeing this pixel from below or near below
                return;
            }
            // compute GSD

            double distInCamDirection = groundToCam.dot3(cameraDirectionNormal);
            if (distToGsdMultiplier > 0) {
                gsdReal =
                    gsdReal * n
                        + distInCamDirection * distToGsdMultiplier / Math.abs(cameraDirectionNormal.dot3(normal));
                n++;
                gsdReal /= n;
            }

            // compute covariance
            Vec4 groundToCamTangential = groundToCam.perpendicularTo3(normal); // projection onto plane
            double[] sample2 = new double[3];
            sample2 = groundToCamTangential.toDoubleArray(sample2, 0, 3);

            // update mean estimates
            double[] sampleMeanPrev = new double[3];

            for (int i = 0; i < 3; i++) {
                sampleMeanPrev[i] = sampleMean[i]; // sample mean at n-1
                sampleMean[i] += (sample2[i] - sampleMean[i]) / n; // sample mean at n
            }

            // update (unbiased) cov estimates
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    sampleCov[i][j] += ((sample2[i] - sampleMean[i]) * (sample2[j] - sampleMeanPrev[j]));
                }
            }
        }

        public void updateCovFinal() {
            // if n is 0 or 1, sampleCov and eigenVal are all 0.0 (already that value)
            // if n is 2, leave ratio at 0 (two of three eigenvalue are 0)
            if (n > 2) {
                // finalize covariance
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        sampleCov[i][j] /= (double)(n - 1); // for unbiased estimate
                    }
                }

                // to calculate the evs of the 2x2 symmetric covariance matrix we just solve the quadratic equation
                // assumes projected onto xy plane (z=0) instead of tangent plane but seems to work
                double a = sampleCov[0][0];
                double b = sampleCov[1][1];
                double c = sampleCov[1][0];
                double D = Math.sqrt((a - b) * (a - b) + 4 * c * c);
                double lambda_1 =
                    a + b + D; // that's actually 0.5*(a+b+D), but since we are only interested in the ratio
                double lambda_2 = a + b - D;

                double eig_ratio = lambda_1 / lambda_2;
                eigenAbsRatio = eig_ratio;
            }
        }

    }

    public static enum SectorType {
        auto_allRed, // alles was nicht von einer AOI abgedeckt ist, ist transparent // Everything that is not covered
        // by an AOI is transparent
        auto_redInside, // randpixel die 0 bilder haben werden transparent, //der restliche sektor wird rot aufgefüllt
        // // border pixels that have 0 images become transparent, //the rest of the sector is filled in
        // red
        truncated_redInside,
    }

    public static final Color col_nothing = new Color(0, 0, 0, 0);

    public static int defaultAlpha = 90;
    public static final Color col_really_bad =
        ColorHelper.setAlpha(new Color(0xFF, 0x5f, 0x5f).brighter(), defaultAlpha);
    public static final Color col_bad = ColorHelper.setAlpha(new Color(0xFF, 0x5f, 0x5f), defaultAlpha);
    public static final Color col_medium = ColorHelper.setAlpha(new Color(0xf3, 0xd5, 0x4e), defaultAlpha);
    public static final Color col_ok = ColorHelper.setAlpha(new Color(0, 0x69, 0), defaultAlpha);
    public static final Color col_okMax = ColorHelper.setAlpha(new Color(0, 0x69, 0).darker(), defaultAlpha);

    public static int count = 0;

    public static final double DEF_Resolution = 0.1; // in meter
    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerCoverage";
    public static final String KEY_Resolution = KEY + ".Resolution";

    public static final float log_shift = (float)Math.log(3);
    public static final float log_norm = (float)Math.log(10) - log_shift;

    public static final double MAX_Resolution = 50; // in meter

    public static final int maxNum =
        255; // TODO, improve performance so much, so we can increase this number... 1023 would be nice ;-)

    public static final double MIN_Resolution = 0.5; // in meter
    protected RenderableLayer analyticSurfaceLayer = new RenderableLayer();

    Runnable recomputeRunnableCoverage =
        new IRecomputeRunnable() {

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    recomputeCoverageBlocking();
                    Debug.getLog()
                        .info(
                            "Coverage recalc Done. "
                                + AMapLayerCoverage.this
                                + ".  It took "
                                + (System.currentTimeMillis() - start) / 1000.
                                + " sec");
                } catch (Exception e) {
                    Debug.getLog().log(Level.WARNING, "problems computing coverage", e);
                }
            }

            @Override
            public void runLaterOnUIThread() {
                mapLayerValuesChanged(AMapLayerCoverage.this);
                syncRoot.runAsync(
                    () -> {
                        analyticSurfaceLayer.firePropertyChange(AVKey.LAYER, null, analyticSurfaceLayer);
                    });
                // FIXME REMOVE ME MEMORY LEAK FORCING!?!?
                // recompCoverage.tryStartRecomp();
            }

        };

    Recomputer recompCoverage = new Recomputer(recomputeRunnableCoverage);

    Runnable recomputeRunnableCorners =
        new IRecomputeRunnable() {

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    updateCameraCornersBlocking();
                    updateCameraCornersBlockingDone();
                    Debug.getLog()
                        .info(
                            "image corner raytracing recalc Done. "
                                + AMapLayerCoverage.this
                                + ".  It took "
                                + (System.currentTimeMillis() - start) / 1000.
                                + " sec");
                } catch (Exception e) {
                    Debug.getLog().log(Level.WARNING, "problems computing image preview corners", e);
                }
            }

            @Override
            public void runLaterOnUIThread() {
                // TODO FIXME
                // call other slaves!?!?!
            }

        };

    protected void updateCameraCornersBlockingDone() {
        recomputeCoverage(); // trigger slave
    }

    private Recomputer recompCorners = new Recomputer(recomputeRunnableCorners);

    protected MAnalyticSurface surface =
        new AnalyticSurfaceTooltipable() {
            @Override
            public synchronized void render(DrawContext dc) {
                super.render(dc);
                visibilityTracker.flagCoverageLayerWasRendered();
            }
        };

    protected boolean m_computationSucc = false;

    private static boolean isDebug;

    private ArrayList<ComputeCornerData> m_cornersList;
    private int m_height;
    private int m_width;
    private int m_maxN = 1;
    private double m_qmMedium = -1;
    private double m_qmOk = -1;
    private double m_quality = -1;
    private double m_quality_pseudo = -1;

    public double getCoverageRatioOrtho() {
        return m_quality;
    }

    public double getCoverageRatioPseudoOrtho() {
        return m_quality_pseudo;
    }

    protected double resolution = DEF_Resolution; // in meter

    private Sector m_sector = null;

    protected SectorType m_secType;

    private ArrayList<GridPointSummary> m_values;

    VisibilityTracker visibilityTracker = DependencyInjector.getInstance().getInstanceOf(VisibilityTracker.class);

    public AMapLayerCoverage(boolean isDefVis) {
        super(isDefVis);
        syncRoot.runAsync(
            () -> {
                analyticSurfaceLayer.setPickEnabled(true);
                analyticSurfaceLayer.setName("Coverage Surfaces");
                analyticSurfaceLayer.addRenderable(surface);
                surface.setClientLayer(analyticSurfaceLayer);
                AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
                attr.setDrawShadow(false);
                attr.setDrawOutline(false);
                attr.setInteriorOpacity(1);
                surface.setSurfaceAttributes(attr);
                surface.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                analyticSurfaceLayer.setEnabled(false);
            });
        operationLevelChangeListener =
            (observable, oldValue, newValue) -> {
                isDebug = newValue == OperationLevel.DEBUG;
                // analyticSurfaceLayer.firePropertyChange(AVKey.LAYER, null, analyticSurfaceLayer);
                recomputeCoverage();
            };
        de.saxsys.mvvmfx.internal.viewloader.DependencyInjector.getInstance()
            .getInstanceOf(ISettingsManager.class)
            .getSection(GeneralSettings.class)
            .operationLevelProperty()
            .addListener(new WeakChangeListener(operationLevelChangeListener));

        updateCameraCornersListener = observable -> updateCameraCorners();
        DependencyInjector.getInstance()
            .getInstanceOf(WWElevationModel.class)
            .wwjElevationModelProperty()
            .addListener(new WeakInvalidationListener(updateCameraCornersListener));

        DependencyInjector.getInstance()
            .getInstanceOf(INetworkInformation.class)
            .networkAvailableProperty()
            .addListener(new WeakChangeListener<>(networkBecomesAvailableChangeListener));

        DependencyInjector.getInstance()
            .getInstanceOf(INetworkInformation.class)
            .internetAvailableProperty()
            .addListener(new WeakChangeListener<>(networkBecomesAvailableChangeListener));

        isDebug =
            de.saxsys.mvvmfx.internal.viewloader.DependencyInjector.getInstance()
                    .getInstanceOf(ISettingsManager.class)
                    .getSection(GeneralSettings.class)
                    .getOperationLevel()
                == OperationLevel.DEBUG;
    }

    /**
     * call cameraChangedBackground on every aerial Image to update its corerns BUT, it they are anyway recomputed
     * computeCorners, just irgnore call of this function
     */
    protected abstract void updateCameraCornersBlocking();

    public void updateCameraCorners() {
        if (recompCorners == null) {
            return;
        }
        // Debug.printStackTrace("recomp coverage");
        recompCorners.tryStartRecomp();
    }

    //    public abstract ArrayList<ImagePolygone> computeCorners();
    public abstract ArrayList<ComputeCornerData> computeCorners();

    public static class CornerMask {
        public double gsd;
        public Vector<LatLon> corners;
    }

    public abstract ArrayList<CornerMask> computeMaskCorners();

    public abstract boolean containsNonCoverageAbleAOIs();

    public abstract boolean shouldShowCoverage();

    protected void disableLayer() {
        Debug.getLog().fine("Disabling Coverage Grid: " + this);
        synchronized (this) {
            if (m_sector == null) {
                return;
            }
        }

        syncRoot.runAsync(
            () -> {
                synchronized (AMapLayerCoverage.this) {
                    analyticSurfaceLayer.setEnabled(false);
                    surface.disable();
                    m_sector = null;
                    m_values = null;
                    m_quality = -1;
                    m_quality_pseudo = -1;
                    m_gsdMissmatchRange = null;
                    m_qmMedium = 0;
                    m_qmOk = 0;
                    m_width = 0;
                    m_height = 0;
                    m_maxN = 1;
                    m_cornersList = null;
                    m_hasAltitudeWarnings = false;
                    updateTimestamp = System.currentTimeMillis();
                }

                analyticSurfaceLayer.firePropertyChange(AVKey.LAYER, null, analyticSurfaceLayer);

                Dispatcher.platform().run(() -> mapLayerValuesChanged(AMapLayerCoverage.this));
            });
    }

    @Override
    public void dispose() {
        recompCoverage = null;
        recompCorners = null;
        super.dispose();
    }

    static int find(int idx, int[] clusterRename) {
        int x = idx;
        int _x = clusterRename[x];
        while (_x != x) {
            x = _x;
            _x = clusterRename[x];
        }

        if (idx != x) {
            clusterRename[idx] = x;
        }

        return x;
    }

    public synchronized GridPointSummary getGridPoint(LatLon latLon) {
        if (m_values == null) {
            return null;
        }

        if (m_sector == null) {
            return null;
        }

        if (latLon == null) {
            return null;
        }

        int curIX =
            (int)
                Math.round(
                    m_width
                        * (latLon.getLongitude().degrees - m_sector.getMinLongitude().degrees)
                        / m_sector.getDeltaLonDegrees());
        int curIY =
            (int)
                Math.round(
                    m_height
                        * (latLon.getLatitude().degrees - m_sector.getMinLatitude().degrees)
                        / m_sector.getDeltaLatDegrees());
        int id = (m_height - 1 - curIY) * m_width + curIX;
        if (id < 0 || id >= m_values.size()) {
            return null;
        }

        return m_values.get(id);
    }

    @Override
    public OptionalDouble getMaxElev() {
        return OptionalDouble.empty();
    }

    public int getMaxMultiplicity() {
        return m_maxN;
    }

    @Override
    public OptionalDouble getMinElev() {
        return OptionalDouble.empty();
    }

    public double getQmMedium() {
        return m_qmMedium;
    }

    public double getQmOK() {
        return m_qmOk;
    }

    public double getResolution() {
        return resolution;
    }

    @Override
    public Sector getSector() {
        return m_sector;
    }

    public abstract SectorType getSectorType();;

    public MAnalyticSurface getSurface() {
        return surface;
    }

    public abstract String getTooltipAddon();

    @Override
    public synchronized String getTooltip(Position position, Point point) {
        GridPointSummary gp = getGridPoint(position);
        if (gp == null) {
            return null;
        }

        String quality = gp.getName();
        if (quality == null) {
            return null;
        }

        String tooltip = languageHelper.getString(KEY + ".tooltip", gp.n, quality, getTooltipAddon());
        if (isDebug) {
            tooltip += " (gp.n=" + gp.n + " gp.eigenAbsRatio=" + gp.eigenAbsRatio + ")";
        }

        return tooltip;
    }

    @Override
    public Layer getWWLayer() {
        return analyticSurfaceLayer;
    }

    public void pick(DrawContext dc, Point point) {
        analyticSurfaceLayer.pick(dc, point);
    }

    public void preRender(DrawContext dc) {
        getSurface().preRender(dc);
    }

    public void recomputeCoverage() {
        if (recompCoverage == null) {
            return;
        }
        // Debug.printStackTrace("recomp coverage");
        recompCoverage.tryStartRecomp();
    }

    protected boolean elevationDataAvaliableTmp = true;
    public boolean elevationDataAvaliable = true;

    protected void recomputeCoverageBlocking() {
        int maxN = 0;
        double qmOk = 0;
        double qmMedium = 0;
        double quality = -1;
        double quality_pseudo = -1;
        boolean hasAltitudeWarnings = false;
        MinMaxPair gsdMissmatchRange = new MinMaxPair();
        // Debug.printStackTrace();

        // if (!isVisible()) return; //calways compute this, since the coverage % is always shown!

        // (new Exception()).printStackTrace();
        // System.out.println("triggered recomp of " + this );
        SectorType secType = getSectorType();

        elevationDataAvaliableTmp = true;
        if (!shouldShowCoverage()) {
            disableLayer();
            return;
        }

        // list of ComputeCornerData per each image
        ArrayList<ComputeCornerData> cornersList = computeCorners();
        if (cornersList == null) {
            disableLayer();
            return;
        }

        elevationDataAvaliable = elevationDataAvaliableTmp;
        ArrayList<CornerMask> maskCorners = computeMaskCorners();

        MinMaxPair minMaxYProj = new MinMaxPair();
        MinMaxPair minMaxXProj = new MinMaxPair();

        MinMaxPair minMaxY = new MinMaxPair();
        MinMaxPair minMaxX = new MinMaxPair();

        // update the area where coverage will be checked using ground projections
        for (ComputeCornerData p : cornersList) {
            ArrayList<LatLon> corners = p.getGroundProjectedCorners();
            for (LatLon c : corners) {
                minMaxXProj.update(c.getLongitude().degrees);
                minMaxYProj.update(c.getLatitude().degrees);
            }
        }
        // update the area where coverage will be checked using AOI corners
        if (maskCorners != null) {
            for (CornerMask mask : maskCorners) {
                for (LatLon c : mask.corners) {
                    minMaxX.update(c.getLongitude().degrees);
                    minMaxY.update(c.getLatitude().degrees);
                }
            }
        }

        double aoiXSize = minMaxX.size();
        double aoiYSize = minMaxY.size();
        double projXSize = minMaxXProj.size();
        double projYSize = minMaxYProj.size();
        double photoAreaToAOI = (projXSize * projYSize) / (aoiYSize * aoiXSize);

        // in 90% of the case this condition is true
        if (photoAreaToAOI <= THRESHOLD || isAreaSmall(aoiXSize, aoiYSize, cornersList)) {
            minMaxX.enlarge(minMaxXProj);
            minMaxY.enlarge(minMaxYProj);
        } else {
            // if the angle between some camera directions and terrain is small then photoAreaToAOI will be a big number
            // keeping area small (as AOI) and adding some padding
            minMaxX.enlarge(minMaxX.size() * PADDING);
            minMaxY.enlarge(minMaxY.size() * PADDING);
        }

        Sector sector = Sector.fromDegrees(minMaxY.min, minMaxY.max, minMaxX.min, minMaxX.max);
        Position referencePos = new Position(sector.getCentroid(), 0.);

        double resolution = this.resolution;

        // System.out.println("sector is: " + sector);
        int stepsPerDegX =
            (int)
                Math.round(
                    globe.getRadius() * 2 * Math.PI * sector.getCentroid().getLatitude().cos() / 360. / resolution);
        int stepsPerDegY = (int)Math.round(globe.getRadius() * 2 * Math.PI / 360. / resolution);

        int height = (int)Math.ceil(minMaxY.size() * stepsPerDegY);
        int width = (int)Math.ceil(minMaxX.size() * stepsPerDegX);

        double shrink = Math.max(height, width) / (double)maxNum;
        if (shrink > 2000) {
            disableLayer();
            return;
        }

        // the area is too big, amount of grid points will be shrinked
        if (shrink > 1) {
            resolution *= shrink;
            stepsPerDegX =
                (int)
                    Math.round(
                        globe.getRadius() * 2 * Math.PI * sector.getCentroid().getLatitude().cos() / 360. / resolution);
            stepsPerDegY = (int)Math.round(globe.getRadius() * 2 * Math.PI / 360. / resolution);

            height = (int)Math.ceil(minMaxY.size() * stepsPerDegY);
            width = (int)Math.ceil(minMaxX.size() * stepsPerDegX);
        }

        Debug.getLog().fine("Recompute Coverage Grid: " + this + " width:" + width + " height:" + height);

        // System.out.println("resX=" + stepsPerDegX + " resY=" + stepsPerDegY);
        // System.out.println("width=" + width + " heigth=" + height);
        // System.out.println("sector=" + sector);
        if (Math.min(height, width) < 3) {
            disableLayer();
            return;
        }

        double degPerStepY = 1.0 / (double)stepsPerDegY;
        double degPerStepX = 1.0 / (double)stepsPerDegX;
        // System.out.println("dresX=" + degPerStepX + " dresY=" + degPerStepY);

        ArrayList<GridPoint> v = new ArrayList<>();
        for (int iy = 0; iy != height; iy++) {
            for (int ix = 0; ix != width; ix++) {
                GridPoint gp = new GridPoint();
                v.add(gp);
            }
        }

        // add location data and evelation values to GridPoints (to compute tangent planes)
        for (int iy = 0; iy < height; iy++) {
            double pixellat = minMaxY.min + iy * degPerStepY;
            for (int ix = 0; ix < width; ix++) {
                double pixellon = minMaxX.min + ix * degPerStepX;
                // System.out.println("pixellat=" + pixellat + " pixellon=" + pixellon);
                double pixelElevation = elevationModel.getElevationAsGoodAsPossible(pixellat, pixellon);
                // System.out.println("pixelelevation=" + pixelelevation);

                Angle pixelLon = Angle.fromDegreesLongitude(pixellon);
                Angle pixelLat = Angle.fromDegreesLatitude(pixellat);
                pixelLon =
                    pixelLon.subtract(
                        Angle.fromDegrees(referencePos.getLongitude().degrees)); // offset by sector centroid
                pixelLat = pixelLat.subtract(Angle.fromDegrees(referencePos.getLatitude().degrees));
                LatLon pixelPointLL = new LatLon(pixelLat, pixelLon);

                int i = (height - 1 - iy) * width + ix; // not sure why in this order and not i = iy * width + ix
                v.get(i).pixelPoint =
                    new Vec4(
                        pixelPointLL.getLongitude().radians
                            * pixelPointLL.getLatitude().cos()
                            * Earth.WGS84_EQUATORIAL_RADIUS,
                        pixelPointLL.getLatitude().radians * Earth.WGS84_POLAR_RADIUS,
                        pixelElevation);
            }
        }

        // add tangent plane to GridPoints
        for (int iy = 0; iy < height; iy++) {
            for (int ix = 0; ix < width; ix++) {
                int i = (height - 1 - iy) * width + ix; // not sure why in this order and not i = iy * width + ix

                int ixsmall = (i % width == 0) ? i : i - 1;
                int ixbig = ((i + 1) % width == 0) ? i : i + 1;
                double dfdx =
                    (v.get(ixbig).pixelPoint.z - v.get(ixsmall).pixelPoint.z)
                        / (v.get(ixbig).pixelPoint.x - v.get(ixsmall).pixelPoint.x);

                int iysmall = ((i - width) > 0) ? i - width : i;
                int iybig = ((i + width) < v.size()) ? i + width : i;
                double dfdy =
                    (v.get(iybig).pixelPoint.z - v.get(iysmall).pixelPoint.z)
                        / (v.get(iybig).pixelPoint.y - v.get(iysmall).pixelPoint.y);

                // Ax + By + Cz + D = 0 plane, (A, B, C) normal vector
                v.get(i).normal = new Vec4(-dfdx, -dfdy, 1.0).normalize3();
                // v.get(i).pixelPlane[3] =
                //    -dfdx * v.get(i).pixelPoint[0]
                //        - dfdy * v.get(i).pixelPoint[1]
                //        + v.get(i).pixelPoint[2]; // not needed for normal vector
            }
        }

        for (ComputeCornerData p : cornersList) {
            ArrayList<LatLon> corners = p.getGroundProjectedCorners();
            int lineNo = p.getLineNumber();
            double[] vx = new double[5];
            double[] vy = new double[5];
            MinMaxPair minMaxLocY = new MinMaxPair();
            int i = 0;
            // transforming to normal double values
            for (LatLon c : corners) {
                double x = c.getLongitude().degrees;
                double y = c.getLatitude().degrees;
                minMaxLocY.update(y);
                vx[i] = x;
                vy[i] = y;
                i++;
            }

            vx[4] = vx[0];
            vy[4] = vy[0];

            Angle centerLon = p.getShiftedPosOnLevel().longitude;
            Angle centerLat = p.getShiftedPosOnLevel().latitude;
            centerLon =
                centerLon.subtract(Angle.fromDegrees(referencePos.getLongitude().degrees)); // offset by sector centroid
            centerLat = centerLat.subtract(Angle.fromDegrees(referencePos.getLatitude().degrees));
            LatLon centerPointLL = new LatLon(centerLat, centerLon);
            Vec4 centerPoint =
                new Vec4(
                    centerPointLL.getLongitude().radians
                        * centerPointLL.getLatitude().cos()
                        * Earth.WGS84_EQUATORIAL_RADIUS,
                    centerPointLL.getLatitude().radians * Earth.WGS84_POLAR_RADIUS,
                    p.getShiftedPosOnLevel().elevation); // center point for this picture/camera

            // System.out.println("centerPoint="+centerPoint[0]+","+centerPoint[1]+","+centerPoint[2]);

            minMaxLocY.shift(-minMaxY.min);
            minMaxLocY.mult(stepsPerDegY);
            int minIY = (int)Math.ceil(minMaxLocY.min) + 1;
            int maxIY = (int)Math.floor(minMaxLocY.max) - 1;

            // slicing inty y lines on the grid
            for (int curIY = minIY; curIY <= maxIY; curIY++) {
                // System.out.println("curIY="+curIY);
                double currentY = minMaxY.min + ((double)curIY) / stepsPerDegY;

                // System.out.println("curIY = " + curIY + " cury="+currentY);
                MinMaxPair minMaxLocX = new MinMaxPair();

                // scan for each line the minimal and maximal x value
                for (i = 0; i != 4; i++) {
                    double v1x = vx[i];
                    double v2x = vx[i + 1];
                    double v1y = vy[i];
                    double v2y = vy[i + 1];
                    if (v1y < v2y) {
                        double tmpX = v1x;
                        double tmpY = v1y;
                        v1x = v2x;
                        v1y = v2y;
                        v2x = tmpX;
                        v2y = tmpY;
                    }
                    // System.out.println("v1y="+v1y + " v2y="+v2y);
                    if (v1y >= currentY && v2y <= currentY) {
                        double x = v1x + (v2x - v1x) / (v2y - v1y) * (currentY - v1y);
                        minMaxLocX.update(x);
                    }
                }
                // System.out.println("corners="+corners);
                // System.out.println("minMaxLocX="+minMaxLocX);

                // transform into grid system
                minMaxLocX.shift(-minMaxX.min);
                minMaxLocX.mult(stepsPerDegX);
                int minIX = (int)Math.ceil(minMaxLocX.min) + 1;
                minIX = Math.max(0, minIX); // truncate to sector

                int maxIX = (int)Math.floor(minMaxLocX.max) - 1;
                maxIX = Math.min(maxIX, width); // truncate to sector
                // System.out.println("minIX="+minIX + " maxIX="+maxIX);

                i = (height - 1 - curIY) * width + minIX;
                for (int curIX = minIX; curIX <= maxIX; curIX++) {
                    // i = curIY * width + curIX;
                    if (i >= 0 && i < v.size()) {
                        v.get(i)
                            .updateAllStep(
                                centerPoint, p.getCameraDirectionNormal(), 1 / p.getGsdToDistanceMultiplier());
                    }

                    i++;
                }
            }
        }

        for (int i = 0; i < v.size(); i++) {
            // System.out.println("i="+i);
            v.get(i).updateCovFinal();
        }

        ArrayList<GridPoint> vNew;
        if (maskCorners != null) {
            for (int i = maskCorners.size() - 1; i >= 0; i--) { // remove masks without content!
                if (maskCorners.get(i) == null || maskCorners.get(i).corners.size() < 3) {
                    maskCorners.remove(i);
                }
            }
        }

        if (maskCorners != null && maskCorners.size() > 0) {
            vNew = new ArrayList<>(v.size());
            for (int iy = 0; iy != height; iy++) {
                for (int ix = 0; ix != width; ix++) {
                    vNew.add(new GridPoint(-1));
                }
            }
            // System.out.println("start pciArea Stuff" + this);

            for (CornerMask cornerMask : maskCorners) {
                // System.out.println("picAreaCorners " + corners);
                double[] vx = new double[cornerMask.corners.size() + 1];
                double[] vy = new double[cornerMask.corners.size() + 1];
                MinMaxPair minMaxLocY = new MinMaxPair();
                int i = 0;
                // transforming to normal double values
                for (LatLon c : cornerMask.corners) {
                    double x = c.getLongitude().degrees;
                    double y = c.getLatitude().degrees;
                    minMaxLocY.update(y);
                    vx[i] = x;
                    vy[i] = y;
                    i++;
                }

                vx[cornerMask.corners.size()] = vx[0];
                vy[cornerMask.corners.size()] = vy[0];

                // System.out.println("minY="+minY);
                // System.out.println("maxY="+maxY);

                minMaxLocY.shift(-minMaxY.min);
                minMaxLocY.mult(stepsPerDegY);
                int minIY = (int)Math.ceil(minMaxLocY.min) + 1;
                int maxIY = (int)Math.floor(minMaxLocY.max) - 1;

                // slicing inty y lines on the grid
                for (int curIY = minIY; curIY <= maxIY; curIY++) {
                    double currentY = minMaxY.min + ((double)curIY) / stepsPerDegY;
                    // System.out.println("curIY = " + curIY + " cury="+currentY);
                    // MinMaxPair minMaxLocX = new MinMaxPair();

                    // scan for each line for a list of intersection x values
                    ArrayList<Double> xIntersections = new ArrayList<>();
                    for (i = 0; i != cornerMask.corners.size(); i++) {
                        double v1x = vx[i];
                        double v2x = vx[i + 1];
                        double v1y = vy[i];
                        double v2y = vy[i + 1];
                        if (v1y < v2y) {
                            double tmpX = v1x;
                            double tmpY = v1y;
                            v1x = v2x;
                            v1y = v2y;
                            v2x = tmpX;
                            v2y = tmpY;
                        }

                        if (v1y >= currentY && v2y <= currentY) {
                            double x = v1x + (v2x - v1x) / (v2y - v1y) * (currentY - v1y);
                            // minMaxLocX.update(x);
                            xIntersections.add(x);
                        }
                    }
                    // sort them..
                    java.util.Collections.sort(xIntersections);

                    // transform into grid system
                    ArrayList<Double> xIntersectionsTransformed = new ArrayList<>(xIntersections.size());
                    for (Double d : xIntersections) {
                        d -= minMaxX.min;
                        d *= stepsPerDegX;
                        xIntersectionsTransformed.add(d);
                    }

                    // System.out.println("minIX="+minIX + " maxIX="+maxIX);
                    for (int interval = 0; interval + 1 < xIntersectionsTransformed.size(); interval += 2) {
                        int minIX = (int)Math.ceil(xIntersectionsTransformed.get(interval));
                        int maxIX = (int)Math.floor(xIntersectionsTransformed.get(interval + 1));
                        i = (height - 1 - curIY) * width + minIX;
                        for (int curIX = minIX; curIX <= maxIX; curIX++) {
                            if (i >= 0 && i < v.size()) {
                                GridPoint gp = v.get(i);
                                gp.gsdTarget = cornerMask.gsd;
                                vNew.set(i, gp);
                                // System.out.println("taking over "+ i + " -> " +v.get(i).n);
                            }

                            i++;
                        }
                    }
                }
            }

            int totalInside = 0;
            int totalOk = 0;
            int totalMedium = 0;
            for (int i = 0; i != v.size(); i++) {
                GridPoint gp = vNew.get(i);
                if (gp.n >= 0) {
                    totalInside++;
                    if (gp.isOK()) {
                        totalOk++;
                    } else if (gp.isMedium()) {
                        totalMedium++;
                    }

                    if (gp.gsdReal > 0 && gp.gsdTarget > 0) {
                        gsdMissmatchRange.update(gp.gsdReal / gp.gsdTarget);
                    }
                }
            }

            if (totalInside != 0) {
                quality = ((double)totalOk) / totalInside;
                quality_pseudo = ((double)(totalMedium + totalOk)) / totalInside;
            }
        } else {
            quality = -1;
            quality_pseudo = -1;
            vNew = null;
        }

        if (secType == SectorType.truncated_redInside) {
            if (vNew != null) {
                v = vNew;
            }
        } else if (secType == SectorType.auto_redInside) {
            // figure out whats outer area by clustering all empty nodes
            // with the hoshen-kopelman algorithm, additionally all empty nodes hitting a border should belong to
            // cluster 0

            int[] clusterRename = new int[width * height];
            // cluster =0, outer cluster without points
            clusterRename[0] = 0;
            int nextFreeClusterNo = 1;

            // first cell
            GridPoint last, cur;

            // first row
            for (int x = 0; x != width; x++) {
                cur = v.get(x);
                if (cur.n == 0) {
                    cur.clusterNo = 0;
                }
            }

            // all except the first rot
            for (int y = 1; y != height; y++) {
                // first cell in this row
                int idxRow = y * width;
                cur = v.get(idxRow);
                if (cur.n == 0) {
                    cur.clusterNo = 0;
                }

                // all the rest..
                for (int x = 1; x != width; x++) {
                    last = cur;
                    idxRow++;
                    cur = v.get(idxRow);
                    if (cur.n != 0) {
                        // dont name inner empty nodes with a cluster label;
                        continue;
                    }

                    GridPoint above = v.get(idxRow - width);
                    if (last.n == 0) {
                        if (above.n == 0) {
                            // collision
                            if (last.clusterNo == above.clusterNo) {
                                cur.clusterNo = last.clusterNo;
                            } else {
                                int a = find(last.clusterNo, clusterRename);
                                int b = find(above.clusterNo, clusterRename);
                                cur.clusterNo = Math.min(a, b);
                                int i = Math.max(a, b);
                                clusterRename[i] = cur.clusterNo;
                            }
                        } else {
                            cur.clusterNo = last.clusterNo;
                        }
                    } else {
                        if (above.n == 0) {
                            cur.clusterNo = above.clusterNo;
                        } else {
                            // new cluster
                            cur.clusterNo = nextFreeClusterNo;
                            clusterRename[nextFreeClusterNo] = nextFreeClusterNo;
                            nextFreeClusterNo++;
                        }
                    }
                }

                // fix end of every line, that this also is connected
                if (cur.n == 0) {
                    clusterRename[find(cur.clusterNo, clusterRename)] = 0;
                }
            }

            // fixing last row
            int idxRow = (height - 1) * width;
            for (int x = 1; x != width; x++) {
                idxRow++;
                cur = v.get(idxRow);
                if (cur.n == 0) {
                    clusterRename[find(cur.clusterNo, clusterRename)] = 0;
                }
            }

            // cleanup rename mapping, so that every mapping is pointing to a identity mapping
            for (int i = 1; i != nextFreeClusterNo; i++) {
                clusterRename[i] = clusterRename[clusterRename[i]];
            }

            // apply renaming
            for (int i = width; i != v.size(); i++) { // skip first line
                cur = v.get(i);
                if (cur.n == 0) {
                    cur.clusterNo = clusterRename[cur.clusterNo];
                }
            }
        }

        for (int i = 0; i != v.size(); i++) {
            GridPoint p = v.get(i);
            maxN = Math.max(maxN, p.n);
            if (p.isOK()) {
                qmOk++;
            } else if (p.isMedium()) {
                qmMedium++;
            }
        }

        qmMedium += qmOk;

        final double area = resolution * resolution;
        qmOk *= area;
        qmMedium *= area;

        synchronized (this) {
            m_values = new ArrayList<>();
            v.stream().forEach((x) -> m_values.add(new GridPointSummary(x.n, x.eigenAbsRatio, x.clusterNo)));
            surface.setAll(sector, m_values, width, height);
            this.m_sector = sector;
            this.m_width = width;
            this.m_height = height;

            // System.gc();
            // int i = 0;
            // System.out.println("\n");
            // for (GridPoint gs : list){
            //// System.out.print(gs.serial+" ");
            // i++;
            // }
            // System.out.println("\nValiuesSize:"+v.size()+ " allSize:"+i);

            this.m_quality = quality;
            this.m_quality_pseudo = quality_pseudo;
            this.m_gsdMissmatchRange = gsdMissmatchRange;
            this.m_qmOk = qmOk;
            this.m_qmMedium = qmMedium;
            this.m_maxN = maxN;
            this.m_cornersList = cornersList;
            this.m_hasAltitudeWarnings = hasAltitudeWarnings;
            this.updateTimestamp = System.currentTimeMillis();

            // System.out.println("width="+width + " height="+height + " vDim=" +v.size());
            syncRoot.runAsync(
                () -> {
                    analyticSurfaceLayer.setEnabled(true);
                });
        }

        syncRoot.runAsync(
            () -> {
                analyticSurfaceLayer.firePropertyChange(AVKey.LAYER, null, analyticSurfaceLayer);
            });
        mapLayerValuesChanged(this);

        if (!elevationDataAvaliable && !WorldWind.getNetworkStatus().isNetworkUnavailable()) {
            Debug.getLog().config("updateCameraCorners: " + this + " due to missing elevation model data");
            recompCorners.maybeStartAgainIfNotDoneYet(5000);
        }
    }

    private boolean isAreaSmall(double areaXSize, double areaYSize, List<ComputeCornerData> projections) {
        // empiric definition of what a small area is
        if (projections.size() > 0) {
            double areaSquare = areaXSize * areaYSize;
            Sector sector = projections.get(0).getSector();
            double projSquare = sector.getDeltaLatDegrees() * sector.getDeltaLonDegrees();
            return areaSquare / projSquare < MIN_RELATION;
        }

        return false;
    }

    public void render(DrawContext dc) {
        getSurface().render(dc);
    }

    public void setResolution(double resolution) {
        if (resolution > MAX_Resolution) {
            resolution = MAX_Resolution;
        } else if (resolution < MIN_Resolution) {
            resolution = MIN_Resolution;
        }

        if (this.resolution == resolution) {
            return;
        }

        this.resolution = resolution;
        resolutionChanged();
    }

    public void resolutionChanged() {
        recomputeCoverage();
    }

    @Override
    public void addRecomputeListener(IRecomputeListener l) {
        recompCoverage.addRecomputeListener(l);
    }

    public void addRecomputeCornersListener(IRecomputeListener l) {
        recompCorners.addRecomputeListener(l);
    }

    @Override
    public void addRecomputeListenerAtBegin(IRecomputeListener l) {
        recompCoverage.addRecomputeListener(l);
    }

    @Override
    public void removeRecomputeListener(IRecomputeListener l) {
        if (recompCoverage != null) {
            recompCoverage.removeRecomputeListener(l);
        }
    }

    long updateTimestamp;

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }
}
