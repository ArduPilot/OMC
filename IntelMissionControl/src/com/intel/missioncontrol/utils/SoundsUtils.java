/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundsUtils {
    public static final Logger log = LoggerFactory.getLogger(SoundsUtils.class);

    public static void playSound(Sounds sound) {
        try {
            AudioClip clip = new AudioClip(SoundsUtils.class.getResource(sound.getUri()).toExternalForm());
            clip.play();
        } catch (Exception e) {
            log.error("Could not play sound for some reason", e);
        }
    }
}
