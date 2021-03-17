/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.livevideo.ILiveVideoService;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.property.UIAsyncListProperty;

public class UILiveVideoScope implements Scope {

    private final SimpleBooleanProperty liveVideoActive = new SimpleBooleanProperty();

    private final SimpleObjectProperty<IUILiveVideoStream> selectedStream = new SimpleObjectProperty<>();

    public enum WidgetState {
        INACTIVE,
        IN_PANE,
        DETACHED
    }

    private final ObjectProperty<WidgetState> widgetState = new SimpleObjectProperty<>(WidgetState.INACTIVE);
    private final UIAsyncListProperty<IUILiveVideoStream> videoStreamList = new UIAsyncListProperty<>(this);

    private IUILiveVideoStream lastSelectedStream = null;

    @Inject
    public UILiveVideoScope(IApplicationContext applicationContext, ILiveVideoService liveVideoService) {
        liveVideoActive.bindBidirectional(applicationContext.liveVideoActive);
        liveVideoActive.addListener((obs, oldVal, newVal) -> activate(newVal));

        this.videoStreamList.addListener((ListChangeListener.Change<? extends IUILiveVideoStream> change) -> {
            while(change.next()) {
                for (IUILiveVideoStream item: change.getAddedSubList()) {
                    if (selectedStream.get() == null) {
                        if (lastSelectedStream != null) {
                            if (item.isSameLiveVideoStream(lastSelectedStream)) {
                                selectedStream.set(item);
                                liveVideoActive.set(true);
                            }
                        } else if (item.isDefaultStream()) {
                            selectedStream.set(item);
                            liveVideoActive.set(true);
                        }
                    }
                }

                for (IUILiveVideoStream item: change.getRemoved()) {
                    if (item == selectedStream.get()) {
                        lastSelectedStream = item;
                        selectedStream.set(null);
                    }
                }
            }
        });

        this.videoStreamList.bind(liveVideoService.streamListProperty());

    }

    private void activate(boolean value) {
        WidgetState currentState = widgetState.get();
        if (value) {
            switch (currentState) {
            case INACTIVE:
                widgetState.set(WidgetState.IN_PANE);
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
            case INACTIVE:
                // this will only happen, if we run the Platform.runLater from above
                // given that always visible will be set zero before detach
                throw new RuntimeException("Illegal transition");
            case IN_PANE:
                widgetState.set(WidgetState.INACTIVE);
                break;
            case DETACHED:
                widgetState.set(WidgetState.INACTIVE);
                break;
            }
        }
    }

    void detach(boolean value) {
        WidgetState currentState = widgetState.get();

        if (value) {
            switch (currentState) {
            case INACTIVE:
                throw new RuntimeException("Not implemented");
            case IN_PANE:
                widgetState.set(WidgetState.DETACHED);
                break;
            case DETACHED:
                throw new RuntimeException("Illegal transition");
            }
        } else {
            switch (currentState) {
            case INACTIVE:
                throw new RuntimeException("Illegal transition");
            case IN_PANE:
                throw new RuntimeException("Illegal transition");
            case DETACHED:
                widgetState.set(WidgetState.IN_PANE);
                break;
            }
        }
    }

    ReadOnlyObjectProperty<WidgetState> widgetStateProperty() {
        return widgetState;
    }

    Property<IUILiveVideoStream> selectedStreamProperty() {
        return selectedStream;
    }

    ReadOnlyListProperty<IUILiveVideoStream> getStreamList() {
        return videoStreamList.getReadOnlyProperty();
    }
}
