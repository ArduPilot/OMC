package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.layers.dataset.DatasetLayerGroup;
import com.intel.missioncontrol.map.worldwind.layers.dataset.DatasetLayerVisibilitySettings;
import com.intel.missioncontrol.mission.MatchingViewOptions;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import java.util.Map;
import java.util.function.Function;
import javafx.beans.property.Property;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.collections.ArrayMap;

public class DatasetLayerGroupViewModel extends LayerGroupViewModel {

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    public DatasetLayerGroupViewModel(
            DatasetLayerGroup datasetLayerGroup, ILanguageHelper languageHelper, GeneralSettings generalSettings) {
        super(datasetLayerGroup, languageHelper, false, true);

        final String prefix = DatasetLayerGroup.class.getName();
        Map<String, Function<MatchingViewOptions, Property<Boolean>>> items = new ArrayMap<>();
        items.put(".showPreview", MatchingViewOptions::showPreviewProperty);
        items.put(".showImageLocations", MatchingViewOptions::showImageLocationsProperty);
        items.put(".showAois", MatchingViewOptions::showAoisProperty);
        if (generalSettings.getOperationLevel() != OperationLevel.USER) {
            items.put(".showRtk", MatchingViewOptions::showRtkProperty);
        }

        items.put(".showCoverage", MatchingViewOptions::showCoverageProperty);
        items.put(".showTrack", MatchingViewOptions::showTrackProperty);

        for (Map.Entry<String, Function<MatchingViewOptions, Property<Boolean>>> entry : items.entrySet()) {
            LayerViewModel viewModel = new SimpleLayerViewModel(languageHelper.getString(prefix + entry.getKey()));
            viewModel
                .selectedProperty()
                .bindBidirectional(
                    propertyPathStore
                        .from(datasetLayerGroup.currentViewOptionsProperty())
                        .selectBoolean(entry.getValue()));
            subLayerItems.add(viewModel);
        }

        DatasetLayerVisibilitySettings datasetVisibility = datasetLayerGroup.getDatasetLayerVisibilitySettings();

        LayerViewModel viewModel =
            new SimpleLayerViewModel(
                languageHelper.getString(prefix + "." + datasetVisibility.showCurrentDatasetsProperty().getName()));
        viewModel.selected.bindBidirectional(datasetVisibility.showCurrentDatasetsProperty());
        subLayerItems.add(viewModel);

        viewModel =
            new SimpleLayerViewModel(
                languageHelper.getString(prefix + "." + datasetVisibility.showOtherDatasetsProperty().getName()));
        viewModel.selected.bindBidirectional(datasetVisibility.showOtherDatasetsProperty());
        subLayerItems.add(viewModel);

        viewModel =
            new SimpleLayerViewModel(
                languageHelper.getString(prefix + "." + datasetVisibility.showCurrentFlightplanProperty().getName()));
        viewModel.selected.bindBidirectional(datasetVisibility.showCurrentFlightplanProperty());
        subLayerItems.add(viewModel);
    }

}
