/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.credits;

import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import gov.nasa.worldwind.util.BrowserOpener;
import gov.nasa.worldwind.util.Logging;
import java.net.URL;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class MapCreditViewModel implements ViewModel {

    private final StringProperty text;
    private final ObjectProperty<Image> image;
    private final String link;
    private final Command executeCommand = new DelegateCommand(this::execute);

    public MapCreditViewModel(String text, Image image, String link) {
        this.text = new ReadOnlyStringWrapper(text);
        this.image = new ReadOnlyObjectWrapper<>(image);
        this.link = link;
    }

    public MapCreditViewModel(String text, String link) {
        this.text = new ReadOnlyStringWrapper(text);
        this.image = new ReadOnlyObjectWrapper<>();
        this.link = link;
    }

    public ReadOnlyStringProperty textProperty() {
        return text;
    }

    public ReadOnlyObjectProperty<Image> imageProperty() {
        return image;
    }

    public Command getExecuteCommand() {
        return executeCommand;
    }

    private void execute() {
        if (link != null && link.length() > 0) {
            String msg;
            try {
                BrowserOpener.browse(new URL(link));

            } catch (Exception e) {
                msg = Logging.getMessage("generic.ExceptionAttemptingToInvokeWebBrower for URL", link);
                Logging.logger().warning(msg);
            }
        }
    }

}
