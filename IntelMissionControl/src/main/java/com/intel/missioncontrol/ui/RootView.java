/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.google.inject.Inject;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.ViewModel;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

/**
 * Supports CSS theming capabilities for FXML views. Views that extend from RootView don't need to provide their own
 * styles for colors and common controls, they are injected into each view based on a user-configurable setting. When a
 * FXML file includes other FXML files using the fx:include tag, only the base FXML view needs to extend the RootView
 * class. All included views inherit the CSS theme files from the base view.
 */
public abstract class RootView<T extends ViewModel> extends ViewBase<T> {

    @Inject
    private ISettingsManager settingsManager;

    // When the IUserSettings::theme property changes, we unload all theme-related
    // CSS files and replace them by new theme CSS files. This will cause all views
    // to change their appearance.
    //
    private final ChangeListener<Theme> themeChangeListener =
        (observable, oldTheme, newTheme) -> {
            Parent rootNode = getRootNode();
            String[] oldThemeStylesheets = oldTheme.getStylesheets();
            ObservableList<String> stylesheets = rootNode.getStylesheets();
            ArrayList<String> existingStylesheets = new ArrayList<>();

            for (String stylesheet : stylesheets) {
                boolean isThemeStylesheet = false;
                for (String themeStylesheet : oldThemeStylesheets) {
                    if (stylesheet.equals(themeStylesheet)) {
                        isThemeStylesheet = true;
                        break;
                    }
                }

                if (!isThemeStylesheet) {
                    existingStylesheets.add(stylesheet);
                }
            }

            stylesheets.clear();
            stylesheets.addAll(newTheme.getStylesheets());
            stylesheets.addAll(existingStylesheets);
        };

    @Override
    protected void initializeView() {
        super.initializeView();

        GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);
        generalSettings.themeProperty().addListener(new WeakChangeListener<>(themeChangeListener));

        Parent rootNode = getRootNode();
        ObservableList<String> stylesheets = rootNode.getStylesheets();
        String[] existingStylesheets = new String[stylesheets.size()];
        stylesheets.toArray(existingStylesheets);
        stylesheets.clear();
        stylesheets.addAll(generalSettings.themeProperty().get().getStylesheets());
        stylesheets.addAll(existingStylesheets);
    }

}
