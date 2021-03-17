/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IObstacleAvoidance;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedFutureCommand;
import java.lang.ref.WeakReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.util.Duration;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Future;

public class ObstacleAvoidanceTelemetryViewModel extends DialogViewModel {

    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IObstacleAvoidance.Mode> oaMode = new SimpleAsyncObjectProperty<>(this);
    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final BooleanProperty obstacleAvoidanceEnabled = new SimpleBooleanProperty();
    private final Command closeDialogCommand;
    private final ParameterizedFutureCommand<Boolean> enableObstacleAvoidanceCommand;
    private final ChangeListener<Object> missionPropertyListener =
        (observableValue, oldValue, newValue) -> getCloseCommand().execute();

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private FlightScope flightScope;

    private WeakReference<Toast> previousToastReference;

    @Inject
    public ObstacleAvoidanceTelemetryViewModel(IApplicationContext applicationContext, ILanguageHelper languageHelper) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        closeDialogCommand = new DelegateCommand(() -> getCloseCommand().execute());
        enableObstacleAvoidanceCommand = new ParameterizedFutureCommand<>(this::sendObstacleAvoidanceCommandAsync);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
        drone.bind(flightScope.currentDroneProperty());
        applicationContext.currentMissionProperty().addListener(new WeakChangeListener<>(missionPropertyListener));
        initializeObstacleAvoidance();
    }

    /** Initializes the Collision Avoidance Property. */
    private void initializeObstacleAvoidance() {
        oaMode.addListener(
            (observable, oldValue, newValue) ->
                obstacleAvoidanceEnabled.setValue(newValue == IObstacleAvoidance.Mode.ENABLED));
        oaMode.bind(
            PropertyPath.from(drone)
                .select(IDrone::obstacleAvoidanceProperty)
                .selectReadOnlyAsyncObject(IObstacleAvoidance::modeProperty, IObstacleAvoidance.Mode.NOT_AVAILABLE));
    }

    /**
     * Sends command to the Drone for Enabling/Disabling the Collision Avoidance Property.
     *
     * @param enable - Current Value
     */
    private Future<Void> sendObstacleAvoidanceCommandAsync(boolean enable) {
        IObstacleAvoidance obstacleAvoidance = drone.get().obstacleAvoidanceProperty().get();
        Future<Void> future = obstacleAvoidance.enableAsync(enable);

        future.whenSucceeded(() -> this.obstacleAvoidanceEnabled.setValue(enable));

        future.whenCancelled(() -> obstacleAvoidanceCommandFailed(enable));
        future.whenFailed(() -> obstacleAvoidanceCommandFailed(enable));
        return future;
    }

    private void obstacleAvoidanceCommandFailed(boolean enable) {
        obstacleAvoidanceEnabled.setValue(!enable);

        // Toast - failure notification to the user.
        Toast toast = previousToastReference == null ? null : previousToastReference.get();
        if (toast != null
                && toast.getText()
                    .equals(languageHelper.getString(ObstacleAvoidanceTelemetryViewModel.class, "commandFailed"))) {
            toast.dismiss();
        }

        toast =
            Toast.of(ToastType.ALERT)
                .setText(languageHelper.getString(ObstacleAvoidanceTelemetryViewModel.class, "commandFailed"))
                .setCloseable(true)
                .setTimeout(Duration.seconds(30))
                .setShowIcon(true)
                .setAction(
                    languageHelper.getString(ObstacleAvoidanceTelemetryViewModel.class, "retry"),
                    false,
                    true,
                    () -> sendObstacleAvoidanceCommandAsync(enable),
                    MoreExecutors.directExecutor())
                .create();
        previousToastReference = new WeakReference<>(toast);
        applicationContext.addToast(toast);
    }

    public MainScope getMainScope() {
        return mainScope;
    }

    public BooleanProperty obstacleAvoidanceEnabledProperty() {
        return obstacleAvoidanceEnabled;
    }

    public Command getCloseDialogCommand() {
        return closeDialogCommand;
    }

    public ParameterizedCommand<Boolean> getEnableObstacleAvoidanceCommand() {
        return enableObstacleAvoidanceCommand;
    }

}
