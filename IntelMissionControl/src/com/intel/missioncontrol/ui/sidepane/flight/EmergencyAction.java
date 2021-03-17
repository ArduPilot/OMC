/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.VBox;

/** @author Vladimir Iordanov */
public class EmergencyAction extends VBox implements AlertAwareComponent {

    private final ViewTuple<EmergencyActionView, EmergencyActionViewModel> viewTuple;

    public EmergencyAction() {
        viewTuple = FluentViewLoader.fxmlView(EmergencyActionView.class).root(this).load();
    }

    @Override
    public EmergencyActionViewModel getViewModel() {
        return viewTuple.getViewModel();
    }

    public String getTitleDetailsPart2() {
        return getViewModel().getTitleDetailsPart2();
    }

    public StringProperty titleDetailsPart2Property() {
        return getViewModel().titleDetailsPart2Property();
    }

    public void setTitleDetailsPart2(String titleDetails) {
        getViewModel().titleDetailsPart2Property().setValue(titleDetails);
    }

    public AlertLevel getAlert() {
        return getViewModel().alertPropery().getValue();
    }

    public Property<AlertLevel> alertProperty() {
        return getViewModel().alertPropery();
    }

    public void setAlert(AlertLevel alert) {
        getViewModel().alertPropery().setValue(alert);
    }
}
