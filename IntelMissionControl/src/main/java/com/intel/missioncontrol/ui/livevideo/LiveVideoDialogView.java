/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class LiveVideoDialogView extends DialogView<LiveVideoDialogViewModel> {

    @FXML
    Pane root;

    @FXML
    private com.intel.missioncontrol.ui.controls.Button btnResize;

    @FXML
    private ComboBox<IUILiveVideoStream> comboBoxStreams;

    @FXML
    private LiveVideoScreenView liveVideoScreenController;

    @FXML
    protected void selectPrevCam() { comboBoxStreams.getSelectionModel().selectPrevious(); }

    @FXML
    protected void selectNextCam() { comboBoxStreams.getSelectionModel().selectNext(); }

    private final ILanguageHelper languageHelper;

    @Inject
    public LiveVideoDialogView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @InjectViewModel
    LiveVideoDialogViewModel viewModel;

    private Point2D dragStartPos;
    private Point2D dragStartContentSize;
    private Point2D dragStartStageSize;

    @Override
    protected void initializeView() {
        super.initializeView();

        ReadOnlyObjectProperty<Pane> rootProperty = new SimpleObjectProperty<>(this.root);

        Bindings.select(rootProperty, "scene", "window")
            .addListener(
                (obs, oldValue, newValue) -> {
                    if (newValue instanceof Window) {
                        Window window = (Window)newValue;
                        window.addEventHandler(
                            WindowEvent.WINDOW_SHOWN,
                            windowEvent -> {
                                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

                                if (window.getWidth() > primaryScreenBounds.getWidth()) {
                                    window.setX(primaryScreenBounds.getMinX());
                                    window.setWidth(primaryScreenBounds.getWidth());
                                }

                                if (window.getHeight() > primaryScreenBounds.getHeight()) {
                                    window.setY(primaryScreenBounds.getMinY());
                                    window.setHeight(primaryScreenBounds.getHeight());
                                }
                            });
                        // stage.setMaximized(true);
                    }
                });

        /*
         * FIXME: here we need additional logic to compute the offsets due to decoration furthermore, the auto-centering
         * and auto-resizing should be well defined FIXME: this needs to be done in the DialogService!
         */
        btnResize.setOnMousePressed(
            mouseEvent -> {
                dragStartPos = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                dragStartContentSize = new Point2D(root.getWidth(), root.getHeight());
                dragStartStageSize = null;
                try {
                    Window w = root.getScene().getWindow();
                    dragStartStageSize = new Point2D(w.getWidth(), w.getHeight());
                } catch (NullPointerException e) {
                    // do nothing
                }
            });

        btnResize.setOnMouseDragged(
            mouseEvent -> {
                if (dragStartStageSize == null) return;

                Point2D draggedPos = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                Point2D dragOffset = draggedPos.subtract(dragStartPos);

                Stage s = null;
                try {
                    Window w = this.root.getScene().getWindow();
                    if (w instanceof Stage) s = (Stage)w;
                } catch (NullPointerException e) {
                    // do nothing
                }

                if (s == null) return;

                Point2D draggedContentSize = dragStartContentSize.add(dragOffset);
                Point2D draggedStageSize = dragStartStageSize.add(dragOffset);

                final double minWidth = (root.getMinWidth() >= 0) ? root.getMinWidth() : root.minWidth(-1);
                final double maxWidth = (root.getMaxWidth() >= 0) ? root.getMaxWidth() : root.maxWidth(-1);
                final double minHeight = (root.getMinHeight() >= 0) ? root.getMinHeight() : root.minHeight(-1);
                final double maxHeight = (root.getMaxHeight() >= 0) ? root.getMaxHeight() : root.maxHeight(-1);

                // TODO: if out of bounds, min and max values could be set
                if (draggedContentSize.getX() > minWidth && draggedContentSize.getX() < maxWidth)
                    s.setWidth(draggedStageSize.getX());

                if (draggedContentSize.getY() > minHeight && draggedContentSize.getY() < maxHeight)
                    s.setHeight(draggedStageSize.getY());
            });

        StreamComboBoxHelper.setupComboBox(comboBoxStreams, languageHelper);
        StreamComboBoxHelper.bindBidirectional(comboBoxStreams.getSelectionModel(), viewModel.selectedStreamProperty());

        ((LiveVideoScreenViewModel)liveVideoScreenController.getViewModel())
                .streamProperty()
                .bind(viewModel.selectedStreamProperty());
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return viewModel.titleProperty();
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    public ViewModel getViewModel() {
        return viewModel;
    }
}
