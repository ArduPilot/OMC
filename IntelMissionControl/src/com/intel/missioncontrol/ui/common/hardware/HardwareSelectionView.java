/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common.hardware;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.validation.LabelValidationRichVisualizer;
import com.intel.missioncontrol.ui.validation.LabelValidationVisualizer;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class HardwareSelectionView extends ViewBase<HardwareSelectionViewModel> {

    private static final String AIRPLANE_IMAGE_PATH = "/com/intel/missioncontrol/gfx/uav/";
    private static final String AIRPLANE_IMAGE_FILE_DEFAULT = "default.png";

    private static final Map<PlatformItemType, String> PLATFORM_CAPTION_MAP =
        ImmutableMap.<PlatformItemType, String>builder()
            .put(
                PlatformItemType.FIXED_WING,
                "com.intel.missioncontrol.ui.common.hardware.HardwareSelectionView.fixedWings")
            .put(
                PlatformItemType.MULTICOPTER,
                "com.intel.missioncontrol.ui.common.hardware.HardwareSelectionView.multicopters")
            .build();

    private static final double UAV_IMAGE_FIT_HEIGHT = ScaleHelper.emsToPixels(2.67);

    @InjectViewModel
    private HardwareSelectionViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private ComboBox<PlatformItem> uavComboBox;

    @FXML
    private ComboBox<IGenericCameraDescription> cameraComboBox;

    @FXML
    private ComboBox<ILensDescription> lensComboBox;

    @FXML
    private Label mismatchedExifAlert;

    private final ILanguageHelper languageHelper;

    private final ObjectProperty<ValidationStatus> validationStatusProperty = new SimpleObjectProperty<>();
    private LabelValidationVisualizer visualizer; // strong reference

    @Inject
    public HardwareSelectionView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public void initializeView() {
        super.initializeView();
        uavComboBox.itemsProperty().bind(viewModel.availablePlatformItemsProperty());
        uavComboBox.valueProperty().bindBidirectional(viewModel.selectedPlatformItemProperty());
        uavComboBox.setCellFactory(
            param ->
                new ListCell<PlatformItem>() {
                    @Override
                    protected void updateItem(PlatformItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null) {
                            return;
                        }

                        if (item.isCaption()) {
                            setDisable(true);
                            getStyleClass().add("title-menu-item");
                            setText(languageHelper.getString(PLATFORM_CAPTION_MAP.get(item.getItemType())));
                            setGraphic(null);
                        } else {
                            setDisable(false);
                            getStyleClass().remove("title-menu-item");
                            setText(item.getDescription().getName());
                            ImageView imageView = new ImageView();
                            imageView.setImage(createImageFromImageFile(item.getDescription().getImageFile()));
                            imageView.setFitHeight(UAV_IMAGE_FIT_HEIGHT);
                            imageView.setPreserveRatio(true);
                            setGraphic(imageView);
                        }
                    }
                });

        cameraComboBox.setConverter(
            new StringConverter<>() {
                @Override
                public String toString(IGenericCameraDescription description) {
                    return description != null ? description.getName() : "";
                }

                @Override
                public IGenericCameraDescription fromString(String string) {
                    throw new UnsupportedOperationException();
                }
            });

        cameraComboBox.itemsProperty().bind(viewModel.availableCamerasProperty());
        cameraComboBox.valueProperty().bindBidirectional(viewModel.selectedCameraProperty());
        cameraComboBox
            .disableProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () -> viewModel.availableCamerasProperty().size() <= 1, viewModel.availableCamerasProperty()));

        lensComboBox.setConverter(
            new StringConverter<ILensDescription>() {
                @Override
                public String toString(ILensDescription description) {
                    return description != null ? description.getName() : "";
                }

                @Override
                public ILensDescription fromString(String string) {
                    throw new UnsupportedOperationException();
                }
            });

        lensComboBox.itemsProperty().bind(viewModel.availableLensesProperty());
        lensComboBox.valueProperty().bindBidirectional(viewModel.selectedLensProperty());
        lensComboBox
            .disableProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () -> viewModel.availableLensesProperty().size() <= 1, viewModel.availableLensesProperty()));

        visualizer = new LabelValidationRichVisualizer(validationStatusProperty, mismatchedExifAlert);
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public HardwareSelectionViewModel getViewModel() {
        return viewModel;
    }

    public ObjectProperty<ValidationStatus> validationStatusPropertyProperty() {
        return validationStatusProperty;
    }

    public void bindHardwareConfiguration(ObjectProperty<IHardwareConfiguration> hardwareConfiguation) {
        this.viewModel.bindHardwareConfiguration(hardwareConfiguation);
    }

    private Image createImageFromImageFile(String imageFile) {
        String fileName =
            (imageFile != null && !imageFile.isEmpty())
                ? imageFile.replaceAll("[^.0-9A-Za-z_-]+", "_").replaceAll("\\.\\.", ".")
                : AIRPLANE_IMAGE_FILE_DEFAULT;

        Image res;
        try {
            res = new Image(AIRPLANE_IMAGE_PATH + fileName);
        } catch (java.lang.IllegalArgumentException ex) {
            res = new Image(AIRPLANE_IMAGE_PATH + AIRPLANE_IMAGE_FILE_DEFAULT);
        }

        return res;
    }
}
