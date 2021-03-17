/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

/**
 * this ToggleButton looks like a normal ToggleButton, but is not deselectable, this way you can make sure that as soon
 * as one item in a group is selected, it could never gets deselected again
 */
public class SegmentButton extends RadioButton {

    public SegmentButton() {
        getStyleClass().setAll("toggle-button");
    }

}
