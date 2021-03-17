/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.serialization.DeserializationContext;

public class User extends AbstractUser {

    public User() {
        super();
    }

    public User(IUser source) {
        super(source);
    }

    public User(DeserializationContext context) {
        super(context);
    }

}
