/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence;

import com.intel.missioncontrol.project.property.Identifiable;
import java.util.UUID;

public interface Query<T> {

    class ById<T extends Identifiable> implements Query<T> {
        private final UUID id;

        public ById(UUID id) {
            this.id = id;
        }

        @Override
        public boolean satisfies(T value) {
            return value.getId().equals(id);
        }
    }

    boolean satisfies(T value);

}
