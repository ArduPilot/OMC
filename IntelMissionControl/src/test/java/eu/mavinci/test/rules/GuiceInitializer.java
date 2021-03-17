/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.test.rules;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.intel.missioncontrol.FileExtractor;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.IApplicationContextTestDummy;
import com.intel.missioncontrol.TestPathProvider;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.hardware.DescriptionProvider;
import com.intel.missioncontrol.hardware.HardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IDescriptionProvider;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.test.utils.MockingHelper;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProviderTestDummy;
import com.intel.missioncontrol.utils.VersionProvider;
import com.intel.missioncontrol.utils.IVersionProvider;
import java.util.HashSet;
import java.util.Set;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class GuiceInitializer implements TestRule {
    final Set<Module> modules = new HashSet<>();

    public GuiceInitializer() {}

    /** @param modules - additional modules that might be needed for the Injector initialisation */
    public GuiceInitializer(Set<Module> modules) {
        this.modules.addAll(modules);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                IPathProvider pathProvider = new TestPathProvider();


                modules.add(
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(IPathProvider.class).toInstance(pathProvider);
                            bind(IDescriptionProvider.class).to(DescriptionProvider.class).asEagerSingleton();
                            bind(IHardwareConfigurationManager.class)
                                .to(HardwareConfigurationManager.class)
                                .asEagerSingleton();
                            //bind(IDialogService.class).to(DialogService.class).asEagerSingleton();
                            bind(ILanguageHelper.class).to(LanguageHelper.class).asEagerSingleton();
                            bind(IVersionProvider.class).to(VersionProvider.class).asEagerSingleton();
                            bind(IDialogContextProvider.class).to(IDialogContextProviderTestDummy.class);
                            // this settings manager is better, it at least has non null sections inside
                            ISettingsManager settingManagerMock = MockingHelper.createSettingManagerMock();
                            bind(ISettingsManager.class).toInstance(settingManagerMock);
                            bind(IApplicationContext.class).to(IApplicationContextTestDummy.class);
                        }
                    });
                Injector injector = Guice.createInjector(modules);

                base.evaluate();

                // TODO we have to add a cleanup of the temp folder after the test
            }
        };
    }

}
