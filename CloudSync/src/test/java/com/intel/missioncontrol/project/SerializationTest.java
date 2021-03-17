/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import static com.intel.missioncontrol.PropertyHelper.asMutable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intel.missioncontrol.TestBase;
import com.intel.missioncontrol.project.serialization.JsonSerializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import org.asyncfx.AsyncFX;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SerializationTest extends TestBase {

    @BeforeAll
    static void setup() {
        AsyncFX.setVerifyPropertyAccess(false);
    }

    @AfterAll
    static void teardown() {
        AsyncFX.setVerifyPropertyAccess(true);
    }

    @Test
    void Project_Is_Equal_When_Serialized_And_Deserialized() {
        OffsetDateTime date = OffsetDateTime.parse("2019-01-01T00:00:00Z");

        Project project = new Project();
        project.nameProperty().set("Project0");
        asMutable(project.creationDateProperty()).set(date);
        asMutable(project.lastModifiedDateProperty()).set(date);

        Mission mission = new Mission();
        mission.nameProperty().set("Mission0");
        project.missionsProperty().add(mission);
        mission.getGoals().add(new MeshGoal());

        mission = new Mission();
        mission.nameProperty().set("Mission1");
        project.missionsProperty().add(mission);

        String document = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            JsonSerializer serializer = new JsonSerializer(true);
            serializer.serialize(project, stream);
            document = stream.toString();
        } catch (IOException ignored) {
        }

        Project deserializedProject = null;
        try (ByteArrayInputStream stream = new ByteArrayInputStream(document.getBytes())) {
            JsonSerializer serializer = new JsonSerializer();
            deserializedProject = serializer.deserialize(stream, Project.class);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        assertEquals(project, deserializedProject);
    }

}
