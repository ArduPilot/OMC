/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.main.OsTypes;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/** Created by akorotenko on 8/8/17. */
public class FileListCellView extends ViewBase<FileListCellViewModel> {

    private static final double FILE_SYSTEM_ICON_HEIGHT = ScaleHelper.emsToPixels(1.67);

    private static final double FILE_LIST_HBOX_MARGIN_RIGHT = ScaleHelper.emsToPixels(0.83);
    private static final double FILE_LIST_HBOX_MARGIN_LEFT = ScaleHelper.emsToPixels(0.41);

    @InjectViewModel
    private FileListCellViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private HBox filesListHbox;

    @FXML
    private Hyperlink fileName;

    @FXML
    private ImageView fileSystemIcon;

    @Override
    protected void initializeView() {
        super.initializeView();
        fileName.textProperty().bind(viewModel.fileNameProperty());

        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(viewModel.getFile());
        OsTypes system = StaticInjector.getInstance(IVersionProvider.class).getSystem();
        if (icon == null || !system.isWindows()) {
            JFileChooser fc = new javax.swing.JFileChooser();
            icon = fc.getUI().getFileView(fc).getIcon(viewModel.getFile());
        }

        BufferedImage bufferedImage =
            new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics gfx = bufferedImage.getGraphics();
        try {
            icon.paintIcon(null, gfx, 0, 0);
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

            fileSystemIcon.setImage(fxImage);
            filesListHbox.setMargin(
                fileSystemIcon, new Insets(0, FILE_LIST_HBOX_MARGIN_RIGHT, 0, FILE_LIST_HBOX_MARGIN_LEFT));
            fileSystemIcon.setFitHeight(FILE_SYSTEM_ICON_HEIGHT);
        } finally {
            gfx.dispose();
        }
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected FileListCellViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void closeFileButtonClicked() {
        viewModel.getCloseFileCommand().execute();
    }

    @FXML
    public void openFileClicked(ActionEvent event) {
        viewModel.getOpenFileCommand().execute();
    }
}
