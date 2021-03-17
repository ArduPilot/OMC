/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.geometry.Vec2;
import com.intel.missioncontrol.geospatial.LatLon;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ProjectHelper {

    private static int count;

    static ProjectSnapshot createRandomProject(UUID id, RepositoryType repositoryType) {
        var user = new UserSnapshot(UUID.randomUUID(), "Test User");
        List<MissionSnapshot> missions = new ArrayList<>();
        missions.add(createRandomMission());
        missions.add(createRandomMission());
        missions.add(createRandomMission());

        return new ProjectSnapshot(
            id,
            "Test Project " + count++,
            repositoryType,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            user,
            "EPSG:4326",
            missions,
            Collections.emptyList(),
            Collections.emptyList());
    }

    static MissionSnapshot createRandomMission() {
        return new MissionSnapshot(
            UUID.randomUUID(),
            "Test Mission " + count++,
            LatLon.fromDegrees(0, 0),
            "EPSG:3395",
            Collections.emptyList(),
            Collections.emptyList(),
            0,
            0,
            false,
            false,
            null);
    }

}
