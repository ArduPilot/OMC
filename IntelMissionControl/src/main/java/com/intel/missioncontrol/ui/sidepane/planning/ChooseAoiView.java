/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import org.asyncfx.beans.property.PropertyPathStore;
import com.intel.missioncontrol.flightplantemplate.AreasOfInterestType;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.IntelLabel;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.PlanType;
import java.util.Optional;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ChooseAoiView extends ViewBase<ChooseAoiViewModel> {

    private static final String CSS_TEMPLATE_EDIT_CONTENT = "templateEditContent";

    @InjectViewModel
    private ChooseAoiViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private VBox addAreaOfInterestVbox;

    @FXML
    private Hyperlink editPlanSettingsLabel;

    @Inject
    private ILanguageHelper languageHelper;

    private final EventHandler<MouseEvent> mouseLabelHandler =
        new EventHandler<>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                IntelLabel aoiChoosen = (IntelLabel)mouseEvent.getSource();
                viewModel.getChooseAoiCommand().execute(aoiChoosen.getArea());
            }
        };

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Override
    public void initializeView() {
        super.initializeView();

        viewModel
            .initializePageProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        initializeScreen();
                    }
                });

        editPlanSettingsLabel.setOnMousePressed(event -> viewModel.getEditPlanSettingsCommand().execute());
        propertyPathStore
            .from(viewModel.currentMissionProperty())
            .selectReadOnlyBoolean(Mission::containsTemplatesProperty)
            .addListener(
                ((observable1, oldValue1, containsTemplates) -> {
                    if (Boolean.TRUE.equals(containsTemplates)) {
                        layoutRoot.getStyleClass().add(CSS_TEMPLATE_EDIT_CONTENT);
                    } else {
                        layoutRoot.getStyleClass().remove(CSS_TEMPLATE_EDIT_CONTENT);
                    }
                }));
    }

    @Override
    public ChooseAoiViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    private void initializeScreen() {
        addAreaOfInterestVbox.getChildren().clear();
        IPlatformDescription platformDescription = viewModel.getPlatformDescription();
        // TODO Put a placeholder here
        if (platformDescription != null) {
            drawAoiOptions(platformDescription);
        }
    }

    private void drawAoiOptions(IPlatformDescription platformDescription) {
        AreasOfInterestType.forPlatform(platformDescription)
            .forEach(
                (key, value) -> {
                    if (value.isEmpty()) {
                        return;
                    }

                    GridPane tempGridPane = new GridPane();
                    tempGridPane.setId(key.toString());
                    addGridToVbox(tempGridPane, key);
                    value.forEach(
                        area -> {
                            Label tempLabel = new IntelLabel(area);
                            tempLabel.setText(languageHelper.toFriendlyName(area));
                            String tooltipText =
                                languageHelper.getString(PlanType.class.getName() + "." + area.name() + ".tooltip");
                            tempLabel.setTooltip(new Tooltip(tooltipText));
                            tempLabel.setId(area.toString());
                            tempLabel.getStyleClass().add(area.toString());
                            tempLabel.getStyleClass().add("aoi");

                            tempLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseLabelHandler);
                            // tempLabel.setMinWidth((scrollPane.getWidth() / 2));
                            organizingGridPane(tempGridPane, tempLabel);
                            tempLabel
                                .graphicProperty()
                                .addListener(
                                    (observable, oldValue, newValue) -> {
                                        Optional.ofNullable(newValue)
                                            .filter(ImageView.class::isInstance)
                                            .map(ImageView.class::cast)
                                            .ifPresent(
                                                imageView -> {
                                                    imageView.setFitHeight(ScaleHelper.emsToPixels(1.333));
                                                    imageView.setFitWidth(ScaleHelper.emsToPixels(1.333));
                                                });
                                    });
                        });
                    tempGridPane
                        .getColumnConstraints()
                        .forEach(
                            column -> {
                                column.setHgrow(Priority.SOMETIMES);
                                column.setPercentWidth(50);
                            });
                });
    }

    private void organizingGridPane(GridPane gridPane, Label label) {
        int size = gridPane.getChildren().size();
        gridPane.add(label, 0, size);
    }

    private void addGridToVbox(GridPane gridPane, AreasOfInterestType areasOfInterestType) {
        VBox tempVbox = new VBox();
        tempVbox.getChildren().add(createHeaderLabel(areasOfInterestType));
        tempVbox.getChildren().add(gridPane);
        addAreaOfInterestVbox.getChildren().add(tempVbox);
    }

    private Label createHeaderLabel(AreasOfInterestType areasOfInterestType) {
        Label tempLabel = new Label();
        tempLabel.setId("headerLabels");
        tempLabel.setText(languageHelper.toFriendlyName(areasOfInterestType));
        return tempLabel;
    }

}
