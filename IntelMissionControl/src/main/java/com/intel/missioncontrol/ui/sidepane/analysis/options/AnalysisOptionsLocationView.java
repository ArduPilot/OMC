/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.settings.DisplaySettings;
import com.intel.missioncontrol.settings.DisplaySettings.WorkflowHints;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.RTKBasePositionContainer;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AdornerSplitView;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.validation.visualization.ValidationVisualizer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.LocationType;
import gov.nasa.worldwind.geom.Sector;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

public class AnalysisOptionsLocationView extends ViewBase<AnalysisOptionsLocationViewModel> {

    private static final String KEY_NEW_LOCATION =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsLocationView.newLocation";

    private static final String KEY_HINT_SRS_CHECK =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsLocationView.srsCheck";
    private static final String KEY_HINT_GOT_IT =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsLocationView.hintGotIt";

    private static final String KEY_DIALOG_HEADER =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsLocationView.KEY_DIALOG_HEADER";
    private static final String KEY_DIALOG_CONTENT =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsLocationView.KEY_DIALOG_CONTENT";

    private static final String SHOW_ON_MAP =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsLocationView.btnShowOnMap";

    @InjectContext
    private Context context;

    @FXML
    private AdornerSplitView layoutRoot;

    @FXML
    private ToggleGroup rtkLocationGroup;

    @FXML
    private RadioButton rbtnRtkAssumedLocation;

    @FXML
    private RadioButton rbtnRtkManualLocation;

    @FXML
    private VBox assumedLocationBox;

    @FXML
    private VBox manualLocationBox;

    @FXML
    private Label lblSrs;

    @FXML
    private Hyperlink lnkChangeSrs;

    @FXML
    private Label lblAssumedLocationAverage;

    @FXML
    private Label lblLatitude;

    @FXML
    private Label lblLongitude;

    @FXML
    private Label lblAltitude;

    @FXML
    private Button btnShowOnMap;

    @FXML
    private Button btnConfirmCorrectLocation;

    @FXML
    private Label undoConfirmRtkLocationBox;

    @FXML
    private Button btnUndoConfirmRtkLocation;

    @FXML
    private AutoCommitSpinner<VariantQuantity> latitudeSpinner;

    @FXML
    private AutoCommitSpinner<VariantQuantity> longitudeSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> altitudeSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> geoidOffsetSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> antennaHeightSpinner;

    @FXML
    private ComboBox<RTKBasePositionContainer> rtkBaseLocationsCombo;

    @FXML
    private Button btnUseAssumedLocation;

    @FXML
    private Button btnShowOnMapManual;

    @FXML
    private Button btnSaveLocation;

    @InjectViewModel
    private AnalysisOptionsLocationViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private IDialogService dialogService;

    @Inject
    private IMapView mapView;

    @Inject
    private IApplicationContext applicationContext;

    private final List<ValidationVisualizer> validationVisualizers = new ArrayList<>();

    private Toast srsToast;
    private final IDialogContextProvider dialogContextProvider;

    @Inject
    public AnalysisOptionsLocationView(IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    public void initializeView() {
        super.initializeView();
        viewModel.init();

        dialogContextProvider.setContext(viewModel, context);
        assumedLocationBox.managedProperty().bind(assumedLocationBox.visibleProperty());
        manualLocationBox.managedProperty().bind(manualLocationBox.visibleProperty());

        /**
         * this label should bind to @see {@link
         * com.intel.missioncontrol.ui.validation.matching.RtkLocationConfirmedValidator} but the way its done here
         * works as well well enough
         */

        updateVisibility(viewModel.rtkBaseLocationProperty().getValue());

        rtkLocationGroup
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    String rtkLocation = (String)newValue.getUserData();
                    viewModel.rtkBaseLocationProperty().setValue(LocationType.valueOf(rtkLocation));
                });

        viewModel.rtkBaseLocationProperty().addListener((observable, oldValue, newValue) -> updateVisibility(newValue));

        lblSrs.textProperty().bind(viewModel.srsProperty());
        lblAssumedLocationAverage.textProperty().bind(viewModel.assumedLocationAverageProperty());
        lblLatitude.textProperty().bind(viewModel.latitudeProperty());
        lblLongitude.textProperty().bind(viewModel.longitudeProperty());
        lblAltitude.textProperty().bind(viewModel.altitudeProperty());

        btnConfirmCorrectLocation.managedProperty().bind(btnConfirmCorrectLocation.visibleProperty());
        undoConfirmRtkLocationBox.managedProperty().bind(undoConfirmRtkLocationBox.visibleProperty());

        btnConfirmCorrectLocation.visibleProperty().bind(viewModel.rtkLocationConfirmedProperty().not());
        undoConfirmRtkLocationBox.visibleProperty().bind(viewModel.rtkLocationConfirmedProperty());
        btnUndoConfirmRtkLocation.visibleProperty().bind(viewModel.rtkLocationConfirmedProperty());

        initAntennaHeightSpinner();
        initGeoidOffsetSpinner();

