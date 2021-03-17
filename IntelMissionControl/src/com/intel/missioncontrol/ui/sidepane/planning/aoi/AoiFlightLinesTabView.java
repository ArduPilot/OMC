/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.geometry.RotationDirection;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Percentage;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.FlightDirectionComponent;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CPicArea.JumpPatternTypes;
import eu.mavinci.core.flightplan.CPicArea.ScanDirectionsTypes;
import eu.mavinci.core.flightplan.CPicArea.StartCaptureTypes;
import eu.mavinci.core.flightplan.CPicArea.StartCaptureVerticallyTypes;
import eu.mavinci.core.flightplan.CPicArea.VerticalScanPatternTypes;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.flightplan.visitors.FirstLastOfTypeVisitor;
import eu.mavinci.flightplan.PicArea;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class AoiFlightLinesTabView extends ViewBase<AoiFlightLinesTabViewModel> {

    private static final int IMAGES_MIN = CPicArea.MIN_CORRIDOR_MIN_LINES;
    private static final int IMAGES_MAX = CPicArea.MAX_CORRIDOR_MIN_LINES;
    private static final int IMAGES_STEP = 1;

    private static final int CIRCLE_MIN = CPicArea.CIRCLE_MIN;
    private static final int CIRCLE_MAX = CPicArea.CIRCLE_MAX;
    private static final int CIRCLE_STEP = 1;

    private static final double PERCENT_MIN = 0.0;
    private static final double PERCENT_MAX = 99.0;
    private static final double PERCENT_STEP = 1.0;

    private static final double OBJ_DISTANCE_MIN = CPicArea.MIN_OBJ_DISTANCE_METER;
    private static final double OBJ_DISTANCE_MAX = CPicArea.MAX_OBJ_DISTANCE_METER;
    private static final double OBJ_DISTANCE_STEP = 1.0;

    private static final double CAMERA_TILT_MIN = 1.0;
    private static final double CAMERA_TILT_MAX = 45.0;
    private static final double CAMERA_TILT_STEP = 1.0;

    private static final double CAMERA_PITCH_OFFSET_MIN = -90;
    private static final double CAMERA_PITCH_OFFSET_MAX = 90.0;
    private static final double CAMERA_PITCH_OFFSET_STEP = 1.0;

    private static final double CAMERA_ROLL_MIN = 1.0;
    private static final double CAMERA_ROLL_MAX = 20.0;
    private static final double CAMERA_ROLL_STEP = 1.0;

    private static final double CAMERA_ROLL_OFFSET_MIN = -20;
    private static final double CAMERA_ROLL_OFFSET_MAX = 20.0;
    private static final double CAMERA_ROLL_OFFSET_STEP = 1.0;

    private static final double CAMERA_MAX_ANGLE_CHANGE_MAX = 360;
    private static final double CAMERA_MAX_ANGLE_CHANGE_MIN = 0;
    private static final double CAMERA_MAX_ANGLE_CHANGE_STEP = 1;
    public static final String KEY_TAKEOFF = "eu.mavinci.core.flightplan.CPicArea.takeoff";
    public static final String KEY_PREV_AOI = "eu.mavinci.core.flightplan.CPicArea.prevAOI";
    public static final String KEY_NEXT_AOI = "eu.mavinci.core.flightplan.CPicArea.nextAOI";
    private static final String KEY_NEAREST = "eu.mavinci.core.flightplan.CPicArea.nearest";
    private static final String KEY_FURTHEST = "eu.mavinci.core.flightplan.CPicArea.furthest";
    private static final String KEY_START = "eu.mavinci.core.flightplan.CPicArea.start";
    private static final String KEY_FINISH = "eu.mavinci.core.flightplan.CPicArea.finish";
    private static final String KEY_CLOSEST = "eu.mavinci.core.flightplan.CPicArea.closest";

    @FXML
    private Control rootPane;

    @FXML
    private ToggleGroup scanPatternGroup;

    @FXML
    private ToggleGroup jumpPatternGroup;

    @FXML
    private ToggleGroup rotationDirectionGroup;

    @FXML
    private VBox flightDirectionBox;

    @FXML
    private ComboBox<ScanDirectionsTypes> scanDirectionCombo;

    @FXML
    private ComboBox<StartCaptureTypes> startCaptureCombo;

    @FXML
    private ComboBox<StartCaptureVerticallyTypes> verticalStartCombo;

    @FXML
    private CheckBox chkSingleDirection;

    @FXML
    private VBox restrictions3D;

    @FXML @MonotonicNonNull
    private Label minObjectdistanceSpinnerLabel;

    @FXML @MonotonicNonNull
    private Label maxObjectdistanceSpinnerLabel;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> minObjectdistanceSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> maxObjectDistanceSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> minGroundDistanceSpinner;

    @FXML
    private CheckBox chkCameraTilt;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> cameraTiltSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> cameraPitchOffsetSpinner;

    @FXML
    private CheckBox chkCameraRoll;

    @FXML
    private VBox cameraRollMaxChangeBox;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> cameraRollMaxChangeSpinner;

    @FXML
    private VBox cameraTiltMaxChangeBox;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> cameraTiltMaxChangeSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> cameraRollSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> cameraRollOffsetSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> cameraPitchOffsetSpinnerLineBegin;

    @FXML
    private AutoCommitSpinner<Quantity<Percentage>> overlapFlightSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Percentage>> overlapParallelSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Percentage>> overlapFlightMinSpinner;

    @FXML
    private Spinner<Integer> circleCountSpinner;

    @FXML
    private Spinner<Integer> minImagesPerCircleSpinner;

    @FXML
    private VBox rotationBox;

    @FXML
    private VBox circleBox;

    @FXML
    private VBox imagesPerCircleBox;

    @FXML
    private VBox scanDirectionBox;

    @FXML
    private VBox startCaptureBox;

    @FXML
    private VBox scanPatternBox;

    @FXML
    private VBox verticalStartBox;

    @FXML
    private VBox cameraTiltBox;

    @FXML
    private VBox cameraPitchOffsetBox;

    @FXML
    private VBox cameraRollBox;

    @FXML
    private VBox cameraRollOffsetBox;

    @FXML
    private VBox jumpPatternBox;

    @FXML
    private VBox minOverlapFlightDirectionBox;

    @FXML
    private VBox overlapFlightDirectionBox;

    @FXML
    private VBox cameraPitchOffsetLineBeginBox;

    @FXML
    private ToggleButton segmentWiseLeftRightToggle;

    @InjectViewModel
    private AoiFlightLinesTabViewModel viewModel;

    @InjectContext
    private Context context;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private ISettingsManager settingsManager;

    @Override
    public void initializeView() {
        super.initializeView();

        AreaOfInterest areaOfInterest = viewModel.getAreaOfInterest();

        chkSingleDirection.selectedProperty().bindBidirectional(areaOfInterest.singleDirectionProperty());

        chkCameraTilt.selectedProperty().bindBidirectional(areaOfInterest.cameraTiltEnabledProperty());
        cameraTiltSpinner.disableProperty().bind(chkCameraTilt.selectedProperty().not());

        chkCameraRoll.selectedProperty().bindBidirectional(areaOfInterest.cameraRollEnabledProperty());
        cameraRollSpinner.disableProperty().bind(chkCameraRoll.selectedProperty().not());

        ViewHelper.initAutoCommitSpinner(
            cameraRollMaxChangeSpinner,
            viewModel.maxYawRollChangeQuantityProperty(),
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            CAMERA_MAX_ANGLE_CHANGE_MIN,
            CAMERA_MAX_ANGLE_CHANGE_MAX,
            CAMERA_MAX_ANGLE_CHANGE_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            cameraTiltMaxChangeSpinner,
            viewModel.maxPitchChangeQuantityProperty(),
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            CAMERA_MAX_ANGLE_CHANGE_MIN,
            CAMERA_MAX_ANGLE_CHANGE_MAX,
            CAMERA_MAX_ANGLE_CHANGE_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            cameraTiltSpinner,
            viewModel.cameraTiltDegreesQuantityProperty(),
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            CAMERA_TILT_MIN,
            CAMERA_TILT_MAX,
            CAMERA_TILT_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            cameraPitchOffsetSpinner,
            viewModel.cameraPitchOffsetDegreesQuantityProperty(),
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            CAMERA_PITCH_OFFSET_MIN,
            CAMERA_PITCH_OFFSET_MAX,
            CAMERA_PITCH_OFFSET_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            cameraRollSpinner,
            viewModel.cameraRollDegreesQuantityProperty(),
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            CAMERA_ROLL_MIN,
            CAMERA_ROLL_MAX,
            CAMERA_ROLL_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            cameraRollOffsetSpinner,
            viewModel.cameraRollOffsetDegreesQuantityProperty(),
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            CAMERA_ROLL_OFFSET_MIN,
            CAMERA_ROLL_OFFSET_MAX,
            CAMERA_ROLL_OFFSET_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            overlapFlightSpinner,
            viewModel.overlapInFlightQuantityProperty(),
            Unit.PERCENTAGE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            PERCENT_MIN,
            PERCENT_MAX,
            PERCENT_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            overlapFlightMinSpinner,
            viewModel.overlapInFlightMinQuantityProperty(),
            Unit.PERCENTAGE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            PERCENT_MIN,
            PERCENT_MAX,
            PERCENT_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            overlapParallelSpinner,
            viewModel.overlapParallelQuantityProperty(),
            Unit.PERCENTAGE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            PERCENT_MIN,
            PERCENT_MAX,
            PERCENT_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            cameraPitchOffsetSpinnerLineBegin,
            viewModel.cameraPitchOffsetLineBeginDegreesQuantityProperty(),
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            -90.,
            90.,
            CAMERA_ROLL_OFFSET_STEP,
            false);

        overlapFlightSpinner
            .getValueFactory()
            .valueProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    SpinnerValueFactory<Quantity<Percentage>> minOverlapValueFactory =
                        overlapFlightMinSpinner.getValueFactory();
                    Quantity<Percentage> minOverlap = minOverlapValueFactory.getValue();

                    if ((newValue != null) && (minOverlap != null) && (minOverlap.compareTo(newValue)) == 1) {
                        minOverlapValueFactory.setValue(newValue);
                    }
                });

        overlapFlightMinSpinner
            .getValueFactory()
            .valueProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    SpinnerValueFactory<Quantity<Percentage>> minOverlapValueFactory =
                        overlapFlightMinSpinner.getValueFactory();

                    SpinnerValueFactory<Quantity<Percentage>> overlapValueFactory =
                        overlapFlightSpinner.getValueFactory();
                    Quantity<Percentage> overlap = overlapValueFactory.getValue();

                    if ((newValue != null) && (overlap != null) && (newValue.compareTo(overlap)) == 1) {
                        minOverlapValueFactory.setValue(overlap);
                    }
                });

        /*
        maxObstacleHeightBox.setVisible(
            settingsManager.getSection(GeneralSettings.class).getOperationLevel() == OperationLevel.DEBUG);
         */
        ViewHelper.initAutoCommitSpinner(
            minObjectdistanceSpinner,
            viewModel.minObjectDistanceQuantity(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            1,
            OBJ_DISTANCE_MIN,
            OBJ_DISTANCE_MAX,
            OBJ_DISTANCE_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            maxObjectDistanceSpinner,
            viewModel.maxObjectDistanceQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            1,
            OBJ_DISTANCE_MIN,
            OBJ_DISTANCE_MAX,
            OBJ_DISTANCE_STEP,
            false);

        ViewHelper.initAutoCommitSpinner(
            minGroundDistanceSpinner,
            viewModel.minGroundDistanceQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            1,
            OBJ_DISTANCE_MIN,
            OBJ_DISTANCE_MAX,
            OBJ_DISTANCE_STEP,
            false);

        ViewHelper.initToggleGroup(
            areaOfInterest.verticalScanPatternProperty(), VerticalScanPatternTypes.class, scanPatternGroup);

        ViewHelper.initToggleGroup(areaOfInterest.jumpPatternProperty(), JumpPatternTypes.class, jumpPatternGroup);

        ViewHelper.initToggleGroup(
            areaOfInterest.rotationDirectionProperty(), RotationDirection.class, rotationDirectionGroup);

        initFlightDirectionComponent(areaOfInterest);

        initScanDirectionComponent(areaOfInterest);

        ViewHelper.initComboBox(
            areaOfInterest.startCaptureProperty(), StartCaptureTypes.class, startCaptureCombo, languageHelper);

        ViewHelper.initComboBox(
            areaOfInterest.startCaptureVerticallyProperty(),
            StartCaptureVerticallyTypes.class,
            verticalStartCombo,
            languageHelper);

        ViewHelper.initSpinner(
            circleCountSpinner,
            new IntegerSpinnerValueFactory(CIRCLE_MIN, CIRCLE_MAX, CIRCLE_STEP),
            areaOfInterest.circleCountProperty().asObject());

        ViewHelper.initSpinner(
            minImagesPerCircleSpinner,
            new IntegerSpinnerValueFactory(IMAGES_MIN, IMAGES_MAX, IMAGES_STEP),
            areaOfInterest.corridorMinLinesProperty().asObject());

        if (areaOfInterest.getType() == PlanType.WINDMILL) {
            minObjectdistanceSpinnerLabel.setText(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.planning.aoi.AoiFlightLinesTabView.minObjectDistanceWindmill"));
            maxObjectdistanceSpinnerLabel.setText(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.planning.aoi.AoiFlightLinesTabView.maxObjectDistanceWindmill"));
        }

        initControlsVisibility(areaOfInterest);
    }

    @Override
    protected Parent getRootNode() {
        return rootPane;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private void initScanDirectionComponent(AreaOfInterest areaOfInterest) {
        scanDirectionCombo.setConverter(
            new StringConverter<>() {
                @Override
                public String toString(ScanDirectionsTypes object) {
                    PicArea picArea = areaOfInterest.getPicArea();
                    FirstLastOfTypeVisitor visitor = new FirstLastOfTypeVisitor(PicArea.class);
                    visitor.startVisit(picArea.getFlightplan());

                    String start = languageHelper.getString(KEY_START);
                    String finish = languageHelper.getString(KEY_FINISH);
                    String closest = languageHelper.getString(KEY_CLOSEST);
                    String takeoff = languageHelper.getString(KEY_TAKEOFF);
                    String nextAOI = languageHelper.getString(KEY_NEXT_AOI);
                    String prevAOI = languageHelper.getString(KEY_PREV_AOI);

                    // (nearest to)TAKEOFF - FIRST AOI - (nearest to)NEXT AOI/(furthest from)TAKEOFF
                    if (visitor.first.equals(picArea)) {
                        if (object.equals(ScanDirectionsTypes.fromStarting)) {
                            return start + " " + closest + " " + takeoff;
                        } else if (object.equals(ScanDirectionsTypes.towardLaning)) {
                            if (visitor.first.equals(visitor.last)) {
                                return finish + " " + closest + " " + takeoff;
                            } else {
                                return finish + " " + closest + " " + nextAOI;
                            }
                        }
                        // (nearest to)PREV AOI - LAST AOI - (nearest to)TAKEOFF
                    } else if (visitor.last.equals(picArea)) {
                        if (object.equals(ScanDirectionsTypes.fromStarting)) {
                            return start + " " + closest + " " + prevAOI;
                        } else if (object.equals(ScanDirectionsTypes.towardLaning)) {
                            return finish + " " + closest + " " + takeoff;
                        }
                    } else {
                        // (nearest to)PREV AOI - MIDDLE AOI - (nearest to)NEXT AOI
                        if (object.equals(ScanDirectionsTypes.fromStarting)) {
                            return start + " " + closest + " " + prevAOI;
                        } else if (object.equals(ScanDirectionsTypes.towardLaning)) {
                            return finish + " " + closest + " " + nextAOI;
                        }
                    }

                    return languageHelper.toFriendlyName(object);
                }

                @Override
                public ScanDirectionsTypes fromString(String string) {
                    return null;
                }
            });
        scanDirectionCombo.getItems().addAll(getScanDirectionItems(areaOfInterest.getType()));
        scanDirectionCombo.valueProperty().bindBidirectional(areaOfInterest.scanDirectionProperty());
    }

    private List<ScanDirectionsTypes> getScanDirectionItems(PlanType type) {
        return Arrays.asList(ScanDirectionsTypes.class.getEnumConstants())
            .stream()
            .filter(e -> e.isAvaliable(type))
            .collect(Collectors.toList());
    }

    private void initFlightDirectionComponent(AreaOfInterest areaOfInterest) {
        flightDirectionBox.getChildren().clear();
        FlightDirectionComponent flightDirectionComponent =
            (FlightDirectionComponent)AoiFactory.FLIGHT_DIRECTION_COMPONENT.apply(areaOfInterest, context);
        flightDirectionBox.getChildren().add(flightDirectionComponent);
    }

    private void initControlsVisibility(AreaOfInterest areaOfInterest) {
        bindNodeVisibility(overlapFlightDirectionBox);
        bindNodeVisibility(rotationBox);
        bindNodeVisibility(circleBox);
        bindNodeVisibility(imagesPerCircleBox);
        bindNodeVisibility(scanDirectionBox);
        bindNodeVisibility(startCaptureBox);
        bindNodeVisibility(chkSingleDirection);
        bindNodeVisibility(restrictions3D);
        bindNodeVisibility(flightDirectionBox);
        bindNodeVisibility(scanPatternBox);
        bindNodeVisibility(verticalStartBox);
        bindNodeVisibility(cameraTiltBox);
        bindNodeVisibility(cameraPitchOffsetBox);
        bindNodeVisibility(cameraRollBox);
        bindNodeVisibility(cameraRollOffsetBox);
        bindNodeVisibility(jumpPatternBox);
        bindNodeVisibility(minOverlapFlightDirectionBox);
        bindNodeVisibility(segmentWiseLeftRightToggle);
        bindNodeVisibility(cameraRollMaxChangeBox);
        bindNodeVisibility(cameraTiltMaxChangeBox);
        bindNodeVisibility(cameraPitchOffsetLineBeginBox);

        PlanType planType = areaOfInterest.getType();

        areaOfInterest
            .typeProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    updateVisibility(newValue);
                });
        updateVisibility(planType);
    }

    private void updateVisibility(PlanType planType) {
        boolean isCopter = viewModel.isCopter();

        // several things below are removed for WINDMILL planType, but may be added later
        boolean notWindmill = (planType == null || planType != PlanType.WINDMILL);
        boolean notPanorama = (planType == null || planType != PlanType.PANORAMA);
        boolean notPOI = (planType == null || planType != PlanType.POINT_OF_INTEREST);

        overlapFlightDirectionBox.setVisible(notPanorama && notPOI);
        restrictions3D.setVisible((planType != null) && (planType.supportsCrop()));

        boolean hasCircleDirection = (planType != null) && (planType.hasCircleDirection()) && notWindmill;
        rotationBox.setVisible(hasCircleDirection);

        boolean needsImagesPerCircle = (planType != null) && (planType.needsImagesPerCircle()) && notWindmill;
        imagesPerCircleBox.setVisible(
            needsImagesPerCircle
                && settingsManager.getSection(GeneralSettings.class).getOperationLevel() == OperationLevel.DEBUG);

        boolean isCircular = (planType != null) && (planType.needsCircles() && notWindmill);
        circleBox.setVisible(
            isCircular
                && settingsManager.getSection(GeneralSettings.class).getOperationLevel() == OperationLevel.DEBUG);

        scanPatternBox.setVisible((planType != null) && (planType.supportsVerticalScanPatterns()));

        scanDirectionBox.setVisible((planType != null) && (planType.supportsScanDirection()));

        boolean supportsSingleDirection =
            (planType != null) && (planType.supportsSingleDirection()) && planType.supportsScanDirection();
        chkSingleDirection.setVisible(supportsSingleDirection);
        if (settingsManager.getSection(GeneralSettings.class).getOperationLevel() != OperationLevel.DEBUG) {
            startCaptureBox.setVisible(false);
        } else {
            startCaptureBox.setVisible(supportsSingleDirection);
        }

        flightDirectionBox.setVisible((planType != null) && (planType.supportsFlightDirection()) && notWindmill);

        verticalStartBox.setVisible((isCopter) && (planType != null) && (planType.useStartCaptureVertically()));

        cameraTiltMaxChangeBox.setVisible((isCopter) && (planType != null) && (planType.supportMaxTiltChange()));

        cameraRollMaxChangeBox.setVisible((isCopter) && (planType != null) && (planType.supportMaxRollYawChange()));

        boolean supportsSegmentScan = (planType != null) && (planType.supportsSegmentScan());
        if (settingsManager.getSection(GeneralSettings.class).getOperationLevel() != OperationLevel.DEBUG) {
            supportsSegmentScan = false;
        }

        segmentWiseLeftRightToggle.setVisible(supportsSegmentScan);

        cameraTiltBox.setVisible(isCopter && notPanorama && notPOI);

        cameraRollBox.setVisible(isCopter && notPanorama && notPOI);

        cameraPitchOffsetBox.setVisible(isCopter);

        cameraRollOffsetBox.setVisible(isCopter && notPanorama && notPOI);

        jumpPatternBox.setVisible(
            !isCopter && settingsManager.getSection(GeneralSettings.class).getOperationLevel() == OperationLevel.DEBUG);

        minOverlapFlightDirectionBox.setVisible(!isCopter);

        cameraPitchOffsetLineBeginBox.setVisible(isCopter && notWindmill && notPanorama && notPOI);
    }

    private void bindNodeVisibility(Node node) {
        node.managedProperty().bind(node.visibleProperty());
    }

}
