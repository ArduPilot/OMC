/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.livevideo.ILiveVideoService;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.WritableImage;
import org.asyncfx.beans.property.UIAsyncListProperty;

public class LiveVideoDialogViewModel extends DialogViewModel {

    private final SimpleBooleanProperty dialogVisible = new SimpleBooleanProperty(false);

    private final Property<WritableImage> currentFrame = new SimpleObjectProperty<>();
    private IUILiveVideoStream.IUILiveVideoStreamListener streamListener =
        IUILiveVideoStream.createStreamListener(currentFrame);

    private final UIAsyncListProperty<IUILiveVideoStream> videoStreamList = new UIAsyncListProperty<>(this);
    private final Property<IUILiveVideoStream> selectedStream = new SimpleObjectProperty<>(null);

    public final StringProperty title = new SimpleStringProperty();

    @InjectScope
    private UILiveVideoScope liveVideoScope;

    private void changeDialogVisibilityState(
            ObservableValue<? extends Boolean> obs, Boolean oldValue, Boolean newValue) {
        if (newValue == false) getCloseCommand().execute();
    }

    @Inject
    public LiveVideoDialogViewModel(ILiveVideoService liveVideoService, ILanguageHelper languageHelper) {
        this.videoStreamList.bind(liveVideoService.streamListProperty());
        title.bind(
            Bindings.createStringBinding(
                () -> {
                    if (selectedStream.getValue() == null)
                        return languageHelper.getString(LiveVideoDialogViewModel.class, "NoStreamSelected");
                    else return selectedStream.getValue().toString();
                },
                selectedStream));
    }

    @Override
    public void initializeViewModel() {
        super.initializeViewModel();

        dialogVisible.bind(liveVideoScope.widgetStateProperty().isEqualTo(UILiveVideoScope.WidgetState.DETACHED));
        dialogVisible.addListener(this::changeDialogVisibilityState);

        selectedStream.addListener(
            (obs, oldValue, newValue) -> {
                if (newValue != null) newValue.addVideoStreamListener(streamListener);
                if (oldValue != null) oldValue.removeVideoStreamListener(streamListener);
            });

        selectedStream.bindBidirectional(liveVideoScope.selectedStreamProperty());
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

    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    @Override
    protected void onClosing() {
        dialogVisible.removeListener(this::changeDialogVisibilityState);
        selectedStream.unbindBidirectional(liveVideoScope.selectedStreamProperty());
        selectedStream.setValue(null);
        if (liveVideoScope.widgetStateProperty().get() == UILiveVideoScope.WidgetState.DETACHED) {
            // someone pressed the 'x' manually
            liveVideoScope.detach(false);
        } else {
            // someone change the detached state from somewhere else
        }

        super.onClosing();
    }
}
