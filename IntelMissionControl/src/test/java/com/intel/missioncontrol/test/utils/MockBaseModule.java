/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.test.utils;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.intel.missioncontrol.ApplicationContext;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.IInterProcessHandler;
import com.intel.missioncontrol.InterProcessHandler;
import com.intel.missioncontrol.LocalScope;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.common.PathProvider;
import com.intel.missioncontrol.common.PostConstructionListener;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.logging.ProvisioningLogger;
import com.intel.missioncontrol.logging.ReseatableFileAppender;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.SelectionManager;
import com.intel.missioncontrol.project.IProjectManager;
import com.intel.missioncontrol.project.ProjectManager;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.MockDialogService;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavigationService;
import com.intel.missioncontrol.utils.IVersionProvider;
import com.intel.missioncontrol.utils.VersionProvider;

public class MockBaseModule extends AbstractModule {

    @Override
    protected void configure() {
        VersionProvider versionProvider = new VersionProvider();
        PathProvider pathProvider = new PathProvider();
        ReseatableFileAppender.setOutDir(pathProvider.getLogDirectory());
        bindListener(Matchers.any(), new ProvisioningLogger());
        bindListener(Matchers.any(), PostConstructionListener.getInstance());

        // Singleton scope
        bind(IVersionProvider.class).toInstance(versionProvider);
        bind(IPathProvider.class).toInstance(pathProvider);
        bind(IProjectManager.class).to(ProjectManager.class);
        bind(IApplicationContext.class).to(ApplicationContext.class).in(Singleton.class);
        bind(IDialogService.class).to(MockDialogService.class).in(Singleton.class);
        // bind(IDialogContextProvider.class).to(DialogContextProvider.class).in(Singleton.class);
        bind(ILanguageHelper.class).to(LanguageHelper.class).in(Singleton.class);
        bind(IInterProcessHandler.class).to(InterProcessHandler.class).in(Singleton.class);

        // Local scope
        Scope localScope = LocalScope.getInstance();
        bind(INavigationService.class).to(NavigationService.class).in(localScope);
        bind(ISelectionManager.class).to(SelectionManager.class).in(localScope);
    }

}
