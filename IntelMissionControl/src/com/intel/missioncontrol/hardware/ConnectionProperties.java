/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.common.base.Objects;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

class ConnectionProperties implements IConnectionProperties {

    static class Deserializer implements JsonDeserializer<IConnectionProperties> {
        @Override
        public IConnectionProperties deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            return context.deserialize(json, ConnectionProperties.class);
        }
    }

    private boolean pinRequired;
    private String pinValidationRule;
    private String pinErrorMessage;

    public ConnectionProperties(boolean pinRequired, String pinValidationRule) {
        this.pinRequired = pinRequired;
        this.pinValidationRule = pinValidationRule;
    }

    public ConnectionProperties() {}

    public ConnectionProperties(boolean pinRequired, String pinValidationRule, String pinErrorMessage) {
        this.pinRequired = pinRequired;
        this.pinValidationRule = pinValidationRule;
        this.pinErrorMessage = pinErrorMessage;
    }

    @Override
    public boolean isPinRequired() {
        return pinRequired;
    }

    @Override
    public String getPinValidationRule() {
        return pinValidationRule;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!ConnectionProperties.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        final ConnectionProperties other = (ConnectionProperties)o;

        if (this.pinRequired != other.pinRequired || !Objects.equal(this.pinValidationRule, other.getPinValidationRule())) {
            return false;
        }

        return true;
    }
}
