/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.persistence.ResourceResolver;
import com.intel.missioncontrol.project.property.Identifiable;
import com.intel.missioncontrol.project.serialization.DeserializationContext;
import com.intel.missioncontrol.project.serialization.Serializable;
import com.intel.missioncontrol.project.serialization.SerializationContext;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;

public class ResourceReference implements Serializable, Identifiable {

    private final UUID id;
    private final String name;
    private ResourceResolver resolver;

    public ResourceReference(String name) {
        this.id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_16));
        this.name = name;
    }

    public ResourceReference(DeserializationContext context) {
        this.name = context.readString("name");
        this.id = UUID.nameUUIDFromBytes(this.name.getBytes(StandardCharsets.UTF_16));
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setResolver(ResourceResolver resolver) {
        this.resolver = resolver;
    }

    public Future<InputStream> openInputStreamAsync() {
        if (resolver == null) {
            return Futures.failed(new RuntimeException("The resource reference has not been resolved."));
        }

        return resolver.openInputStreamAsync(this);
    }

    public Future<OutputStream> openOutputStreamAsync() {
        if (resolver == null) {
            return Futures.failed(new RuntimeException("The resource reference has not been resolved."));
        }

        return resolver.openOutputStreamAsync(this);
    }

    @Override
    public void getObjectData(SerializationContext context) {
        context.writeString("name", name);
    }

}
