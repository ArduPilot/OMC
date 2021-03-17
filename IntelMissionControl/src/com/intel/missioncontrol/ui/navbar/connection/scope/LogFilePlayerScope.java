/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.scope;

import com.intel.missioncontrol.mission.Mission;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

/** @author Vladimir Iordanov */
public class LogFilePlayerScope implements Scope {
    private final ObjectProperty<Mission> mission = new SimpleObjectProperty<>();
    private final ObjectProperty<String> selectedLogName = new SimpleObjectProperty<>();

    public enum State {
        TABLE_VIEW,
        PLAYER_VIEW
    };

    private ReadOnlyObjectWrapper<State> currentState = new ReadOnlyObjectWrapper<>(State.TABLE_VIEW);

    public ReadOnlyObjectProperty<State> stateProperty() {
        return currentState.getReadOnlyProperty();
    }

    public Mission getMission() {
        return mission.get();
    }

    public String getSelectedLogName() {
        return selectedLogName.get();
    }

    public ObjectProperty<Mission> missionProperty() {
        return mission;
    }

    public ObjectProperty<String> selectedLogNameProperty() {
        return selectedLogName;
    }

    public void switchToPlayerView(Mission mission, String selectedLogFileName) {
        this.mission.set(mission);
        this.selectedLogName.set(selectedLogFileName);

        currentState.set(State.PLAYER_VIEW);
    }

    public void switchToTableView() {
        currentState.set(State.TABLE_VIEW);
    }
}
