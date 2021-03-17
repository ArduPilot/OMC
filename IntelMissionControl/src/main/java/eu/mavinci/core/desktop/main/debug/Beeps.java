/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.desktop.main.debug;

// source sounds: http://www.soundjay.com/
// licence: http://www.soundjay.com/tos.html
// free to use for us without credits to them
public enum Beeps {
    SlideIn("eu/mavinci/sound/beep-6"),
    Blocking("eu/mavinci/sound/beep-1"),
    Modal("eu/mavinci/sound/beep-2");

    public String resource;
    public Object loadedResource;

    Beeps(String resource) {
        this.resource = resource;
    }
}
