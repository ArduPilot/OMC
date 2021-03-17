/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.gdal;

import com.intel.missioncontrol.geometry.Vec2;
import com.intel.missioncontrol.geospatial.GdalGeoTransformFactory;
import com.intel.missioncontrol.geospatial.Position;
import com.intel.missioncontrol.geospatial.ProjectedPosition;
import com.intel.missioncontrol.project.ExtrudedPolygonGeometry;
import com.intel.missioncontrol.project.ExtrudedPolygonGoal;
import com.intel.missioncontrol.project.Mission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MissionTest {

    @Test
    public void update_origin_different_proj_srs() {
        var factory = new GdalGeoTransformFactory();

        Mission mission = new Mission();
        ExtrudedPolygonGoal goal = new ExtrudedPolygonGoal();
        mission.placeablesProperty().add(goal);
        ExtrudedPolygonGeometry geom = new ExtrudedPolygonGeometry();
        goal.geometryProperty().set(geom);

        Position pos1 = Position.fromDegrees(30.00, 10.00, 0);
        Position pos2 = Position.fromDegrees(40.01, 10.01, 0);
        Position pos3 = Position.fromDegrees(40.01, 10.00, 0);
        Position pos4 = Position.fromDegrees(40.00, 10.01, 0);

        ProjectedPosition vec1 = mission.transformFromGlobe(pos1, factory);
        ProjectedPosition vec2 = mission.transformFromGlobe(pos2, factory);
        ProjectedPosition vec3 = mission.transformFromGlobe(pos3, factory);
        ProjectedPosition vec4 = mission.transformFromGlobe(pos4, factory);

        Vec2 vec1_goal = goal.transformFromMission(new Vec2(vec1.x, vec1.y));
        Vec2 vec2_goal = goal.transformFromMission(new Vec2(vec2.x, vec2.y));
        Vec2 vec3_goal = goal.transformFromMission(new Vec2(vec3.x, vec3.y));
        Vec2 vec4_goal = goal.transformFromMission(new Vec2(vec4.x, vec4.y));

        geom.getVertices().add(vec1_goal);
        geom.getVertices().add(vec2_goal);
        geom.getVertices().add(vec3_goal);
        geom.getVertices().add(vec4_goal);

        mission.updateOrigin(factory);

        // plus one more vertex
        Position pos5 = Position.fromDegrees(40.00, 10.02, 0);
        ProjectedPosition vec5 = mission.transformFromGlobe(pos5, factory);
        Vec2 vec5_goal = goal.transformFromMission(new Vec2(vec5.x, vec5.y));
        geom.getVertices().add(vec5_goal);

        mission.updateOrigin(factory);

        Vec2 toMission1 = goal.transformToMission(geom.getVertices().get(0));
        Position toGlobe1 = mission.transformToGlobe(toMission1, factory);

        Vec2 toMission2 = goal.transformToMission(geom.getVertices().get(1));
        Position toGlobe2 = mission.transformToGlobe(toMission2, factory);

        Vec2 toMission3 = goal.transformToMission(geom.getVertices().get(2));
        Position toGlobe3 = mission.transformToGlobe(toMission3, factory);

        Vec2 toMission4 = goal.transformToMission(geom.getVertices().get(3));
        Position toGlobe4 = mission.transformToGlobe(toMission4, factory);

        Assertions.assertTrue(toGlobe1.getLatitude() - pos1.getLatitude() < 1e-8);
        Assertions.assertTrue(toGlobe1.getLongitude() - pos1.getLongitude() < 1e-8);

        Assertions.assertTrue(toGlobe2.getLatitude() - pos2.getLatitude() < 1e-8);
        Assertions.assertTrue(toGlobe2.getLongitude() - pos2.getLongitude() < 1e-8);

        Assertions.assertTrue(toGlobe3.getLatitude() - pos3.getLatitude() < 1e-8);
        Assertions.assertTrue(toGlobe3.getLongitude() - pos3.getLongitude() < 1e-8);

        Assertions.assertTrue(toGlobe4.getLatitude() - pos4.getLatitude() < 1e-8);
        Assertions.assertTrue(toGlobe4.getLongitude() - pos4.getLongitude() < 1e-8);
    }

}
