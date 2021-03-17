/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.SettingsManager;
import java.nio.file.Path;
import java.util.Locale;

public class SettingsModule extends AbstractModule {

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        Provider<ISettingsManager> settingsManagerProvider = getProvider(ISettingsManager.class);

        for (Class<?> settingsClass : SettingsManager.getSettingsClasses()) {
            bind((Class<ISettings>)settingsClass)
                .toProvider(() -> (ISettings)settingsManagerProvider.get().getSection(settingsClass))
                .in(Singleton.class);
        }

        bind(IQuantityStyleProvider.class).to(GeneralSettings.class);
    }

    @Provides
    @Singleton
    ISettingsManager provideSettingsManager(Injector injector, IPathProvider pathProvider) {
        return new SettingsManager(injector, pathProvider.getSettingsFile(), new Class[] {Locale.class, Path.class});
    }

}
