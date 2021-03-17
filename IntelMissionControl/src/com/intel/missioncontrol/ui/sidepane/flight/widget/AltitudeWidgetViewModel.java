/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.widget;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import de.saxsys.mvvmfx.InjectScope;
import eu.mavinci.core.plane.AirplaneConnectorState;
import javafx.beans.value.ChangeListener;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import javafx.util.Duration;

public class AltitudeWidgetViewModel extends ViewModelBase {

    public static final String EVENT_UAV_ALTITUDE_UPDATE = "EVENT_UAV_ALTITUDE_UPDATE";
    public static final String EVENT_TARGET_ALTITUDE_UPDATE = "EVENT_TARGET_ALTITUDE_UPDATE";
    public static final String EVENT_MAX_ALLOWED_ALTITUDE_UPDATE = "EVENT_MAX_ALLOWED_ALTITUDE_UPDATE";
    public static final String EVENT_STOP_AGGREGATION = "EVENT_STOP_AGGREGATION";

    private Queue<AltitudeEvent> currentAltitudeEvents = new ConcurrentLinkedQueue<>();
    private ScheduledFuture<?> altitudeEventAggregator;

    @InjectScope
    private MainScope mainScope;

    @Inject
    private IApplicationContext applicationContext;

    private ChangeListener<AirplaneConnectorState> airplaneConnectorStateChangeListener =
        (observable, oldValue, newValue) -> {
            if (newValue == AirplaneConnectorState.fullyConnected) {
                startValuesAggregation();
            } else {
                stopValuesAggregation();
            }
        };

    private ChangeListener<Number> altitudeChangeListener =
        (observable, oldValue, newValue) -> {
            currentAltitudeEvents.add(new AltitudeEvent((Double)newValue, new Date()));
        };

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        applicationContext
            .currentMissionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        stopValuesAggregation();
                    } else {
                        listenUavConnectionState(newValue.uavProperty().get());
                    }
                });
        if (applicationContext.getCurrentMission() != null) {
            listenUavConnectionState(applicationContext.getCurrentMission().uavProperty().get());
        }
    }

    private void startValuesAggregation() {
        altitudeEventAggregator =
            Dispatcher.schedule(this::aggregateEvents, Duration.millis(100), Duration.millis(250));
        applicationContext
            .currentMissionProperty()
            .get()
            .uavProperty()
            .get()
            .altitudeValueProperty()
            .removeListener(altitudeChangeListener);
        applicationContext
            .currentMissionProperty()
            .get()
            .uavProperty()
            .get()
            .altitudeValueProperty()
            .addListener(altitudeChangeListener);
    }

    private void stopValuesAggregation() {
        if (altitudeEventAggregator != null) {
            altitudeEventAggregator.cancel(true);
            altitudeEventAggregator = null;
        }

        currentAltitudeEvents.clear();
        if (applicationContext.getCurrentMission() != null) {
            applicationContext
                .getCurrentMission()
                .uavProperty()
                .get()
                .altitudeValueProperty()
                .removeListener(altitudeChangeListener);
        }

        publish(EVENT_STOP_AGGREGATION);
    }

    private void listenUavConnectionState(Uav uav) {
        if (uav == null) {
            return;
        }

        uav.connectionProperty().removeListener(airplaneConnectorStateChangeListener);
        uav.connectionProperty().addListener(airplaneConnectorStateChangeListener);
        if (uav.getConnectionState() == AirplaneConnectorState.fullyConnected) {
            startValuesAggregation();
        }
    }

    public void aggregateEvents() {
        AltitudeEvent currentAltitudeEvent = collapseEvents(currentAltitudeEvents);
        if (currentAltitudeEvent == null) {
            return;
        }

        Uav uav = applicationContext.getCurrentMission().uavProperty().get();
        Dispatcher.postToUI(
            () -> {
                publish(EVENT_UAV_ALTITUDE_UPDATE, currentAltitudeEvent);
                publish(
                    EVENT_TARGET_ALTITUDE_UPDATE,
                    new AltitudeEvent(uav.recommendedAltValueProperty().get(), new Date()));
                publish(
                    EVENT_MAX_ALLOWED_ALTITUDE_UPDATE,
                    new AltitudeEvent(uav.maxAltitudeValueProperty().get(), new Date()));
            });
    }

    private AltitudeEvent collapseEvents(Queue<AltitudeEvent> events) {
        AltitudeEvent eventToDraw = events.poll();
        while (eventToDraw != null && !events.isEmpty()) {
            eventToDraw.collapseWith(events.poll());
        }

        return eventToDraw;
    }

    public static class AltitudeEvent {
        private Double altitude;
        private Date timeStamp;

        public AltitudeEvent(Double altitude, Date timeStamp) {
            this.altitude = altitude;
            this.timeStamp = new Date(timeStamp.getTime());
        }

        public Double getAltitude() {
            return altitude;
        }

        public Date getTimeStamp() {
            return new Date(timeStamp.getTime());
        }

        public void collapseWith(AltitudeEvent similarEvent) {
            if (similarEvent != null) {
                altitude = (altitude + similarEvent.getAltitude()) / 2;
                timeStamp = similarEvent.getTimeStamp();
            }
        }
    }
}
