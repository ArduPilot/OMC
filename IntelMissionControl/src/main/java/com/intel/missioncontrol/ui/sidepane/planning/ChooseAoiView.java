/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import com.intel.missioncontrol.flightplantemplate.AreasOfInterestType;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.AoiBox;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.PlanType;
import java.util.List;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.asyncfx.beans.property.PropertyPathStore;

public class ChooseAoiView extends ViewBase<ChooseAoiViewModel> {

    private static final String CSS_TEMPLATE_EDIT_CONTENT = "templateEditContent";

    @InjectViewModel
    private ChooseAoiViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private VBox aoiMenu;

    @FXML
    private Hyperlink editPlanSettingsLabel;

    @Inject
    private ILanguageHelper languageHelper;

    private final EventHandler<MouseEvent> mouseLabelHandler =
            new EventHandler<>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    AoiBox aoiChoosen = (AoiBox) mouseEvent.getSource();
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
        aoiMenu.getChildren().clear();
        IPlatformDescription platformDescription = viewModel.getPlatformDescription();
        // TODO Put a placeholder here
        if (platformDescription != null) {
            drawAoiOptions(platformDescription);
        }
    }

    private void drawAoiOptions(IPlatformDescription platformDescription) {
        for (Map.Entry<AreasOfInterestType, List<PlanType>> entry : AreasOfInterestType.forPlatform(platformDescription).entrySet()) {
            AreasOfInterestType key = entry.getKey();
            List<PlanType> value = entry.getValue();
            if (value.isEmpty()) {
                continue;
            }

            VBox typeBox = new VBox();

            typeBox.setId(key.toString());
            typeBox.getChildren().add(createHeaderLabel(key));
            typeBox.getStyleClass().add("normal-spacing");


            for (PlanType area : value) {
                VBox aoiVbox = new AoiBox(area);

                Label title = new Label();
                title.setText(languageHelper.toFriendlyName(area));
                title.getStyleClass().add("label-bold");

                Label helpTip = new Label();
                helpTip.setText(languageHelper.getString(PlanType.class.getName() + "." + area.name() + ".tooltip"));
                helpTip.getStyleClass().add("hint-label");

                aoiVbox.setId(area.toString());
                aoiVbox.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseLabelHandler);

                aoiVbox.getChildren().add(title);
                aoiVbox.getChildren().add(helpTip);
                aoiVbox.getStyleClass().add(area.toString());
                aoiVbox.getStyleClass().add("aoi");
                aoiVbox.getStyleClass().add("form-row");
                aoiVbox.getStyleClass().add("no-label");
                aoiVbox.getStyleClass().add("label-value");

                groupPush(typeBox, aoiVbox);
            }

            groupPush(aoiMenu, typeBox);
        }

    }

    private void groupPush(Pane group, Pane child) {
        group.getChildren().add(child);
    }

    private Label createHeaderLabel(AreasOfInterestType areasOfInterestType) {
        Label tempLabel = new Label();
        tempLabel.getStyleClass().add("group-header");
        tempLabel.setText(languageHelper.toFriendlyName(areasOfInterestType));
        return tempLabel;
    }

}
