/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox.dataset;

import com.google.gson.annotations.SerializedName;
import com.intel.missioncontrol.linkbox.BatteryAlertLevel;
import com.intel.missioncontrol.linkbox.DataConnectionStatus;
import com.intel.missioncontrol.linkbox.DroneConnectionQuality;
import com.intel.missioncontrol.linkbox.WifiConnectionQuality;
import java.util.List;

public class GpTelemetryData {
    @SerializedName("name")
    String Name;

    @SerializedName("battery")
    String Battery;

    @SerializedName("wifi")
    String Wifi;

    @SerializedName("cloud")
    String Cloud;

    @SerializedName("gnss")
    GNSSState GNSS;

    @SerializedName("connection")
    String Connection;

    @SerializedName("messages")
    List<Message> Messages;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getBattery() {
        return Battery;
    }

    public void setBattery(String battery) {
        Battery = battery;
    }

    public String getWifi() {
        return Wifi;
    }

    public void setWifi(String wifi) {
        Wifi = wifi;
    }

    public String getCloud() {
        return Cloud;
    }

    public void setCloud(String cloud) {
        Cloud = cloud;
    }

    public GNSSState getGNSS() {
        return GNSS;
    }

    public void setGNSS(GNSSState GNSS) {
        this.GNSS = GNSS;
    }

    public String getConnection() {
        return Connection;
    }

    public void setConnection(String connection) {
        Connection = connection;
    }

    public List<Message> getMessages() {
        return Messages;
    }

    public void setMessages(List<Message> messages) {
        Messages = messages;
    }

    @Override
    public String toString() {
        return "{"
            + "\"name\" :"
            + "\""
            + Name
            + "\""
            + ", "
            + "\"battery\" :"
            + "\""
            + Battery
            + "\""
            + ", "
            + "\"wifi\" :"
            + "\""
            + Wifi
            + "\""
            + ","
            + "\"cloud\" :"
            + "\""
            + Cloud
            + "\""
            + ","
            + "\"gnss\" :"
            + GNSS.toString()
            + ","
            + "\"connection\" :"
            + "\""
            + Connection
            + "\""
            + getMessagesString()
            + " }";
    }

    private String getMessagesString() {
        StringBuffer result = new StringBuffer();
        if (Messages.size() > 0) {
            result.append(",\"messages\" :[");
            for (Message message : Messages) {
                if (result.lastIndexOf("messageText") != -1) {
                    result.append(",");
                }

                result.append(message.toString());
            }

            result.append("]");
        }

        return result.toString();
    }

    public BatteryAlertLevel getBatteryAlertLevel() {
        return BatteryAlertLevel.valueOf(Battery.trim().toUpperCase());
    }

    public WifiConnectionQuality getWificonnectionQuality() {
        return WifiConnectionQuality.valueOf(Wifi.trim().toUpperCase());
    }

    public DataConnectionStatus getDataConnectionStatus() {
        return DataConnectionStatus.valueOf(Cloud.trim().toUpperCase());
    }

    public DroneConnectionQuality getDroneConnectionQuality() {
        return DroneConnectionQuality.valueOf(Connection.trim().toUpperCase());
    }

}
