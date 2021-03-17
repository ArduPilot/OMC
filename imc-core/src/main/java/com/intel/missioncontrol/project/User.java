/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class User extends AbstractUser {

    public User() {
        super();
    }

    public User(User source) {
        super(source);
    }

    public User(UserSnapshot source) {
        super(source);
    }

    public User(CompositeDeserializationContext context) {
        super(context);
    }

}
