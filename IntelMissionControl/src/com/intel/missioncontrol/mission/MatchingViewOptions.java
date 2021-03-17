/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.ui.sidepane.analysis.ImageChannel;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ProjectionType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MatchingViewOptions {

    private BooleanProperty showPreview = new SimpleBooleanProperty(this, "showPreview", false);
    private BooleanProperty showImageLocations = new SimpleBooleanProperty(this, "showImageLocations", false);
    private BooleanProperty showAois = new SimpleBooleanProperty(this, "showAois", true);
    private BooleanProperty showRtk = new SimpleBooleanProperty(this, "showRtk", false);
    private BooleanProperty showCoverage = new SimpleBooleanProperty(this, "showCoverage", false);
    private BooleanProperty showAnnotation = new SimpleBooleanProperty(this, "showAnnotation", false);
    private BooleanProperty showTrack = new SimpleBooleanProperty(this, "showTrack", false);
    private ObjectProperty<ProjectionType> imageProjection =
        new SimpleObjectProperty<>(this, "imageProjection", ProjectionType.SURVEYS_2D);
    private DoubleProperty projectionDistance = new SimpleDoubleProperty(this, "projectionDistance");
    private DoubleProperty elevationOffset = new SimpleDoubleProperty(this, "elevationOffset");
    private final ListProperty<ImageChannel> availableChannels =
        new SimpleListProperty<>(this, "availableChannels", FXCollections.observableArrayList());
    private final ObjectProperty<ImageChannel> selectedChannel = new SimpleObjectProperty<>(this, "selectedChannel");

    public boolean getShowPreview() {
        return showPreview.get();
    }

    public BooleanProperty showPreviewProperty() {
        return showPreview;
    }

    public void setShowPreview(boolean showPreview) {
        this.showPreview.set(showPreview);
    }

    public boolean getShowImageLocations() {
        return showImageLocations.get();
    }

    public BooleanProperty showImageLocationsProperty() {
        return showImageLocations;
    }

    public void setShowImageLocations(boolean showImageLocations) {
        this.showImageLocations.set(showImageLocations);
    }

    public boolean getShowAois() {
        return showAois.get();
    }

    public BooleanProperty showAoisProperty() {
        return showAois;
    }

    public void setShowAois(boolean showAois) {
        this.showAois.set(showAois);
    }

    public boolean getShowRtk() {
        return showRtk.get();
    }

    public BooleanProperty showRtkProperty() {
        return showRtk;
    }

    public void setShowRtk(boolean showRtk) {
        this.showRtk.set(showRtk);
    }

    public boolean getShowCoverage() {
        return showCoverage.get();
    }

    public BooleanProperty showCoverageProperty() {
        return showCoverage;
    }

    public void setShowCoverage(boolean showCoverage) {
        this.showCoverage.set(showCoverage);
    }

    public boolean getShowAnnotation() {
        return showAnnotation.get();
    }

    public BooleanProperty showAnnotationProperty() {
        return showAnnotation;
    }

    public void setShowTrack(boolean showTrack) {
        this.showTrack.set(showTrack);
    }

    public boolean getShowTrack() {
        return showTrack.get();
    }

    public BooleanProperty showTrackProperty() {
        return showTrack;
    }

    public void setShowAnnotation(boolean showAnnotation) {
        this.showAnnotation.set(showAnnotation);
    }

    public ProjectionType getImageProjection() {
        return imageProjection.get();
    }

    public ObjectProperty<ProjectionType> imageProjectionProperty() {
        return imageProjection;
    }

    public void setImageProjection(ProjectionType imageProjection) {
        this.imageProjection.set(imageProjection);
    }

    public double getProjectionDistance() {
        return projectionDistance.get();
    }

    public DoubleProperty projectionDistanceProperty() {
        return projectionDistance;
    }

    public void setProjectionDistance(double projectionDistance) {
        this.projectionDistance.set(projectionDistance);
    }

    public ObservableList<ImageChannel> getAvailableChannels() {
        return availableChannels.get();
    }

    public ListProperty<ImageChannel> availableChannelsProperty() {
        return availableChannels;
    }

    public ImageChannel getSelectedChannel() {
        return selectedChannel.get();
    }

    public Property<ImageChannel> selectedChannelProperty() {
        return selectedChannel;
    }

    public DoubleProperty elevationOffsetProperty() {
        return elevationOffset;
    }
}
