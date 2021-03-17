/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.test.rules;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.intel.missioncontrol.TestPathProvider;
import com.intel.missioncontrol.api.FlightPlanTemplateService;
import com.intel.missioncontrol.api.IFlightPlanTemplateService;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.hardware.HardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.LanguageHelper;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.guice.internal.MvvmfxModule;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class GuiceDependenciesInitializer implements MethodRule {

    private AbstractModule additionalModule;

    private Injector injector;

    public GuiceDependenciesInitializer() {
    }

    public GuiceDependenciesInitializer(AbstractModule additionalModule) {
        this.additionalModule = additionalModule;
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod method, Object target) {
       return new Statement() {
           @Override
           public void evaluate() throws Throwable {
               AbstractModule testDependencies = new AbstractModule() {
                   @Override
                   protected void configure() {
                       bind(IPathProvider.class).to(TestPathProvider.class);
                       bind(ILanguageHelper.class).to(LanguageHelper.class);
                       bind(IFlightPlanTemplateService.class).to(FlightPlanTemplateService.class);
                       bind(IHardwareConfigurationManager.class).to(HardwareConfigurationManager.class);
                   }
               };
                ImmutableList.Builder<Module> moduleListBuilder = ImmutableList.<Module>builder()
                    .add(testDependencies, new MvvmfxModule());
               if (additionalModule != null) {
                   moduleListBuilder.add(Modules.override(additionalModule).with(testDependencies));
               }
               injector = Guice.createInjector(moduleListBuilder.build());
               MvvmFX.setCustomDependencyInjector(injector::getInstance);

                // Inject Guice injections support nested class (@see HierarchicalContextRunner)
                recursivelyInjectOuterClassInstances(target);

               statement.evaluate();
           }
       };
    }

    private void recursivelyInjectOuterClassInstances(Object target) {
        collectOuterReferences(target).forEach(injector::injectMembers);
    }

    private Set<Object> collectOuterReferences(Object target) {
        Set<Object> collector = new HashSet<>(Collections.singleton(target));
        Arrays.stream(target.getClass().getDeclaredFields())
            .filter(this::filterOuterRef)
            .findFirst()
            .ifPresent(
                f -> collector.addAll(collectOuterReferences(fieldValue(f, target)))
            );
        return collector;
    }

    private boolean filterOuterRef(Field f) {
        return f.getName().startsWith("this$");
    }

    private Object fieldValue(Field f, Object target) {
        f.setAccessible(true);
        try {
            return f.get(target);
        } catch (IllegalAccessException e) {
            return new Object();
        }
    }

    public Injector getInjector() {
        return injector;
}
}
