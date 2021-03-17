/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.Animations;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

@DefaultProperty(value = "tabs")
public class FancyTabPane<TabIdType extends Enum<TabIdType>> extends Region {

    private static class TabData<TabIdType extends Enum<TabIdType>> {
        final FancyTab<TabIdType> tab;
        final BorderPane pane;

        TabData(FancyTab<TabIdType> tab, BorderPane pane) {
            this.tab = tab;
            this.pane = pane;
        }
    }

    private final Map<TabIdType, TabData<TabIdType>> tabsMap = new HashMap<>();
    private final ObservableList<FancyTab<TabIdType>> tabs = FXCollections.observableArrayList();
    private final ObjectProperty<TabIdType> selectedTab = new SimpleObjectProperty<>();
    private final StackPane stackPane = new StackPane();

    private Class<TabIdType> tabIdType;

    public FancyTabPane() {
        getChildren().add(stackPane);
        stackPane.prefWidthProperty().bind(widthProperty());
        stackPane.prefHeightProperty().bind(heightProperty());
        stackPane.maxWidthProperty().bind(maxWidthProperty());
        stackPane.maxHeightProperty().bind(maxHeightProperty());
        stackPane.minWidthProperty().bind(minWidthProperty());
        stackPane.minHeightProperty().bind(minHeightProperty());
        tabs.addListener(this::tabsChanged);
        selectedTab.addListener(this::selectedTabChanged);
    }

    public void setTabIdType(Class<TabIdType> tabIdType) {
        Expect.notNull(tabIdType, "tabIdType");

        if (this.tabIdType != null) {
            throw new IllegalStateException("Tab id type can only be set once.");
        }

        this.tabIdType = tabIdType;

        for (FancyTab<TabIdType> tab : tabs) {
            if (!tab.getTabId().getClass().isAssignableFrom(tabIdType)) {
                throw new IllegalStateException(
                    "Tab id of "
                        + tab.getClass().getSimpleName()
                        + " has type "
                        + tab.getTabId().getClass().getSimpleName()
                        + ", but should be "
                        + tabIdType.getSimpleName()
                        + ".");
            }
        }
    }

    public Class<TabIdType> getTabIdType() {
        return tabIdType;
    }

    public List<FancyTab<TabIdType>> getTabs() {
        return tabs;
    }

    public ObjectProperty<TabIdType> selectedTabProperty() {
        return selectedTab;
    }

    public TabIdType getSelectedTab() {
        return selectedTab.get();
    }

    public void setSelectedTab(TabIdType selectedTab) {
        this.selectedTab.set(selectedTab);
    }

    private void tabsChanged(ListChangeListener.Change<? extends FancyTab<TabIdType>> change) {
        stackPane.getChildren().clear();
        tabsMap.clear();

        for (FancyTab<TabIdType> tab : change.getList()) {
            if (!FancyTab.Autowiring.hasView(tab)) {
                TabIdType id = tab.getTabId();
                String pageType = id != null ? id.getClass().getSimpleName() : "N/A";
                String pageName = id != null ? id.toString() : "N/A";
                throw new RuntimeException(
                    "No suitable view controller found for "
                        + tab.getClass().getSimpleName()
                        + "<"
                        + pageType
                        + ">[page="
                        + pageName
                        + "]. Please make sure the view controller extends "
                        + FancyTabView.class.getSimpleName()
                        + "<"
                        + pageType
                        + ">.");
            }

            BorderPane root = new BorderPane();
            root.setVisible(false);
            root.setManaged(false);

            Node header = tab.getHeader();
            if (header != null) {
                root.setTop(header);
            }

            Node content = tab.getContent();
            if (content != null) {
                root.setCenter(content);
            }

            Node scrollingContent = tab.getScrollingContent();
            if (scrollingContent != null) {
                ScrollPane scrollPane = new ScrollPane(scrollingContent);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setFitToWidth(true);
                root.setCenter(scrollPane);
            }

            Node footer = tab.getFooter();
            if (footer != null) {
                root.setBottom(footer);
            }

            stackPane.getChildren().add(root);
            tabsMap.put(tab.getTabId(), new TabData<>(tab, root));
        }
    }

    private void selectedTabChanged(
            ObservableValue<? extends TabIdType> observable, TabIdType oldValue, TabIdType newValue) {
        final TabData<TabIdType> oldTabData = tabsMap.get(oldValue);
        final TabData<TabIdType> newTabData = tabsMap.get(newValue);

        if (oldTabData != null) {
            oldTabData.pane.setVisible(false);
            oldTabData.pane.setManaged(false);
        }

        if (newTabData != null) {
            newTabData.pane.setVisible(true);
            newTabData.pane.setManaged(true);
        }

        Node newContent = newTabData != null && newTabData.tab != null ? newTabData.tab.getContent() : null;
        if (newContent != null) {
            Animations.horizontalFadeInLeft(newContent);
        }

        Node newScrollingContent =
            newTabData != null && newTabData.tab != null ? newTabData.tab.getScrollingContent() : null;
        if (newScrollingContent != null) {
            Animations.horizontalFadeInLeft(newScrollingContent);
        }

        Node newFooter = newTabData != null && newTabData.tab != null ? newTabData.tab.getFooter() : null;
        if (newFooter != null) {
            Animations.verticalFadeInUp(newFooter);
        }
    }

}
