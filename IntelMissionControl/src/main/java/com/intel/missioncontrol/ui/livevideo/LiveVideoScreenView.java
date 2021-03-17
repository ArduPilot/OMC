/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class LiveVideoScreenView extends ViewBase<LiveVideoScreenViewModel> {

    @InjectViewModel
    private LiveVideoScreenViewModel viewModel;

    @FXML
    private Pane root;

    @FXML
    private Label labelUnavailable;

    @FXML
    private Label labelConnReconnText;

    @FXML
    private Node labelConnReconn;

    @FXML
    private ImageView progressSpinner;

    @FXML
    private WrappedImageView liveVideo;

    private RotateTransition progressAnimation = new RotateTransition(Duration.seconds(2));

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    public ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        progressAnimation.setNode(progressSpinner);
        progressAnimation.setInterpolator(Interpolator.LINEAR);
        progressAnimation.setFromAngle(0);
        progressAnimation.setToAngle(360);
        progressAnimation.setRate(1);
        progressAnimation.setCycleCount(Animation.INDEFINITE);
        progressAnimation.play();

        progressSpinner
            .fitHeightProperty()
            .bind(
                Bindings.createDoubleBinding(
                    () -> {
                        return labelConnReconnText.getFont().getSize() * 1.33;
                    },
                    labelConnReconnText.fontProperty()));
        labelConnReconn
            .managedProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () -> {
                        State cur = viewModel.stateProperty().get();
                        return cur == State.CONNECTING || cur == State.RECONNECTING;
                    },
                    viewModel.stateProperty()));
        labelConnReconn.visibleProperty().bind(labelConnReconn.managedProperty());

        labelConnReconnText
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        State cur = viewModel.stateProperty().get();
                        if (cur == State.CONNECTING) return "Connecting";
                        else return "Reconnecting";
                    },
                    viewModel.stateProperty()));

        labelUnavailable.managedProperty().bind(viewModel.stateProperty().isEqualTo(State.FAILED));
        labelUnavailable.visibleProperty().bind(labelUnavailable.managedProperty());

        liveVideo.imageProperty().bind(viewModel.currentFrameProperty());
    }
}
