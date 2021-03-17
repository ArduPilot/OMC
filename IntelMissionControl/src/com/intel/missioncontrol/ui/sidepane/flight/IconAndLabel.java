/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.google.common.base.Strings;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

public class IconAndLabel extends VBox implements AlertAwareComponent {

    private final IconAndLabelViewModel viewModel;
    private final Parent view;

    public IconAndLabel() {
        ViewTuple<? extends FxmlView<? extends IconAndLabelViewModel>, ? extends IconAndLabelViewModel> viewTuple =
            FluentViewLoader.fxmlView(getViewClass()).root(this).load();

        viewModel = viewTuple.getViewModel();
        view = viewTuple.getView();
    }

    protected Class<? extends FxmlView<? extends IconAndLabelViewModel>> getViewClass() {
        return IconAndLabelView.class;
    }

    public String getText() {
        return getViewModel().textProperty().getValue();
    }

    public void setText(String text) {
        getViewModel().textProperty().setValue(text);
    }

    @SuppressWarnings("deprecation")
    public String getImageUrl() {
        Image image = getViewModel().imageProperty().getValue();
        if (image == null) {
            return null;
        }

        return image.getUrl();
    }

    public void setImageUrl(String url) {
        String currentUrl = getImageUrl();

        if ((currentUrl != null) && (currentUrl.equals(url))) {
            return;
        }

        Image image = ((Strings.isNullOrEmpty(url)) ? (null) : (new Image(url)));
        getViewModel().imageProperty().setValue(image);
    }

    public AlertLevel getAlert() {
        return getViewModel().alertPropery().getValue();
    }

    public void setAlert(AlertLevel alert) {
        getViewModel().alertPropery().setValue(alert);
    }

    @Override
    public IconAndLabelViewModel getViewModel() {
        return viewModel;
    }

    public Parent getView() {
        return view;
    }

}
