/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx;

import java.util.concurrent.TimeUnit;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
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

@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class SimpleIntegerPropertyBenchmark {

    @State(Scope.Thread)
    public static class MyState {
        final IntegerProperty source = new SimpleIntegerProperty();
        final IntegerProperty target = new SimpleIntegerProperty();
        Blackhole blackhole;

        public MyState() {
            target.bind(source);
            target.addListener(this::changed);
        }

        @Setup
        public void setup(Blackhole blackhole) {
            this.blackhole = blackhole;
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
