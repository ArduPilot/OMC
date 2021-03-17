/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.mission.Drone;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import de.saxsys.mvvmfx.InjectScope;
import java.util.concurrent.locks.ReentrantLock;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.asyncfx.concurrent.Dispatcher;

/** Created by eivanchenko on 8/7/2017. */
@SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
public abstract class AbstractUavDataViewModel<T> extends ViewModelBase {

    @InjectScope
    private MainScope mainScope;

    @Inject
    private IApplicationContext applicationContext;

    private ObservableList<UavDataParameter<T>> data;
    private T oldData;
    private ObjectProperty<Mission> mission;
    private ObjectProperty<Drone> uav;

    private final ReentrantLock planeChangeLock = new ReentrantLock();

    public AbstractUavDataViewModel(Mission mission) {
        this.mission = new SimpleObjectProperty<>(mission);
        this.uav = new SimpleObjectProperty<>();
        this.data = FXCollections.observableArrayList();
        this.mission.addListener((observable, oldValue, newValue) -> missionChanged());
        uav.addListener((observable, oldValue, newValue) -> planeChanged());
    }

    protected void preInitialize() {}

    protected void postInitialize() {}

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        preInitialize();
        if (mainScope != null && getMission() == null) {
            mission.bind(applicationContext.currentLegacyMissionProperty());
        }

        postInitialize();
    }

    public Mission getMission() {
        return mission.get();
    }

    public ObjectProperty<Mission> missionProperty() {
        return mission;
    }

    protected final void missionChanged() {
        uav.unbind();
        if (getMission() != null) {
            uav.bind(mission.get().droneProperty());
        }
    }

    protected final void planeChanged() {
        planeChangeLock.lock();
        try {
            releaseUavReferences();
            resetData();
            establishUavReferences();
        } catch (RuntimeException rte) {
            throw rte;
        } finally {
            planeChangeLock.unlock();
        }
    }

    public Drone getUav() {
        return uav.get();
    }

    public ObjectProperty<Drone> uavProperty() {
        return uav;
    }

    protected abstract void releaseUavReferences();

    protected abstract void establishUavReferences();

    private void resetData() {
        oldData = null;
        data.forEach(UavDataParameter::reset);
    }

    protected void update(T newData) {
        if (oldData != null && oldData.equals(newData)) {
            return;
        }

        oldData = newData; // TODO tricky part here
        Dispatcher.platform().run(() -> updateParameters(newData));
    }

    private void updateParameters(T newData) {
        data.forEach(p -> p.updateValue(newData));
    }

    public ObservableList<UavDataParameter<T>> getData() {
        return data;
    }

    protected final void setData(ObservableList<UavDataParameter<T>> data) {
        this.data = data;
    }
}
