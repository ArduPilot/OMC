/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.bindings.BeanAdapter;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CPicArea.FacadeScanningSide;
import eu.mavinci.core.flightplan.CPicArea.JumpPatternTypes;
import eu.mavinci.core.flightplan.CPicArea.ScanDirectionsTypes;
import eu.mavinci.core.flightplan.CPicArea.StartCaptureTypes;
import eu.mavinci.core.flightplan.CPicArea.StartCaptureVerticallyTypes;
import eu.mavinci.core.flightplan.CPicArea.VerticalScanPatternTypes;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.helper.Pair;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AoiTooltipBuilder;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.PicAreaCorners;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.PointWithAltitudes;
import eu.mavinci.flightplan.ReferencePoint;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AreaOfInterest implements IFlightplanChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AreaOfInterest.class);

    public static final String AOI_PREFIX = AoiTooltipBuilder.AOI_PREFIX;
    private final DoubleProperty gsd = new SimpleDoubleProperty(0.01);
    private final DoubleProperty altitude = new SimpleDoubleProperty(0.0);
    private final DoubleProperty height = new SimpleDoubleProperty(10.0);
    private final DoubleProperty width = new SimpleDoubleProperty(10.0);
    private final DoubleProperty length = new SimpleDoubleProperty(200.0);
    private final DoubleProperty yaw = new SimpleDoubleProperty(0);
    private final ListProperty<AreaOfInterestCorner> cornerList =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<PlanType> type = new SimpleObjectProperty<>();
    private final StringProperty name = new SimpleStringProperty("");
    private final DoubleProperty surfaceArea = new SimpleDoubleProperty(0.0);

    private final BooleanProperty cropHeightMinEnabled = new SimpleBooleanProperty(false);
    private final BooleanProperty cropHeightMaxEnabled = new SimpleBooleanProperty(false);

    private final DoubleProperty cropHeightMin = new SimpleDoubleProperty(0.0);
    private final DoubleProperty cropHeightMax = new SimpleDoubleProperty(0.0);

    private final DoubleProperty minGroundDistance = new SimpleDoubleProperty(0.0);
    private final DoubleProperty minObjectDistance = new SimpleDoubleProperty(0.0);
    private final DoubleProperty maxObjectDistance = new SimpleDoubleProperty(0.0);
    private final IntegerProperty minCornerCount = new SimpleIntegerProperty();

    private final ObjectProperty<VerticalScanPatternTypes> verticalScanPattern =
        new SimpleObjectProperty<>(VerticalScanPatternTypes.upDown);

    private final ObjectProperty<FacadeScanningSide> facadeScanSide =
        new SimpleObjectProperty<>(FacadeScanningSide.left);

    private final ObjectProperty<JumpPatternTypes> jumpPattern =
        new SimpleObjectProperty<>(JumpPatternTypes.interleaving);

    private final ObjectProperty<ScanDirectionsTypes> scanDirection =
        new SimpleObjectProperty<>(ScanDirectionsTypes.towardLaning);

    private final ObjectProperty<StartCaptureTypes> startCapture =
        new SimpleObjectProperty<>(StartCaptureTypes.endOfLine);

    private final ObjectProperty<StartCaptureVerticallyTypes> startCaptureVertically =
        new SimpleObjectProperty<>(StartCaptureVerticallyTypes.down);

    private final BooleanProperty singleDirection = new SimpleBooleanProperty(false);

    private final BooleanProperty cameraTiltEnabled = new SimpleBooleanProperty(false);
    private final DoubleProperty cameraTiltDegrees = new SimpleDoubleProperty(0.0);
    private final DoubleProperty cameraPitchOffsetDegrees = new SimpleDoubleProperty(0.0);

    private final BooleanProperty cameraRollEnabled = new SimpleBooleanProperty(false);
    private final DoubleProperty cameraRollDegrees = new SimpleDoubleProperty(0.0);
    private final DoubleProperty cameraRollOffsetDegrees = new SimpleDoubleProperty(0.0);
    private final DoubleProperty maxYawRollChange = new SimpleDoubleProperty(0.0);
    private final DoubleProperty maxPitchChange = new SimpleDoubleProperty(0.0);

    private final DoubleProperty overlapInFlight = new SimpleDoubleProperty(0.0);
    private final DoubleProperty overlapInFlightMin = new SimpleDoubleProperty(0.0);
    private final DoubleProperty overlapParallel = new SimpleDoubleProperty(0.0);

    private final ObjectProperty<RotationDirection> rotationDirection =
        new SimpleObjectProperty<>(RotationDirection.LEFT);

    private final IntegerProperty circleCount = new SimpleIntegerProperty();
    private final IntegerProperty corridorMinLines = new SimpleIntegerProperty();

    private final BooleanProperty addCeiling = new SimpleBooleanProperty(false);

    public boolean isOptimizeWayPoints() {
        return optimizeWayPoints.get();
    }

    public BooleanProperty optimizeWayPointsProperty() {
        return optimizeWayPoints;
    }

    private final BooleanProperty optimizeWayPoints = new SimpleBooleanProperty(false);

    private final DoubleProperty restrictionFloor = new SimpleDoubleProperty();
    private final BooleanProperty restrictionFloorEnabled = new SimpleBooleanProperty();
    private final ObjectProperty<CPicArea.RestrictedAreaHeightReferenceTypes> restrictionFloorRef =
        new SimpleObjectProperty<>();
    private final DoubleProperty restrictionCeiling = new SimpleDoubleProperty();
    private final BooleanProperty restrictionCeilingEnabled = new SimpleBooleanProperty();
    private final ObjectProperty<CPicArea.RestrictedAreaHeightReferenceTypes> restrictionCeilingRef =
        new SimpleObjectProperty<>();
    private final DoubleProperty restrictionEgmOffset = new SimpleDoubleProperty();

    private final DoubleProperty cameraPitchOffsetLineBeginDegrees = new SimpleDoubleProperty();

    private final StringProperty modelFilePath = new SimpleStringProperty();
    private final ObjectProperty<CPicArea.ModelSourceTypes> modelSource = new SimpleObjectProperty<>();
    private final DoubleProperty modelScale = new SimpleDoubleProperty();
    private final ObjectProperty<Position> originPosition = new SimpleObjectProperty<>();
    private final DoubleProperty originElevation = new SimpleDoubleProperty();
    private final DoubleProperty originYaw = new SimpleDoubleProperty();

    private final ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentX = new SimpleObjectProperty<>();
    private final ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentY = new SimpleObjectProperty<>();
    private final ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentZ = new SimpleObjectProperty<>();
    private final DoubleProperty modelAxisOffsetX = new SimpleDoubleProperty();
    private final DoubleProperty modelAxisOffsetY = new SimpleDoubleProperty();
    private final DoubleProperty modelAxisOffsetZ = new SimpleDoubleProperty();
    private final ListProperty<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>> modelAxisTransformations =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    // for WINDMILL AOI
    private final DoubleProperty hubDiameter = new SimpleDoubleProperty();
    private final DoubleProperty hubLength = new SimpleDoubleProperty();
    private final IntegerProperty numberOfBlades = new SimpleIntegerProperty();
    private final DoubleProperty bladeLength = new SimpleDoubleProperty();
    private final DoubleProperty bladeDiameter = new SimpleDoubleProperty();
    private final DoubleProperty bladeThinRadius = new SimpleDoubleProperty();
    private final DoubleProperty bladePitch = new SimpleDoubleProperty();
    private final DoubleProperty bladeStartRotation = new SimpleDoubleProperty();
    private final DoubleProperty bladeCoverLength = new SimpleDoubleProperty();

    private final PicArea picArea;
    // this object is reference to pic area template in corresponing flight plan
    private final CPicArea picAreaTemplate;
    private final SimpleBooleanProperty isInitialAdding = new SimpleBooleanProperty(false);
    private final BeanAdapter<PicArea> beanAdapter;
    private final MavinciObjectFactory mavinciObjectFactory =
        DependencyInjector.getInstance().getInstanceOf(MavinciObjectFactory.class);
    private final ISettingsManager settingsManager =
        DependencyInjector.getInstance().getInstanceOf(ISettingsManager.class);
    private final GeneralSettings generalStettings =
        DependencyInjector.getInstance().getInstanceOf(ISettingsManager.class).getSection(GeneralSettings.class);
    private final BooleanBinding hasEnoughCorners = cornerList.sizeProperty().greaterThanOrEqualTo(minCornerCount);

    public AreaOfInterest(Mission mission, PlanType aoiId) {
        this.picArea = mavinciObjectFactory.createPicArea(mission, aoiId);
        this.picAreaTemplate =
            mission.currentFlightPlanProperty().get().getLegacyFlightplan().getPicAreaTemplate(aoiId);
        beanAdapter = new BeanAdapter<>(picArea);
        init();
    }

    public AreaOfInterest(PicArea picArea) {
        this(picArea, picArea.getFlightplan().getPicAreaTemplate(picArea.getPlanType()));
    }

    public AreaOfInterest(PicArea picArea, CPicArea picAreaTemplate) {
        this.picArea = picArea;
        this.picAreaTemplate = picAreaTemplate;
        beanAdapter = new BeanAdapter<>(picArea);
        init();
    }

    private void init() {
        type.addListener(
            (observable, oldValue, newValue) -> minCornerCount.set(newValue == null ? 0 : newValue.getMinCorners()));

        beanAdapter.bind(type).to(PicArea::getPlanType, PicArea::setPlanType);
        beanAdapter.bind(gsd).to(PicArea::getGsd, PicArea::setGsd);
        beanAdapter.bind(altitude).to(PicArea::getAlt, CPicArea::setAlt);
        beanAdapter.bind(height).to(PicArea::getObjectHeight, PicArea::setObjectHeight);
        beanAdapter.bind(yaw).to(PicArea::getYaw, PicArea::setYaw);
        beanAdapter.bind(name).to(PicArea::getName, PicArea::setName);
        beanAdapter.bind(surfaceArea).to(PicArea::getArea);
        beanAdapter.bind(length).to(PicArea::getCorridorLength);
        beanAdapter.bind(cropHeightMinEnabled).to(PicArea::isEnableCropHeightMin, PicArea::setEnableCropHeightMin);
        beanAdapter.bind(cropHeightMaxEnabled).to(PicArea::isEnableCropHeightMax, PicArea::setEnableCropHeightMax);
        beanAdapter.bind(cropHeightMin).to(PicArea::getCropHeightMin, PicArea::setCropHeightMin);
        beanAdapter.bind(cropHeightMax).to(PicArea::getCropHeightMax, PicArea::setCropHeightMax);
        beanAdapter.bind(minGroundDistance).to(PicArea::getMinGroundDistance, PicArea::setMinGroundDistance);
        beanAdapter.bind(minObjectDistance).to(PicArea::getMinObjectDistance, PicArea::setMinObjectDistance);
        beanAdapter.bind(maxObjectDistance).to(PicArea::getMaxObjectDistance, PicArea::setMaxObjectDistance);
        beanAdapter.bind(facadeScanSide).to(PicArea::getFacadeScanningSide, PicArea::setFacadeScanningSide);
        beanAdapter.bind(verticalScanPattern).to(PicArea::getVerticalScanPattern, PicArea::setVerticalScanPattern);
        beanAdapter.bind(jumpPattern).to(PicArea::getJumpPattern, PicArea::setJumpPattern);
        beanAdapter.bind(scanDirection).to(PicArea::getScanDirection, PicArea::setScanDirection);
        beanAdapter.bind(startCapture).to(PicArea::getStartCapture, PicArea::setStartCapture);
        beanAdapter
            .bind(startCaptureVertically)
            .to(PicArea::getStartCaptureVertically, PicArea::setStartCaptureVertically);
        beanAdapter.bind(singleDirection).to(PicArea::isOnlySingleDirection, PicArea::setOnlySingleDirection);
        beanAdapter.bind(cameraTiltEnabled).to(PicArea::isCameraTiltToggleEnable, PicArea::setCameraTiltToggleEnable);
        beanAdapter
            .bind(cameraTiltDegrees)
            .to(PicArea::getCameraTiltToggleDegrees, PicArea::setCameraTiltToggleDegrees);
        beanAdapter
            .bind(cameraPitchOffsetDegrees)
            .to(PicArea::getCameraPitchOffsetDegrees, PicArea::setCameraPitchOffsetDegrees);
        beanAdapter.bind(cameraRollEnabled).to(PicArea::isCameraRollToggleEnable, PicArea::setCameraRollToggleEnable);
        beanAdapter
            .bind(cameraRollDegrees)
            .to(PicArea::getCameraRollToggleDegrees, PicArea::setCameraRollToggleDegrees);
        beanAdapter
            .bind(cameraRollOffsetDegrees)
            .to(PicArea::getCameraRollOffsetDegrees, PicArea::setCameraRollOffsetDegrees);
        beanAdapter.bind(overlapInFlight).to(PicArea::getOverlapInFlight, PicArea::setOverlapInFlight);
        beanAdapter.bind(overlapInFlightMin).to(PicArea::getOverlapInFlightMin, PicArea::setOverlapInFlightMin);
        beanAdapter.bind(overlapParallel).to(PicArea::getOverlapParallel, PicArea::setOverlapParallel);
        beanAdapter
            .bind(rotationDirection)
            .to(
                picArea -> picArea.isCircleLeftTrueRightFalse() ? RotationDirection.LEFT : RotationDirection.RIGHT,
                (picArea, direction) -> picArea.setCircleLeftTrueRightFalse(direction == null || direction.isLeft()));
        beanAdapter.bind(circleCount).to(PicArea::getCircleCount, PicArea::setCircleCount);
        beanAdapter.bind(corridorMinLines).to(PicArea::getCorridorMinLines, PicArea::setCorridorMinLines);
        beanAdapter.bind(addCeiling).to(PicArea::isAddCeiling, PicArea::setAddCeiling);
        beanAdapter.bind(width).to(PicArea::getCorridorWidthInMeter, PicArea::setCorridorWidthInMeter);
        beanAdapter.bind(maxYawRollChange).to(PicArea::getMaxYawRollChange, PicArea::setMaxYawRollChange);
        beanAdapter.bind(maxPitchChange).to(PicArea::getMaxPitchChange, PicArea::setMaxPitchChange);

        beanAdapter.bind(restrictionFloor).to(PicArea::getRestrictionFloor, PicArea::setRestrictionFloor);
        beanAdapter
            .bind(restrictionFloorEnabled)
            .to(PicArea::isRestrictionFloorEnabled, PicArea::setRestrictionFloorEnabled);
        beanAdapter.bind(restrictionFloorRef).to(PicArea::getRestrictionFloorRef, PicArea::setRestrictionFloorRef);
        beanAdapter.bind(restrictionCeiling).to(PicArea::getRestrictionCeiling, PicArea::setRestrictionCeiling);
        beanAdapter
            .bind(restrictionCeilingEnabled)
            .to(PicArea::isRestrictionCeilingEnabled, PicArea::setRestrictionCeilingEnabled);
        beanAdapter
            .bind(restrictionCeilingRef)
            .to(PicArea::getRestrictionCeilingRef, PicArea::setRestrictionCeilingRef);
        beanAdapter.bind(restrictionEgmOffset).to(PicArea::getRestrictionEgmOffset);

        beanAdapter
            .bind(cameraPitchOffsetLineBeginDegrees)
            .to(PicArea::getPitchOffsetLineBegin, PicArea::setPitchOffsetLineBegin);

        beanAdapter.bind(modelFilePath).to(PicArea::getModelFilePath, PicArea::setModelFilePath);
        beanAdapter.bind(modelSource).to(PicArea::getModelSource, PicArea::setModelSource);
        beanAdapter.bind(modelScale).to(PicArea::getModelScale, PicArea::setModelScale);
        beanAdapter
            .bind(originPosition)
            .to(
                picArea1 -> picArea1.getFlightplan().getRefPoint().getPosition(),
                (picArea1, pos) -> picArea1.getFlightplan().getRefPoint().setPosition(pos));
        beanAdapter
            .bind(originYaw)
            .to(
                picArea1 -> picArea1.getFlightplan().getRefPoint().getYaw(),
                (picArea1, yaw) -> picArea1.getFlightplan().getRefPoint().setYaw(yaw));
        beanAdapter
            .bind(originElevation)
            .to(
                picArea1 -> picArea1.getFlightplan().getRefPoint().getElevation(),
                (picArea1, elev) -> picArea1.getFlightplan().getRefPoint().setElevation(elev));
        beanAdapter.bind(modelAxisAlignmentX).to(PicArea::getModelAxisAlignmentX, PicArea::setModelAxisAlignmentX);
        beanAdapter.bind(modelAxisAlignmentY).to(PicArea::getModelAxisAlignmentY, PicArea::setModelAxisAlignmentY);
        beanAdapter.bind(modelAxisAlignmentZ).to(PicArea::getModelAxisAlignmentZ, PicArea::setModelAxisAlignmentZ);
        beanAdapter.bind(modelAxisOffsetX).to(PicArea::getModelAxisOffsetX, PicArea::setModelAxisOffsetX);
        beanAdapter.bind(modelAxisOffsetY).to(PicArea::getModelAxisOffsetY, PicArea::setModelAxisOffsetY);
        beanAdapter.bind(modelAxisOffsetZ).to(PicArea::getModelAxisOffsetZ, PicArea::setModelAxisOffsetZ);
        beanAdapter
            .bind(modelAxisTransformations)
            .to(PicArea::getModelAxisTransformations, PicArea::setModelAxisTransformations);

        // for WINDMILL aoi
        beanAdapter.bind(hubDiameter).to(PicArea::getWindmillHubDiameter, PicArea::setWindmillHubDiameter);
        beanAdapter.bind(hubLength).to(PicArea::getWindmillHubLength, PicArea::setWindmillHubLength);
        beanAdapter.bind(numberOfBlades).to(PicArea::getWindmillNumberOfBlades, PicArea::setWindmillNumberOfBlades);
        beanAdapter.bind(bladeLength).to(PicArea::getWindmillBladeLength, PicArea::setWindmillBladeLength);
        beanAdapter.bind(bladeDiameter).to(PicArea::getWindmillBladeDiameter, PicArea::setWindmillBladeDiameter);
        beanAdapter.bind(bladeThinRadius).to(PicArea::getWindmillBladeThinRadius, PicArea::setWindmillBladeThinRadius);
        beanAdapter.bind(bladePitch).to(PicArea::getWindmillBladePitch, PicArea::setWindmillBladePitch);
        beanAdapter
            .bind(bladeStartRotation)
            .to(PicArea::getWindmillBladeStartRotation, PicArea::setWindmillBladeStartRotation);
        beanAdapter
            .bind(bladeCoverLength)
            .to(PicArea::getWindmillBladeCoverLength, PicArea::setWindmillBladeCoverLength);

        beanAdapter.bind(optimizeWayPoints).to(PicArea::isOptimizeWayPoints, PicArea::setOptimizeWayPoints);

        // Subscribe self to flightPlan events
        Flightplan flightplan = picArea.getFlightplan();
        if (flightplan != null) {
            flightplan.addFPChangeListener(this);
        }

        reflectChangesOfPicArea();
    }

    public double getSurfaceArea() {
        return surfaceArea.get();
    }

    public ReadOnlyDoubleProperty surfaceAreaProperty() {
        return surfaceArea;
    }

    @Override
    public void flightplanStructureChanged(IFlightplanRelatedObject statement) {
        if (!isSamePicArea(statement)) {
            return;
        }

        Dispatcher.platform().runLater(this::reflectChangesOfPicArea);
    }

    @Override
    public void flightplanValuesChanged(IFlightplanRelatedObject statement) {
        if (!isSamePicArea(statement)) {
            return;
        }

        Dispatcher.platform().runLater(this::reflectChangesOfPicArea);
    }

    @Override
    public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {
        // TODO FIXME maybe remove the widget
        if (!isSamePicArea(statement)) {
            return;
        }

        Dispatcher.platform().runLater(this::reflectChangesOfPicArea);
    }

    @Override
    public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {
        if (!isSamePicArea(statement)) {
            return;
        }

        Dispatcher.platform().runLater(this::reflectChangesOfPicArea);
    }

    public Point getLastPoint() {
        PicAreaCorners corners = picArea.getCorners();
        return corners.getLastElement();
    }

    private boolean isSamePicArea(IFlightplanRelatedObject statement) {
        if (statement instanceof ReferencePoint && statement.getFlightplan() == picArea.getFlightplan()) return true;
        if (statement == picArea.getFlightplan()) return true;
        PicArea parentArea = extractPicArea(statement);
        return ((parentArea != null) && (parentArea == picArea));
    }

    private PicArea extractPicArea(IFlightplanRelatedObject statement) {
        if (statement instanceof PicArea) {
            return (PicArea)statement;
        }

        IFlightplanContainer parent = statement.getParent();
        if (parent instanceof PicArea) {
            return (PicArea)parent;
        } else if (parent == null) {
            return null;
        }

        parent = parent.getParent();
        if (parent instanceof PicArea) {
            return (PicArea)parent;
        }

        return null;
    }

    private synchronized void reflectChangesOfPicArea() {
        beanAdapter.updateValuesFromSource();

        PicAreaCorners legacyCorners = picArea.getCorners();
        // first compare if list might stay the same
        // in that case we should not recreate wrapper, since this would break selection persistence
        int i = 0;
        boolean sameList = true;
        if (legacyCorners.sizeOfFlightplanContainer() != cornerList.size()) {
            sameList = false;
        } else {
            for (IFlightplanStatement tmp : legacyCorners) {
                Point point = (Point)tmp;
                if (!point.equals(cornerList.get(i).getLegacyPoint().getPoint())) {
                    sameList = false;
                    break;
                }

                if (cornerList.get(i).getLegacyPoint().updateAltitudes()) {
                    cornerList.get(i).updateValuesFromSource();
                }

                i++;
            }
        }

        if (!sameList) {
            i = 1;
            List<AreaOfInterestCorner> corners = new ArrayList<>();
            for (IFlightplanStatement tmp : legacyCorners) {
                PointWithAltitudes point = new PointWithAltitudes((Point)tmp);
                corners.add(new AreaOfInterestCorner(i, point, generalStettings, picArea, settingsManager));

                i++;
            }

            cornerList.forEach((areaOfInterestCorner -> areaOfInterestCorner.removeFPListeners()));
            this.cornerList.setAll(corners);
        }
    }

    public PlanType getType() {
        return type.get();
    }

    public String getTypeKey() {
        return AOI_PREFIX + getType().toString();
    }

    public PicArea getPicArea() {
        return picArea;
    }

    public CPicArea getPicAreaTemplate() {
        return picAreaTemplate;
    }

    public IntegerProperty minCornerCountProperty() {
        return minCornerCount;
    }

    public LatLon getCenter() {
        return picArea.getCorners().getSector().getCentroid();
    }

    public SimpleBooleanProperty isInitialAddingProperty() {
        return isInitialAdding;
    }

    public ListProperty<AreaOfInterestCorner> cornerListProperty() {
        return cornerList;
    }

    public DoubleProperty lengthProperty() {
        return length;
    }

    public BooleanBinding hasEnoughCornersBinding() {
        return hasEnoughCorners;
    }

    public DoubleProperty gsdProperty() {
        return gsd;
    }

    public DoubleProperty altProperty() {
        return altitude;
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public DoubleProperty yawProperty() {
        return yaw;
    }

    public ObjectProperty<PlanType> typeProperty() {
        return type;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public BooleanProperty cropHeightMinEnabledProperty() {
        return cropHeightMinEnabled;
    }

    public BooleanProperty cropHeightMaxEnabledProperty() {
        return cropHeightMaxEnabled;
    }

    public DoubleProperty cropHeightMinProperty() {
        return cropHeightMin;
    }

    public DoubleProperty cropHeightMaxProperty() {
        return cropHeightMax;
    }

    public DoubleProperty minGroundDistanceProperty() {
        return minGroundDistance;
    }

    public DoubleProperty minObjectDistanceProperty() {
        return minObjectDistance;
    }

    public DoubleProperty maxObjectDistanceProperty() {
        return maxObjectDistance;
    }

    public ObjectProperty<FacadeScanningSide> facadeScanSideProperty() {
        return facadeScanSide;
    }

    public ObjectProperty<VerticalScanPatternTypes> verticalScanPatternProperty() {
        return verticalScanPattern;
    }

    public ObjectProperty<JumpPatternTypes> jumpPatternProperty() {
        return jumpPattern;
    }

    public ObjectProperty<ScanDirectionsTypes> scanDirectionProperty() {
        return scanDirection;
    }

    public ObjectProperty<StartCaptureTypes> startCaptureProperty() {
        return startCapture;
    }

    public ObjectProperty<StartCaptureVerticallyTypes> startCaptureVerticallyProperty() {
        return startCaptureVertically;
    }

    public BooleanProperty singleDirectionProperty() {
        return singleDirection;
    }

    public BooleanProperty cameraTiltEnabledProperty() {
        return cameraTiltEnabled;
    }

    public BooleanProperty cameraRollEnabledProperty() {
        return cameraRollEnabled;
    }

    public DoubleProperty cameraRollDegreesProperty() {
        return cameraRollDegrees;
    }

    public DoubleProperty cameraRollOffsetDegreesProperty() {
        return cameraRollOffsetDegrees;
    }

    public DoubleProperty cameraTiltDegreesProperty() {
        return cameraTiltDegrees;
    }

    public DoubleProperty overlapInFlightProperty() {
        return overlapInFlight;
    }

    public DoubleProperty overlapInFlightMinProperty() {
        return overlapInFlightMin;
    }

    public DoubleProperty overlapParallelProperty() {
        return overlapParallel;
    }

    public ObjectProperty<RotationDirection> rotationDirectionProperty() {
        return rotationDirection;
    }

    public IntegerProperty circleCountProperty() {
        return circleCount;
    }

    public IntegerProperty corridorMinLinesProperty() {
        return corridorMinLines;
    }

    public BooleanProperty addCeilingProperty() {
        return addCeiling;
    }

    public DoubleProperty maxPitchChangeProperty() {
        return maxPitchChange;
    }

    public DoubleProperty maxYawRollChangeProperty() {
        return maxYawRollChange;
    }

    public DoubleProperty restrictionFloorProperty() {
        return restrictionFloor;
    }

    public BooleanProperty restrictionFloorEnabledProperty() {
        return restrictionFloorEnabled;
    }

    public ObjectProperty<CPicArea.RestrictedAreaHeightReferenceTypes> restrictionFloorRefProperty() {
        return restrictionFloorRef;
    }

    public DoubleProperty restrictionCeilingProperty() {
        return restrictionCeiling;
    }

    public BooleanProperty restrictionCeilingEnabledProperty() {
        return restrictionCeilingEnabled;
    }

    public ObjectProperty<CPicArea.RestrictedAreaHeightReferenceTypes> restrictionCeilingRefProperty() {
        return restrictionCeilingRef;
    }

    public DoubleProperty originElevationProperty() {
        return originElevation;
    }

    public DoubleProperty cameraPitchOffsetLineBeginDegreesProperty() {
        return cameraPitchOffsetLineBeginDegrees;
    }

    // for WINDMILL AOI
    public DoubleProperty hubDiameterProperty() {
        return hubDiameter;
    }

    public DoubleProperty hubLengthProperty() {
        return hubLength;
    }

    public IntegerProperty numberOfBladesProperty() {
        return numberOfBlades;
    }

    public DoubleProperty bladeLengthProperty() {
        return bladeLength;
    }

    public DoubleProperty bladeDiameterProperty() {
        return bladeDiameter;
    }

    public DoubleProperty bladeThinRadiusProperty() {
        return bladeThinRadius;
    }

    public DoubleProperty bladePitchProperty() {
        return bladePitch;
    }

    public DoubleProperty bladeStartRotationProperty() {
        return bladeStartRotation;
    }

    public DoubleProperty bladeCoverLengthProperty() {
        return bladeCoverLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AreaOfInterest that = (AreaOfInterest)o;

        return picArea.equals(that.picArea);
    }

    @Override
    public int hashCode() {
        return picArea.hashCode();
    }

    public void optimizeArea(double maxAspectRatio) {
        picArea.optimizeArea(maxAspectRatio);
    }

    public String getLocalizedTypeName(ILanguageHelper languageHelper) {
        PlanType planType = getType();
        if (languageHelper != null) {
            switch (planType) {
            case POLYGON:
                return languageHelper.getString("planningView.polygon");
            case CORRIDOR:
                return languageHelper.getString("planningView.corridorMapping");
            case CITY:
                return languageHelper.getString("planningView.cityMapping");
            case SPIRAL:
                return languageHelper.getString("planningView.spiral");
            case SEARCH:
                return languageHelper.getString("planningView.search");
            case TOWER:
                return languageHelper.getString("planningView.tower");
            case WINDMILL:
                return languageHelper.getString("planningView.windmill");
            case BUILDING:
                return languageHelper.getString("planningView.building");
            case FACADE:
                return languageHelper.getString("planningView.facade");
            case GEOFENCE_CIRC:
            case GEOFENCE_POLY:
                return languageHelper.getString("planningView.geoFence");
            case NO_FLY_ZONE_CIRC:
            case NO_FLY_ZONE_POLY:
                return languageHelper.getString("planningView.restrictedArea");
            case PANORAMA:
                return languageHelper.getString("planningView.pa");
            case POINT_OF_INTEREST:
                return languageHelper.getString("planningView.poi");
            default:
                return planType.name();
            }
        }

        return planType.name();
    }

    public void restoreFromDefaults() {
        picArea.setDefaultsFromMasterPicArea(picAreaTemplate);
    }

    public void updateDefaultsWithCurrentValues() {
        picAreaTemplate.setDefaultsFromMasterPicArea(picArea);
    }

    public DoubleProperty cameraPitchOffsetDegreesProperty() {
        return cameraPitchOffsetDegrees;
    }

    public void dragVertex(int fromIdx, int toIdx) {
        if (fromIdx == toIdx) {
            return;
        }

        try {
            PicAreaCorners corners = picArea.getCorners();
            IFlightplanStatement corner = corners.removeFromFlightplanContainer(fromIdx);
            corners.addToFlightplanContainer(toIdx, corner);
            reflectChangesOfPicArea();
        } catch (Exception e) {
            LOGGER.warn("cant permute AOI vertexces", e);
        }
    }

    public AreaOfInterestCorner getWrapper(Point point) {
        if (point.getParent() != picArea.getCorners()) {
            return null;
        }

        for (AreaOfInterestCorner corner : cornerList) {
            if (corner.getLegacyPoint().getPoint() == point) {
                return corner;
            }
        }

        return null;
    }

    public Point addVertex(LatLon center, AreaOfInterestCorner addBeforeThis) {
        int idx = cornerList.indexOf(addBeforeThis);
        Point p = new Point(center.latitude.degrees, center.longitude.degrees);

        try {
            if (idx < 0 || idx >= cornerList.size()) {
                picArea.getCorners().addToFlightplanContainer(p);
            } else {
                picArea.getCorners().addToFlightplanContainer(idx, p);
            }

        } catch (Exception e) {
            LOGGER.warn("can't add new point", e);
            return null;
        }

        return p;
    }

    public StringProperty modelFilePathProperty() {
        return modelFilePath;
    }

    public ObjectProperty<CPicArea.ModelSourceTypes> modelSourceProperty() {
        return modelSource;
    }

    public DoubleProperty modelScaleProperty() {
        return modelScale;
    }

    public ObjectProperty<Position> originPositionProperty() {
        return originPosition;
    }

    public DoubleProperty originYawProperty() {
        return originYaw;
    }

    public ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentXProperty() {
        return modelAxisAlignmentX;
    }

    public ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentYProperty() {
        return modelAxisAlignmentY;
    }

    public ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentZProperty() {
        return modelAxisAlignmentZ;
    }

    public DoubleProperty modelAxisOffsetXProperty() {
        return modelAxisOffsetX;
    }

    public DoubleProperty modelAxisOffsetYProperty() {
        return modelAxisOffsetY;
    }

    public DoubleProperty modelAxisOffsetZProperty() {
        return modelAxisOffsetZ;
    }

    public ListProperty<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>> modelAxisTransformationsProperty() {
        return modelAxisTransformations;
    }

    public DoubleProperty restrictionEgmOffsetProperty() {
        return restrictionEgmOffset;
    }
}
