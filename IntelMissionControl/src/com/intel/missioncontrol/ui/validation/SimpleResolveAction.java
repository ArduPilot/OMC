/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

public class SimpleResolveAction implements IResolveAction {

    private final String message;
    private final Runnable resolveMethod;

    // reinclude later when show concept works
    /*public SimpleResolveAction(String message) {
        this.message = message;
        this.resolveMethod = null;
    }*/

    public SimpleResolveAction(String message, Runnable resolveMethod) {
        this.message = message;
        this.resolveMethod = resolveMethod;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean canResolve() {
        return resolveMethod != null;
    }

    @Override
    public void resolve() {
        if (resolveMethod == null) {
            throw new IllegalStateException("Cannot automatically resolve this issue.");
        }

        resolveMethod.run();
    }
}
