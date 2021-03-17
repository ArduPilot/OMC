/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Parent;

public class LiveVideoDialogViewModel extends DialogViewModel {

    private final ILiveVideoWidgetService liveVideoWidgetService;

    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    private final SimpleBooleanProperty isVisible = new SimpleBooleanProperty(false);

    public final SimpleStringProperty title = new SimpleStringProperty("Empty title");

    @Inject
    public LiveVideoDialogViewModel(ILiveVideoWidgetService liveVideoWidgetService) {
        this.liveVideoWidgetService = liveVideoWidgetService;

        isVisible.bind(liveVideoWidgetService.isVisibleProperty());

        isVisible.addListener(
            (obs, oldVal, newVal) -> {
                if (!newVal) getCloseCommand().execute();
            });
    }

    Parent getLiveVideoView() {
        ViewTuple viewTuple = FluentViewLoader.fxmlView(LiveVideoView.class).load();
        LiveVideoViewModel liveVideoModel = (LiveVideoViewModel)viewTuple.getViewModel();
        liveVideoModel.setIsInDialog(true);
        title.bind(liveVideoModel.titleProperty());

        return viewTuple.getView();
    }

    public ReadOnlyBooleanProperty isDialogViewVisible() {
        return isVisible;
    }

    @Override
    protected void onClosing() {
        liveVideoWidgetService.isDetachedProperty().set(false);
        isVisible.unbind();
        super.onClosing();
    }
}
