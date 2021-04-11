package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.LayerGroup;
import com.intel.missioncontrol.map.worldwind.GeotiffLayerWrapper;
import com.intel.missioncontrol.settings.GeoTiffsSettings;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.ViewModel;

public class GeoTiffLayerGroupViewModel extends LayerGroupViewModel {

    private final IDialogService dialogService;
    private final ViewModel parent;

    public GeoTiffLayerGroupViewModel(
            LayerGroup layerGroup,
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            ViewModel parent,
            GeoTiffsSettings geoTiffsSettings) {
        super(layerGroup, languageHelper, false, true);

        subLayerItems.bindContent(
            layerGroup.subLayersProperty(),
            layer -> new GeotiffLayerViewModel(geoTiffsSettings, (GeotiffLayerWrapper)layer, languageHelper));

        this.dialogService = dialogService;
        this.parent = parent;
        // if needed to delete geotiff groups same way as wms change here
        setHasSettings(true);
    }

    @Override
    protected void onOpenSettings() {
        dialogService.requestDialogAsync(parent, GeoTiffExternalSourceViewModel.class, false);
    }

}
