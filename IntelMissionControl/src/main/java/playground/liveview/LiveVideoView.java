/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.intel.missioncontrol.ui.controls.Button;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class LiveVideoView implements FxmlView<LiveVideoViewModel> {

    private WritableImage writableImage;
    private boolean checkedForFullScreen = false;

    @FXML
    private Pane root;

    @FXML
    private Button btnPrevCam;

    @FXML
    private Button btnNextCam;

    @FXML
    private ComboBox<ILiveVideoStream> comboBoxCamera;

    @FXML
    private Button btnDetach;

    @FXML
    private Button btnResize;

    @FXML
    private HBox controlBar;

    @FXML
    private CheckBox checkBox;

    @FXML
    private VBox liveVideoContainer;

    @FXML
    private WrappedImageView liveVideo;

    @FXML
    protected void selectPrevCam(ActionEvent event) {
        comboBoxCamera.getSelectionModel().selectPrevious();
    }

    @FXML
    protected void selectNextCam(ActionEvent event) {
        comboBoxCamera.getSelectionModel().selectNext();
    }

    @InjectViewModel
    private LiveVideoViewModel viewModel;

    private Property<ILiveVideoStream> selectedStream = new SimpleObjectProperty<>();

    @FXML
    protected void initialize() {

        /* define handlers first */
        ChangeListener<Bounds> handleLiveViewResize =
            (obs, oldVal, newVal) -> {
                ILiveVideoStream stream = selectedStream.getValue();
                if (stream != null) stream.requestVideoSize((int)newVal.getWidth(), (int)newVal.getHeight());
            };

        ILiveVideoStream.ILiveVideoStreamListener streamListener =
            new ILiveVideoStream.ILiveVideoStreamListener() {
                @Override
                public void updateFrame(ILiveVideoStream.ImageFrame frame) {
                    final int width = frame.scaledWidth;
                    final int height = frame.scaledHeight;
                    if (writableImage == null
                            || writableImage.getWidth() != width
                            || writableImage.getHeight() != height) {
                        writableImage = new WritableImage(width, height);
                        liveVideo.setImage(writableImage);
                    }

                    /* FIXME: this is a dirty and insecure hack, to become fullscreen */
                    if (!checkedForFullScreen) {
                        checkedForFullScreen = true;
                        if (viewModel.isInDialogProperty().get()) {
                            liveVideoContainer.setPrefSize(frame.origWidth, frame.origHeight);
                            root.getScene().getWindow().sizeToScene();
                            root.getScene().getWindow().centerOnScreen();
                        }
                    }

                    liveVideo.setResolution(frame.origWidth, frame.origHeight);

                    writableImage
                        .getPixelWriter()
                        .setPixels(
                            0, 0, width, height, PixelFormat.getByteBgraPreInstance(), frame.pixelBuffer, 4 * width);
                }

                @Override
                public void cancelled() {}

            };

        /* attach bindings */
        comboBoxCamera.itemsProperty().bind(viewModel.videoStreamListProperty());
        selectedStream.bind(comboBoxCamera.getSelectionModel().selectedItemProperty());

        btnResize.visibleProperty().bind(viewModel.isInDialogProperty());
        btnResize.managedProperty().bind(btnResize.visibleProperty());

        checkBox.visibleProperty().bind(viewModel.isInDialogProperty());
        checkBox.managedProperty().bind(checkBox.visibleProperty());

        btnDetach.visibleProperty().bind(viewModel.isInDialogProperty().not());
        btnDetach.managedProperty().bind(btnDetach.visibleProperty());

        /**
         * FIXME: this is almost like hardcoding, should be more intelligent
         */
        viewModel.isInDialogProperty().addListener( (obs, oldVal, newVal) -> {
            btnPrevCam.getStyleClass().removeAll("icon-chevron-left-inverse", "icon-chevron-left");
            btnNextCam.getStyleClass().removeAll("icon-chevron-right-inverse", "icon-chevron-right");
            if (newVal) {
                btnPrevCam.getStyleClass().add("icon-chevron-left");
                btnNextCam.getStyleClass().add("icon-chevron-right");
            } else {
                btnPrevCam.getStyleClass().add("icon-chevron-left-inverse");
                btnNextCam.getStyleClass().add("icon-chevron-right-inverse");
            }
        });

        /* add listeners */
        comboBoxCamera
            .getItems()
            .addListener((ListChangeListener.Change<? extends ILiveVideoStream> change) -> checkComboBoxValue());

        selectedStream.addListener(
            (obs, oldItem, newItem) -> {
                viewModel.setSelectedStream(newItem);
                if (oldItem != null) {
                    // deactivate old item
                    oldItem.removeVideoStreamListener(streamListener);
                }

                if (newItem != null) {
                    // activate new Item
                    newItem.addVideoStreamListener(streamListener);
                    Bounds bounds = liveVideo.getBoundsInParent();
                    newItem.requestVideoSize((int) bounds.getWidth(), (int) bounds.getHeight());
                }
            });

        liveVideo.boundsInParentProperty().addListener(handleLiveViewResize);

        root.sceneProperty()
            .addListener(
                (obs, oldScene, newScene) -> {
                    if (oldScene != null && newScene == null) {
                        ILiveVideoStream stream = selectedStream.getValue();
                        if (stream != null) stream.removeVideoStreamListener(streamListener);
                        comboBoxCamera.itemsProperty().unbind();
                    }
                });

        comboBoxCamera.getSelectionModel().select(viewModel.getSelectedStream());
        
        viewModel.setTitlePostfix(comboBoxCamera.getSelectionModel().selectedItemProperty().asString());

        checkComboBoxValue();
    }

    private void checkComboBoxValue() {
        if (comboBoxCamera.getSelectionModel().getSelectedIndex() != -1) return;
        String info = (comboBoxCamera.getItems().size() == 0) ? "No streams available" : "Select stream";
        // FIXME: this doesn't seem to work when the currently selected item gets removed
        LiveVideoStream dummy = new LiveVideoStream(null);
        dummy.setDescription(info);
        comboBoxCamera.setValue(dummy);
    }
}
