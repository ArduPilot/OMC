/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.rs232;

import java.util.stream.Stream;

public interface PortsSource {
    Stream<String> listPorts();
}
