package com.intel.missioncontrol.ui.search;

import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.scene.control.ToggleButton;

public class SearchHistoryItemView extends ToggleButton implements JavaView<SearchHistoryItemViewModel> {

    @InjectViewModel
    private SearchHistoryItemViewModel viewModel;

    public void initialize() {
        getStyleClass().add("search-result-button");
        setText(viewModel.getText() + "...");
    }

}