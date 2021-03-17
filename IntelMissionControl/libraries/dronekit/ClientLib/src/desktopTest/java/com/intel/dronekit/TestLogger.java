package com.intel.dronekit;

import org.junit.Test;
import timber.log.Timber;

import java.io.IOException;

public class TestLogger {

    @Test
    static void test() {
        new Thread(
                () -> {
                    Timber.w("warning from lambda");
                }
        ).start();

        Timber.i("info %s %d", 23, 23);
        Timber.d("debug debug");
        Timber.e("error %s %d", "one", 34);
        Timber.w("warning");
        BarObj obj = new BarObj();
        obj.foo();

        runner(() -> {
            Timber.w("runner");
            Timber.e( new IOException(),"object %d", 34);
        });

    }

    static void runner(Runnable runnable) {
        runnable.run();
    }

    static class BarObj {

        void foo() {
            Timber.i("info %s %d", 23, 23);
            runner(() -> {
                Timber.i("inner lambda runner");
            });

        }

    }
}
