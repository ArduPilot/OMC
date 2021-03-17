/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;

public class LiveVideoPaneModel implements ViewModel {
    private final ILiveVideoWidgetService liveVideoWidgetService;
    private final IDialogService dialogService;
    private SimpleObjectProperty<Parent> liveVideoViewProperty = new SimpleObjectProperty<>(null);

    @Inject
    public LiveVideoPaneModel(ILiveVideoWidgetService liveVideoWidgetService, IDialogService dialogService) {
        this.liveVideoWidgetService = liveVideoWidgetService;
        this.dialogService = dialogService;
        liveVideoWidgetService.setPaneControllerViewModel(this);
        liveVideoWidgetService.setDialogControllerViewModel(this);
    }

    void makePaneVisible() {
        ViewTuple viewTuple = FluentViewLoader.fxmlView(LiveVideoView.class).load();
        ((LiveVideoViewModel)viewTuple.getViewModel()).setIsInDialog(false);

        liveVideoViewProperty.set(viewTuple.getView());
    }

    void makePaneInvisible() {
        liveVideoViewProperty.set(null);
    }

    void makeDialogVisible() {
        dialogService.requestDialogAsync(this, LiveVideoDialogViewModel.class, false);
    }

    void requestDetach() {
        liveVideoWidgetService.isDetachedProperty().set(true);
    }

    public SimpleObjectProperty<Parent> liveVideoViewPropertyProperty() {
        return liveVideoViewProperty;
    }
}
