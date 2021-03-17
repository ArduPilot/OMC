/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.ViewModel;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.Parent;

public abstract class AlertAwareView<T extends ViewModel> extends ViewBase<T> {

    public static final String STYLE_RED = "redRoundBorder";
    public static final String STYLE_YELLOW = "orangeRoundBorder";
    public static final String STYLE_GREEN = "greenStatus";

    @Override
    public void initializeView() {
        super.initializeView();

        // set initial alert style
        updateAlertStyleClass(null, getViewModel().alertPropery().getValue());

        getViewModel()
            .alertPropery()
            .addListener((observable, oldValue, newValue) -> updateAlertStyleClass(oldValue, newValue));
    }

    protected void updateAlertStyleClass(AlertLevel oldAlert, AlertLevel newAlert) {
        ObservableList<String> styleClass = getRootNode().getStyleClass();
        String oldStyleClass = getAlertStyleClass(oldAlert);
        String newStyleClass = getAlertStyleClass(newAlert);

        if (oldStyleClass == null) {
            styleClass.remove(STYLE_RED);
            styleClass.remove(STYLE_YELLOW);
            styleClass.remove(STYLE_GREEN);
        } else {
            styleClass.remove(oldStyleClass);
        }

        if (newStyleClass != null) {
            styleClass.add(newStyleClass);
        }
    }

    protected String getAlertStyleClass(AlertLevel alert) {
        if (alert == null) {
            return null;
        }

        switch (alert) {
        case RED:
            {
                return STYLE_RED;
            }

        case YELLOW:
            {
                return STYLE_YELLOW;
            }

        case GREEN:
        default:
            {
                return STYLE_GREEN;
            }
        }
    }

    protected abstract Parent getRootNode();

    protected abstract AlertAwareViewModel getViewModel();

}
