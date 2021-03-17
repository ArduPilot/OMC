/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import static eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData.d64;

import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.FontHelper;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapDragManager;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.map.worldwind.layers.flightplan.FlightplanLayerVisibilitySettings;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.Drone;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.WayPoint;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.core.flightplan.visitors.FirstWaypointVisitor;
import eu.mavinci.core.flightplan.visitors.LastWaypointVisitor;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConfig;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.desktop.gui.wwext.CircleWithUserData;
import eu.mavinci.desktop.gui.wwext.CylinderWithUserData;
import eu.mavinci.desktop.gui.wwext.ExtrudedPolygonWithUserData;
import eu.mavinci.desktop.gui.wwext.IconLayerCentered;
import eu.mavinci.desktop.gui.wwext.IconRendererCentered;
import eu.mavinci.desktop.gui.wwext.PolylineWithUserData;
import eu.mavinci.desktop.gui.wwext.SurfacePolygonWithUserData;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import eu.mavinci.desktop.gui.wwext.UserFacingTextLayer;
import eu.mavinci.desktop.helper.ColorHelper;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.desktop.helper.IRecomputeRunnable;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.PhantomCorner;
import eu.mavinci.flightplan.Photo;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.PicAreaCorners;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.flightplan.Takeoff;
import eu.mavinci.flightplan.Waypoint;
import eu.mavinci.flightplan.WaypointLoop;
import eu.mavinci.flightplan.WindmillData;
import eu.mavinci.flightplan.computation.LocalTransformationProvider;
import eu.mavinci.flightplan.computation.objectSurface.VoxelGrid;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceMultiPolygon;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceSector;
import gov.nasa.worldwind.render.UserFacingText;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.util.ContourList;
import gov.nasa.worldwind.util.combine.Combinable;
import gov.nasa.worldwind.util.combine.ShapeCombiner;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;

