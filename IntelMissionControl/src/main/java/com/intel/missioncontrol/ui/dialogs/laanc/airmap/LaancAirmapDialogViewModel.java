/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.laanc.airmap;

import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.validation.flightplan.AirmapLaancService;
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

public class LaancAirmapDialogViewModel extends DialogViewModel<Boolean, AirmapLaancService.LaancApprovalQr> {

    private static final Logger LOGGER = LogManager.getLogger(LaancAirmapDialogViewModel.class);

    private final Command proceedCommand;
    private AirmapLaancService.LaancApprovalQr qrCode;

    public LaancAirmapDialogViewModel() {
        proceedCommand =
            new DelegateCommand(
                () -> {
                    if (qrCode == null) {
                        LOGGER.error("Laanc QR code was not generated");
                        return;
                    }

                    Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(qrCode.getLink().toString()), null);
                });
    }

    @Override
    protected void initializeViewModel(AirmapLaancService.LaancApprovalQr qrCode) {
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
        if (qrCode == null) {
            LOGGER.error("Laanc QR code was not generated");
            return null;
        }

        BufferedImage bufferedImage = qrCode.getQrCodeImage();
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
