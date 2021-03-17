/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.rs232;

public interface IConfigurationProperties {

    String getPort(String key, String defaultValue);

    void changePort(String key, String port);

    int getBaudRate(String key, int defaultValue);

    void changeBaudRate(String key, int baudRate);

    int getDataBits(String key, int defaultValue);

    void changeDataBits(String key, int dataBits);

    int getStopBits(String key, int defaultValue);

    void changeStopBits(String key, int stopBits);

    int getParity(String key, int defaultValue);

    void changeParity(String key, int parity);
}
