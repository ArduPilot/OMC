/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.geom.Mesh;
import com.intel.missioncontrol.project.property.Identifiable;
import com.intel.missioncontrol.project.serialization.DeserializationContext;
import com.intel.missioncontrol.project.serialization.Serializable;
import com.intel.missioncontrol.project.serialization.SerializationContext;
import java.util.UUID;

public class MeshReference implements Serializable, Identifiable {

    private final UUID id;
    private final Mesh mesh;

    public MeshReference(Mesh mesh) {
        this.id = UUID.randomUUID();
        this.mesh = mesh;
    }

    public MeshReference(MeshReference meshReference) {
        this.id = meshReference.id;
        this.mesh = meshReference.mesh;
    }

    public MeshReference(DeserializationContext context) {
        this.id = UUID.fromString(context.readString("id"));
        this.mesh = null;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public void getObjectData(SerializationContext context) {
        context.writeString("id", id.toString());
    }

}
