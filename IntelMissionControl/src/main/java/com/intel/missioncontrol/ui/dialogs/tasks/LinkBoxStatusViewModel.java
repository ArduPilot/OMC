/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.tasks;

import com.google.inject.Inject;
import com.intel.missioncontrol.linkbox.BatteryAlertLevel;
import com.intel.missioncontrol.linkbox.DataConnectionStatus;
import com.intel.missioncontrol.linkbox.DroneConnectionQuality;
import com.intel.missioncontrol.linkbox.ILinkBox;
import com.intel.missioncontrol.linkbox.ILinkBoxConnectionService;
import com.intel.missioncontrol.linkbox.LinkBoxAlertLevel;
import com.intel.missioncontrol.linkbox.LinkBoxGnssState;
import com.intel.missioncontrol.linkbox.WifiConnectionQuality;
import com.intel.missioncontrol.rtk.IRTKStation;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry.RTKConfigurationViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;

public class LinkBoxStatusViewModel extends DialogViewModel<Void, ViewModel> {

    private final AsyncObjectProperty<ILinkBox> linkBox = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IRTKStation> rtkstation = new SimpleAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<BatteryAlertLevel> batteryInfo = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<WifiConnectionQuality> linkboxConnectionQuality =
        new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<DataConnectionStatus> dataConnection = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<DroneConnectionQuality> droneConnectionQuality =
        new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<LinkBoxGnssState> gnssState = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<LinkBoxAlertLevel> warningLevel = new UIAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty linkBoxName = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty message = new UIAsyncStringProperty(this);
    private final UIAsyncIntegerProperty numberOfSatellites = new UIAsyncIntegerProperty(this);
    private final IDialogService dialogService;
    private FutureCommand showRTKConfigurationDialogCommand;

    @Inject
    public LinkBoxStatusViewModel(ILinkBoxConnectionService linkBoxConnectionService, IDialogService dialogService) {
        this.dialogService = dialogService;
        linkBox.bind(linkBoxConnectionService.getLinkBox());
        rtkstation.bind(linkBoxConnectionService.getRTKStation());

        linkBoxName.bind(PropertyPath.from(linkBox).selectReadOnlyAsyncString(ILinkBox::linkBoxNameProperty));
        message.bind(PropertyPath.from(linkBox).selectReadOnlyAsyncString(ILinkBox::messageProperty));

        batteryInfo.bind(PropertyPath.from(linkBox).selectReadOnlyAsyncObject(ILinkBox::getBatteryInfo));
        linkboxConnectionQuality.bind(
            PropertyPath.from(linkBox).selectReadOnlyAsyncObject(ILinkBox::getLinkBoxConnectionQuality));
        dataConnection.bind(PropertyPath.from(linkBox).selectReadOnlyAsyncObject(ILinkBox::getDataConnectionStatus));
        droneConnectionQuality.bind(
            PropertyPath.from(linkBox).selectReadOnlyAsyncObject(ILinkBox::getDroneConnectionQuality));

        gnssState.bind(PropertyPath.from(rtkstation).selectReadOnlyAsyncObject(IRTKStation::getGnssState));
        warningLevel.bind(PropertyPath.from(linkBox).selectReadOnlyAsyncObject(ILinkBox::getAlertLevel));
        numberOfSatellites.bind(
            PropertyPath.from(rtkstation).selectReadOnlyAsyncInteger(IRTKStation::getNumberOfSatellites));
    }

    @Override
    protected void initializeViewModel(ViewModel ownerViewModel) {
        super.initializeViewModel(ownerViewModel);
        showRTKConfigurationDialogCommand =
            new FutureCommand(
                () -> dialogService.requestDialogAsync(ownerViewModel, RTKConfigurationViewModel.class, false));
    }

    ReadOnlyAsyncStringProperty linkBoxNameProperty() {
        return linkBoxName;
    }

    ReadOnlyAsyncStringProperty messageProperty() {
        return message;
    }

    ReadOnlyAsyncObjectProperty<BatteryAlertLevel> batteryAlertLevelProperty() {
        return batteryInfo;
    }

    ReadOnlyAsyncObjectProperty<WifiConnectionQuality> linkboxConnectionQualityProperty() {
        return linkboxConnectionQuality;
    }

    ReadOnlyAsyncObjectProperty<DataConnectionStatus> cloudDataConnectionStatusProperty() {
        return dataConnection;
    }

    ReadOnlyAsyncObjectProperty<LinkBoxGnssState> gnssStateProperty() {
        return gnssState;
    }

    ReadOnlyAsyncIntegerProperty numberOfSatellitesProperty() {
        return numberOfSatellites;
    }

    ReadOnlyAsyncObjectProperty<DroneConnectionQuality> droneConnectionQualityProperty() {
        return droneConnectionQuality;
    }

    ReadOnlyAsyncObjectProperty<LinkBoxAlertLevel> warningLevelProperty() {
        return warningLevel;
    }

    Command getShowRTKConfigurationDialogCommand() {
        return showRTKConfigurationDialogCommand;
    }
}
