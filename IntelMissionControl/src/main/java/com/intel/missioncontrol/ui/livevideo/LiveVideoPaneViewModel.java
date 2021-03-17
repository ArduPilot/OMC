/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.WritableImage;
import org.asyncfx.beans.property.UIAsyncListProperty;
import com.intel.missioncontrol.livevideo.ILiveVideoService;

@ScopeProvider(scopes = {UILiveVideoScope.class})
public class LiveVideoPaneViewModel extends ViewModelBase {
    private final IDialogService dialogService;

    private final SimpleBooleanProperty videoPaneVisible = new SimpleBooleanProperty();
    private final Property<UILiveVideoScope.WidgetState> widgetState = new SimpleObjectProperty<>(null);

    private final UIAsyncListProperty<IUILiveVideoStream> videoStreamList = new UIAsyncListProperty<>(this);
    private final Property<IUILiveVideoStream> selectedStream = new SimpleObjectProperty<>();

    private final Property<WritableImage> currentFrame = new SimpleObjectProperty<>();
    private IUILiveVideoStream.IUILiveVideoStreamListener streamListener =
        IUILiveVideoStream.createStreamListener(currentFrame);

    private final DelegateCommand detachCommand;

    @InjectScope
    private UILiveVideoScope liveVideoScope;

    @Inject
    public LiveVideoPaneViewModel(IDialogService dialogService, ILiveVideoService liveVideoService) {
        this.dialogService = dialogService;
        detachCommand = new DelegateCommand(() -> liveVideoScope.detach(true));
        this.videoStreamList.bind(liveVideoService.streamListProperty());
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        videoPaneVisible.bind(liveVideoScope.widgetStateProperty().isEqualTo(UILiveVideoScope.WidgetState.IN_PANE));

        widgetState.addListener(
            (obs, oldValue, newValue) -> {
                if (newValue == UILiveVideoScope.WidgetState.IN_PANE) {
                    // we became visible
                    selectedStream.bindBidirectional(liveVideoScope.selectedStreamProperty());
                } else if (oldValue == UILiveVideoScope.WidgetState.IN_PANE) {
                    // we became invisible
                    selectedStream.unbindBidirectional(liveVideoScope.selectedStreamProperty());
                    selectedStream.setValue(null);
                }

                if (newValue == UILiveVideoScope.WidgetState.DETACHED)
                    dialogService.requestDialogAsync(this, LiveVideoDialogViewModel.class, false);
            });

        selectedStream.addListener(
            (obs, oldValue, newValue) -> {
                if (newValue != null) newValue.addVideoStreamListener(streamListener);
                if (oldValue != null) oldValue.removeVideoStreamListener(streamListener);
            });

        widgetState.bind(liveVideoScope.widgetStateProperty());
    }

    ReadOnlyBooleanProperty videoPaneVisibleProperty() {
        return videoPaneVisible;
    }

    public Command getDetachCommand() {
        return detachCommand;
    }

    public ReadOnlyListProperty<IUILiveVideoStream> getStreamList() {
        return videoStreamList.getReadOnlyProperty();
    }

    Property<IUILiveVideoStream> selectedStreamProperty() {
        return selectedStream;
    }

    ReadOnlyProperty<WritableImage> currentFrameProperty() {
        return currentFrame;
    }
}
