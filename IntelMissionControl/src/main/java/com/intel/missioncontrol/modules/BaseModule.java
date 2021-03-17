/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.intel.missioncontrol.ApplicationContext;
import com.intel.missioncontrol.FileExtractor;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.IFileExtractor;
import com.intel.missioncontrol.IInterProcessHandler;
import com.intel.missioncontrol.InterProcessHandler;
import com.intel.missioncontrol.LocalScope;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.common.PostConstructionListener;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.logging.ProvisioningLogger;
import com.intel.missioncontrol.logging.ReseatableFileAppender;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.SelectionManager;
import com.intel.missioncontrol.project.IProjectManager;
import com.intel.missioncontrol.project.ProjectManager;
import com.intel.missioncontrol.ui.dialogs.DialogContextProvider;
import com.intel.missioncontrol.ui.dialogs.DialogService;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavigationService;
import com.intel.missioncontrol.utils.IVersionProvider;
import com.intel.missioncontrol.utils.VersionProvider;

public class BaseModule extends AbstractModule {

    private final IPathProvider pathProvider;

    public BaseModule(IPathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }

    @Override
    protected void configure() {
        VersionProvider versionProvider = new VersionProvider();
        ReseatableFileAppender.setOutDir(pathProvider.getLogDirectory());
        bindListener(Matchers.any(), new ProvisioningLogger());
        bindListener(Matchers.any(), PostConstructionListener.getInstance());

        // Singleton scope
        bind(IVersionProvider.class).toInstance(versionProvider);
        bind(IPathProvider.class).toInstance(pathProvider);
        bind(IFileExtractor.class).to(FileExtractor.class).in(Singleton.class);
        bind(IProjectManager.class).to(ProjectManager.class).in(Singleton.class);
        bind(ApplicationContext.class).in(Singleton.class);
        bind(IApplicationContext.class).to(ApplicationContext.class);
        bind(IDialogService.class).to(DialogService.class).in(Singleton.class);
        bind(IDialogContextProvider.class).to(DialogContextProvider.class).in(Singleton.class);
        bind(ILanguageHelper.class).to(LanguageHelper.class).in(Singleton.class);
        bind(IInterProcessHandler.class).to(InterProcessHandler.class).in(Singleton.class);

        // Local scope
        Scope localScope = LocalScope.getInstance();
        bind(INavigationService.class).to(NavigationService.class).in(localScope);
        bind(ISelectionManager.class).to(SelectionManager.class).in(localScope);
    }

}
