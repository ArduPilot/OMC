/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.scope;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.navbar.connection.view.ExternalBaseStationView;
import de.saxsys.mvvmfx.Scope;
import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.EnumMap;
import java.util.Map;

/** @author Vladimir Iordanov */
public class ExternalConnectionScope implements Scope {
    private final ObjectProperty<ExternalBaseStationView.ExternalConnetionType> currentSection =
        new SimpleObjectProperty<>(ExternalBaseStationView.ExternalConnetionType.RS232);
    private final Map<ExternalBaseStationView.ExternalConnetionType, Command> connectCommands =
        new EnumMap<>(ExternalBaseStationView.ExternalConnetionType.class);
    private final Map<ExternalBaseStationView.ExternalConnetionType, Command> disconnectCommands =
        new EnumMap<>(ExternalBaseStationView.ExternalConnetionType.class);

    public void registerConnectCommand(ExternalBaseStationView.ExternalConnetionType connetionType, Command command) {
        connectCommands.put(connetionType, command);
    }

    public void registerDisconnectCommand(
            ExternalBaseStationView.ExternalConnetionType connetionType, Command command) {
        disconnectCommands.put(connetionType, command);
    }

    public Command getCurrentConnectCommand() {
        Command command = connectCommands.get(currentSection.getValue());
        Expect.notNull(command, "connectcommand");
        return command;
    }

    public Command getCurrentDisconnectCommand() {
        Command command = disconnectCommands.get(currentSection.getValue());
        Expect.notNull(command, "disconnectcommand");
        return command;
    }

    public ExternalBaseStationView.ExternalConnetionType getCurrentSection() {
        return currentSection.get();
    }

    public ObjectProperty<ExternalBaseStationView.ExternalConnetionType> currentSectionProperty() {
        return currentSection;
    }

    public Map<ExternalBaseStationView.ExternalConnetionType, Command> getConnectCommands() {
        return connectCommands;
    }

    public Map<ExternalBaseStationView.ExternalConnetionType, Command> getDisconnectCommands() {
        return disconnectCommands;
    }
}
