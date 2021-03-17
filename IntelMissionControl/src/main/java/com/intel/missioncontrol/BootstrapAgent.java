/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.concurrent.CallerThread;
import com.intel.missioncontrol.concurrent.MethodAccess;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import javafx.application.Platform;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapAgent.class);
    private static final String VIEW_MODEL_TYPE = "de.saxsys.mvvmfx.ViewModel";
    private static final String VIEW_TYPE = "de.saxsys.mvvmfx.internal.viewloader.View";

    public static class FxInterceptor {
        @SuppressWarnings("unused")
        @RuntimeType
        public static Object intercept(@SuperCall Callable<?> superCall, @Origin Method method) throws Exception {
            boolean isFxAppThread = Platform.isFxApplicationThread();
            MethodAccess methodAccess = method.getAnnotation(MethodAccess.class);
            boolean illegalCall = false;
            CallerThread expectedThread = CallerThread.UI;
            if (methodAccess != null) {
                CallerThread callerThread = methodAccess.value();
                if (callerThread == CallerThread.ANY
                        || (callerThread == CallerThread.UI && isFxAppThread)
                        || (callerThread == CallerThread.BACKGROUND && !isFxAppThread)) {
                    return superCall.call();
                }

                illegalCall = true;
                expectedThread = callerThread;
            }

            if (illegalCall || !isFxAppThread) {
                throw new IllegalStateException(
                    "Illegal cross-thread call to "
                        + getMethodName(method)
                        + ": expected = "
                        + expectedThread.toString()
                        + "; currentThread = "
                        + Thread.currentThread().getName());
            }

            return superCall.call();
        }

        private static String getMethodName(Method method) {
            String name = method.toString();
            int start = name.indexOf('(');
            int end = start;
            while (name.charAt(start) != ' ' && start > 0) {
                --start;
            }

            return name.substring(name.charAt(start) == ' ' ? start + 1 : start, end) + "()";
        }
    }

    public static void install() {
        LOGGER.info("Installing " + BootstrapAgent.class.getName());
        File temp;
        try {
            temp = Files.createTempDirectory("imc-bootstrap-agent").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Instrumentation instrumentation = ByteBuddyAgent.install();
        new AgentBuilder.Default()
            .ignore(ElementMatchers.nameStartsWith("java."))
            .ignore(ElementMatchers.nameStartsWith("javax."))
            .ignore(ElementMatchers.nameStartsWith("javafx."))
            .enableBootstrapInjection(instrumentation, temp)
            .type(ElementMatchers.hasSuperType(ElementMatchers.named(VIEW_MODEL_TYPE)))
            .or(ElementMatchers.hasSuperType(ElementMatchers.named(VIEW_TYPE)))
            .transform(
                (builder, typeDescription, classLoader, javaModule) ->
                    builder.method(ElementMatchers.any()).intercept(MethodDelegation.to(FxInterceptor.class)))
            .installOn(instrumentation);
    }

}
