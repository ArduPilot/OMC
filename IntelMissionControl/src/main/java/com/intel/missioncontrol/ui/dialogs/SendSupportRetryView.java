/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.google.inject.Inject;
import com.intel.missioncontrol.api.support.SupportConstants;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

/** Created by akorotenko on 7/19/17. */
public class SendSupportRetryView extends DialogView<SendSupportRetryViewModel> {

    public static final String REACH_SERVER_VIEW_TITLE =
        "com.intel.missioncontrol.ui.dialogs.SendSupportRetryView.title";

    private static final double REFRESH_ICON_SIZE = ScaleHelper.emsToPixels(1.3);

    @InjectViewModel
    private SendSupportRetryViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private TableView<Pair<String, String>> reportDetails;

    @FXML
    private TableColumn<Pair<String, String>, String> columnKey;

    @FXML
    private TableColumn<Pair<String, String>, String> columnValue;

    @FXML
    private ImageView refreshIcon;

    private final ILanguageHelper languageHelper;

    @Inject
    public SendSupportRetryView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(REACH_SERVER_VIEW_TITLE));
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        reportDetails.setItems(viewModel.getReportDetails());

        columnKey.setCellValueFactory(
            dataItem ->
                new SimpleStringProperty(
                    languageHelper.getString(
                        SupportConstants.class.getCanonicalName() + "." + dataItem.getValue().getKey())));

        columnValue.setCellValueFactory(dataItem -> new SimpleStringProperty(dataItem.getValue().getValue()));

        columnKey.setMaxWidth(1f * Integer.MAX_VALUE * 30); // 30% width
        columnValue.setMaxWidth(1f * Integer.MAX_VALUE * 70); // 70% width

        refreshIcon.setFitHeight(REFRESH_ICON_SIZE);
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected SendSupportRetryViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void tryLaterClicked() {
        viewModel.getCloseCommand().execute();
    }

    @FXML
    public void keepTryingClicked() {
        viewModel.keepTrying();
    }

    @FXML
    public void discardClicked() {
        viewModel.discard();
    }

}
