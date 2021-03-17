/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class LiveVideoWidgetService implements ILiveVideoWidgetService {

    private final ILiveVideoService liveVideoService;
    private final SimpleBooleanProperty isVisible = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isDetached = new SimpleBooleanProperty(false);
    private LiveVideoPaneModel paneCtrlModel = null;
    private LiveVideoPaneModel dialogCtrlModel = null;
    private boolean inTransition = false;

    private enum WidgetState {
        INVISIBLE,
        IN_PANE,
        DETACHED
    }

    private WidgetState currentState = WidgetState.INVISIBLE;

    @Override
    public ReadOnlyBooleanProperty canMakeVisibleProperty() {
        return liveVideoService.availableProperty();
    }

    @Override
    public BooleanProperty isVisibleProperty() {
        return isVisible;
    }

    @Override
    public BooleanProperty isDetachedProperty() {
        return isDetached;
    }

    @Override
    public void setDialogControllerViewModel(LiveVideoPaneModel viewModel) {
        if (dialogCtrlModel != null)
            throw new RuntimeException("setDialogControllerViewModel should be called only once");
        dialogCtrlModel = viewModel;
    }

    @Override
    public void setPaneControllerViewModel(LiveVideoPaneModel viewModel) {
        if (paneCtrlModel != null)
            throw new RuntimeException("setDialogControllerViewModel should be called only once");
        paneCtrlModel = viewModel;
    }

    @Inject
    public LiveVideoWidgetService(ILiveVideoService liveVideoService) {
        this.liveVideoService = liveVideoService;

        isVisible.addListener(
            (obs, oldVal, newVal) -> {
                if (inTransition) return;
                if (newVal) {
                    switch (currentState) {
                    case INVISIBLE:
                        goFromInvisibleToInPane();
                        break;
                    case DETACHED:
                        // detached should be changed first before visible
                        throw new RuntimeException("Illegal transition");
                    case IN_PANE:
                        // this should not happen, right?
                        throw new RuntimeException("Illegal transition");
                    }
                } else {
                    switch (currentState) {
                    case INVISIBLE:
                        // this will only happen, if we run the Platform.runLater from above
                        // given that always visible will be set zero before detach
                        throw new RuntimeException("Illegal transition");
                    case IN_PANE:
                        goFromInPaneToInvisible();
                        break;
                    case DETACHED:
                        goFromDetachedToInvisible();
                        break;
                    }
                }
            });

        isDetached.addListener(
            (obs, oldVal, newVal) -> {
                if (inTransition) return;
                if (newVal) {
                    switch (currentState) {
                    case INVISIBLE:
                        goFromInvisibleToDetached();
                        break;
                    case IN_PANE:
                        goFromInPaneToDetached();
                        break;
                    case DETACHED:
                        throw new RuntimeException("Illegal transition");
                    }
                } else {
                    switch (currentState) {
                    case INVISIBLE:
                        throw new RuntimeException("Illegal transition");
                    case IN_PANE:
                        throw new RuntimeException("Illegal transition");
                    case DETACHED:
                        goFromDetachedToInPane();
                        break;
                    }
                }
            });
    }

    private void goFromInvisibleToInPane() {
        if (paneCtrlModel != null) {
            paneCtrlModel.makePaneVisible();
            currentState = WidgetState.IN_PANE;
        } else {
            inTransition = true;
            Platform.runLater(
                () -> {
                    isVisible.set(false);
                    inTransition = false;
                });
        }
    }

    private void goFromInvisibleToDetached() {
        if (dialogCtrlModel != null) {
            inTransition = true;
            isVisible.set(true);
            inTransition = false;
            dialogCtrlModel.makeDialogVisible();
            currentState = WidgetState.DETACHED;
        } else {
            inTransition = true;
            Platform.runLater(
                () -> {
                    isDetached.set(false);
                    inTransition = false;
                });
        }
    }

    private void goFromInPaneToDetached() {
        paneCtrlModel.makePaneInvisible();

        // FIXME: should we really take care of these cases?
        if (dialogCtrlModel != null) {
            dialogCtrlModel.makeDialogVisible();
            currentState = WidgetState.DETACHED;
        }
    }

    private void goFromInPaneToInvisible() {
        paneCtrlModel.makePaneInvisible();
        currentState = WidgetState.INVISIBLE;
    }

    private void goFromDetachedToInPane() {
        paneCtrlModel.makePaneVisible();
        currentState = WidgetState.IN_PANE;
    }

    private void goFromDetachedToInvisible() {
        // we don't need to call close on the widget, because it closes itself and then notifies us
        inTransition = true;
        isDetached.set(false);
        inTransition = false;
        currentState = WidgetState.INVISIBLE;
    }
}
