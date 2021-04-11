package com.intel.missioncontrol.ui.sidepane.start;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ProjectItemView extends ViewBase<ProjectItemViewModel> {

    private static final String SR_TOOLTIP_PREFIX = "tooltipPrefix";

    @InjectViewModel
    private ProjectItemViewModel viewModel;

    @FXML
    private RadioButton layoutRoot;

    @FXML
    private Label nameLabel;

    @FXML
    private Label lastUpdateLabel;

    @FXML
    private Pane cloudIcon;

    @FXML
    private Tooltip fullName;

    @FXML
    private VBox flightPlansInfo;

    @FXML
    private VBox flightsInfo;

    @FXML
    private VBox datasetsInfo;

    @FXML
    private Label flightPlansCountLabel;

    @FXML
    private Label flightsCountLabel;

    @FXML
    private Label datasetsCountLabel;

    @FXML
    private ImageView imageView;

    private final ILanguageHelper languageHelper;

    @Inject
    public ProjectItemView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        layoutRoot.getStyleClass().remove("radio-button");
        layoutRoot.getStyleClass().add("table-row-cell");

        nameLabel.setText(viewModel.getName());
        fullName.setText(languageHelper.getString(ProjectItemView.class, SR_TOOLTIP_PREFIX) + viewModel.getName());

        flightPlansInfo.setVisible(viewModel.hasFlightPlans());
        flightPlansInfo.setManaged(viewModel.hasFlightPlans());

        flightsInfo.setVisible(viewModel.hasFlights());
        flightsInfo.setManaged(viewModel.hasFlights());

        datasetsInfo.setVisible(viewModel.hasDatasets());
        datasetsInfo.setManaged(viewModel.hasDatasets());

        flightPlansCountLabel.setText(String.valueOf(viewModel.getFlightPlansCount()));
        flightsCountLabel.setText(String.valueOf(viewModel.getFlightsCount()));
        datasetsCountLabel.setText(String.valueOf(viewModel.getDatasetsCount()));

        lastUpdateLabel.setText(viewModel.getLastUpdate());
        cloudIcon.setVisible(viewModel.isRemote());
        cloudIcon.setManaged(viewModel.isRemote());

        viewModel.imageProperty().addListener((observable, oldValue, newValue) -> updateScreenshot(newValue));
        updateScreenshot(viewModel.imageProperty().get());
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private void updateScreenshot(Image image) {
        imageView.setImage(image);
    }

}
