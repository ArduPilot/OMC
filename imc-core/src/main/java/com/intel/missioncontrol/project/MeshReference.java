/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.property.Identifiable;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;
import com.intel.missioncontrol.serialization.CompositeSerializable;
import com.intel.missioncontrol.serialization.CompositeSerializationContext;
import java.util.UUID;

public class MeshReference implements CompositeSerializable, Identifiable {

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

    public MeshReference(CompositeDeserializationContext context) {
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
    public void serialize(CompositeSerializationContext context) {
        context.writeString("id", id.toString());
    }

}
