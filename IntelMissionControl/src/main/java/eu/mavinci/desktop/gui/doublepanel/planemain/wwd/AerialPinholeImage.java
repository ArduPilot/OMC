/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Unit;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.wwext.BasicMarkerWithUserData;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import eu.mavinci.desktop.gui.wwext.MarkerLayerWithCustomRenderer;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.PhotoLogLine;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Color;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.logging.Level;

public class AerialPinholeImage
        implements ISectorReferenced, Renderable, PreRenderable, IWWRenderableWithUserData, Locatable {

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeImage";
    public static final String KEY_Unmatchable = KEY + ".unmatchable";
    public static final String KEY_FilteredOut = KEY + ".filteredOut";

    private static BasicMarkerAttributes markerAttributesNone;
    private static BasicMarkerAttributes markerAttributesFloating;
    private static BasicMarkerAttributes markerAttributesDGPS;
    private static BasicMarkerAttributes markerAttributesFixed;

    private ArrayList<Marker> markers = new ArrayList<Marker>();
    protected MarkerLayerWithCustomRenderer markerLayer = new MarkerLayerWithCustomRenderer(markers);

    private ComputeCornerData computeCornerData;
    private CPhotoLogLine line;

    private boolean isVisible = false;
    private boolean isEnabled = true;
    private boolean isMatchable = false;

    ComputeCornerData.IAerialPinholeImageContext context;
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    public AerialPinholeImage(MapLayerMatch match) {
        this(match.getPhotoLogLine(), match.getMatching());
        Expect.notNull(match, "match");
        this.userData = match;
    }

    public AerialPinholeImage(PhotoData photo, ComputeCornerData.IAerialPinholeImageContext context) {
        this(new PhotoLogLine(photo, context.getHardwareConfiguration()), context);
    }

    private AerialPinholeImage(CPhotoLogLine line, ComputeCornerData.IAerialPinholeImageContext context) {
        this(line);
        Expect.notNull(context, "context");
        this.context = context;
    }

    protected AerialPinholeImage(CPhotoLogLine line) {
        Expect.notNull(line, "line");
        this.line = line;
        initMarkerLayer();
    }

    static {
        Color color = new Color(0x2C0000);
        Material material = new Material(color, color, color, color, 0);
        markerAttributesNone = new BasicMarkerAttributes(material, BasicMarkerShape.HEADING_ARROW, 0xff, 8, 0);
        markerAttributesNone.setHeadingMaterial(material);
        markerAttributesNone.setHeadingScale(1.5);

        color = new Color(0x556300);
        material = new Material(color, color, color, color, 0);
        markerAttributesFloating = new BasicMarkerAttributes(material, BasicMarkerShape.HEADING_ARROW, 0xff, 8, 0);
        markerAttributesFloating.setHeadingMaterial(material);
        markerAttributesFloating.setHeadingScale(1.5);

        color = new Color(0x032A3A);
        material = new Material(color, color, color, color, 0);
        markerAttributesDGPS = new BasicMarkerAttributes(material, BasicMarkerShape.HEADING_ARROW, 0xff, 8, 0);
        markerAttributesDGPS.setHeadingMaterial(material);
        markerAttributesDGPS.setHeadingScale(1.5);

        color = Color.WHITE;
        material = new Material(color, color, color, color, 0);
        markerAttributesFixed = new BasicMarkerAttributes(material, BasicMarkerShape.HEADING_ARROW, 0xff, 8, 0);
        markerAttributesFixed.setHeadingMaterial(material);
        markerAttributesFixed.setHeadingScale(1.5);
    }

    private void initMarkerLayer() {
        markerLayer.setKeepSeparated(false);
        markerLayer.setOverrideMarkerElevation(false);
    }

    protected final void resetMarker() {
        ComputeCornerData computeCornerData = this.computeCornerData;
        if (computeCornerData == null || computeCornerData.getShiftedPosOnLevelPlus2() == null || line == null) {
            return;
        }

        markers.clear();
        BasicMarkerAttributes markerAttributes = markerAttributesDGPS;

        if (line.fixType != null) {
            switch (line.fixType) {
            case rtkFixedBL:
                markerAttributes = markerAttributesFixed;
                break;
            case rtkFloatingBL:
                markerAttributes = markerAttributesFloating;
                break;
            case dgps:
                markerAttributes = markerAttributesDGPS;
                break;
            default:
                markerAttributes = markerAttributesNone;
                break;
            }
        }

        BasicMarkerWithUserData marker =
            new BasicMarkerWithUserData(
                computeCornerData.getShiftedPosOnLevelPlus2(), markerAttributes, Angle.fromDegrees(line.cameraYaw));
        marker.setUserData(getUserData());
        marker.setSelectable(false);
        markers.add(marker);
    }

    protected final void invalidatePhotoLogLine() {
        computeCornerData = null;
        photoLogLineInvalidated();
    }

    protected void photoLogLineInvalidated() {}

    public final CPhotoLogLine getPhotoLogLine() {
        return line;
    }

    public boolean isMatchable() {
        ComputeCornerData computeCornerData = this.computeCornerData;
        return computeCornerData != null && computeCornerData.isMatchable();
    }

    public String getName() {
        String name = "";
        if (!isMatchable()) {
            name += " " + languageHelper.getString(KEY_Unmatchable);
        } else {
            if (!isVisible) {
                name += " " + languageHelper.getString(KEY_FilteredOut);
            }
        }

        return name;
    }

    @Override
    public String toString() {
        return "Image:" + getName();
    }

    public void reset() {
        computeCornerData = null;
        lastFuzz = null;
        lastAdditionalDelaySec = -1;
        resetVisibility();
    }

    private void resetVisibility() {
        isVisible = context != null && context.getHardwareConfiguration() != null;
    }

    public synchronized void recomputeCornersSyncIfNotAvaliable() {
        if (computeCornerData == null) {
            recomputeCornersSync();
        }
    }

    public synchronized void recomputeCornersSync() {
        setCorners(ComputeCornerData.computeCorners(context, line));
    }

    private double lastAdditionalDelaySec = -1;

    public Sector getSector() {
        ComputeCornerData computeCornerData = this.computeCornerData;
        if (computeCornerData == null) {
            return null;
        }

        return computeCornerData.getSector();
    }

    protected void setCorners(ComputeCornerData computeCornerData) {
        this.computeCornerData = computeCornerData;
    }

    private FuzzinessData lastFuzz = null;

    public FuzzinessData getFuzzinessBlocking(double exposureTime) {
        if (this.computeCornerData == null) {
            recomputeCornersSync();
        }

        ComputeCornerData computeCornerData = this.computeCornerData;
        if (this.computeCornerData == null) {
            return lastFuzz;
        }

        IHardwareConfiguration hardwareConfiguration = context.getHardwareConfiguration();

        if (exposureTime < 0 && hardwareConfiguration != null) {
            IGenericCameraConfiguration cameraConfiguration =
                hardwareConfiguration.getPayload(IGenericCameraConfiguration.class);
            exposureTime =
                1.0
                    / cameraConfiguration
                        .getDescription()
                        .getOneOverExposureTime()
                        .convertTo(Unit.MILLISECOND)
                        .getValue()
                        .doubleValue();
        }

        if (lastFuzz != null && lastAdditionalDelaySec == exposureTime && computeCornerData.isElevationDataReady()) {
            return lastFuzz;
        }

        lastFuzz = FuzzinessData.getFuzziness(context, line, exposureTime, computeCornerData);
        return lastFuzz;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
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
    public void preRender(DrawContext dc) {
        try {
            if (!isEnabled) {
                return;
            }

            if (!isVisible) {
                resetVisibility();
            }

            if (isVisible) {
                if (computeCornerData != null /* && isMatchable*/) {
                    preRenderDetail(dc);
                }
            }
        } catch (RuntimeException e) {
            Debug.getLog().log(Level.SEVERE, "problems prerendering Aerial Image", e);
            throw e;
        }
    }

    public void preRenderDetail(DrawContext dc) {};

    @Override
    public void render(DrawContext dc) {}

    public boolean shouldRender() {
        if (!isEnabled) {
            return false;
        }

        return isVisible && computeCornerData != null /* && isMatchable*/;
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void setDraggable(boolean isDraggable) {}

    @Override
    public void setSelectable(boolean isSelectable) {}

    @Override
    public void setUserData(Object o) {
        this.userData = o;
        reset();
    }

    private Object userData;

    @Override
    public Object getUserData() {
        return userData;
    }

    public MapLayerMatch getMatch() {
        Object userData = this.userData;
        if (userData instanceof MapLayerMatch) {
            return (MapLayerMatch)userData;
        }

        return null;
    }

    @Override
    public Position getPosition() {
        Sector s = getSector();
        if (s != null) {
            return new Position(s.getCentroid(), 0);
        }

        return null;
    }

    private boolean isPopupTriggering = false;

    @Override
    public boolean isPopupTriggering() {
        return isPopupTriggering;
    }

    @Override
    public void setPopupTriggering(boolean isPopupTriggering) {
        this.isPopupTriggering = isPopupTriggering;
    }

    private boolean hasTooltip = true;

    @Override
    public boolean hasTooltip() {
        return hasTooltip;
    }

    @Override
    public void setHasTooltip(boolean hasTooltip) {
        this.hasTooltip = hasTooltip;
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

    public IHardwareConfiguration getHardwareConfiguration() {
        return context.getHardwareConfiguration();
    }

    public ComputeCornerData getComputeCornerData() {
        return computeCornerData;
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
