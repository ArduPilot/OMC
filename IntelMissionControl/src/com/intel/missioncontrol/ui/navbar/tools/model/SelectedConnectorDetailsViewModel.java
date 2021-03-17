/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.management.BackendState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.BackendInfo;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by eivanchenko on 8/7/2017. */
public class SelectedConnectorDetailsViewModel extends AbstractUavDataViewModel<BackendState> {

    public static final String KEY_NoFIX = "com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.noFIX";
    public static final String KEY_NoGPS = "com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.noGPS";

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectedConnectorDetailsViewModel.class);

    @Inject
    private ILanguageHelper languageHelper;

    private ObjectProperty<BackendInfo> backendInfo = new SimpleObjectProperty<>();

    private UavDataParameter<BackendState> nameParam =
        new SimpleUavDataParameter<BackendState>("name", UavDataParameterType.GENERAL) {

            @Override
            protected Object extractRawValue(BackendState state) {
                return state.getBackend().name;
            }
        };

    private UavDataParameter<BackendState> gpsStatusParam =
        new SimpleUavDataParameter<BackendState>("gpsStatus", UavDataParameterType.GENERAL) {

            @Override
            protected Object extractRawValue(BackendState state) {
                Backend backend = state.getBackend();
                if (backend.hasGPS) {
                    if (backend.hasFix) {
                        return Position.fromDegrees(backend.lat, backend.lon, backend.alt / 100.).toString();
                    } else {
                        return languageHelper.getString(KEY_NoFIX);
                    }
                } else {
                    return languageHelper.getString(KEY_NoGPS);
                }
            }
        };

    private UavDataParameter<BackendState> hostParam =
        new SimpleUavDataParameter<BackendState>("host", UavDataParameterType.GENERAL) {
            @Override
            protected Object extractRawValue(BackendState state) {
                Backend backend = state.getBackend();
                return String.format("%s:%d", state.getHost().getHostString(), backend.port);
            }
        };

    private UavDataParameter<BackendState> dgpsReferenceStationParam =
        new SimpleUavDataParameter<BackendState>("dgpsReferenceStation", UavDataParameterType.GENERAL) {
            @Override
            protected Object extractRawValue(BackendState state) {
                Backend backend = state.getBackend();
                return String.valueOf(backend.hasRTCMInput);
            }
        };

    private UavDataParameter<BackendState> serialNumberParam =
        new SimpleUavDataParameter<BackendState>("host", UavDataParameterType.GENERAL) {
            @Override
            protected Object extractRawValue(BackendState state) {
                Backend backend = state.getBackend();
                return backend.info.serialNumber;
            }
        };

    private UavDataParameter<BackendState> hardwareRevisionParam =
        new SimpleUavDataParameter<BackendState>("hardwareRevision", UavDataParameterType.GENERAL) {
            @Override
            protected Object extractRawValue(BackendState state) {
                Backend backend = state.getBackend();
                return String.valueOf(backend.info.revisionHardware);
            }
        };

    private UavDataParameter<BackendState> hardwareTypeParam =
        new SimpleUavDataParameter<BackendState>("hardwareType", UavDataParameterType.GENERAL) {
            @Override
            protected Object extractRawValue(BackendState state) {
                Backend backend = state.getBackend();
                return backend.info.hardwareType;
            }
        };

    private UavDataParameter<BackendState> softwareVersionParam =
        new SimpleUavDataParameter<BackendState>("softwareVersion", UavDataParameterType.GENERAL) {
            @Override
            protected Object extractRawValue(BackendState state) {
                Backend backend = state.getBackend();
                return backend.info.getHumanReadableSWversion();
            }
        };

    private UavDataParameter<BackendState> protocolVersionParam =
        new SimpleUavDataParameter<BackendState>("protocolVersion", UavDataParameterType.GENERAL) {
            @Override
            protected Object extractRawValue(BackendState state) {
                Backend backend = state.getBackend();
                return String.valueOf(backend.info.protocolVersion);
            }
        };

    private UavDataParameter<BackendState> batteryParam =
        new SimpleUavDataParameter<BackendState>("battery", UavDataParameterType.GENERAL) {
            @Override
            protected Object extractRawValue(BackendState state) {
                Backend backend = state.getBackend();
                return String.format("%fV", backend.batteryVoltage);
            }
        };

    private UavDataParameter<BackendState> pingTimeParam =
        new SimpleUavDataParameter<BackendState>("pingTime", UavDataParameterType.GENERAL) {
            @Override
            protected Object extractRawValue(BackendState state) {
                long nanos = state.getPingTimeNanos();
                if (nanos >= 0) {
                    return String.format("%ss", StringHelper.numberToIngName(nanos * 1e-9, -3, false));
                } else {
                    return UavDataParameter.NOT_A_VALUE;
                }
            }
        };

    public SelectedConnectorDetailsViewModel() {
        this(null);
    }

    public SelectedConnectorDetailsViewModel(Mission mission) {
        super(mission);
    }

    @Override
    protected void preInitialize() {
        ObservableList<UavDataParameter<BackendState>> data = getData();

        nameParam.setDisplayName(
            languageHelper.getString("com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.backend.name"));
        data.add(nameParam);

        gpsStatusParam.setDisplayName(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.backend.gps.status"));
        data.add(gpsStatusParam);

        hostParam.setDisplayName(
            languageHelper.getString("com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.host"));
        data.add(hostParam);

        dgpsReferenceStationParam.setDisplayName(
            languageHelper.getString("com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.rtcm"));
        data.add(dgpsReferenceStationParam);

        serialNumberParam.setDisplayName(
            languageHelper.getString("com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.serial.number"));
        data.add(serialNumberParam);

        hardwareRevisionParam.setDisplayName(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.hardware.revision"));
        data.add(hardwareRevisionParam);

        hardwareTypeParam.setDisplayName(
            languageHelper.getString("com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.hardware.type"));
        data.add(hardwareTypeParam);

        softwareVersionParam.setDisplayName(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.software.version"));
        data.add(softwareVersionParam);

        protocolVersionParam.setDisplayName(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.protocol.version"));
        data.add(protocolVersionParam);

        batteryParam.setDisplayName(
            languageHelper.getString("com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.battery"));
        data.add(batteryParam);

        pingTimeParam.setDisplayName(
            languageHelper.getString("com.intel.missioncontrol.ui.tools.SelectedConnectorDetailsView.ping.time"));
        data.add(pingTimeParam);
        initListeners();
    }

    @Override
    protected void releaseUavReferences() {
        backendInfo.unbind();
    }

    @Override
    protected void establishUavReferences() {
        backendInfo.bind(getUav().backendInfoRawProperty());
    }

    private void initListeners() {
        backendInfo.addListener(
            (observableValue, olddata, newdata) -> {
                if (newdata != null) {
                    IAirplane plane = getUav().getLegacyPlane();
                    if (plane == null) {
                        update(null);
                    } else {
                        try {
                            update(plane.getAirplaneCache().getBackendStateOffline());
                        } catch (AirplaneCacheEmptyException e) {
                            String toolName =
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.tools.ToolsView.connectorDetails");
                            LOGGER.debug("Unable update '{}' tool data. Reason: the cache is empty", toolName);
                        }
                    }
                }
            });
    }

}
