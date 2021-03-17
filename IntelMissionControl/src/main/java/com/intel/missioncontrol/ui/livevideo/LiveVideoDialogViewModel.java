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
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.UIAsyncListProperty;

public class LiveVideoDialogViewModel extends DialogViewModel {

    private final SimpleBooleanProperty dialogVisible = new SimpleBooleanProperty(false);

    private final Property<IUILiveVideoStream> selectedStream = new SimpleObjectProperty<>(null);

    public final StringProperty title = new SimpleStringProperty();

    @InjectScope
    private UILiveVideoScope liveVideoScope;

    private void changeDialogVisibilityState(
            ObservableValue<? extends Boolean> obs, Boolean oldValue, Boolean newValue) {
        if (!newValue) getCloseCommand().execute();
    }

    @Inject
    public LiveVideoDialogViewModel(ILanguageHelper languageHelper) {
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

        selectedStream.bindBidirectional(liveVideoScope.selectedStreamProperty());
    }

    public ReadOnlyListProperty<IUILiveVideoStream> getStreamList() {
        return liveVideoScope.getStreamList();
    }

    Property<IUILiveVideoStream> selectedStreamProperty() {
        return selectedStream;
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
