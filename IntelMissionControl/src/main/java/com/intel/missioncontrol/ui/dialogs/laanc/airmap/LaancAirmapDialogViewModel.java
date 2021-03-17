/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.laanc.airmap;

import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LaancAirmapDialogViewModel extends DialogViewModel<Boolean, String[]> {

    private static final Logger LOGGER = LogManager.getLogger(LaancAirmapDialogViewModel.class);

    private final Command proceedCommand;
    private String[] qrCode;

    public LaancAirmapDialogViewModel() {
        proceedCommand =
            new DelegateCommand(
                () ->
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(qrCode[0]), null));
    }

    @Override
    protected void initializeViewModel(String[] qrCode) {
        super.initializeViewModel(qrCode);
        this.qrCode = qrCode;
    }

    public Command getProceedCommand() {
        return proceedCommand;
    }

    @SuppressLinter(
        value = {"IllegalViewModelMethod", "ViewClassInViewModel"},
        reviewer = "mstrauss",
        justification = "okay here"
    )
    public Image getImage() {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(this.qrCode[1]));
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            LOGGER.warn("cant open image", e);
        }

        return null;
    }
}