        rtkBaseLocationsCombo.itemsProperty().bind(viewModel.rtkStationsProperty());
        rtkBaseLocationsCombo.valueProperty().bindBidirectional(viewModel.selectedRtkStationProperty());
        rtkBaseLocationsCombo.setConverter(
            new StringConverter<RTKBasePositionContainer>() {

                @Override
                public String toString(RTKBasePositionContainer object) {
                    if ((object == null) || (object.isEmpty())) {
                        return languageHelper.getString(KEY_NEW_LOCATION);
                    }

                    return object.getName();
                }

                @Override
                public RTKBasePositionContainer fromString(String string) {
                    return null;
                }
            });

        ViewHelper.initAutoCommitSpinner(
            altitudeSpinner,
            viewModel.altitudeQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            6, // more than default "5" because in case 3 digit integer part you still want to have not less than 3
            // fractional digits
            3,
            0.0,
            1000.0,
            1.0, // step in m, otherwise it looks strange
            false);

        ViewHelper.initCoordinateSpinner(
            latitudeSpinner,
            viewModel.latitudeQuantityProperty(),
            settingsManager.getSection(GeneralSettings.class),
            true);
        ViewHelper.initCoordinateSpinner(
            longitudeSpinner,
            viewModel.longitudeQuantityProperty(),
            settingsManager.getSection(GeneralSettings.class),
            false);

        initSrsCheckHint();
    }

    private void initAntennaHeightSpinner() {
        ViewHelper.initAutoCommitSpinner(
            antennaHeightSpinner,
            viewModel.antennaHeightProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            6,
            3,
            0.0,
            100.0,
            0.001, // step im mm, because antennas are smaller
            false);
    }

    private void initGeoidOffsetSpinner() {
        ViewHelper.initAutoCommitSpinner(
            geoidOffsetSpinner,
            viewModel.geoidOffsetProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            3,
            0.0,
            1000.0,
            1.0,
            false);
    }

    private void updateVisibility(LocationType location) {
        switch (viewModel.rtkBaseLocationProperty().getValue()) {
        case ASSUMED:
            rtkLocationGroup.selectToggle(rbtnRtkAssumedLocation);
            break;
        case MANUAL:
            rtkLocationGroup.selectToggle(rbtnRtkManualLocation);
            break;
        }

        assumedLocationBox.setVisible(location == LocationType.ASSUMED);
        manualLocationBox.setVisible(location == LocationType.MANUAL);

        showSrsHint(location, isSrsHintEnabled());
    }

    private void showSrsHint(LocationType location, boolean isSrsHintEnabled) {
        if ((location == LocationType.MANUAL) && (isSrsHintEnabled)) {
            if (srsToast != null) {
                applicationContext.addToast(srsToast);
            }
        } else {
            if (srsToast != null) {
                srsToast.dismiss();
                srsToast = null;
            }
        }
    }

    private boolean isSrsHintEnabled() {
        WorkflowHints hintSettings = getWorkflowHintSettings();

        if (hintSettings == null) {
            return true;
        }

        return hintSettings.srsCheckEnabledProperty().get();
    }

    private WorkflowHints getWorkflowHintSettings() {
        DisplaySettings displaySettings = settingsManager.getSection(DisplaySettings.class);
        if (displaySettings == null) {
            return null;
        }

        return displaySettings.getWorkflowHints();
    }

    private void initSrsCheckHint() {
        final WorkflowHints hintSettings = getWorkflowHintSettings();

        if (hintSettings == null) {
            return;
        }

        srsToast =
            Toast.of(ToastType.INFO)
                .setShowIcon(true)
                .setText(languageHelper.getString(KEY_HINT_SRS_CHECK))
                .setCloseable(true)
                .setTimeout(Toast.LONG_TIMEOUT)
                .setAction(
                    languageHelper.getString(KEY_HINT_GOT_IT),
                    true,
                    true,
                    () -> hintSettings.srsCheckEnabledProperty().set(false),
                    Platform::runLater)
                .create();

        hintSettings
            .srsCheckEnabledProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    showSrsHint(viewModel.rtkBaseLocationProperty().getValue(), newValue));
    }

    @FXML
    public void confirmCorrectLocation() {
        viewModel.rtkLocationConfirmedProperty().setValue(true);
    }

    @FXML
    public void undoConfirmRtkLocation() {
        viewModel.rtkLocationConfirmedProperty().setValue(false);
    }

    @FXML
    public void changeSrs() {
        viewModel.changeSrs();
    }

    @FXML
    public void showOnMap() {
        double latitude = viewModel.latitudeValueProperty().get();
        double longitude = viewModel.longitudeValueProperty().get();
        final Sector sector = Sector.fromDegrees(latitude, latitude, longitude, longitude);
        mapView.goToSectorAsync(sector, OptionalDouble.of(viewModel.altitudeValueProperty().get()));
    }

    @FXML
    public void useAssumedLocation() {
        viewModel.useAssumedRtkPosition();
    }

    @FXML
    public void saveLocation() {
        String name = requestRtkName();

        if (StringUtils.isBlank(name)) {
            return;
        }

        viewModel.saveLocation(name.trim());
    }

    private String requestRtkName() {
        return dialogService.requestInputDialogAndWait(
            viewModel,
            languageHelper.getString(KEY_DIALOG_HEADER),
            languageHelper.getString(KEY_DIALOG_CONTENT),
            false);
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }
}
