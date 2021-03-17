/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.intel.missioncontrol.FileExtractor;
import com.intel.missioncontrol.IFileExtractor;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.common.PathProvider;
import com.intel.missioncontrol.drone.connection.ConnectionListenerService;
import com.intel.missioncontrol.drone.connection.DroneConnectionService;
import com.intel.missioncontrol.drone.connection.IConnectionListenerService;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.hardware.DescriptionProvider;
import com.intel.missioncontrol.hardware.HardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IDescriptionProvider;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.modules.SettingsModule;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.RootView;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.DialogContextProvider;
import com.intel.missioncontrol.ui.dialogs.DialogService;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.imageio.ImageLoaderInstaller;
import com.intel.missioncontrol.utils.IVersionProvider;
import com.intel.missioncontrol.utils.VersionProvider;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.guice.internal.MvvmfxModule;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.LicenceManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LiveviewStarter extends Application {

    private Injector injector;

    public static void main(String...args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {


        final Set<Module> modules = new HashSet<>();
        modules.add(new SettingsModule());
        modules.add(
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(IVersionProvider.class).toInstance(new VersionProvider());
                    bind(ILicenceManager.class).to(LicenceManager.class).in(Singleton.class);
                    bind(IPathProvider.class).toInstance(new PathProvider());
                    bind(IDialogService.class).to(DialogService.class).in(Singleton.class);
                    bind(IDialogContextProvider.class).to(DialogContextProvider.class).in(Singleton.class);
                    bind(ILanguageHelper.class).to(LanguageHelper.class).in(Singleton.class);
                    bind(IHardwareConfigurationManager.class).to(HardwareConfigurationManager.class).in(Singleton.class);
                    bind(IDescriptionProvider.class).to(DescriptionProvider.class);
                    bind(IFileExtractor.class).to(FileExtractor.class).in(Singleton.class);
                    bind(ILiveVideoService.class).to(LiveVideoService.class).in(Singleton.class);
                    bind(ILiveVideoWidgetService.class).to(LiveVideoWidgetService.class).in(Singleton.class);
                    bind(IConnectionListenerService.class).to(ConnectionListenerService.class).in(Singleton.class);
                    bind(IDroneConnectionService.class).to(DroneConnectionService.class).in(Singleton.class);
                    install(new FactoryModuleBuilder().build(WorkingHorse.Factory.class));
                }
            });

        modules.add(new MvvmfxModule());

        injector = Guice.createInjector(modules);

        ResourceBundle bundle = ResourceBundle.getBundle("com/intel/missioncontrol/IntelMissionControl");
        MvvmFX.setGlobalResourceBundle(bundle);


        Map<Class<? extends ViewModel>, Class<? extends RootView<? extends ViewModelBase>>> map = new HashMap<>();
        map.put(LiveVideoDialogViewModel.class, LiveVideoDialogView.class);
        ((DialogService)injector.getInstance(IDialogService.class)).setDialogMap(map);

        MvvmFX.setCustomDependencyInjector(key -> injector.getInstance(key));

        ImageLoaderInstaller.installThreadContextClassLoader();
        ISettingsManager settingsManager = injector.getInstance(ISettingsManager.class);
        ImageLoaderInstaller.install(settingsManager);

        stage.setTitle("Liveview");

        ViewTuple<LiveviewView, LiveviewViewModel> viewTuple = FluentViewLoader.fxmlView(LiveviewView.class).providedScopes(ViewModelBase.Accessor.newInitializerScope(null)).load();

        Parent root = viewTuple.getView();

        stage.setScene(new Scene(root));
        stage.setOnCloseRequest( windowEvent -> {
            injector.getInstance(ILiveVideoService.class).shutdown();
        });

        stage.show();
    }
}