public class FlightplanLayer extends AbstractLayer
        implements IAirplaneListenerStartPos,
            IAirplaneListenerConfig,
            IAirplaneListenerPosition,
            IRecomputeListener,
            MouseListener,
            MouseMotionListener,
            PositionListener {

    public static final double HIGHLIGHT_SCALE = 1.5;

    public static final Color AOI_COLOR_HOVER_OR_SELECT = new Color(0x00, 0xAE, 0xEF); // 00AEEF
    public static final Color AOI_COLOR_NORMAL = new Color(0x00, 0x3c, 0x71); // 003c71
    public static final Color AOI_COLOR_ON_AIR = new Color(0x00, 0x69, 0x00); // 006900
    public static final Color FLIGHT_LINE_COLOR = new Color(0xDF, 0xFF, 0x00); // dfff00
    public static final Color CAMERA_POINTING_LINE = new Color(0xF3, 0xF3, 0xF3); // f3f3f3
    public static final Color CAMERA_SHAPRE_PREVIEW = Color.WHITE;
    public static final Color WAYPOINT_NUMBER = Color.WHITE;

    private final ChangeListener<WorkflowStep> workflowStepListener =
        (observable, oldValue, newValue) -> FlightplanLayer.this.reconstructLayer();
    private final ChangeListener<Boolean> flatEarthChangeListener;
    private final ChangeListener<InputMode> mouseModeChangeListener;
    private final ChangeListener<Object> currentSelectionChangedListener;
    private final ChangeListener<Boolean> recomputeListener;

    private Flightplan fp;
    private FlightPlan flighPlan;

    private IconLayerCentered iconLayer = new IconLayerCentered();
    private IconLayerCentered iconLayerFoot = new IconLayerCentered();
    private IconLayerCentered waypointsLayer = new IconLayerCentered();
    private RenderableLayer lineLayer = new RenderableLayer();
    private RenderableLayer forgroundLayer = new RenderableLayer();
    private UserFacingTextLayer textLayer = new UserFacingTextLayer();
    private Vector<Marker> markers = new Vector<>();
    private MarkerLayer markerLayer = new MarkerLayer(markers);
    private AnnotationLayer annotationLayer = new AnnotationLayer();
    private AoiAnnotationBalloon balloon = new AoiAnnotationBalloon();

    private IconLayerCentered iconLayerAct = new IconLayerCentered();
    private IconLayerCentered iconLayerFootAct = new IconLayerCentered();
    private IconLayerCentered waypointsLayerAct = new IconLayerCentered();
    private RenderableLayer lineLayerAct = new RenderableLayer();
    private RenderableLayer forgroundLayerAct = new RenderableLayer();
    private UserFacingTextLayer textLayerAct = new UserFacingTextLayer();
    private Vector<Marker> markersAct = new Vector<>();
    private MarkerLayer markerLayerAct = new MarkerLayer(markersAct);
    private AnnotationLayer annotationLayerAct = new AnnotationLayer();
    private AoiAnnotationBalloon balloonAct = new AoiAnnotationBalloon();

    private ArrayList<Vector<LatLon>> allGeofences = new ArrayList<>();

    private boolean isOnAirFP = true;
    IAirplane plane;
    private IMapDragManager dragger;

    private boolean isDragable;

    public FPcoveragePreview preview;
    private SimulationResultPathLayer simPath;
    private Position posStart;

    private final AoiTooltipBuilder aoiTooltipBuilder;
    private final IMapController mapController;
    private final IMapView mapView;
    private final ISelectionManager selectionManager;
    private final INavigationService navigationService;
    private final IElevationModel elevationModel;
    private final Globe globe;
    private final Dispatcher dispatcher;

    private final AsyncObjectProperty<OperationLevel> operationLevel = new SimpleAsyncObjectProperty<>(this);

    private final FlightplanLayerVisibilitySettings flightplanLayerVisibilitySettings;

    private final IFlightplanChangeListener fpListener =
        new IFlightplanChangeListener() {

            @Override
            public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
                recomp.tryStartRecomp();
            }

            @Override
            public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                recomp.tryStartRecomp();
            }

            @Override
            public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {
                recomp.tryStartRecomp();
            }

            @Override
            public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {
                recomp.tryStartRecomp();
            }
        };

    public FlightplanLayer(
            FlightPlan flighPlan,
            Drone uav,
            IWWGlobes globes,
            IMapModel mapModel,
            IWWMapView mapView,
            IMapController mapController,
            ISelectionManager selectionManager,
            IElevationModel elevationModel,
            INavigationService navigationService,
            ILanguageHelper languageHelper,
            GeneralSettings generalSettings,
            FlightplanLayerVisibilitySettings flightplanLayerVisibilitySettings,
            Dispatcher dispatcher) {
        this.mapController = mapController;
        this.mapView = mapView;
        this.globe = globes.getDefaultGlobe();
        this.navigationService = navigationService;
        this.elevationModel = elevationModel;
        this.flighPlan = flighPlan;
        this.selectionManager = selectionManager;
        this.flightplanLayerVisibilitySettings = flightplanLayerVisibilitySettings;
        this.dispatcher = dispatcher;
        recomputeListener = (observable, oldValue, newValue) -> recomp.tryStartRecomp();
        flightplanLayerVisibilitySettings.aoiVisibleProperty().addListener(new WeakChangeListener<>(recomputeListener));
        flightplanLayerVisibilitySettings
            .coveragePreviewVisibleProperty()
            .addListener(new WeakChangeListener<>(recomputeListener));
        flightplanLayerVisibilitySettings
            .flightLineVisibleProperty()
            .addListener(new WeakChangeListener<>(recomputeListener));
        flightplanLayerVisibilitySettings
            .showCamPreviewProperty()
            .addListener(new WeakChangeListener<>(recomputeListener));
        flightplanLayerVisibilitySettings
            .startLandVisibleProperty()
            .addListener(new WeakChangeListener<>(recomputeListener));
        flightplanLayerVisibilitySettings
            .showVoxelsDilatedProperty()
            .addListener(new WeakChangeListener<>(recomputeListener));
        flightplanLayerVisibilitySettings.showVoxelsProperty().addListener(new WeakChangeListener<>(recomputeListener));
        flightplanLayerVisibilitySettings
            .waypointVisibleProperty()
            .addListener(new WeakChangeListener<>(recomputeListener));

        mouseModeChangeListener = (observable, oldValue, newValue) -> recomp.tryStartRecomp();
        mapController.mouseModeProperty().addListener(new WeakChangeListener<>(mouseModeChangeListener));
        currentSelectionChangedListener =
            (observable, oldValue, newValue) -> {
                recomp.tryStartRecomp();
            };
        selectionManager
            .currentSelectionProperty()
            .addListener(new WeakChangeListener<>(currentSelectionChangedListener));
        selectionManager.getHighlighted().addListener(new WeakChangeListener<>(currentSelectionChangedListener));
        flatEarthChangeListener = (observable, oldValue, newValue) -> recomp.tryStartRecomp();
        mapView.flatEarthProperty().addListener(new WeakChangeListener<>(flatEarthChangeListener));

        this.fp = flighPlan.getLegacyFlightplan();
        this.fp.addFPChangeListener(fpListener);
        setFont();

        aoiTooltipBuilder = new AoiTooltipBuilder(languageHelper);

        isOnAirFP = fp.isOnAirFlightplan();
        if (!isOnAirFP) {
            simPath = new SimulationResultPathLayer(fp, globes, mapView);
            preview = fp.getFPcoverage();
            preview.addRecomputeListener(this);
        }

        navigationService.workflowStepProperty().addListener(new WeakChangeListener<>(workflowStepListener));

        this.plane = uav.getLegacyPlane();
        dragger = mapModel.getDragManager();
        // System.out.println("the FP " + fp + " isSelectable " + isSelectable);
        // System.out.println("dragger" + dragger);

        markerLayer.setPickEnabled(false);
        markerLayer.setKeepSeparated(true);
        markerLayerAct.setPickEnabled(false);
        markerLayerAct.setKeepSeparated(true);

        annotationLayer.setName("AnnotationAOI");
        annotationLayer.setPickEnabled(false);
        annotationLayer.setValue(AVKey.IGNORE, true);
        annotationLayer.addAnnotation(balloon);
        annotationLayerAct.setName("AnnotationAOI");
        annotationLayerAct.setPickEnabled(false);
        annotationLayerAct.setValue(AVKey.IGNORE, true);
        annotationLayerAct.addAnnotation(balloonAct);

        textLayer.getTextRenderer().setAlwaysOnTop(true);
        textLayer.getTextRenderer().setEffect(AVKey.TEXT_EFFECT_NONE);
        textLayer.setPickEnabled(false);
        textLayerAct.getTextRenderer().setAlwaysOnTop(true);
        textLayerAct.getTextRenderer().setEffect(AVKey.TEXT_EFFECT_NONE);
        textLayerAct.setPickEnabled(false);

        forgroundLayer.setPickEnabled(true);
        forgroundLayerAct.setPickEnabled(true);

        iconLayer.setAlwaysUseAbsoluteElevation(true);
        iconLayerAct.setAlwaysUseAbsoluteElevation(true);

        iconLayerFoot.setAlwaysUseAbsoluteElevation(true);
        iconLayerFootAct.setAlwaysUseAbsoluteElevation(true);
        iconLayerFoot.setReferencePoint(IconRendererCentered.ReferencePoint.BOTTOM_CENTER);
        iconLayerFootAct.setReferencePoint(IconRendererCentered.ReferencePoint.BOTTOM_CENTER);

        waypointsLayer.setAlwaysUseAbsoluteElevation(true);
        waypointsLayerAct.setAlwaysUseAbsoluteElevation(true);

        lineLayer.setPickEnabled(false);
        lineLayerAct.setPickEnabled(false);

        plane.addListener(this);
        operationLevel.bind(generalSettings.operationLevelProperty());
        operationLevel.addListener((observable, oldValue, newValue) -> recomp.tryStartRecomp());
        if (!isConstructed) {
            recomp.tryStartRecomp();
        }
    }

    public FlightPlan getFlighPlan() {
        return flighPlan;
    }

    Color color;
    Color shadowColor;
    Double elevationStartPoint;

    BasicMarkerAttributes markerAttributes;

    private boolean isHalfVisible() {
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

    @Override
    public void mousePressed(MouseEvent mouseEvent) {}

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        if (currentMousePos == null) {
            return;
        }

        currentMousePos = null;
        recomp.tryStartRecomp();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {}

    Position currentMousePos;

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {}

    @Override
    public void moved(PositionEvent positionEvent) {
        if (selectedPicArea != null
                && !isOnAirFP
                && mapController.getMouseMode() == InputMode.ADD_POINTS
                && positionEvent != null) {
            Position tmp = positionEvent.getPosition();
            if (tmp == null) {
                return;
            }

            if (tmp != null && tmp.equals(currentMousePos)) {
                return;
            }

            currentMousePos = tmp;
            recomp.tryStartRecomp();
        }
    }

    private class FlighplanLayerGeneratorVisitor extends AFlightplanVisitor {

        String nextIconString = null;

        FpIcon lastIcon = null;
        FpIcon lastWaypointIcon = null;

        boolean waypointActive = false; // Application.getGuiLevel().compareTo(GuiLevels.EXPERT) >= 0;

        // private int curWaypointNo = 1;

        boolean showShadows = false; // !plane.getPlatformDescription().isInCopterMode() && !mapModel.isFlatEarthMode();

        boolean isCameraOnCompterMode = false;
        boolean firstWaypoint = true;

        double camTargetAlt;

        public FlighplanLayerGeneratorVisitor() {
            setSkipIgnoredPaths(true);
        }

        @Override
        public boolean visit(IFlightplanRelatedObject fpObj) {
            Position pos = null;
            IPlatformDescription platformDesc = fp.getHardwareConfiguration().getPlatformDescription();
            if (fpObj instanceof IReentryPoint && fpObj instanceof IFlightplanPositionReferenced) {
                IReentryPoint rp = (IReentryPoint)fpObj;
                int id = rp.getId();
                if (!ReentryPointID.isAutoPlanned(id)
                        || isCameraOnCompterMode
                        || !(ReentryPointID.isOnMainLine(id)
                            && ReentryPointID.getRefinementID(id) > 0
                            && ReentryPointID.getRefinementID(id) < ReentryPointID.maxNoRefinements - 1)) {
                    if (isHalfVisible()) {
                        return false;
                    }

                    pos = getPositionForWaypoint((IFlightplanPositionReferenced)fpObj);
                }
            }

            if (fpObj instanceof Waypoint) {
                if (isDragging || mapController.getMouseMode() == InputMode.ADD_POINTS) {
                    return false;
                }

                if (isHalfVisible()) {
                    return false;
                }

                Waypoint point = (Waypoint)fpObj;

                if (pos == null) {
                    pos = getPositionForWaypoint(point);
                }

                if (!point.isIgnore()) {
                    fpPos.add(pos); // make him not poping up in the flight path at this place
                }

                if (nextIconString == null) {
                    if (firstWaypoint) {
                        nextIconString = "com/intel/missioncontrol/gfx/waypoint-start.svg";
                        firstWaypoint = false;
                    } else {
                        nextIconString = "com/intel/missioncontrol/gfx/waypoint-normal.svg";
                    }
                }

                // curWaypointNo++;
                lastIcon = new FpIcon(nextIconString, pos, point, waypointActive, isCameraOnCompterMode);
                lastWaypointIcon = lastIcon;
                /*if ((point.isReentryPoint()) || (lastIcon.isCurrentReentry())) {
                    lastIcon.setSize(d64);
                    lastIcon.isSelected = true;
                    // OverlayText txt = new OverlayText(Integer.toString(curWaypointNo), pos);
                    // txt.setFont(f);
                    // txt.setColor(Color.WHITE);
                    // textLayer.add(txt);
                }*/

                var highlightedWaypoints = selectionManager.getHighlighted();

                boolean isSelectedThisPoint = false;

                try (var lockList = highlightedWaypoints.lock()) {
                    for (WayPoint wp : lockList) {
                        if (point == wp.getLegacyWaypoint()) {
                            isSelectedThisPoint = true;
                            break;
                        }
                    }
                }

                if (isSelectedThisPoint) {
                    lastIcon =
                        new FpIcon(
                            "com/intel/missioncontrol/gfx/waypoint-selected.svg",
                            pos,
                            point,
                            waypointActive,
                            isCameraOnCompterMode);
                    lastWaypointIcon = lastIcon;
                    lastIcon.setSize(d64);
                    lastIcon.isSelected = true;

                    OverlayText txt = new OverlayText(Integer.toString(point.getId() - 1), pos);
                    txt.setFont(f);
                    txt.setColor(WAYPOINT_NUMBER);
                    textLayer.add(txt);
                }

                if (flightplanLayerVisibilitySettings.isWaypointVisible()) {
                    waypointsLayer.addIcon(lastIcon);
                }
                // BasicMarker marker= new BasicMarker(pos,wpMarkerAttributes);
                // markers.add(marker);

                nextIconString = null;
                // System.out.println("point + "+point+" "+nextIconString+" "+isCameraOnCompterMode+ " "+camTargetAlt);
                if (isCameraOnCompterMode
                        && point.isTriggerImageHereCopterMode()
                        && flightplanLayerVisibilitySettings.isWaypointVisible()) {
                    // draw white line in camera pointing direction with length of picArea taget altitude
                    lineLayer.addRenderable(new FPcameraPointingLine(point, pos, camTargetAlt, isSelectedThisPoint));
                }

                if (isCameraOnCompterMode
                        && point.isTriggerImageHereCopterMode()
                        && flightplanLayerVisibilitySettings.isShowCamPreview()) {
                    CPhotoLogLine line =
                        new CPhotoLogLine(
                            pos.getLatitude().degrees,
                            pos.getLongitude().degrees,
                            pos.getAltitude(),
                            point.getOrientation());

                    final double projectionDistance = camTargetAlt <= 0 ? 5 : camTargetAlt / 4;
                    Matrix cameraTransform =
                        CameraHelper.getCorrectedStateTransform(line, fp.getHardwareConfiguration());

                    Vec4[] cornerDirections = CameraHelper.getCornerDirections(fp.getHardwareConfiguration());
                    Position[] idealCorners = new Position[4];
                    Vec4 origin = globe.computePointFromPosition(pos);
                    Matrix m = globe.computeModelCoordinateOriginTransform(pos);
                    for (int i = 0; i < 4; i++) {
                        cornerDirections[i] = cornerDirections[i].transformBy3(cameraTransform).transformBy3(m);
                        Vec4 p = origin.add3(cornerDirections[i].normalize3().multiply3(projectionDistance));
                        Position cornerPosition = globe.computePositionFromPoint(p);
                        idealCorners[i] = cornerPosition;
                    }

                    Vector<Position> mainLine = new Vector<>();
                    mainLine.add(pos);
                    mainLine.add(idealCorners[3]);
                    mainLine.add(pos);
                    mainLine.add(idealCorners[0]);
                    mainLine.add(pos);
                    mainLine.add(idealCorners[1]);
                    mainLine.add(pos);
                    mainLine.add(idealCorners[2]);
                    mainLine.add(idealCorners[1]);
                    mainLine.add(idealCorners[0]);
                    mainLine.add(idealCorners[3]);
                    PolylineWithUserData line1 = new PolylineWithUserData();
                    line1.setPositions(mainLine);
                    line1.setColor(CAMERA_SHAPRE_PREVIEW);
                    line1.setLineWidth(1);
                    lineLayer.addRenderable(line1);

                    mainLine = new Vector<>();
                    mainLine.add(idealCorners[2]);
                    mainLine.add(idealCorners[3]);
                    line1 = new PolylineWithUserData();
                    line1.setPositions(mainLine);
                    line1.setColor(CAMERA_SHAPRE_PREVIEW);
                    line1.setLineWidth(4);
                    lineLayer.addRenderable(line1);
                }

                if (point.isCirceling()) {
                    if (showShadows) {
                        lineLayer.addRenderable(new FpCircle(point, pos, point.getRadiusWithinM(), true));
                    }

                    lineLayer.addRenderable(new FpCircle(point, pos, point.getRadiusWithinM(), false));
                }

                if (point.getAssertAltitudeMode() == AltAssertModes.jump) {
                    // TODO turn radius can change in the config_variables received from the plane -- but no need to
                    // pass the plane anyway
                    double radius = platformDesc.getTurnRadius().convertTo(Unit.METER).getValue().doubleValue();
                    if (showShadows) {
                        lineLayer.addRenderable(new FpCircleAssertAlt(point, pos, radius, true));
                    }

                    lineLayer.addRenderable(new FpCircleAssertAlt(point, pos, radius, false));
                }
            } else if (fpObj instanceof Photo) {
                if (isDragging || mapController.getMouseMode() == InputMode.ADD_POINTS) {
                    return false;
                }

                if (isHalfVisible()) {
                    return false;
                }

                Photo photo = (Photo)fpObj;
                isCameraOnCompterMode = photo.isTriggerOnlyOnWaypoints() && photo.isPowerOn();
            } else if (fpObj instanceof WaypointLoop) {
                if (operationLevel.get() != OperationLevel.DEBUG) {
                    return false;
                }

                if (isDragging || mapController.getMouseMode() == InputMode.ADD_POINTS) {
                    return false;
                }

                if (isHalfVisible()) {
                    return false;
                }

                WaypointLoop loop = (WaypointLoop)fpObj;
                FirstWaypointVisitor visFirst = new FirstWaypointVisitor();
                visFirst.setSkipIgnoredPaths(true);
                visFirst.startVisit(loop);
                LastWaypointVisitor visLast = new LastWaypointVisitor();
                visLast.setSkipIgnoredPaths(true);
                visLast.startVisit(loop);
                if (visFirst.firstWaypoint != null
                        && visFirst.firstWaypoint != visLast.lastWaypoint
                        && visLast.lastWaypoint != null) {
                    ArrayList<Position> posLoop = new ArrayList<>();
                    posLoop.add(getPositionForWaypoint(visLast.lastWaypoint));
                    posLoop.add(getPositionForWaypoint(visFirst.firstWaypoint));

                    if (showShadows) {
                        lineLayer.addRenderable(new FpLineLoopClosing(fp, posLoop, true));
                    }

                    lineLayer.addRenderable(new FpLineLoopClosing(fp, posLoop, false));

                    makeMarkersFromPosList(posLoop);
                }

                nextIconString = "com/intel/missioncontrol/gfx/point-normal.svg";
                lastIcon = null;
            } else if (fpObj instanceof PicArea) {
                PicArea picArea = (PicArea)fpObj;
                camTargetAlt = picArea.getAlt();
                // System.out.println("render pic area");
                if (!flightplanLayerVisibilitySettings.isAoiVisible()) {
                    return false;
                }

                if (picArea.getPlanType() == PlanType.COPTER3D) {
                    VoxelGrid grid = picArea.voxelGridTmp;
                    if (grid != null
                            && (flightplanLayerVisibilitySettings.isShowVoxels()
                                || flightplanLayerVisibilitySettings.isShowVoxelsDilated())) {
                        int maxCoverage = Math.max(1, grid.getMaxCoverage());
                        Color[] colTable = new Color[maxCoverage + 1];
                        for (int coverage = 0; coverage <= maxCoverage; coverage++) {
                            float ok = ((float)Math.min(coverage, maxCoverage)) / maxCoverage;
                            Color col;
                            col = new Color(1f - ok, 0, ok, 1.f);
                            if (coverage == 0) col = Color.WHITE;
                            colTable[coverage] = col;
                        }

                        int[] covHistogram = new int[maxCoverage + 1];
                        grid.applyToAll(
                            (vec, voxel) -> {
                                // if (!voxel.coreSurface && !voxel.dilatedSurface) {
                                if (voxel.coreSurface && flightplanLayerVisibilitySettings.isShowVoxels()) {
                                    Position p = grid.trafo.transformToGlobe(vec);
                                    // System.out.println("vec:" + vec + " " + p + " core:" + voxel.coreSurface);

                                    Material mW = new Material(colTable[voxel.coverage]);
                                    BasicMarkerAttributes attr = new BasicMarkerAttributes();
                                    attr.setMaterial(mW);
                                    attr.setMinMarkerSize(grid.rasterSize / 2 * 0.95);
                                    attr.setMaxMarkerSize(grid.rasterSize / 2 * 0.95);
                                    attr.setOpacity(1);
                                    BasicMarker mark = new BasicMarker(p, attr);

                                    markers.add(mark);
                                }

                                if (voxel.dilatedSurface && flightplanLayerVisibilitySettings.isShowVoxelsDilated()) {
                                    Position p = grid.trafo.transformToGlobe(vec);
                                    // System.out.println("vec:" + vec + " " + p + " core:" + voxel.coreSurface);

                                    Material mW = new Material(Color.GREEN);
                                    BasicMarkerAttributes attr = new BasicMarkerAttributes();
                                    attr.setMaterial(mW);
                                    attr.setMinMarkerSize(grid.rasterSize / 2 * 0.95);
                                    attr.setMaxMarkerSize(grid.rasterSize / 2 * 0.95);
                                    attr.setOpacity(0.7);
                                    BasicMarker mark = new BasicMarker(p, attr);

                                    markers.add(mark);
                                }
                            });
                    }
                }

                boolean isSelected = picArea == selectedPicArea;
                boolean virtualAddingMouse =
                    currentMousePos != null
                        && isSelected
                        && !isOnAirFP
                        && mapController.getMouseMode() == InputMode.ADD_POINTS;

                noOfPointsToRender = picArea.getPlanType().getMinCorners() == 1 ? 1 : -1;

                int minCorners =
                    Math.min(
                        picArea.getPlanType().getMinCorners(),
                        2); // otherwise we wont see polygoes after 1st click as preview
                if (virtualAddingMouse) {
                    minCorners--;
                }

                // System.out.println("minCorners:"+minCorners +" "+ picArea.getCornersVec().size());
                Vector<LatLon> corVec = picArea.getCornersVec();
                if (corVec == null || corVec.size() < minCorners) {
                    return false;
                }

                // adding extension in case a user would click NOW an entire AOI
                Vector<LatLon> hull;
                if (virtualAddingMouse) {
                    hull = picArea.getHull(currentMousePos);
                } else {
                    hull = picArea.getHull();
                }

                if (hull == null) {
                    return false;
                }

                if (isSelected) {
                    annotationLayer.setEnabled(true);
                    String tooltipText = aoiTooltipBuilder.getTooltipText(picArea);
                    balloon.setText(tooltipText);

                    Flightplan flightplan = picArea.getFlightplan();
                    if (flightplan != null) {
                        LatLon latLonNorth = picArea.getNorthernmostVertexPosition();

                        double alt = flightplan.getRefPointAltWgs84WithElevation();

                        if (picArea.getPlanType().needsHeights()) {
                            alt += picArea.getObjectHeightRelativeToRefPoint();
                        } else {
                            if (flightplan.getPhotoSettings().getAltitudeAdjustMode()
                                    == AltitudeAdjustModes.CONSTANT_OVER_R) {
                                alt += picArea.getAlt();
                            } else {
                                if (latLonNorth != null) {
                                    alt = elevationModel.getElevationAsGoodAsPossible(latLonNorth) + picArea.getAlt();
                                }
                            }
                        }

                        if (latLonNorth != null) {
                            Position position = new Position(latLonNorth, alt);
                            balloon.setPosition(position);
                        }
                    }
                }

                if (!mapView.isFlatEarth()
                        && (picArea.getPlanType() == PlanType.TOWER
                            || picArea.getPlanType() == PlanType.WINDMILL
                            || picArea.getPlanType() == PlanType.BUILDING
                            || picArea.getPlanType() == PlanType.FACADE
                            || picArea.getPlanType() == PlanType.POINT_OF_INTEREST
                            || picArea.getPlanType() == PlanType.PANORAMA
                            || (picArea.getPlanType().isNoFlyZone() && picArea.isRestrictionCeilingEnabled()))) {

                    // Create and set an attribute bundle.
                    ShapeAttributes sideAttributes = new BasicShapeAttributes();
                    sideAttributes.setDrawInterior(true);
                    sideAttributes.setInteriorMaterial(
                        new Material(isSelected ? AOI_COLOR_HOVER_OR_SELECT : AOI_COLOR_NORMAL));
                    boolean isShowingAOIfilled = true;
                    sideAttributes.setInteriorOpacity(
                        picArea.getPlanType().isNoFlyZone() ? 0.1 : (isShowingAOIfilled ? 0.55 : 0));
                    sideAttributes.setDrawOutline(true);
                    sideAttributes.setOutlineMaterial(
                        new Material(isSelected ? AOI_COLOR_HOVER_OR_SELECT : AOI_COLOR_NORMAL));
                    sideAttributes.setOutlineOpacity(picArea.getPlanType().isNoFlyZone() ? 0.1 : 0.8);

                    // sideAttributes.setInteriorMaterial(Material.MAGENTA);
                    // sideAttributes.setOutlineOpacity(0.5);
                    // sideAttributes.setInteriorOpacity(0.5);
                    // sideAttributes.setOutlineMaterial(Material.GREEN);
                    // sideAttributes.setOutlineWidth(2);
                    sideAttributes.setDrawOutline(true);
                    sideAttributes.setDrawInterior(true);
                    sideAttributes.setEnableLighting(true);

                    ShapeAttributes sideHighlightAttributes = new BasicShapeAttributes(sideAttributes);
                    if (mapController.getMouseMode() == InputMode.DEFAULT) {
                        sideHighlightAttributes.setInteriorMaterial(new Material(AOI_COLOR_HOVER_OR_SELECT));
                        if (fpObj == selection) {
                            sideHighlightAttributes.setOutlineWidth(2);
                        } else {
                            sideHighlightAttributes.setOutlineWidth(1);
                        }

                        sideHighlightAttributes.setOutlineOpacity(1);
                    }

                    ShapeAttributes capAttributes = new BasicShapeAttributes(sideAttributes);

                    capAttributes.setInteriorMaterial(
                        new Material(isSelected ? AOI_COLOR_HOVER_OR_SELECT : AOI_COLOR_NORMAL));
                    capAttributes.setInteriorOpacity(picArea.getPlanType().isNoFlyZone() ? 0.1 : 0.8);
                    capAttributes.setDrawInterior(true);
                    capAttributes.setEnableLighting(true);

                    if (picArea.getPlanType().isCircular()) {
                        LatLon center;
                        if (picArea.getCorners().sizeOfFlightplanContainer() > 0) {
                            eu.mavinci.flightplan.Point first =
                                picArea.getCorners().getFirstElement(eu.mavinci.flightplan.Point.class);
                            center = first == null ? currentMousePos : first.getLatLon();
                        } else {
                            center = currentMousePos;
                        }

                        Ensure.notNull(center, "center");
                        double groundHeight = elevationModel.getElevationAsGoodAsPossible(center);

                        double topHeight;
                        if (picArea.getPlanType().hasWaypoints()) {
                            double alt = elevationStartPoint;
                            topHeight = alt + picArea.getObjectHeightRelativeToRefPoint();
                        } else {
                            topHeight =
                                picArea.getRestrictionHeightAboveWgs84(
                                    center, picArea.getRestrictionCeiling(), picArea.getRestrictionCeilingRef());
                        }

                        double centerHeight = (groundHeight + topHeight) * .5;
                        double radiusHeight = topHeight - groundHeight;
                        if (radiusHeight < 0.1) radiusHeight = 0.1;

                        double radius =
                            picArea.getPlanType() == PlanType.PANORAMA ? 1 : picArea.getCorridorWidthInMeter();

                        CylinderWithUserData area =
                            new CylinderWithUserData(new Position(center, centerHeight), radiusHeight, radius);
                        area.setSelectable(!isSelected && mapController.isSelecting());
                        area.setAltitudeMode(WorldWind.ABSOLUTE);
                        area.setAttributes(sideAttributes);
                        area.setHighlightAttributes(sideHighlightAttributes);
                        area.setUserData(picArea);
                        area.setHasTooltip(false);
                        forgroundLayer.addRenderable(area);

                        if (picArea.getPlanType() == PlanType.WINDMILL) {
                            // get WINDMILL parameters, fixed for now, need ui/etc to change
                            WindmillData windmill = picArea.windmill;

                            // TOWER already built, now do hub and blades
                            CylinderWithUserData hub =
                                new CylinderWithUserData(
                                    new Position(center, topHeight + windmill.hubRadius),
                                    windmill.hubRadius,
                                    windmill.hubHalfLength,
                                    windmill.hubRadius,
                                    Angle.fromDegrees(0),
                                    Angle.fromDegrees(90 - windmill.hubYaw),
                                    Angle.fromDegrees(90));

                            hub.setSelectable(!isSelected && mapController.getMouseMode().isSelecting());
                            hub.setAltitudeMode(WorldWind.ABSOLUTE);
                            hub.setAttributes(sideAttributes);
                            hub.setHighlightAttributes(sideHighlightAttributes);
                            hub.setUserData(picArea);
                            hub.setHasTooltip(false);
                            forgroundLayer.addRenderable(hub);

                            // now put up the blades
                            Vec4 centerHub = new Vec4(0, 0, 0);
                            Matrix hubTransform =
                                picArea.getBladeTransform(
                                    windmill.bladePitch,
                                    windmill.hubHalfLength - windmill.bladeRadius,
                                    windmill.hubYaw,
                                    topHeight + windmill.hubRadius,
                                    0);
                            centerHub = centerHub.transformBy4(hubTransform);

                            Vec4 centerVec = new Vec4(0, 0, windmill.hubRadius + windmill.bladeLength / 2);
                            double bladeRotationStep = 360 / windmill.numberOfBlades;
                            double bladeRotationDegs = windmill.bladeStartRotation;

                            for (int i = 0; i < windmill.numberOfBlades; i++) {
                                Matrix bladeTransform =
                                    picArea.getBladeTransform(
                                        windmill.bladePitch,
                                        windmill.hubHalfLength - windmill.bladeRadius,
                                        windmill.hubYaw,
                                        topHeight + windmill.hubRadius,
                                        bladeRotationDegs);
                                Vec4 centerBlade = centerVec.transformBy4(bladeTransform);

                                Vec4 centerDelta = (centerHub.subtract3(centerBlade)).normalize3();
                                // System.out.println("centerDelta:" + centerDelta);
                                Vec4 zaxis = new Vec4(0, 0, 1);
                                Vec4 axis = (centerDelta.cross3(zaxis)).normalize3();
                                Angle rotation = centerDelta.angleBetween3(zaxis);
                                // System.out.println("axis:" + axis + " angle:" + rotation.getDegrees());
                                Quaternion quat = Quaternion.fromAxisAngle(rotation, axis);
                                // System.out.println("Quaternion:" + quat);

                                double a =
                                    Math.atan2(
                                        (2 * (quat.w * quat.z + quat.x * quat.y)),
                                        (1 - 2 * (quat.y * quat.y + quat.z * quat.z)));
                                double p = -Math.asin(2 * (quat.w * quat.y - quat.z * quat.x));
                                double r =
                                    Math.atan2(
                                        (2 * (quat.w * quat.x + quat.y * quat.z)),
                                        (1 - 2 * (quat.x * quat.x + quat.y * quat.y)));

                                Angle azimuth =
                                    Angle.fromRadians(
                                        a
                                            + Math.PI
                                                * (((a == 0) ? -1 : 1) * windmill.bladePitch - windmill.hubYaw)
                                                / 180);
                                Angle tilt = Angle.fromRadians(p);
                                Angle roll = Angle.fromRadians(r);
                                // System.out.println("az:" + azimuth.getDegrees() + " tilt:" + tilt.getDegrees() + "
                                // roll:" + roll.getDegrees());

                                if (!picArea.canTransform()) {
                                    continue;
                                }

                                Position centerPos = picArea.transformToGlobe(centerBlade);
                                CylinderWithUserData blade =
                                    new CylinderWithUserData(
                                        centerPos,
                                        windmill.bladeRadius,
                                        windmill.bladeLength / 2,
                                        windmill.bladeThinRadius,
                                        azimuth,
                                        tilt,
                                        roll);

                                bladeRotationDegs += bladeRotationStep;

                                blade.setSelectable(!isSelected && mapController.getMouseMode().isSelecting());
                                blade.setAltitudeMode(WorldWind.ABSOLUTE);
                                blade.setAttributes(sideAttributes);
                                blade.setHighlightAttributes(sideHighlightAttributes);
                                blade.setUserData(picArea);
                                blade.setHasTooltip(false);
                                forgroundLayer.addRenderable(blade);
                            }
                        }
                    } else { // building or facade
                        if (hull.size() < 3) { // 3d share with only one or two points will throw exceptions
                            return false;
                        }

                        // Collections.reverse(hull);
                        double groundHeight = elevationModel.getElevationAsGoodAsPossible(hull.firstElement());

                        double topHeight;
                        if (picArea.getPlanType().hasWaypoints()) {
                            double alt = elevationStartPoint;
                            topHeight = alt + picArea.getObjectHeightRelativeToRefPoint();
                        } else {
                            topHeight =
                                picArea.getRestrictionHeightAboveWgs84(
                                    picArea.getCenter(),
                                    picArea.getRestrictionCeiling(),
                                    picArea.getRestrictionCeilingRef());
                        }

                        double heightOverGround = topHeight - groundHeight;
                        ExtrudedPolygonWithUserData area = new ExtrudedPolygonWithUserData(hull, heightOverGround);
                        // area.setOuterBoundary(hull);
                        area.setUserData(picArea);
                        area.setHasTooltip(false);
                        area.setSelectable(!isSelected && mapController.getMouseMode() == InputMode.DEFAULT);
                        area.setAltitudeMode(WorldWind.CONSTANT);
                        area.setBaseDepth(5000);
                        // area.setAltitudeMode(WorldWind.ABSOLUTE);
                        // area.setAttributes(sideAttributes);
                        // area.setHighlightAttributes(sideHighlightAttributes);
                        area.setSideAttributes(sideAttributes);
                        area.setSideHighlightAttributes(sideHighlightAttributes);
                        area.setCapAttributes(sideAttributes);
                        area.setCapHighlightAttributes(sideHighlightAttributes);
                        forgroundLayer.addRenderable(area);
                    }
                } else {
                    SurfacePolygonWithUserData area = new SurfacePolygonWithUserData(hull);
                    area.setUserData(picArea);
                    area.setSelectable(!isSelected && mapController.isSelecting());
                    area.setDraggable(false);
                    area.setPopupTriggering(false);
                    area.setHasTooltip(false);
                    area.setPathType(AVKey.LINEAR);
                    ShapeAttributes atr = new BasicShapeAttributes();
                    atr.setDrawInterior(true);
                    atr.setInteriorMaterial(new Material(isSelected ? AOI_COLOR_HOVER_OR_SELECT : AOI_COLOR_NORMAL));
                    boolean isShowingAOIfilled = true;
                    atr.setInteriorOpacity(isShowingAOIfilled ? .5 : 0);
                    atr.setDrawOutline(true);
                    atr.setOutlineMaterial(new Material(isSelected ? AOI_COLOR_HOVER_OR_SELECT : AOI_COLOR_NORMAL));
                    // TODO getUIScale
                    // atr.setOutlineWidth(languageProvider.getUIScale() * 2);

                    ShapeAttributes atrHighlight = atr.copy();
                    atrHighlight.setInteriorMaterial(new Material(AOI_COLOR_HOVER_OR_SELECT));
                    atrHighlight.setOutlineMaterial(new Material(AOI_COLOR_HOVER_OR_SELECT));

                    if (picArea.getPlanType().isGeofence()) {
                        allGeofences.add(hull);
                        atr.setDrawInterior(isSelected);
                    }

                    area.setAttributes(atr);
                    area.setHighlightAttributes(atrHighlight);
                    forgroundLayer.addRenderable(area);
                }

                // adding virtual center vertex for dragging an entire AOI
                if (corVec != null
                        && corVec.size() > 1
                        && isSelected
                        && !isOnAirFP
                        && !picArea.getPlanType().isCircular()) {
                    LatLon posCenter = picArea.getCenterShifted();
                    if (posCenter != null) {
                        iconLayer.addIcon(new FpMoveIcon(picArea, posCenter));
                    }

                    if (selectionManager.getSelection() == picArea) {
                        LinkedList<eu.mavinci.flightplan.Point> points = new LinkedList<>();
                        for (IFlightplanStatement p : picArea.getCorners()) {
                            points.add((eu.mavinci.flightplan.Point)p);
                        }

                        if (picArea.getPlanType().isClosedPolygone()) {
                            points.add(points.getFirst());
                        }

                        // adding virtual vertices between vertices of the corners for ease redining lines.
                        eu.mavinci.flightplan.Point lastPoint = null;
                        LatLon lastLatLon = null;
                        for (eu.mavinci.flightplan.Point nextPoint : points) {
                            LatLon nextLatLon = nextPoint.getLatLon();
                            if (lastPoint != null && lastLatLon != null) {
                                LatLon center = LatLon.interpolate(0.5, lastLatLon, nextLatLon);
                                FpSelectableIcon icon =
                                    new FpSelectableIcon(
                                        "com/intel/missioncontrol/gfx/point-virtual.svg",
                                        new Position(center, -10000),
                                        new PhantomCorner(lastPoint, center),
                                        true,
                                        false);
                                icon.setSize(d64);
                                iconLayer.addIcon(icon);
                            }

                            lastPoint = nextPoint;
                            lastLatLon = nextLatLon;
                        }
                    }
                }
            } else if (fpObj instanceof LandingPoint) {
                if (isDragging || mapController.getMouseMode() == InputMode.ADD_POINTS) {
                    return false;
                }

                if (isHalfVisible()) {
                    iconLayer.removeAllIcons();
                    iconLayerFoot.removeAllIcons();
                    waypointsLayer.removeAllIcons();
                    return false;
                }

                if (!flightplanLayerVisibilitySettings.isStartLandVisible()) {
                    return false;
                }

                LandingPoint landPoint = (LandingPoint)fpObj;
                Ensure.notNull(landPoint, "landpoint");
                Position posLanding =
                    new Position(landPoint.getLatLon(), elevationStartPoint + landPoint.getAltInMAboveFPRefPoint());

                if (landPoint.isLandAutomatically()) {
                    double terrainLand = elevationModel.getElevationAsGoodAsPossible(posLanding);
                    posLanding = new Position(posLanding, terrainLand);
                }

                iconLayer.addIcon(renderLandingIcon(posLanding, landPoint));
                fpPos.add(posLanding);
            } else if (fpObj instanceof Takeoff) {
                if (mapController.getMouseMode() == InputMode.ADD_POINTS) {
                    return false;
                }

                Takeoff origin = ((Takeoff)fpObj);
                // System.out.println("render origin: "+origin);
                // System.out.println("fp:"+fp.getRefPointAltWgs84WithElevation());
                // by now do nothing
                if (origin.isDefined()) {
                    LatLon latLonOrigin = origin.getLatLon();
                    double terrainOrigin = elevationModel.getElevationAsGoodAsPossible(latLonOrigin);
                    Position posOrigin =
                        new Position(
                            latLonOrigin,
                            Math.max(origin.getElevation(), IElevationModel.MIN_LEVEL_OVER_GROUND) + terrainOrigin);
                    // fpPos.add(posOrigin); // make him not poping up in the flight path at this place

                    if (flightplanLayerVisibilitySettings.isStartLandVisible()) {
                        FpSelectableIcon icon =
                            new FpSelectableIcon(
                                "com/intel/missioncontrol/gfx/map_takeoff.svg", posOrigin, origin, true, false);
                        icon.setSize(FpSelectableIcon.d32);
                        icon.setAlwaysOnTop(true);
                        iconLayerFoot.addIcon(icon);
                    }
                }
            } else if (fpObj instanceof ReferencePoint) {
                if (mapController.getMouseMode() == InputMode.ADD_POINTS) {
                    return false;
                }

                ReferencePoint origin = ((ReferencePoint)fpObj);
                // System.out.println("render origin: "+origin);
                // System.out.println("fp:"+fp.getRefPointAltWgs84WithElevation());
                // by now do nothing
                if (origin.isDefined() && !origin.isAuto()) {
                    LatLon latLonOrigin = origin.getLatLon();
                    double terrainOrigin = elevationModel.getElevationAsGoodAsPossible(latLonOrigin);
                    Position posOrigin =
                        new Position(
                            latLonOrigin,
                            Math.max(origin.getElevation(), IElevationModel.MIN_LEVEL_OVER_GROUND) + terrainOrigin);
                    // fpPos.add(posOrigin); // make him not poping up in the flight path at this place

                    FpSelectableIcon icon =
                        new FpSelectableIcon(
                            "com/intel/missioncontrol/gfx/map_reference-point.svg", posOrigin, origin, true, false);
                    icon.setSize(FpSelectableIcon.d32);
                    icon.setAlwaysOnTop(true);
                    iconLayerFoot.addIcon(icon);
                }
            } else if (fpObj instanceof eu.mavinci.flightplan.Point) {
                // this are the corners of the AOI
                if (!flightplanLayerVisibilitySettings.isAoiVisible()) {
                    return false;
                }
                // System.out.println("selected Pic:" + selectedPicArea + " "+fpObj);
                if (selectedPicArea != fpObj.getParent().getParent()) {
                    return false;
                }

                if (noOfPointsToRender == 0) {
                    return false;
                }

                noOfPointsToRender--;

                // System.out.println("do the icon");
                eu.mavinci.flightplan.Point point = (eu.mavinci.flightplan.Point)fpObj;
                Position posCorner = new Position(point.getLatLon(), -10000);
                iconLayer.addIcon(new FpIconCorner(posCorner, point, true));
            } else {
                // ignore.. processed maybe indirectly
            }

            return false;
        }

        private WWIcon renderLandingIcon(Position posLanding, LandingPoint landPoint) {
            FpIcon icon =
                new FpIcon("com/intel/missioncontrol/gfx/map_landingspot.svg", posLanding, landPoint, false, false);
            icon.setSize(FpSelectableIcon.d32);
            icon.setAlwaysOnTop(true);
            return icon;
        }

    }

    private boolean isDragging = false;
    private ArrayList<Position> fpPos = new ArrayList<>();

    private boolean isConstructed = false;

    // boolean isSelected = false;

    private LocalTransformationProvider trafo;
    private PicArea selectedPicArea;
    private Object selection;
    private int noOfPointsToRender;

    private synchronized void reconstructLayer() {
        // System.out.println("A###### " +id + " recompute #################################################");
        // if (preview ==null) preview = new FPcoveragePreview(fp,plane);
        // long time= System.currentTimeMillis();
        // preview.recompute();
        // System.out.println("A###### " +id + " recomp-done " + (System.currentTimeMillis() - time) + "
        // #################################################");
        selection = selectionManager.getSelection();
        isConstructed = true;
        // isNotReachedJet= false;
        isOnAirFP = fp.isOnAirFlightplan();

        isDragable = !isOnAirFP && navigationService.workflowStepProperty().get() == WorkflowStep.PLANNING;

        selectedPicArea = null;
        if (!isOnAirFP) {
            // System.out.println("selec:"+selection);
            if (selection instanceof PicAreaCorners) {
                selectedPicArea = (PicArea)((PicAreaCorners)selection).getParent();
            } else if (selection instanceof PicArea) {
                selectedPicArea = (PicArea)selection;
            }

            if (selectedPicArea == null && selection instanceof eu.mavinci.flightplan.Point) {
                eu.mavinci.flightplan.Point selPoint = (eu.mavinci.flightplan.Point)selection;
                if (selPoint.getParent().getParent() instanceof PicArea) {
                    selectedPicArea = (PicArea)selPoint.getParent().getParent();
                }
            }

            if (selectedPicArea == null && selection instanceof eu.mavinci.flightplan.Waypoint) {
                eu.mavinci.flightplan.Waypoint selPoint = (eu.mavinci.flightplan.Waypoint)selection;
                if (selPoint.getParent() instanceof PicArea) {
                    selectedPicArea = (PicArea)selPoint.getParent();
                }
            }
        }
        // iconLayer.setPickEnabled(isSelectable);
        lineLayer.setPickEnabled(mapController.isSelecting() && !isOnAirFP);
        forgroundLayer.setPickEnabled(mapController.isSelecting() && !isOnAirFP);

        // System.out.println("FP Layer "+ controller.getMouseMode());
        // System.out.println("pickEnabled" + lineLayer.isPickEnabled());
        elevationStartPoint = fp.getRefPointAltWgs84WithElevation();

        // isSelected = false;
        // if (selection instanceof IFlightplanRelatedObject) {
        // IFlightplanRelatedObject fpObj = (IFlightplanRelatedObject) selection;
        // if (fpObj.getFlightplan()==fp) {
        // isSelected=true;
        //// } else if (fpObj instanceof CCell) {
        //// CCell cell = (CCell) fpObj;
        //// if (fp == cell.getChildFlightplan()) isSelected=true;
        // }
        // }

        // adjusting layer visibilety, or if not exist, create new layers for
        // this FP

        fpPos.clear();

        // Origin origin = ((Origin)fp.getOrigin());
        // System.out.println("render origin: "+origin);
        // System.out.println("fp:"+fp.getRefPointAltWgs84WithElevation());
        // by now do nothing
        // add this code, to render a line from starting position to 1.st waypoint
        /*if (origin.isDefined()) {
            Position posOrigin =
                EarthElevationModel.setOverGround(LatLon.fromDegrees(origin.getLat(), origin.getLon()));
            fpPos.add(posOrigin); // make him not poping up in the flight path at this place
        }*/

        lineLayer.removeAllRenderables();
        markers.removeAllElements();
        forgroundLayer.removeAllRenderables();

        color = fp.isOnAirFlightplan() ? AOI_COLOR_ON_AIR : AOI_COLOR_NORMAL; // fpLayer.getColor();
        // System.out.println("colorOfFP" + fp + " is" + color + " alpha:" + color.getAlpha());
        shadowColor = ColorHelper.scaleAlphaToShadow(color);

        Material material = new Material(FLIGHT_LINE_COLOR);
        // wpMarkerAttributes = new BasicMarkerAttributes();
        // wpMarkerAttributes.setMaterial(material);

        markerAttributes = new BasicMarkerAttributes(material, BasicMarkerShape.HEADING_ARROW, 1d, 4, 0);
        markerAttributes.setHeadingMaterial(material);
        markerAttributes.setHeadingScale(2);

        // isDraggingIcons = false;
        IFlightplanRelatedObject draggedObject = null;
        Position dragPosition = null;
        isDragging = false;
        if (dragger.isDraggingProperty().get()) {
            Object userData = dragger.getDragObjectUserData();
            if (userData != null) {
                if (userData instanceof IFlightplanRelatedObject) {
                    draggedObject = (IFlightplanRelatedObject)userData;
                    if (draggedObject.getFlightplan() == fp) {
                        dragPosition = dragger.getLastDraggingPosition();
                        // System.out.println("dragPos="+dragPosition);
                        isDragging = true;
                    }
                }
            }
        }

        // in dragging mode do NOT touch the icon layer, because dragging will
        // fail otherwise!
        // if (!isDraggingIcons)
        iconLayer.removeAllIcons();
        iconLayerFoot.removeAllIcons();
        waypointsLayer.removeAllIcons();

        textLayer.clear();
        annotationLayer.setEnabled(false);
        posStart = null;
        allGeofences.clear();

        if (!isOnAirFP || !fp.isEmpty()) { // dont draw empty on air FP
            trafo =
                new LocalTransformationProvider(new Position(fp.getRefPoint().getLatLon(), 0), Angle.ZERO, 0, 0, true);

            FlighplanLayerGeneratorVisitor visitor = new FlighplanLayerGeneratorVisitor();
            visitor.startVisit(fp);
            if (visitor.lastWaypointIcon != null && !visitor.lastWaypointIcon.isSelected) {
                visitor.lastWaypointIcon.setImageSource(
                    changeIconBrightness(
                        "com/intel/missioncontrol/gfx/waypoint-stop.svg", visitor.lastWaypointIcon.getUserData()));
            }

            if (flightplanLayerVisibilitySettings.isFlightLineVisible()) {
                if (visitor.showShadows) {
                    lineLayer.addRenderable(new FpLine(fp, fpPos, true));
                }

                lineLayer.addRenderable(new FpLine(fp, fpPos, false));
                makeMarkersFromPosList(fpPos);
            }

            if (!allGeofences.isEmpty()) {
                // just substracting the shares from a whole sphere is way too slow in rendering,
                // so we are first picking a smaller sector. this sector cant be right around the no fly zones, since
                // otherwise we will end
                // up with a not at all rendered sector
                // additionally we have to compute outer sectors to cover the remaining globe
                // especially on Longitude they cant span more then 180 deg, since otherwise the wrong part of the earth
                // got covered, so we need quite some logic to compute a good devision to cover the remaining glove
                Sector allSector = fp.getSector();
                LatLon center = allSector.getCentroid();
                Angle dLat = allSector.getDeltaLat().multiply(2);
                Angle dLon = allSector.getDeltaLon().multiply(2);
                if (dLat.compareTo(dLon) > 0) {
                    dLon = dLat;
                } else {
                    dLat = dLon;
                }

                allSector =
                    new Sector(
                        center.getLatitude().subtract(dLat),
                        center.getLatitude().add(dLat),
                        center.getLongitude().subtract(dLon),
                        center.getLongitude().add(dLon));

                SurfaceSector surfaceSector = new SurfaceSector(allSector);

                Combinable[] allShapes = new Combinable[allGeofences.size() + 1];
                allShapes[0] = surfaceSector;
                int i = 1;
                for (Vector<LatLon> fence : allGeofences) {
                    allShapes[i] = new SurfacePolygon(fence);
                    i++;
                }

                double resolutionRadians = 1 / globe.getRadius(); // 1m resolution
                ShapeCombiner combiner = new ShapeCombiner(globe, resolutionRadians);
                ContourList contours = combiner.difference(allShapes);
                ShapeAttributes attrs = new BasicShapeAttributes();
                attrs.setInteriorMaterial(Material.RED);
                attrs.setInteriorOpacity(0.35);
                attrs.setOutlineMaterial(Material.RED);
                // TODO getUIScale
                // attrs.setOutlineWidth(languageProvider.getUIScale() * 2);
                attrs.setDrawOutline(false);

                SurfaceMultiPolygon shape = new SurfaceMultiPolygon(attrs, contours);
                shape.setHighlightAttributes(attrs);
                shape.setPathType(AVKey.LINEAR);
                forgroundLayer.addRenderable(shape);

                ArrayList<Sector> souroundingSectors = new ArrayList<>();
                souroundingSectors.add(new Sector(allSector.getMaxLatitude(), Angle.POS90, Angle.NEG180, Angle.POS180));
                souroundingSectors.add(new Sector(Angle.NEG90, allSector.getMinLatitude(), Angle.NEG180, Angle.POS180));

                ArrayList<Angle> longitudesMin = new ArrayList<>();
                longitudesMin.add(Angle.NEG180);
                longitudesMin.add(allSector.getMinLongitude());

                ArrayList<Angle> longitudesMax = new ArrayList<>();
                longitudesMax.add(allSector.getMaxLongitude());
                longitudesMax.add(Angle.POS180);
                for (ArrayList<Angle> longitudes : new ArrayList[] {longitudesMin, longitudesMax}) {
                    Angle last = longitudes.get(0);
                    Angle dAngle = longitudes.get(1).subtract(last);
                    if (dAngle.compareTo(Angle.POS180) >= 0) {
                        longitudes.add(1, last.add(dAngle.divide(2)));
                    }

                    for (int k = 1; k < longitudes.size(); k++) {
                        Angle next = longitudes.get(k);
                        souroundingSectors.add(
                            new Sector(allSector.getMinLatitude(), allSector.getMaxLatitude(), last, next));
                        last = next;
                    }
                }

                for (Sector sec : souroundingSectors) {
                    SurfaceSector boundingSurface = new SurfaceSector(sec);
                    boundingSurface.setPathType(AVKey.LINEAR);
                    boundingSurface.setAttributes(attrs);
                    boundingSurface.setHighlightAttributes(attrs);
                    forgroundLayer.addRenderable(boundingSurface);
                }
            }
        }

        // System.out.println("fp" + fp.size());

        // if we are in dragging mode, set info texts
        if (isDragging && (draggedObject instanceof LandingPoint || draggedObject instanceof Waypoint)) {
            String strTotal = "    total:     " + StringHelper.lengthToIngName(fp.getLengthInMeter(), -3, false);
            // System.out.println("strTotal: " +strTotal);
            // System.out.println("finalDragPos"+dragPosition);
            textLayer.add(new OverlayText(strTotal, elevationModel.getPositionOverGround(dragPosition)));

            if (draggedObject instanceof Waypoint) {
                Waypoint cur = (Waypoint)draggedObject;

                Waypoint prev = (Waypoint)fp.getPreviousWaypoint(cur);
                if (prev != null) {
                    textLayer.add(getOverlayText(cur, dragPosition, prev));
                } else {
                    // take startcircle if one exist
                    for (IFlightplanStatement tmpStatement : fp) {
                        if (tmpStatement == cur) {
                            break;
                        }
                    }
                }

                Waypoint next = (Waypoint)fp.getNextWaypoint(cur);
                if (next != null) {
                    textLayer.add(getOverlayText(cur, dragPosition, next));
                } else {
                    // take landingpoint
                    textLayer.add(getOverlayText(cur, dragPosition, fp.getLandingpoint()));
                }

                IFlightplanContainer fpCont = cur.getParent();
                if (fpCont instanceof WaypointLoop) {
                    WaypointLoop loop = (WaypointLoop)fpCont;
                    FirstWaypointVisitor visFirst = new FirstWaypointVisitor();
                    visFirst.startVisit(loop);
                    LastWaypointVisitor visLast = new LastWaypointVisitor();
                    visLast.startVisit(loop);

                    Waypoint first = (Waypoint)visFirst.firstWaypoint;
                    Waypoint last = (Waypoint)visLast.lastWaypoint;
                    Ensure.notNull(first, "first");
                    Ensure.notNull(last, "last");
                    if (last != first) {
                        if (cur == last) {
                            textLayer.add(getOverlayText(cur, dragPosition, first));
                        } else if (cur == first) {
                            textLayer.add(getOverlayText(cur, dragPosition, last));
                        }
                    }
                }
            }
        }
        // System.out.println("flightplan layer reconstructed" + fp.getInternalID());
        // (new Exception(""+fp.getInternalID())).printStackTrace();
        firePropertyChange(AVKey.LAYER, null, this);
    }

    protected void makeMarkersFromPosList(ArrayList<Position> posList) {
        if (posList.size() == 0) {
            return;
        }

        for (int i = 1; i != posList.size(); i++) {
            Position pos1 = posList.get(i - 1);
            Position pos2 = posList.get(i);
            if (pos1.getLatitude().equals(pos2.getLatitude()) && pos1.getLongitude().equals(pos2.getLongitude())) {
                continue;
            }

            Position pos = Position.interpolate(0.25, pos1, pos2);
            Angle heading = LatLon.greatCircleAzimuth(pos1, pos2);
            Marker marker = new BasicMarker(pos, markerAttributes, heading);
            markers.add(marker);
        }
    }

    public OverlayText getOverlayText(Waypoint cur, Position posCur, IFlightplanPositionReferenced other) {
        double dist = Flightplan.getDistanceInMeter(cur, other);
        Position p = getPositionForWaypoint(other);
        return new OverlayText(
            StringHelper.lengthToIngName(dist, -3, false),
            elevationModel.getPositionOverGround(Position.interpolate(0.5, posCur, p)));
    }

    Font f;

    public Position fixTextElevation(Position p) {
        if (p.elevation != 0 && mapView.isFlatEarth()) {
            return new Position(p, -10000);
        }

        return elevationModel.renormPosition(p);
    }

    public class OverlayText extends UserFacingText {
        public OverlayText(CharSequence text, Position textPosition) {
            super(text, fixTextElevation(textPosition));
            setPriority(1e16);
            setFont(f);
            setBackgroundColor(Color.BLACK);
        }

        @Override
        public void setPosition(Position position) {
            super.setPosition(fixTextElevation(position));
        }

    }

    private Position getPositionForWaypoint(IFlightplanPositionReferenced wp) {
        return Position.fromDegrees(wp.getLat(), wp.getLon(), elevationStartPoint + wp.getAltInMAboveFPRefPoint());
    }

    public BufferedImage changeIconBrightness(String resourcePath, Object o) {
        BufferedImage image = Application.getBufferedImageFromResource(resourcePath);
        // System.out.println("" + isOnAirRelated + " " + waypointIsNotReachedJet + " "+ resourcePath);
        if (!isOnAirRelated) {
            return image;
        }

        if (o instanceof IReentryPoint) { // wenn überhaupt nur reentrypoints grauen, und nicht z.b ecken der picArea!!
            IFlightplanRelatedObject fpObj = (IFlightplanRelatedObject)o;
            Boolean b = plane.getAirplaneCache().isFPelementNotReachedJet(fpObj);
            if (b != null && b) {
                return image;
            }
        } else {
            return image;
        }
        // if (isNotReachedJet) return image;

        // System.out.println("->scale");
        return Application.getBufferedImageBrighter(image);
    }

    // boolean isNotReachedJet = false;

    private class FpIcon extends UserFacingIconWithUserData {

        boolean isActive;
        boolean isCurrentReentry;
        boolean isSelected;

        public FpIcon(
                String iconPath, Position pos, Object userDataObject, boolean isActive, boolean isCameraOnCompterMode) {
            super(changeIconBrightness(iconPath, userDataObject), pos, userDataObject);
            this.isActive = isActive;
            setHighlightScale(FlightplanLayer.HIGHLIGHT_SCALE);
            // setToolTipText(userDataObject.toString()); //this consumes A LOT of time, dont do it, just request it if
            // its needed
            setToolTipTextColor(java.awt.Color.YELLOW);
            setHasTooltip(false);
            setSize(d16);
        }

        @Override
        public void setImageFromResource(String resourcePath) {
            if (isCurrentReentry) return;
            super.setImageSource(changeIconBrightness(resourcePath, getUserData()));
        }

        @Override
        public boolean isDraggable() {
            return isActive;
        }

        @Override
        public boolean isSelectable() {
            return FlightplanLayer.this.isDragable && isActive;
        }

        @Override
        public boolean isPopupTriggering() {
            return false;
        }

        @Override
        public String getToolTipText() {
            if (getToolTipText() == null) {
                setToolTipText(getUserData().toString());
            }

            return super.getToolTipText();
        }

        public boolean isCurrentReentry() {
            return isCurrentReentry;
        }
    }

    private class FpMoveIcon extends FpSelectableIcon {

        public FpMoveIcon(PicArea picArea, LatLon pos) {
            super("com/intel/missioncontrol/gfx/point-move.svg", new Position(pos, -10000), picArea, true, false);
            setSize(d32);
        }
    }

    private class FpSelectableIcon extends FpIcon {

        public FpSelectableIcon(
                String iconPath, Position pos, Object userDataObject, boolean isActive, boolean isCameraOnCompterMode) {
            super(iconPath, pos, userDataObject, isActive, isCameraOnCompterMode);
        }

        @Override
        public boolean isSelectable() {
            return isDragable && isActive && (mapController.isSelecting());
        }
    }

    private class FpIconCorner extends UserFacingIconWithUserData {

        boolean isActive;
        boolean isCurrentReentry = false;

        public FpIconCorner(Position pos, eu.mavinci.flightplan.Point userDataObject, boolean isActive) {
            super(
                (userDataObject == selection
                    ? Application.getBufferedImageFromResource("com/intel/missioncontrol/gfx/point-selected.svg")
                    : Application.getBufferedImageFromResource("com/intel/missioncontrol/gfx/point-normal.svg")),
                pos,
                userDataObject);
            this.isActive = isActive;
            setHighlightScale(FlightplanLayer.HIGHLIGHT_SCALE);
            // setToolTipText(userDataObject.toString()); //this consumes A LOT of time, dont do it, just request it if
            // its needed
            setToolTipTextColor(java.awt.Color.YELLOW);
            setHasTooltip(false);
            setSize(d64);
        }

        @Override
        public void setImageFromResource(String resourcePath) {
            if (isCurrentReentry) {
                return;
            }

            super.setImageSource(changeIconBrightness(resourcePath, getUserData()));
        }

        @Override
        public boolean isDraggable() {
            return isActive;
        }

        @Override
        public boolean isSelectable() {
            return isDragable && isActive && (mapController.isSelecting());
        }

        @Override
        public boolean isPopupTriggering() {
            return false;
        }

        @Override
        public String getToolTipText() {
            if (getToolTipText() == null) {
                setToolTipText(getUserData().toString());
            }

            return super.getToolTipText();
        }

        @Override
        public boolean isSelectableWhileAddNewPoints() {
            return super.isSelectableWhileAddNewPoints();
        }
    }

    private class FPcameraPointingLine extends PolylineWithUserData {
        public FPcameraPointingLine(Waypoint point, Position pos, double camTargetAlt, boolean isSelectedThisPoint) {
            setUserData(point);
            setFollowTerrain(false);
            setPathType(AVKey.LINEAR);
            setLineWidth(isSelectedThisPoint ? 5 : 1);
            setColor(ColorHelper.setAlpha(CAMERA_POINTING_LINE, 255));
            // System.out.println(fp + " getColorFrom Line" + getColor() + " alpha " + getColor().getAlpha());
            LinkedList<Position> posList = new LinkedList<>();
            posList.add(pos);
            if (point.getTargetDistance() >= 0) {
                camTargetAlt = point.getTargetDistance();
            }

            Vec4 direction = new Vec4(0, 0, -camTargetAlt);

            Orientation o = point.getOrientation();
            double roll = o.isRollDefined() ? o.getRoll() : 0;
            double pitch = o.isPitchDefined() ? o.getPitch() : 0;
            double yaw = o.isYawDefined() ? o.getYaw() : 0;
            Matrix m = MathHelper.getRollPitchYawTransformationMAVinicAngles(roll, pitch, yaw);
            direction = direction.transformBy4(m.getInverse());
            LocalTransformationProvider localTransformation =
                new LocalTransformationProvider(pos, Angle.ZERO, 0, 0, true);

            Position pos2 = localTransformation.transformToGlobe(direction);
            posList.add(pos2);
            setPositions(posList);
            setHasTooltip(false);
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
            return false;
        }
    }

    private class FpLine extends PolylineWithUserData {
        public FpLine(Object userData, Iterable<? extends Position> pos, boolean isShadow) {
            setUserData(userData);
            setFollowTerrain(isShadow);
            setPathType(AVKey.LINEAR);
            setHasTooltip(false);
            int baseWidth =
                fp.getHardwareConfiguration().getPlatformDescription().planIndividualImagePositions() ? 2 : 4;
            if (userData == selection) {
                setLineWidth(baseWidth * FlightplanLayer.HIGHLIGHT_SCALE);
            } else {
                setLineWidth(baseWidth);
            }

            setColor(
                ColorHelper.setAlpha(
                    FLIGHT_LINE_COLOR,
                    255)); // isShadow ? FlightplanLayer.this.shadowColor : FlightplanLayer.this.color);

            // System.out.println(fp + " getColorFrom Line" + getColor() + " alpha " + getColor().getAlpha());
            setPositions(pos);
        }

        @Override
        public boolean isDraggable() {
            return false; // FlightplanLayer.this.isDragable && controller.getMouseMode() == MouseMode.MoveFP;
        }

        @Override
        public boolean isPopupTriggering() {
            return false;
        }

        @Override
        public boolean isSelectable() {
            return false; // return FlightplanLayer.this.isDragable;
        }
    }

    private class FpCircle extends CircleWithUserData {
        public FpCircle(Object userData, Position center, double radius, boolean isShadow) {
            super(userData, center, radius);
            setFollowTerrain(isShadow);
            setSelectableWhileAddNewPoints(true);
            int baseWidth =
                fp.getHardwareConfiguration().getPlatformDescription().planIndividualImagePositions() ? 2 : 5;
            if (userData == selection) {
                setLineWidth(baseWidth * FlightplanLayer.HIGHLIGHT_SCALE);
            } else {
                setLineWidth(baseWidth);
            }

            setColor(isShadow ? FlightplanLayer.this.shadowColor : FlightplanLayer.this.color);
        }

        @Override
        public boolean isDraggable() {
            return false; // FlightplanLayer.this.isDragable && controller.getMouseMode() == MouseMode.MoveFP;
        }

        @Override
        public boolean isPopupTriggering() {
            return false;
        }

        @Override
        public boolean isSelectable() {
            return false; // return FlightplanLayer.this.isDragable;
        }
    }

    private class FpCircleAssertAlt extends FpCircle {

        public FpCircleAssertAlt(Object userData, Position center, double radius, boolean isShadow) {
            super(userData, center, radius, isShadow);
            setStippleFactor(2);
        }

    }

    private class FpLineLoopClosing extends FpLine {

        public FpLineLoopClosing(Object userData, Iterable<? extends Position> pos, boolean isShadow) {
            super(userData, pos, isShadow);
            setStippleFactor(2);
        }
    }

    private class FpSplittingLine extends PolylineWithUserData {
        public FpSplittingLine(Object userData, Iterable<? extends Position> pos) {
            setUserData(userData);
            setFollowTerrain(true);
            setPathType(AVKey.LINEAR);
            setColor(FlightplanLayer.this.color);
            // System.out.println("userData:"+userData);
            // System.out.println("selec:"+selection);
            if (userData == selection) {
                // System.out.println("highlight it!");
                setLineWidth(15);
            } else {
                setLineWidth(5);
            }
            // setStippleFactor(3);
            // System.out.println(fp + " getColorFrom Line" + getColor() + " alpha " + getColor().getAlpha());
            setPositions(pos);
        }

        @Override
        public boolean isDraggable() {
            return false; // FlightplanLayer.this.isDragable;
        }

        @Override
        public boolean isPopupTriggering() {
            return false;
        }

        @Override
        public boolean isSelectable() {
            return false; // FlightplanLayer.this.isDragable;
        }
    }

    public FpSplittingLine createSplittingLine(Object userData, LatLon start, LatLon end) {
        Vector<Position> v = new Vector<>(2);
        if (start == null) {
            v.add(null);
        } else {
            v.add(new Position(start, -10000));
        }

        if (end == null) {
            v.add(null);
        } else {
            v.add(new Position(end, -10000));
        }

        FpSplittingLine line = new FpSplittingLine(userData, v);
        return line;
    }

    @Override
    protected synchronized void doRender(DrawContext dc) {
        // System.out.println("doRender nextPrevRenderable:"+nextPrevRenderable + " "  + this.hashCode());
        if (isOnAirFP) {
            dc.pushProjectionOffest(0.99);
        }

        if (nextPrevRenderable != null) {
            nextPrevRenderable.render(dc);
        }

        if (!isDragging && simPath != null && flightplanLayerVisibilitySettings.isFlightLineVisible()) {
            simPath.render(dc);
        }

        if (fp != null) {
            lineLayerAct.render(dc);
            markerLayerAct.render(dc);
        }

        forgroundLayerAct.render(dc);
        iconLayerAct.render(dc);
        iconLayerFootAct.render(dc);
        waypointsLayerAct.render(dc);
        textLayerAct.render(dc);
        annotationLayerAct.render(dc);
        if (isOnAirFP) {
            dc.popProjectionOffest();
        }
    }

    FPcoveragePreview nextPrevRenderable;

    @Override
    protected synchronized void doPreRender(DrawContext dc) {
        super.doPreRender(dc);
        boolean isShowingCoveragePreview = flightplanLayerVisibilitySettings.isCoveragePreviewVisible();
        if (isOnAirFP
                || (isOnAirRelated && isOnAirRelatedSync)
                || isDragging
                || containsOnAirRelatedInSync
                || !isShowingCoveragePreview
                || preview.getUpdateTimestamp() < fp.getUpdateTimestamp()) {
            nextPrevRenderable = null;
        } else {
            nextPrevRenderable = preview;
        }

        if (nextPrevRenderable != null) {
            // System.out.println("preRender"+id);
            nextPrevRenderable.preRender(dc);
        }

        if (!isDragging && simPath != null && flightplanLayerVisibilitySettings.isFlightLineVisible()) {
            simPath.preRender(dc);
        }

        lineLayerAct.preRender(dc);
        markerLayerAct.preRender(dc);
        forgroundLayerAct.preRender(dc);
        iconLayerAct.preRender(dc);
        iconLayerFootAct.preRender(dc);
        waypointsLayerAct.preRender(dc);
        textLayerAct.preRender(dc);
        annotationLayerAct.preRender(dc);
    }

    @Override
    public boolean isPickEnabled() {
        return isDragable;
    }

    @Override
    protected void doPick(DrawContext dc, Point point) {
        if (isPickEnabled()) {
            if (simPath != null && simPath.isPickEnabled() && flightplanLayerVisibilitySettings.isFlightLineVisible()) {
                simPath.pick(dc, point);
            }

            if (iconLayerAct.isPickEnabled()) {
                iconLayerAct.pick(dc, point);
            }

            if (iconLayerFootAct.isPickEnabled()) {
                iconLayerFootAct.pick(dc, point);
            }

            if (forgroundLayerAct.isPickEnabled()) {
                forgroundLayerAct.pick(dc, point);
            }
            // System.out.println("pickAOI");
            if (lineLayerAct.isPickEnabled()) {
                lineLayerAct.pick(dc, point);
            }

            if (nextPrevRenderable != null) {
                nextPrevRenderable.pick(dc, point);
            }
        }
    }

    IRecomputeRunnable runnable =
        new IRecomputeRunnable() {

            @Override
            public void run() {
                // System.out.println("computing in background" + fp.hashCode());
                reconstructLayer();
            }

            @Override
            public void runLaterOnUIThread() {
                // System.out.println("swap"+ fp.hashCode());
                IconLayerCentered iconLayerTmp = iconLayer;
                iconLayer = iconLayerAct;
                iconLayerAct = iconLayerTmp;

                IconLayerCentered iconLayerFootTmp = iconLayerFoot;
                iconLayerFoot = iconLayerFootAct;
                iconLayerFootAct = iconLayerFootTmp;

                IconLayerCentered waypointsLayerTmp = waypointsLayer;
                waypointsLayer = waypointsLayerAct;
                waypointsLayerAct = waypointsLayerTmp;

                RenderableLayer lineLayerTmp = lineLayer;
                lineLayer = lineLayerAct;
                lineLayerAct = lineLayerTmp;

                RenderableLayer forgroundLayerTmp = forgroundLayer;
                forgroundLayer = forgroundLayerAct;
                forgroundLayerAct = forgroundLayerTmp;

                UserFacingTextLayer textLayerTmp = textLayer;
                textLayer = textLayerAct;
                textLayerAct = textLayerTmp;

                Vector<Marker> markersTmp = markers;
                markers = markersAct;
                markersAct = markersTmp;

                MarkerLayer markerLayerTmp = markerLayer;
                markerLayer = markerLayerAct;
                markerLayerAct = markerLayerTmp;

                AnnotationLayer annotationLayerTmp = annotationLayer;
                annotationLayer = annotationLayerAct;
                annotationLayerAct = annotationLayerTmp;

                AoiAnnotationBalloon baloonTmp = balloon;
                balloon = balloonAct;
                balloonAct = baloonTmp;

                FlightplanLayer.this.firePropertyChange(AVKey.LAYER, null, FlightplanLayer.this);
            }

        };

    Recomputer recomp =
        new Recomputer(runnable) {
            @Override
            public boolean tryStartRecomp() {
                /*runnable.run();
                runnable.runLaterOnUIThread();
                return true;*/
                // Debug.printStackTrace(fp.hashCode() );
                // System.out.println("tryStartRecomp");
                return super.tryStartRecomp();
            }

            @Override
            protected long getDelayBeforeStart() {
                return 5; // extra short to get more smooth drag and drop rendering
            }
        };

    @Override
    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        if (pressureZero == 0) {
            return;
        }

        recomp.tryStartRecomp();
    }

    @Override
    public void recv_config(Config_variables c) {
        recomp.tryStartRecomp();
    }

    int oldReentry = -1;
    boolean isOnAirRelated = false;
    boolean isOnAirRelatedSync = false;
    boolean containsOnAirRelatedInSync = false;

    @Override
    public void recv_position(PositionData p) {
        containsOnAirRelatedInSync = false;
        if (!fp.isOnAirFlightplanOrRelatedLocalFlightplan(plane)) {
            if (isOnAirRelated) {
                isOnAirRelated = false;
                isOnAirRelatedSync = false;
                recomp.tryStartRecomp();
            }

            return;
        }

        isOnAirRelated = true;
        isOnAirRelatedSync = plane.getFPmanager().getOnAirFlightplan().equals(fp);
        if (ReentryPointID.equalIDexceptCell(oldReentry, p.reentrypoint)) {
            return;
        }

        oldReentry = p.reentrypoint;
        recomp.tryStartRecomp();
    }

    void setFont() {
        f = FontHelper.getBaseFont(1, Font.BOLD);
    }

    @Override
    public void recomputeReady(Recomputer recomputer, boolean anotherRecomputeIsWaiting, long runNo) {
        // coverage layer computation is done, I have to trigger WWD redraw here
        // redraw WWD layer
        dispatcher.runLaterAsync(() -> firePropertyChange(AVKey.LAYER, null, FlightplanLayer.this));
    }
}
