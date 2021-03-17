/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import de.saxsys.mvvmfx.FxmlView;

public class IconAndLabelExtended extends IconAndLabel {

    @Override
    protected Class<? extends FxmlView<? extends IconAndLabelViewModel>> getViewClass() {
        return IconAndLabelExtendedView.class;
    }

    public String getLeftLabel() {
        return getViewModel().labelLeftProperty().getValue();
    }

    public void setLeftLabel(String left) {
        getViewModel().labelLeftProperty().setValue(left);
    }

    public String getRightLabel() {
        return getViewModel().labelRightProperty().getValue();
    }

    public void setRightLabel(String right) {
        getViewModel().labelRightProperty().setValue(right);
    }

    public String getSeparator() {
        return getViewModel().separatorProperty().getValue();
    }

    public void setSeparator(String separator) {
        getViewModel().separatorProperty().setValue(separator);
    }

    @Override
    public IconAndLabelExtendedViewModel getViewModel() {
        return (IconAndLabelExtendedViewModel)super.getViewModel();
    }

}
