/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Window;

public class LiveVideoDialogView extends DialogView<LiveVideoDialogViewModel> {

    private DragContext dragContext;

    @FXML
    Pane layoutRoot;

    @InjectViewModel
    LiveVideoDialogViewModel viewModel;

    @Override
    protected void initializeView() {
        super.initializeView();
        Parent liveVideoView = viewModel.getLiveVideoView();

        /**
         * FIXME: here we need additional logic to compute the offsets due to decoration
         * furthermore, the auto-centering and auto-resizing should be well defined
          */
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        layoutRoot.setMaxWidth(primaryScreenBounds.getWidth()-10);
        layoutRoot.setMaxHeight(primaryScreenBounds.getHeight()-40);

        liveVideoView
            .needsLayoutProperty()
            .addListener(
                (obs, needOld, needNew) -> {
                    if (needNew) return; // we want to act when layout was done

                    layoutRoot.setMinWidth(liveVideoView.minWidth(-1));
                    layoutRoot.setMinHeight(liveVideoView.minHeight(-1));
                });

        Button btnResize = (Button)liveVideoView.lookup("#btnResize");
        btnResize.setOnMousePressed(
            ev -> {
                dragContext =
                    new DragContext(layoutRoot.getWidth(), layoutRoot.getHeight(), ev.getSceneX(), ev.getSceneY());
                try {
                    Window w = layoutRoot.getScene().getWindow();
                    dragContext.setInitialStageDimension(w.getWidth(), w.getHeight());
                } catch (NullPointerException e) {
                    // do nothing
                }

                dragContext.setMinimalDimension(layoutRoot.getMinWidth(), layoutRoot.getMinHeight());
            });

        btnResize.setOnMouseDragged(
            mouseEvent -> {
                DragContext.Dimension newDim =
                    dragContext.getNewDimension(mouseEvent.getSceneX(), mouseEvent.getSceneY());

                try {
                    Window w = layoutRoot.getScene().getWindow();
                    if (newDim.stageWidth <= layoutRoot.getMaxWidth()) w.setWidth(newDim.stageWidth);
                    if (newDim.stageHeight <= layoutRoot.getMaxHeight()) w.setHeight(newDim.stageHeight);
                } catch (NullPointerException e) {
                    // do nothing
                }
            });

        layoutRoot.getChildren().add(liveVideoView);

        viewModel
            .isDialogViewVisible()
            .addListener(
                (obs, oldVal, newVal) -> {
                    if (!newVal) layoutRoot.getChildren().clear();
                });
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return viewModel.titleProperty();
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }
}
