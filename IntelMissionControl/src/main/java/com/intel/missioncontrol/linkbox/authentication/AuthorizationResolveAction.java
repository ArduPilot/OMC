/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox.authentication;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.validation.IResolveAction;

public class AuthorizationResolveAction implements IResolveAction {

    private final ILanguageHelper languageHelper;
    private final Runnable resolveAction;

    public AuthorizationResolveAction(ILanguageHelper languageHelper, Runnable resolveAction) {
        this.languageHelper = languageHelper;
        this.resolveAction = resolveAction;
    }

    @Override
    public String getMessage() {
        return languageHelper.getString(AuthorizationResolveAction.class, "resolveAction");
    }

    @Override
    public boolean canResolve() {
        return resolveAction != null;
    }

    @Override
    public void resolve() {
        if (resolveAction != null) {
            resolveAction.run();
        }
    }

}
