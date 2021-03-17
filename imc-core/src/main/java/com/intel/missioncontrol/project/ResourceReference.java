/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.persistence.ResourceResolver;
import com.intel.missioncontrol.project.property.Hierarchical;
import com.intel.missioncontrol.project.property.Identifiable;
import com.intel.missioncontrol.serialization.BinaryDeserializationContext;
import com.intel.missioncontrol.serialization.BinarySerializable;
import com.intel.missioncontrol.serialization.BinarySerializationContext;
import com.intel.missioncontrol.serialization.PrimitiveDeserializationContext;
import com.intel.missioncontrol.serialization.PrimitiveSerializable;
import com.intel.missioncontrol.serialization.PrimitiveSerializationContext;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;

public class ResourceReference
        implements PrimitiveSerializable, BinarySerializable, Identifiable, Hierarchical<Project> {

    private final UUID id;
    private final String name;
    private Project parent;

    public ResourceReference(String name) {
        this.id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_16));
        this.name = name;
    }

    public ResourceReference(PrimitiveDeserializationContext context) {
        this.name = context.read();
        this.id = UUID.nameUUIDFromBytes(this.name.getBytes(StandardCharsets.UTF_16));
    }

    public ResourceReference(BinaryDeserializationContext context) {
        this.name = context.readString();
        this.id = UUID.nameUUIDFromBytes(this.name.getBytes(StandardCharsets.UTF_16));
    }

    @Override
    public Project getParent() {
        return parent;
    }

    void setParent(Project parent) {
        this.parent = parent;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Future<InputStream> openInputStreamAsync() {
        ResourceResolver resolver = parent != null ? parent.getRepository().getResourceResolver() : null;
        if (resolver == null) {
            return Futures.failed(new RuntimeException("The resource reference has not been resolved."));
        }

        return resolver.openInputStreamAsync(this);
    }

    public Future<OutputStream> openOutputStreamAsync() {
        ResourceResolver resolver = parent != null ? parent.getRepository().getResourceResolver() : null;
        if (resolver == null) {
            return Futures.failed(new RuntimeException("The resource reference has not been resolved."));
        }

        return resolver.openOutputStreamAsync(this);
    }

    @Override
    public void serialize(PrimitiveSerializationContext context) {
        context.write(name);
    }

    @Override
    public void serialize(BinarySerializationContext context) {
        context.writeString(name);
    }

}
