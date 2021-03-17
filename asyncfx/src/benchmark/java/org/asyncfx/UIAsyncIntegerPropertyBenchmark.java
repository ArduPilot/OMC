/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.testfx.api.FxToolkit;

@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class UIAsyncIntegerPropertyBenchmark {

    @State(Scope.Thread)
    public static class MyState {
        final AsyncIntegerProperty source = new SimpleAsyncIntegerProperty(this);
        final AsyncIntegerProperty target = new UIAsyncIntegerProperty(this);
        Blackhole blackhole;

        public MyState() {
            target.bind(source);
            target.addListener(this::changed);
        }

        @Setup
        public void setup(Blackhole blackhole) throws TimeoutException {
            this.blackhole = blackhole;
            FxToolkit.registerPrimaryStage();
        }

        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            blackhole.consume(newValue);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void testMethod(MyState state) {
        state.source.set(state.source.get() + 1);
    }

}
