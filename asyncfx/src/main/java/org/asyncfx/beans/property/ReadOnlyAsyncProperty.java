/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.value.AsyncObservableValue;
import org.asyncfx.concurrent.Dispatcher;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface ReadOnlyAsyncProperty<T> extends AsyncObservableValue<T> {

    long getUniqueId();

    Object getBean();

    String getName();

    /** Gets the value of this property, regardless of whether it is protected by a {@link ConsistencyGroup}. */
    T getValueUncritical();

    PropertyMetadata<T> getMetadata();

    AccessController getAccessController();

    default Executor getExecutor() {
        Dispatcher dispatcher = getMetadata().getDispatcher();
        if (dispatcher != null) {
            return dispatcher;
        }

        Object bean = getBean();
        dispatcher = bean instanceof PropertyObject ? ((PropertyObject)bean).getDispatcher() : null;
        return dispatcher != null ? dispatcher : MoreExecutors.directExecutor();
    }

}
