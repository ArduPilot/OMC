/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.logging;

import eu.mavinci.core.obfuscation.IKeepAll;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

@Plugin(name = "ReseatableFile", category = "Core", elementType = "appender", printObject = true)
@SuppressWarnings("unused")
public class ReseatableFileAppender extends AbstractAppender implements IKeepAll {

    public static class Builder<B extends ReseatableFileAppender.Builder<B>>
            extends org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<ReseatableFileAppender> {
        public ReseatableFileAppender build() {
            Layout<? extends Serializable> layout = this.getOrCreateLayout(Charset.defaultCharset());
            ReseatableFileAppender appender =
                new ReseatableFileAppender(this.getName(), this.getFilter(), layout, outDir);
            appenders.add(new WeakReference<>(appender));
            return appender;
        }
    }

    @PluginBuilderFactory
    @SuppressWarnings("unchecked")
    public static <B extends ReseatableFileAppender.Builder<B>> B newBuilder() {
        return (B)new ReseatableFileAppender.Builder().asBuilder();
    }

    private static Path outDir;
    private static List<WeakReference<ReseatableFileAppender>> appenders = new ArrayList<>();

    private Appender nestedAppender;

    public static synchronized void setOutDir(Path path) {
        outDir = path;

        ListIterator<WeakReference<ReseatableFileAppender>> it = appenders.listIterator();
        while (it.hasNext()) {
            WeakReference<ReseatableFileAppender> ref = it.next();
            ReseatableFileAppender appender = ref.get();
            if (appender == null) {
                it.remove();
            } else {
                appender.updatePath(path);
            }
        }
    }

    private ReseatableFileAppender(
            String name, Filter filter, Layout<? extends Serializable> layout, @Nullable Path outDir) {
        super(name, filter, layout);

        if (outDir != null) {
            updatePath(outDir);
        }
    }

    @Override
    public void append(LogEvent logEvent) {
        Appender appender = this.nestedAppender;
        if (appender != null) {
            appender.append(logEvent);
        }
    }

    private void updatePath(Path path) {
        nestedAppender =
            RollingFileAppender.newBuilder()
                .withName(getName())
                .withPolicy(SizeBasedTriggeringPolicy.createPolicy("2m"))
                .withFileName(path.resolve("application.log").toString())
                .withFilePattern(path.resolve("application.%i.log").toString())
                .withLayout(getLayout())
                .build();
    }

}
