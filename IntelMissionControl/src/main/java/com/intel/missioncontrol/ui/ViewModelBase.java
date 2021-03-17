/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.Scope;
import de.saxsys.mvvmfx.ViewModel;

/** This is the base class for all view models that are constructed using view model injection. */
public class ViewModelBase<TPayload> implements ViewModel {

    public static class Accessor {
        public static Scope newInitializerScope(Object payload) {
            return new InitializerScope(payload);
        }
    }

    /** Don't use this anywhere, it's an implementation detail. Don't make it public. */
    static class InitializerScope implements Scope {
        private final Object payload;

        InitializerScope() {
            this(null);
        }

        InitializerScope(Object payload) {
            this.payload = payload;
        }
    }

    @InjectScope
    private InitializerScope initializerScope;

    private boolean superInitializeCalled;
    private boolean superPayloadInitializeCalled;

    protected ViewModelBase() {}

    protected void initializeViewModel() {
        superInitializeCalled = true;
    }

    protected void initializeViewModel(TPayload payload) {
        superPayloadInitializeCalled = true;
    }

    @SuppressWarnings("unchecked")
    public final void initialize() {
        if (initializerScope.payload != null) {
            try {
                initializeViewModel((TPayload)initializerScope.payload);
            } catch (ClassCastException e) {
                throw new RuntimeException(
                    "The supplied payload type ["
                        + initializerScope.payload.getClass()
                        + "] does not match the type that is requested by initializeViewModel.",
                    e);
            }
        }

        initializeViewModel();

        if (!superInitializeCalled) {
            throw new IllegalStateException("Did you forget to call super.initializeViewModel()?");
        }

        if (initializerScope.payload != null && !superPayloadInitializeCalled) {
            throw new IllegalStateException("Did you forget to call super.initializeViewModel(payload)?");
        }
    }

}
