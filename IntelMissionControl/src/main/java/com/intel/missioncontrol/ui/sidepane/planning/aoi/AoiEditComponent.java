/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.AoiSummaryItemView;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.AoiSummaryItemViewModel;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.internal.viewloader.ResourceBundleManager;
import eu.mavinci.core.flightplan.PlanType;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AoiEditComponent extends VBox implements Initializable {

    private final AreaOfInterest aoi;
    private final Context context;
    private final IQuantityStyleProvider quantityStyleProvider;
    private final ILanguageHelper languageHelper;

    private final boolean includeGsd;
    private final boolean includeFlightDirection;

    public AoiEditComponent(
            AreaOfInterest aoi,
            Context context,
            IQuantityStyleProvider quantityStyleProvider,
            ILanguageHelper languageHelper) {
        this(aoi, context, true, true, quantityStyleProvider, languageHelper);
    }

    public AoiEditComponent(
            AreaOfInterest aoi,
            Context context,
            boolean includeGsd,
            boolean includeFlightDirection,
            IQuantityStyleProvider quantityStyleProvider,
            ILanguageHelper languageHelper) {
        this.aoi = aoi;
        this.context = context;
        this.quantityStyleProvider = quantityStyleProvider;
        this.languageHelper = languageHelper;
        this.includeGsd = includeGsd;
        this.includeFlightDirection = includeFlightDirection;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AoiEditComponent.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setResources(ResourceBundleManager.getInstance().getGlobalResourceBundle());
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public List<? extends Parent> getWidgets() {
        Ensure.notNull(aoi, "aoi");
        ArrayList<Parent> all = new ArrayList<>();
        all.addAll(getEditors());
        all.addAll(getSummaryWidgets());

        return all;
    }

    public List<? extends Parent> getEditors() {
        return AoiFactory.getWidgetBuilders(aoi.getType())
            .stream()
            .filter(f -> (includeGsd) ? (true) : (f != AoiFactory.GSD_COMPONENT))
            .filter(
                f ->
                    (includeFlightDirection)
                        ? (true)
                        : (f != AoiFactory.FLIGHT_DIRECTION_COMPONENT || aoi.getType() == PlanType.WINDMILL))
            .filter(
                f ->
                    (includeGsd)
                        ? (f != AoiFactory.RADIUS_HEIGHT_HUB_COMPONENT)
                        : (true)) // to keep hub params from side pane
            .filter(
                f -> (includeGsd) ? (f != AoiFactory.BLADES_COMPONENT) : (true)) // to keep blade params from side pane
            .map(f -> f.apply(aoi, context))
            .collect(Collectors.toList());
    }

    public List<? extends Parent> getSummaryWidgets() {
        ArrayList<Parent> all = new ArrayList<>();

        AoiSummaryFactory aoiSummaryFactory = new AoiSummaryFactory(quantityStyleProvider, languageHelper);
        AoiSummaryScope scope = aoiSummaryFactory.getAoiSummary(aoi);
        List<Parent> viewers =
            scope.getKeyValues().entrySet().stream().map(a -> loadItem(a)).collect(Collectors.toList());

        int i = 0;
        while (i < viewers.size()) {
            GridPane grid = new GridPane();
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            grid.getColumnConstraints().add(column);
            column = new ColumnConstraints();
            column.setPercentWidth(50);
            grid.getColumnConstraints().add(column);

            grid.add(viewers.get(i), 0, 0);
            i++;

            if (i < viewers.size()) {
                grid.add(viewers.get(i), 1, 0);
                i++;
            }

            grid.getStyleClass().add("form-row");
            all.add(grid);
        }

        return all;
    }

    private Parent loadItem(Map.Entry<String, ObservableValue<String>> entry) {
        ViewTuple<AoiSummaryItemView, AoiSummaryItemViewModel> loadedView =
            FluentViewLoader.fxmlView(AoiSummaryItemView.class).context(context).load();

        AoiSummaryItemViewModel itemViewModel = loadedView.getViewModel();
        itemViewModel.keyProperty().setValue(entry.getKey());
        itemViewModel.valueProperty().bind(entry.getValue());

        return loadedView.getView();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getChildren().addAll(getWidgets());
    }
}
