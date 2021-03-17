/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DescriptionsTest {

    @Test
    void testDescriptionsParsing() {
        String project = new File("").getAbsolutePath();
        Path descriptions = Path.of(project, "\\src\\main\\resources\\com\\intel\\missioncontrol\\descriptions");
        DescriptionProvider provider = new DescriptionProvider(descriptions);
        Assertions.assertNotEquals(0, provider.getPlatformDescriptions().size());
    }
}
