/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.google.inject.Inject;
import org.asyncfx.beans.property.UIAsyncListProperty;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public class LiveVideoViewModel implements ViewModel {
    private final ILiveVideoService liveVideoService;
    private final SimpleBooleanProperty isInDialog = new SimpleBooleanProperty(false);
    private final StringProperty title;
    private final String titlePrefix = "Livevideo";
    private final UIAsyncListProperty<ILiveVideoStream> videoStreamList = new UIAsyncListProperty<>(this);

    public ReadOnlyListProperty<ILiveVideoStream> videoStreamListProperty() {
        return videoStreamList.getReadOnlyProperty();
    }

    @Inject
    public LiveVideoViewModel(ILiveVideoService liveVideoService) {
        this.liveVideoService = liveVideoService;

        this.title = new SimpleStringProperty(titlePrefix);

        this.videoStreamList.bind(liveVideoService.streamListProperty());
    }

    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    SimpleBooleanProperty isInDialogProperty() {
        return isInDialog;
    }

    void setIsInDialog(boolean isInDialog) {
        this.isInDialog.set(isInDialog);
    }

    void setTitlePostfix(ObservableValue<? extends String> postfix) {
        this.title.bind(Bindings.concat(titlePrefix, ": ", postfix));
    }

    void setSelectedStream(ILiveVideoStream stream) {
        liveVideoService.setSelectedStream(stream);
    }

    ILiveVideoStream getSelectedStream() {
        return liveVideoService.getSelectedStream();
    }

}
