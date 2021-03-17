/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LiveviewViewModel extends ViewModelBase {

    private final ILiveVideoWidgetService liveVideoWidgetService;

    @Inject
    public LiveviewViewModel(ILiveVideoWidgetService liveVideoWidgetService) {

        this.liveVideoWidgetService = liveVideoWidgetService;

    }

    public String getStatusMessage() {
        return statusMessage.get();
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage.set(statusMessage);
    }

    private final StringProperty statusMessage = new SimpleStringProperty("Initialized ...");

    public void handleDetach() {
        liveVideoWidgetService.isDetachedProperty().set(true);
    }



    public ReadOnlyBooleanProperty canMakeVisibleProperty() {
        return liveVideoWidgetService.canMakeVisibleProperty();
    }

    public BooleanProperty liveVideoVisibleProperty() {
        return liveVideoWidgetService.isVisibleProperty();
    }


}
