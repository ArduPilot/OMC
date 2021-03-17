/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence;

import com.intel.missioncontrol.project.property.Identifiable;
import java.util.UUID;

public interface Query {

    class ById implements Query {
        private final UUID id;

        public ById(UUID id) {
            this.id = id;
        }

        @Override
        public boolean satisfies(Identifiable value) {
            return value.getId().equals(id);
        }
    }

    class All implements Query {
        @Override
        public boolean satisfies(Identifiable value) {
            return true;
        }
    }

    boolean satisfies(Identifiable value);

}
