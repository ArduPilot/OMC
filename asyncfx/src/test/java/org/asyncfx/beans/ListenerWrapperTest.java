/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans;

import com.google.common.util.concurrent.MoreExecutors;
import java.time.Duration;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.value.RateControlledChangeListenerWrapper;
import org.asyncfx.concurrent.Dispatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ListenerWrapperTest extends TestBase {

    @Test
    void ListenerWrappers_Compare_Equal_To_Wrapped_Listener() {
        var listener =
            new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {}
            };

        var wrapper0 = new AsyncInvalidationListenerWrapper(listener, MoreExecutors.directExecutor());
        var warpper1 = new RateControlledInvalidationListenerWrapper(wrapper0, Duration.ofSeconds(1));

        Assertions.assertEquals(wrapper0, listener);
        Assertions.assertEquals(warpper1, listener);
        Assertions.assertEquals(warpper1, wrapper0);
    }

    @Test
    void RateControlledChangeListener_Throttles_High_Frequency_Events() {
        Awaiter awaiter = new Awaiter();
        final int iterations = 50;

        var prop = new SimpleAsyncIntegerProperty(null);
        int[] count = new int[1];
        int[] value = new int[1];

        var listener =
            new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() == iterations) {
                        awaiter.signal();
                    }

                    value[0] = newValue.intValue();
                    count[0]++;
                }
            };

        prop.addListener(new RateControlledChangeListenerWrapper<>(listener, Duration.ofMillis(250)));

        Dispatcher.background()
            .runLater(
                () -> {
                    for (int i = 0; i < iterations; ++i) {
                        prop.set(prop.get() + 1);
                        sleep(20);
                    }
                });

        awaiter.await(1);

        Assertions.assertEquals(iterations, value[0]);
        Assertions.assertTrue(count[0] > 5 && count[0] < 10, () -> Integer.toString(count[0]));
    }

}
