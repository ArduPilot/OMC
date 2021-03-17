/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.dialogs.DialogResult;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenter;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.WeakRunnable;
import eu.mavinci.airspace.Airspace;
import eu.mavinci.airspace.IAirspace;
import eu.mavinci.airspace.LowestAirspace;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.plane.APTypes;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.CAirplane;
import eu.mavinci.core.plane.listeners.AirplaneListenerDelegator;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackend;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerHealth;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPlaneInfo;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPowerOn;
import eu.mavinci.core.plane.listeners.IAirplaneListenerSimulationSettings;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.core.plane.sendableobjects.SingleHealthDescription;
import eu.mavinci.core.update.EnumUpdateTargets;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.UserNotificationHubSwing;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.FlightplanManager;
import eu.mavinci.plane.logfile.ALogWriter;
import eu.mavinci.plane.logfile.LogWriterFLG;
import eu.mavinci.plane.logfile.LogWriterVLG;
import eu.mavinci.plane.simjava.AirplaneSim;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Vector;
import java.util.logging.Level;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;

public class Airplane extends CAirplane
        implements IAirplane,
            IAirplaneListenerHealth,
            IAirplaneListenerSimulationSettings,
            IAirplaneListenerBackendConnectionLost,
            IAirplaneListenerStartPos,
            IAirplaneListenerPlaneInfo,
            IAirplaneListenerBackend,
            IAirplaneListenerPowerOn,
            IAirplaneListenerPosition,
            IAirplaneListenerFlightphase {

    private final GeneralSettings generalSettings =
        DependencyInjector.getInstance().getInstanceOf(GeneralSettings.class);

    WindEstimate windEstimate;

    IDialogService dialogService;

    private static final ILanguageHelper languageHelper;
    private static final QuantityFormat quantityFormat;

    static {
        languageHelper = DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);
        quantityFormat =
            new AdaptiveQuantityFormat(DependencyInjector.getInstance().getInstanceOf(IQuantityStyleProvider.class));
        quantityFormat.setMaximumFractionDigits(0);
    }

    public Airplane(AsyncObjectProperty<Path> baseFolder, AsyncObjectProperty<LatLon> startCoordinates) {
        this.baseFolder.bind(baseFolder);
        this.startCoordinates.bindBidirectional(startCoordinates);
        init();
    }

    @Override
    protected void init() {
        super.init();
        dialogService = DependencyInjector.getInstance().getInstanceOf(IDialogService.class);

        // rootHandler = AirplaneListenerDelegatorFactory.createNewAirplaneListenerDelegator();
        rootHandler = new AirplaneListenerDelegator();
        cache = new AirplaneCache(this);

        // marco: attention: the order of this three classes
        // is importent for later order of listiner invokation!! don't change it!
        addListenerAtBegin(cache);
        fpManager = new FlightplanManager(this);
        addListener(this);
        m_flg_logger = new LogWriterFLG(this);
        m_tcp_logger = new LogWriterVLG(this);

        windEstimate = new WindEstimate(this);

        if (startCoordinates.get() != null) {
            try {
                double lat = startCoordinates.get().latitude.degrees;
                double lon = startCoordinates.get().longitude.degrees;
                rootHandler.recv_startPos(lon, lat, 1);
            } catch (NumberFormatException e) {
                Debug.getLog().log(Level.FINER, "Startpos wrong formattet in session", e);
            }
        }

        // TODO has to be moved to new update frame
        // if (Application.isAutoUpdating()) {
        //    planeUpdate.startAutoUpdate();
        //    backendUpdate.startAutoUpdate();
        // }

        Dispatcher.background()
            .runLaterAsync(
                new WeakRunnable(tryLoadBackendDumps), Duration.ofMillis(30 * 1000), Duration.ofMillis(2 * 60 * 1000));
    }

    private Runnable tryLoadBackendDumps =
        new Runnable() {

            @Override
            public void run() {
                if (!isWriteable() || connector instanceof AirplaneSim) {
                    return;
                }
                // TODO remove
            }
        };

    private ALogWriter m_flg_logger;
    private ALogWriter m_tcp_logger;
    private final AsyncObjectProperty<Path> baseFolder = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<LatLon> startCoordinates = new SimpleAsyncObjectProperty<>(this);

    public static final String KEY = "eu.mavinci.plane.Airplane";

    public ALogWriter getFlgLogWriter() {
        return m_flg_logger;
    }

    public ALogWriter getTCPLogWriter() {
        return m_tcp_logger;
    }

    public WindEstimate getWindEstimate() {
        return windEstimate;
    }

    @Override
    public void fireGuiClose() {
        super.fireGuiClose();
    }

    @Override
    public boolean fireGuiCloseRequest() {
        if (!super.fireGuiCloseRequest()) {
            return false;
        }

        return true;
    }

    public void fireLoggingChangedTCP(boolean is_logging_tcp) {
        rootHandler.loggingStateChangedTCP(is_logging_tcp);
    }

    public void fireLoggingChangedFLG(boolean is_logging_flg) {
        rootHandler.loggingStateChangedFLG(is_logging_flg);
    }

    public void fireSimPaused(boolean paused) {
        rootHandler.replayPaused(paused);
    }

    public void fireSimStopped(boolean stopped) {
        rootHandler.replayStopped(stopped);
    }

    public void fireSimFinished() {
        rootHandler.replayFinished();
    }

    public void elapsedSimTime(double secs, double secsTotal) {
        rootHandler.elapsedSimTime(secs, secsTotal);
    }

    @Override
    public AirplaneCache getAirplaneCache() {
        return (AirplaneCache)super.getAirplaneCache();
    }

    @Override
    public FlightplanManager getFPmanager() {
        return (FlightplanManager)super.getFPmanager();
    }

    @Override
    public IHardwareConfiguration getHardwareConfiguration() {
        // this object will always stay valid for this current aircraft...
        return getFPmanager().getHardwareConfigurationOnAir();
    }

    boolean simSettingsRestoreAttempted = true;

    @Override
    public synchronized void connectionStateChange(AirplaneConnectorState newState) {
        if (newState == AirplaneConnectorState.fullyConnected) {
            // if autologging is enabled, start it now!
            if (generalSettings.getLogDefaultOn()) {
                m_tcp_logger.autoChangeEnability(); // the request is done below
            }

            simSettingsRestoreAttempted = false;
        } else if (newState == AirplaneConnectorState.unconnected) {
            simSettingsRestoreAttempted = true;
            clearNextPosReceives = false;
        } else {
            simSettingsRestoreAttempted = false;
        }

        // its essential for LocalJava sim to do this afterwards! otherwise e.g. simSettingsRestoreAttempted has wrong
        // content
        super.connectionStateChange(newState);
    }

    @Override
    public synchronized void err_backendConnectionLost(final ConnectionLostReasons reason) {
        if (reason == ConnectionLostReasons.DISCONNECTED_BY_USER) {
            return;
        }

        if (reason == ConnectionLostReasons.CONNECTION_REMOVED) {
            return;
        }

        if (reason == ConnectionLostReasons.LOGFILE_REPLAY_STOPPED) {
            return;
        }

        if (reason == ConnectionLostReasons.LOGFILE_AT_END) {
            // Show info message
            dialogService.showInfoMessage(
                languageHelper.getString(KEY + ".logFilePlayFinished.title"),
                languageHelper.getString(KEY + ".logFilePlayFinished"));
        }
    }

    @Override
    public void requestAll() {
        if (!isWriteable()) {
            return;
        }

        super.requestAll();
        // the following stuff is not nessesary to know in android
        // requestDirListing("/"); //Not request here, its done in FTP dialog
    }

    Vector<Integer> indWarnYellow = new Vector<Integer>();
    Vector<Integer> indWarnRed = new Vector<Integer>();

    @SuppressWarnings("unchecked")
    @Override
    public void recv_health(HealthData d) {
        try {
            PlaneInfo info = cache.getPlaneInfo();
            Vector<Integer> nextIndWarnYellow = info.indexesWarnYellow(d);
            Vector<Integer> nextIndWarnRed = info.indexesWarnRed(d);

            Vector<Integer> tmpWarnYellow = (Vector<Integer>)nextIndWarnYellow.clone();
            Vector<Integer> tmpWarnRed = (Vector<Integer>)nextIndWarnRed.clone();

            nextIndWarnYellow.removeAll(indWarnYellow);
            nextIndWarnRed.removeAll(indWarnRed);

            indWarnRed = tmpWarnRed;
            indWarnYellow = tmpWarnYellow;

            Vector<SingleHealthDescription> hds = info.healthDescriptions;
            for (int i = 0; i != nextIndWarnRed.size(); i++) {
                int ind = nextIndWarnRed.get(i);
                SingleHealthDescription hd = hds.get(ind);
                float abs = d.absolute.get(ind);
                if (hd.isFlag()) {
                    DependencyInjector.getInstance()
                        .getInstanceOf(IApplicationContext.class)
                        .addToast(
                            Toast.of(ToastType.ALERT)
                                .setText(languageHelper.getString(KEY + ".warning.red_flag.msg", hd.name))
                                .setShowIcon(true)
                                .create());
                } else {
                    DependencyInjector.getInstance()
                        .getInstanceOf(IApplicationContext.class)
                        .addToast(
                            Toast.of(ToastType.ALERT)
                                .setText(
                                    languageHelper.getString(
                                        KEY + ".warning.red.msg", hd.name, abs, hd.unit, hd.minYellow, hd.maxYellow))
                                .setShowIcon(true)
                                .create());
                }
            }

            for (int i = 0; i != nextIndWarnYellow.size(); i++) {
                int ind = nextIndWarnYellow.get(i);
                SingleHealthDescription hd = hds.get(ind);
                float abs = d.absolute.get(ind);
                if (Double.parseDouble(info.releaseVersion) > 3) {
                    if (hd.isFlag()) {
                        Debug.getLog().info(languageHelper.getString(KEY + ".warning.yellow_flag.msg", hd.name));
                    } else {
                        Debug.getLog()
                            .info(
                                languageHelper.getString(
                                    KEY + ".warning.yellow.msg", hd.name, abs, hd.unit, hd.minGreen, hd.maxGreen));
                    }
                } else {
                    if (hd.isFlag()) {
                        DependencyInjector.getInstance()
                            .getInstanceOf(IApplicationContext.class)
                            .addToast(
                                Toast.of(ToastType.INFO)
                                    .setText(languageHelper.getString(KEY + ".warning.yellow_flag.msg", hd.name))
                                    .create());
                    } else {
                        DependencyInjector.getInstance()
                            .getInstanceOf(IApplicationContext.class)
                            .addToast(
                                Toast.of(ToastType.INFO)
                                    .setText(
                                        languageHelper.getString(
                                            KEY + ".warning.yellow.msg",
                                            hd.name,
                                            abs,
                                            hd.unit,
                                            hd.minGreen,
                                            hd.maxGreen))
                                    .create());
                    }
                }
            }

        } catch (AirplaneCacheEmptyException e) {
        }
    }

    SimulationSettings simSettingsToRestore;

    @Override
    public void recv_simulationSettings(SimulationSettings settings) {
        // Debug.printStackTrace(simSettingsRestoreAttempted);
        if (!simSettingsRestoreAttempted) {
            return; // dont overwrite it with simulation defaults, before tryed to restore
        }

        simSettingsToRestore = settings;
    }

    boolean clearNextPosReceives = false;

    @Override
    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        // Debug.printStackTrace(lon, lat, pressureZero,
        // simSettingsRestoreAttempted,getAirplaneCache().wasLastRecvStartPosMajorChange());
        if (getAirplaneCache().wasLastRecvStartPosMajorChange()) {
            clearNextPosReceives = true;
            DependencyInjector.getInstance().getInstanceOf(IMapClearingCenter.class).clearAllCaches();
        }

        if (!simSettingsRestoreAttempted) {
            return; // dont overwrite it with simulation defaults, before tryed to restore
        }

        startCoordinates.set(new LatLon(Angle.fromDegrees(lat), Angle.fromDegrees(lon)));
    }

    int lastAirspaceWarning = 0;

    @Override
    public void recv_position(PositionData p) {
        if (clearNextPosReceives) {
            try {
                if (AirplaneCache.distanceMeters(
                            p.lat, p.lon, getAirplaneCache().getStartLatBaro(), getAirplaneCache().getStartLonBaro())
                        < AirplaneCache.AutoRecenterStartDistanceMeter) {
                    clearNextPosReceives = false;
                    Dispatcher dispatcher = Dispatcher.platform();
                    dispatcher.run(
                        new Runnable() {
                            @Override
                            public void run() {
                                DependencyInjector.getInstance()
                                    .getInstanceOf(IMapClearingCenter.class)
                                    .clearAllCaches();
                            }
                        });
                }
            } catch (AirplaneCacheEmptyException e) {
                // Debug.getLog().log(Level.WARNING,"clearing track after startPosJump failed",e);
            }
        }

        // warn airspace stuff
        LowestAirspace lowest;
        try {
            lowest = getAirplaneCache().getMaxMAVAltitude();
        } catch (AirplaneCacheEmptyException e) {
            Debug.getLog().log(Level.SEVERE, "THIS should never happen", e);
            return;
        }

        double airspaceAlt = lowest.getMinimalAltOverGround();

        double altitudeAboveGround = 0;
        try {
            altitudeAboveGround = getAirplaneCache().getCurPlaneElevOverGround();

            // rounded to nearest meter

        } catch (AirplaneCacheEmptyException e) {
            if (p != null) {
                altitudeAboveGround = Math.round(p.altitude / 100.);
            }
        }

        int nextAirspaceWarning = 0;
        String txt;
        String relationChar;
        if (airspaceAlt <= 5) {
            nextAirspaceWarning = 2;
            relationChar = "≥";
        } else if (altitudeAboveGround < airspaceAlt - Airspace.SAFETY_MARGIN_IN_METER) {
            nextAirspaceWarning = 0;
            relationChar = "≤";
        } else if (altitudeAboveGround < airspaceAlt) {
            nextAirspaceWarning = 1;
            relationChar = "≤";
        } else {
            nextAirspaceWarning = 2;
            relationChar = "≥";
        }

        if (nextAirspaceWarning > lastAirspaceWarning) {
            boolean levelRed = nextAirspaceWarning == 2;
            txt =
                quantityFormat.format(Quantity.of(altitudeAboveGround, Unit.METER), UnitInfo.LOCALIZED_LENGTH)
                    + " "
                    + relationChar
                    + " "
                    + quantityFormat.format(Quantity.of(airspaceAlt, Unit.METER), UnitInfo.LOCALIZED_LENGTH);

            IAirspace airplane = lowest.getMinimalAirspace();
            Ensure.notNull(airplane, "airplane");
            DependencyInjector.getInstance()
                .getInstanceOf(IApplicationContext.class)
                .addToast(
                    Toast.of(ToastType.ALERT)
                        .setText(
                            languageHelper.getString(
                                KEY + ".warning.altitude.msg." + levelRed, txt, airplane.toString()))
                        .setShowIcon(true)
                        .create());
        }

        lastAirspaceWarning = nextAirspaceWarning;
    }

    @Override
    public void recv_simulationSpeed(Float speed) {}

    @Override
    public void recv_planeInfo(PlaneInfo info) {
        generalSettings.lastSeenAPForSupportProperty().set(info.serialNumber + " " + info.getHumanReadableSWversion());

        if (!info.fromSession) {
            String key = "seen." + EnumUpdateTargets.AUTOPILOT + "." + info.serialNumber;
            int cur = 0;
            try {
                cur = Integer.parseInt(generalSettings.getProperty(key, "0").get());
            } catch (Exception e) {
            }

            cur++;
            generalSettings.setProperty(key, "" + cur);
        }
        // checking camera config
        checkCamera();
    }

    @Override
    public void recv_backend(Backend host, MVector<Port> ports) {
        generalSettings
            .lastSeenConnectorForSupportProperty()
            .set(host.info.serialNumber + " " + host.info.getHumanReadableSWversion());

        if (!host.info.fromSession) {
            String key = "seen." + EnumUpdateTargets.BACKEND + "." + host.info.serialNumber;
            long cur = 0;
            try {
                cur = Long.parseLong(generalSettings.getProperty(key, "0").get());
            } catch (Exception e) {
            }

            cur++;
            generalSettings.setProperty(key, "" + cur);
        }

        Backend clone;
        try {
            clone = (Backend)host.clone();
        } catch (CloneNotSupportedException e) {
            Debug.getLog().log(Level.WARNING, "could not store backend state to session", e);
            return;
        }

        clone.time_sec = 0; // otherwise the
        clone.batteryVoltage = 0;
        clone.hasGPS = false;
        clone.hasRTCMInput = false;
        clone.hasFix = false;
    }

    @Override
    public void recv_powerOn() {
        DependencyInjector.getInstance().getInstanceOf(IMapClearingCenter.class).clearAllCaches();

        // Debug.printStackTrace("attempSetStartPos", isWriteable(), isSimulation(), getConnectionState());
        if (getConnectionState() == AirplaneConnectorState.fullyConnected) {
            if (isWriteable() && isSimulation()) {
                // We don't need to rewrite startPosition of simulator
                //                if (getSession().containsKey(AirplaneSession.KEY_STARTPOS_LAT)
                //                        && getSession().containsKey(AirplaneSession.KEY_STARTPOS_LON)) {
                //                    try {
                //                        double lat =
                // Double.parseDouble(getSession().getProperty(AirplaneSession.KEY_STARTPOS_LAT));
                //                        double lon =
                // Double.parseDouble(getSession().getProperty(AirplaneSession.KEY_STARTPOS_LON));
                //                        Debug.getLog().config("restoring sim starting position to: lat=" + lat + "
                // lon=" + lon);
                //                        // Debug.printStackTrace(lon,lat);
                //                        setStartpos(lon, lat);
                //                    } catch (NumberFormatException e) {
                //                        Debug.getLog().log(Level.FINER, "Startpos wrong formattet in session", e);
                //                    }
                //                }
                try {
                    SimulationSettings settings = simSettingsToRestore;
                    if (settings == null) {
                        settings = getAirplaneCache().getSimulationSettings();
                    }

                    setSimulationSettings(settings);
                } catch (AirplaneCacheEmptyException e) {
                    Debug.getLog().log(Level.WARNING, "Could not get SimulationSettings from cache", e);
                }
            }

            simSettingsRestoreAttempted = true;
        }
    }

    File camResourceFile = null;

    boolean hasWarnedCameraWrong = false;

    void checkCamera() {
        if (hasWarnedCameraWrong) {
            return;
        }

        PlaneInfo info;
        try {
            info = cache.getPlaneInfo();
        } catch (AirplaneCacheEmptyException e) {
            return;
        }
        // check that release version not a null make tests pass
        if (info.releaseVersion != null
                && !getPlatformDescription().getAPtype().compatibleWithThisAPrelease(info.releaseVersion)) {
            hasWarnedCameraWrong =
                true; // set this temorary to ignore, other wise the message will pop up many times simultaniously on
            // connecting

            final String KEY = "eu.mavinci.desktop.gui.doublepanel.camerasettings.PlaneParameterWidget";

            final String KEY_APtypes = KEY + ".cmbAPtype";

            DialogResult result =
                dialogService.requestCancelableConfirmation(
                    languageHelper.getString(KEY + ".wrongApTypeInCamera.title"),
                    UserNotificationHubSwing.wrapText(
                        languageHelper.getString(
                            KEY + ".wrongApTypeInCamera",
                            info.releaseVersion,
                            languageHelper.getString(KEY_APtypes + "." + getPlatformDescription().getAPtype()),
                            getPlatformDescription().getName())));

            if (result == DialogResult.YES) {
                hasWarnedCameraWrong = true;
                APTypes apType = APTypes.getCompatibleAPrelease(info.releaseVersion);
                if (apType != null) {
                    // camera.setAPtype(apType);
                }
            } else if (result == DialogResult.NO) {
                hasWarnedCameraWrong = true;
            } else {
                hasWarnedCameraWrong = false;
            }

            // Debug.getUserNotifier().handlePopupMustConfirm(
            // languageHelper.getString(KEY+".wrongApTypeInCamera", info.releaseVersion,
            // languageHelper.getString(PlaneParameterWidget.KEY_APtypes
            // +"."+camera.getAPtype()))
            // , languageHelper.getString(KEY+".wrongApTypeInCamera.title"),false);
        }
    }

    private IPlatformDescription getPlatformDescription() {
        return getHardwareConfiguration().getPlatformDescription();
    }

    @Override
    public String toString() {
        if (fpManager.getOnAirFlightplan() == null || fpManager.getOnAirFlightplan().getFile() == null) {
            return null;
        } else {
            return nativeHardwareConfiguration + " flightPlan:" + fpManager.getOnAirFlightplan().getFile().getName();
        }
    }

    @Override
    public void recv_flightPhase(Integer fp) {}

    @Override
    public void disconnectSilently() {
        curConnectorState = AirplaneConnectorState.unconnected;
    }

    @Override
    public void setHardwareConfiguration(IHardwareConfiguration hardwareConfiguration) {
        getFPmanager().getOnAirFlightplan().getHardwareConfiguration().initializeFrom(hardwareConfiguration);
    }

    @Override
    public void cancelLaunch() {
        getAirplaneConnector().cancelLaunch();
    }

    @Override
    public void cancelLanding() {
        getAirplaneConnector().cancelLanding();
    }

    @Override
    public File getMatchingsFolder() {
        return MissionConstants.getMatchingsFolder(baseFolder.get());
    }

    @Override
    public File getFlightplanAutosaveFolder() {
        return MissionConstants.getFlightplanAutosaveFolder(baseFolder.get());
    }

    @Override
    public File getFTPFolder() {
        return MissionConstants.getFlightLogsFolder(baseFolder.get());
    }

    public AsyncObjectProperty<Path> baseFolderProperty() {
        return baseFolder;
    }

    @Override
    public File getBaseFolder() {
        return baseFolder.get().toFile();
    }

    @Override
    public File getLogFolder() {
        return MissionConstants.getLogFolder(baseFolder.get());
    }

    @Override
    public CPicArea getPicAreaTemplate(PlanType type) {
        Flightplan fp = (Flightplan)fpManager.getOnAirFlightplan();
        return fp.getPicAreaTemplate(type);
    }

}
