/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.legacy;

import com.google.common.collect.Maps;
import com.intel.missioncontrol.drone.AlertLevel;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.SingleHealthDescription;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlaneHealth {

    private static final String NO_DATA = "--";
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.ExpertLayer";
    private static final String KEY_FLAG_OK = KEY + ".ok";
    private static final String KEY_FLAG_FAILURE = KEY + ".failure";
    private static final String KEY_FLAG_NO_DATA = KEY + ".noData";

    private final Map<String, PlaneHealthChannelInfo> channels = new HashMap<>();

    static {
        NUMBER_FORMAT.setMinimumFractionDigits(0);
        NUMBER_FORMAT.setMaximumFractionDigits(1);
        NUMBER_FORMAT.setGroupingUsed(false);
    }

    public PlaneHealth() {}

    public PlaneHealth(PlaneInfo info) {
        setPlaneInfo(info);
    }

    public void setPlaneInfo(PlaneInfo info) {
        channels.clear();

        if (info == null) {
            return;
        }

        MVector<SingleHealthDescription> descriptions = info.healthDescriptions;

        if (descriptions == null) {
            return;
        }

        for (int i = 0; i < descriptions.size(); i++) {
            SingleHealthDescription description = descriptions.get(i);

            if (description == null) {
                continue;
            }

            channels.put(description.name, new PlaneHealthChannelInfo(i, description));
        }
    }

    public static Map<String, PlaneHealthChannelStatus> getNotImportantChannelStatusesWithAlert(
            Map<String, PlaneHealthChannelStatus> statuses) {
        return Maps.filterEntries(statuses, entry -> !entry.getValue().isImportant());
    }

    public Map<String, PlaneHealthChannelStatus> getChannelStatusesWithAlert(final HealthData data) {
        return getChannelStatuses(
            data,
            channelInfo -> true,
            channelStatus -> {
                if (channelStatus.isFailEvent()) {
                    // magic from PlaneInfo
                    return (Math.round(channelStatus.getAbsoluteValue()) >= 512);
                }

                return !channelStatus.isGreen();
            });
    }

    public Map<String, PlaneHealthChannelStatus> getChannelStatuses(
            final HealthData data,
            final Predicate<? super PlaneHealthChannelInfo> channelInfoPredicate,
            final Predicate<? super PlaneHealthChannelStatus> channelStatusPredicate)
            throws IllegalArgumentException {
        Expect.notNull(data, "data");
        Expect.notNull(channelInfoPredicate, "channelInfoPredicate");
        Expect.notNull(channelStatusPredicate, "channelStatusPredicate");

        if (channels.isEmpty()) {
            return Collections.emptyMap();
        }

        return channels.values()
            .stream()
            .filter(channelInfoPredicate)
            .map(channelInfo -> getChannelStatus(data, channelInfo))
            .filter(channelStatusPredicate)
            .collect(Collectors.toMap(PlaneHealthChannelStatus::getName, Function.identity()));
    }

    public PlaneHealthChannelInfo getChannelInfo(PlaneHealthChannel channel) throws IllegalArgumentException {
        Expect.notNull(channel, "channel");

        return getChannelInfo(channel.getName());
    }

    public PlaneHealthChannelInfo getChannelInfo(String channelName) throws IllegalArgumentException {
        Expect.notNull(channelName, "channelName");

        return channels.get(channelName);
    }

    public PlaneHealthChannelStatus getChannelStatus(HealthData data, PlaneHealthChannel channel)
            throws IllegalArgumentException {
        Expect.notNull(channel, "channel");
        Expect.notNull(data, "data");

        return getChannelStatus(data, channel.getName());
    }

    public PlaneHealthChannelStatus getChannelStatus(HealthData data, String channelName)
            throws IllegalArgumentException {
        Expect.notNull(data, "data");
        Expect.notNull(channelName, "channelName");

        PlaneHealthChannelInfo channelInfo = getChannelInfo(channelName);
        return getChannelStatus(data, channelInfo);
    }

    private PlaneHealthChannelStatus getChannelStatus(HealthData data, PlaneHealthChannelInfo channelInfo)
            throws IllegalStateException {
        if (channelInfo == null) {
            return null;
        }

        MVector<Float> absData = data.absolute;
        Expect.notNull(absData, "absData");
        Float absolute = getChannelValue(absData, channelInfo.getIndex());
        Float percent = getChannelValue(data.percent, channelInfo.getIndex());

        return new PlaneHealthChannelStatus(channelInfo.getDescription(), absolute, percent);
    }

    private Float getChannelValue(Vector<Float> values, int index) {
        if (values == null) {
            return null;
        }

        if ((index < 0) || (index >= values.size())) {
            return null;
        }

        return values.get(index);
    }

    private static AlertLevel getAlert(SingleHealthDescription description, Number healthValue) {
        if (healthValue == null) {
            return AlertLevel.RED;
        }

        float value = healthValue.floatValue();

        if (description.isRed(value)) {
            return AlertLevel.RED;
        }

        if (description.isYellow(value)) {
            return AlertLevel.YELLOW;
        }

        return AlertLevel.GREEN;
    }

    public static class PlaneHealthChannelStatus {

        private final SingleHealthDescription description;
        private final Float absolute;
        private final Float percent;

        public PlaneHealthChannelStatus(SingleHealthDescription description, Float absolute, Float percent)
                throws IllegalArgumentException {
            Expect.notNull(description, "description");

            this.description = description;
            this.absolute = absolute;
            this.percent = percent;
        }

        public SingleHealthDescription getDescription() {
            return description;
        }

        public String getName() {
            return description.name;
        }

        public Float getAbsolute() {
            return absolute;
        }

        public Float getPercent() {
            return percent;
        }

        public float getAbsoluteValue() {
            return ((absolute == null) ? (0f) : (absolute.floatValue()));
        }

        public float getPercentValue() {
            return ((percent == null) ? (0f) : (percent.floatValue()));
        }

        public String getUnit() {
            if (description.unit == null) {
                return "";
            }

            return description.unit;
        }

        public String getAbsoluteAsString() {
            if (description.isFlag()) {
                return getFlagStatus();
            }

            if (absolute == null) {
                return NO_DATA;
            }

            return NUMBER_FORMAT.format(absolute) + " " + getUnit();
        }

        public String getPercentAsString() {
            if (percent == null) {
                return NO_DATA;
            }

            return StringHelper.ratioToPercent(percent / 100.0, 0, true);
        }

        public AlertLevel getAlert() {
            return PlaneHealth.getAlert(description, absolute);
        }

        public boolean isGreen() {
            return description.isGreen(absolute);
        }

        public boolean isImportant() {
            return description.isImportant;
        }

        public boolean isFailEvent() {
            return PlaneConstants.DEF_FAIL_EVENTS.equals(description.name);
        }

        public boolean isGpsQuality() {
            return PlaneConstants.DEF_GPS_QUALITY.equals(getName());
        }

        public boolean isMainBattery() {
            return description.isMainBatt();
        }

        public GPSFixType getGpsQuality() {
            if ((absolute != null) && (GPSFixType.isValid(absolute))) {
                return GPSFixType.values()[absolute.intValue()];
            }

            return GPSFixType.noFix;
        }

        public String getFlagStatus() {
            return DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class).getString(getFlagStatusKey());
        }

        private String getFlagStatusKey() {
            if (absolute == null) {
                return KEY_FLAG_NO_DATA;
            }

            if (description.isGreen(absolute)) {
                return KEY_FLAG_OK;
            }

            return KEY_FLAG_FAILURE;
        }

        public boolean isFlightMode() {
            return PlaneConstants.FLIGHT_MODE.equals(getName());
        }
    }

    public static class PlaneHealthChannelInfo {

        private final int index;
        private final SingleHealthDescription description;

        public PlaneHealthChannelInfo(int index, SingleHealthDescription description) {
            this.index = index;
            this.description = description;
        }

        public int getIndex() {
            return index;
        }

        public SingleHealthDescription getDescription() {
            return description;
        }

        public boolean isRed(float value) {
            return description.isRed(value);
        }

        public boolean isYellow(float value) {
            return description.isYellow(value);
        }

        public boolean isGreen(float value) {
            return description.isGreen(value);
        }

        public boolean isImportant() {
            return description.isImportant;
        }

        public boolean isFlag() {
            return description.isFlag();
        }

        public AlertLevel getAlert(Number value) {
            return PlaneHealth.getAlert(description, value);
        }

    }

    public static enum PlaneHealthChannel {
        BATTERY_MAIN(PlaneConstants.DEF_BATTERY),
        BATTERY_CONNECTOR(PlaneConstants.DEF_BATTERY_CONNECTOR),
        GPS(PlaneConstants.DEF_GPS),
        GLONASS(PlaneConstants.DEF_GLONASS),
        GPS_QUALITY(PlaneConstants.DEF_GPS_QUALITY),
        MOTOR1(PlaneConstants.DEF_MOTOR1),
        WIND_SPEED(PlaneConstants.WIND_SPEED),
        WIND_DIRECTION(PlaneConstants.WIND_DIRECTION),
        GROUND_SPEED(PlaneConstants.GROUND_SPEED);

        private final String name;

        private PlaneHealthChannel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

}
