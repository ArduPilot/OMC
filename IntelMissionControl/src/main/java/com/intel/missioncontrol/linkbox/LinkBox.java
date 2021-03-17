/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.linkbox.authentication.LinkBoxAuthentication;
import com.intel.missioncontrol.linkbox.dataset.GpTelemetryMessage;
import com.intel.missioncontrol.linkbox.dataset.Message;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.rtk.IRTKStation;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import gov.nasa.worldwind.geom.Position;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.slf4j.LoggerFactory;

public class LinkBox implements ILinkBox, IRTKStation {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MavlinkHandler.class);

    private final AsyncBooleanProperty networkAvailable = new SimpleAsyncBooleanProperty(false);
    private final AsyncObjectProperty<INetworkInformation> networkInfo = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<BatteryAlertLevel> linkBoxBattery = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<WifiConnectionQuality> linkBoxConnectionQuality =
        new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<DroneConnectionQuality> droneConnectionQuality =
        new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<DataConnectionStatus> dataConnectionStatus =
        new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<LinkBoxAlertLevel> linkBoxAlertLevel = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<LinkBoxGnssState> gnssState = new SimpleAsyncObjectProperty<>(this);
    private final AsyncIntegerProperty numberOfSatellites = new SimpleAsyncIntegerProperty(this);
    private final AsyncObjectProperty<Position> rtkPosition = new SimpleAsyncObjectProperty<>(this);
    private final AsyncStringProperty linkBoxName = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty message = new SimpleAsyncStringProperty(this);
    private final AsyncBooleanProperty linkBoxOnline = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty linkBoxAuthorized = new SimpleAsyncBooleanProperty(this);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final AsyncObjectProperty<LinkBoxAuthentication> linkBoxAuthentication =
        new SimpleAsyncObjectProperty<>(this);
    private final ILanguageHelper languageHelper;
    private final ListProperty<ResolvableValidationMessage> resolvableLinkBoxMessages =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    LinkBox(
            INetworkInformation networkInformation,
            ILanguageHelper languageHelper,
            IApplicationContext applicationContext) {
        this.languageHelper = languageHelper;
        linkBoxAuthentication.setValue(new LinkBoxAuthentication());
        applicationContext.addClosingListener(
            () -> {
                if (linkBoxAuthenticatedProperty().get()) {
                    linkBoxAuthentication.get().requestLinkBoxCancellation();
                }
            });
        networkInfo.setValue(networkInformation);
        initialize();
    }

    @Override
    public byte[] getIMCKey() {
        byte[] secretKey = new byte[0];
        try {
            secretKey =
                MessageDigest.getInstance("SHA-256")
                    .digest(linkBoxAuthentication.get().getMavLinkKeyImc().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("error while trying to get SHA-256 digest", e);
        }

        return secretKey;
    }

    @Override
    public byte[] getDroneKey() {
        byte[] secretKey = new byte[0];
        try {
            secretKey =
                MessageDigest.getInstance("SHA-256")
                    .digest(linkBoxAuthentication.get().getMavLinkKeyDrone().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("error while trying to get SHA-256 digest", e);
        }

        return secretKey;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty linkBoxOnlineProperty() {
        return linkBoxOnline;
    }

    @Override
    public ReadOnlyAsyncStringProperty linkBoxNameProperty() {
        return linkBoxName;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<BatteryAlertLevel> getBatteryInfo() {
        return linkBoxBattery;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<WifiConnectionQuality> getLinkBoxConnectionQuality() {
        return linkBoxConnectionQuality;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<DroneConnectionQuality> getDroneConnectionQuality() {
        return droneConnectionQuality;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<DataConnectionStatus> getDataConnectionStatus() {
        return dataConnectionStatus;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<LinkBoxAlertLevel> getAlertLevel() {
        return linkBoxAlertLevel;
    }

    @Override
    public ReadOnlyAsyncStringProperty messageProperty() {
        return message;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty linkBoxAuthenticatedProperty() {
        return linkBoxAuthorized;
    }

    @Override
    public void requestLinkBoxAuthentication() {
        linkBoxAuthentication.get().requestLinkBoxAuthentication();
    }

    @Override
    public ReadOnlyListProperty<ResolvableValidationMessage> resolvableLinkBoxMessagesProperty() {
        return resolvableLinkBoxMessages;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<LinkBoxGnssState> getGnssState() {
        return gnssState;
    }

    @Override
    public ReadOnlyAsyncIntegerProperty getNumberOfSatellites() {
        return numberOfSatellites;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Position> getRTKStationPosition() {
        return rtkPosition;
    }

    private void initialize() {
        initializeAllProperties();

        // Network Configuration
        networkAvailable.addListener((observable, oldValue, newValue) -> connectToServer(newValue));
        networkAvailable.bind(
            PropertyPath.from(networkInfo)
                .selectReadOnlyAsyncBoolean(INetworkInformation::networkAvailableProperty, false));

        linkBoxAuthorized.bind(
            PropertyPath.from(linkBoxAuthentication)
                .selectReadOnlyAsyncBoolean(LinkBoxAuthentication::authorizedProperty));
        linkBoxAuthorized.addListener((observableValue, oldValue, newValue) -> updateResolvableLinkBoxMessages());

        linkBoxOnline.addListener(
            (observableValue, oldValue, newValue) -> {
                if (newValue && !linkBoxAuthorized.get()) {
                    linkBoxAuthentication.get().requestLinkBoxAuthentication();
                    updateResolvableLinkBoxMessages();
                }
            });
    }

    private void initializeAllProperties() {
        // LinkBox
        linkBoxName.setValue("");
        linkBoxBattery.setValue(BatteryAlertLevel.UNDEFINED);
        linkBoxConnectionQuality.setValue(WifiConnectionQuality.OFFLINE);
        dataConnectionStatus.setValue(DataConnectionStatus.UNKNOWN);
        linkBoxAlertLevel.setValue(LinkBoxAlertLevel.INFO);
        message.setValue(null);

        // Drone Info
        droneConnectionQuality.setValue(DroneConnectionQuality.DOWN);

        // RTK Base Station
        numberOfSatellites.setValue(0);
        rtkPosition.setValue(null);
    }

    private void connectToServer(Boolean newValue) {
        if (newValue) {
            FutureTask<String> futureTask1 =
                new FutureTask<>(
                    () -> {
                        HubConnection hubConnection =
                            HubConnectionBuilder.create("http://launchbox.internal/api/v1/hubs/gptelemetry").build();
                        if ((hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED)
                                && networkAvailable.get()) {
                            hubConnection.onClosed(
                                e -> {
                                    linkBoxOnline.setValue(false);
                                    initializeAllProperties();
                                    connectToServer(newValue);
                                });
                            hubConnection.start().blockingAwait(2000, TimeUnit.MILLISECONDS);

                            hubConnection.on("ShowGpTelemetry", this::parseLinkBoxResponse, GpTelemetryMessage.class);
                            if (hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
                                hubConnection.stop();
                                connectToServer(newValue);
                            } else {
                                hubConnection.invoke("RequestGpTelemetry", "IMC");
                                // hubConnection.invoke("subscribe", "IMC");
                            }
                        }
                    },
                    "Connection");
            executor.schedule(futureTask1, 2000, TimeUnit.MILLISECONDS); // && hubConnection.getServerTimeout()
        }
    }

    private void parseLinkBoxResponse(GpTelemetryMessage response) {
        if (!linkBoxOnline.get()) {
            linkBoxOnline.setValue(true);
        }

        // LinkBox
        linkBoxBattery.setValue(response.getGpTelemetryData().getBatteryAlertLevel());
        linkBoxConnectionQuality.setValue(response.getGpTelemetryData().getWificonnectionQuality());
        dataConnectionStatus.setValue(response.getGpTelemetryData().getDataConnectionStatus());
        initializeGeneralInfo(response);

        // RTK Base Station
        gnssState.setValue(response.getGpTelemetryData().getGNSS().getLinkBoxGnssState());
        rtkPosition.set(
            Position.fromDegrees(
                response.getGpTelemetryData().getGNSS().getLatitude(),
                response.getGpTelemetryData().getGNSS().getLongitude()));
        droneConnectionQuality.setValue(response.getGpTelemetryData().getDroneConnectionQuality());
        //TODO: Uncomment - Session Management
        //linkBoxAuthentication.setAuthorized(response.getClientInfo().getAuthorized());

    }

    private void initializeGeneralInfo(GpTelemetryMessage response) {
        linkBoxName.setValue(response.getGpTelemetryData().getName());
        StringBuilder result = new StringBuilder();
        for (Message message : response.getGpTelemetryData().getMessages()) {
            result.append(message.getMessageText()).append(".");
            linkBoxAlertLevel.setValue(message.getLinkBoxAlertLevel());
        }

        message.setValue(result.toString());
    }

    private void updateResolvableLinkBoxMessages() {
        if (linkBoxAuthorized.get()) {
            resolvableLinkBoxMessages.remove(
                linkBoxAuthentication.get().getAuthenticationResolvable(languageHelper, linkBoxName.get()));
        } else {
            resolvableLinkBoxMessages.add(
                linkBoxAuthentication.get().getAuthenticationResolvable(languageHelper, linkBoxName.get()));
        }
    }
}
