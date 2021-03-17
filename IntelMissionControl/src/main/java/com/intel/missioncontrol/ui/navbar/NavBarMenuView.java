/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar;

import com.intel.missioncontrol.common.Expect;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NavBarMenuView<TViewModel extends DialogViewModel, TPageType extends Enum<TPageType>>
        extends NavBarDialogView<TViewModel> {

    @Override
    protected void initializeView() {
        super.initializeView();
        Expect.notNull(getMenuToggleGroup(), "getMenuToggleGroup()");
    }

    protected abstract ToggleGroup getMenuToggleGroup();

    protected abstract @Nullable Pane getContentPane();

    // Puts the radio button that corresponds to the specified page in the selected state.
    // The name of the page must be reflected in the radio button's userData property.
    //
    protected void selectRadioButton(TPageType page) {
        String pageName = page.name();
        for (Toggle toggle : getMenuToggleGroup().getToggles()) {
            Object userData = toggle.getUserData();
            if (userData instanceof String && userData.equals(pageName)) {
                toggle.setSelected(true);
            }
        }
    }

    protected void currentTabChanged(Map<@NotNull TPageType, @NotNull Region> map, TPageType newPage) {
        Pane contentPane = getContentPane();
        if (contentPane == null) {
            return;
        }

        for (int i = 0; i < contentPane.getChildren().size(); i++) {
            Node child = contentPane.getChildren().get(i);
            if (child instanceof Region) {
                Region region = (Region)contentPane.getChildren().get(i);
                region.setVisible(false);
                region.setManaged(false);
            }
        }

        Region region = map.get(newPage);
        com.intel.missioncontrol.helper.Expect.notNull(region, "region");

        region.setManaged(true);
        double prefWidth = region.getPrefWidth();
        if (prefWidth < 0) {
            region.setVisible(true);
            return;
        }

        region.setPrefWidth(prefWidth);

        if (prefWidth > contentPane.getPrefWidth()) {
            Animations.horizontalFadeInRight(region);
        } else {
            Animations.horizontalFadeInLeft(region);
        }

        Animations.animatePrefWidth(contentPane, prefWidth);
    }

}
