/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.TestBase;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.asyncfx.concurrent.Dispatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoordinateTransformTest extends TestBase {

    /*@Test
    public void absolute_coordinates_transformed_to_local_and_back_match()
            throws InterruptedException, ExecutionException, TimeoutException {
        Dispatcher.platform()
            .runLaterAsync(
                () -> {
                    Project project = new Project();
                    Position position = new Position(Angle.fromDegrees(40), Angle.fromDegrees(11), 0);
                    Mission mission = new Mission(position);
                    project.missionsProperty().add(mission);

                    Goal2D goal = new Goal2D();
                    mission.goalsProperty().add(goal);

                    Position corner = new Position(Angle.fromDegrees(41), Angle.fromDegrees(10), 0);
                    mission.addPosition(goal, corner);
                    Position cornerBackToGlobal = mission.getPositions(goal).get(0);

                    Assertions.assertEquals(0.00, corner.latitude.degrees - cornerBackToGlobal.latitude.degrees);
                    Assertions.assertEquals(0.00, corner.longitude.degrees - cornerBackToGlobal.longitude.degrees);
                })
            .get(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void shifting_mission_origin_shifts_all_goals_correspondingly()
            throws InterruptedException, ExecutionException, TimeoutException {
        Dispatcher.platform()
            .runLaterAsync(
                () -> {
                    Project project = new Project();
                    Position origin = new Position(Angle.fromDegrees(40), Angle.fromDegrees(11), 0);
                    Mission mission = new Mission(origin);
                    project.missionsProperty().add(mission);

                    Goal2D goal = new Goal2D();
                    mission.goalsProperty().add(goal);

                    Position corner = new Position(Angle.fromDegrees(41), Angle.fromDegrees(10), 0);

                    mission.addPosition(goal, corner);
                    mission.shiftMission(corner);

                    Position cornerBackToGlobal = mission.getPositions(goal).get(0);

                    Assertions.assertNotEquals(0.00, corner.latitude.degrees - cornerBackToGlobal.latitude.degrees);
                    Assertions.assertNotEquals(0.00, corner.longitude.degrees - cornerBackToGlobal.longitude.degrees);
                })
            .get(1000, TimeUnit.MILLISECONDS);
    }*/
}
