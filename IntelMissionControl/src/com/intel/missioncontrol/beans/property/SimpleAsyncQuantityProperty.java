/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.diagnostics.Debugger;
import com.intel.missioncontrol.measure.Quantity;
import java.util.concurrent.locks.StampedLock;

public class SimpleAsyncQuantityProperty<Q extends Quantity<Q>> extends AsyncQuantityProperty<Q> {

    private final Object bean;
    private String name;

    public SimpleAsyncQuantityProperty(Object bean) {
        this(bean, new QuantityPropertyMetadata.Builder<Q>().create());
    }

    public SimpleAsyncQuantityProperty(Object bean, QuantityPropertyMetadata<Q> metadata) {
        super(metadata);
        this.bean = bean;
        PropertyHelper.checkProperty(bean, this, metadata);
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        if (Debugger.isAttached()) {
            if (name == null) {
                name = PropertyHelper.getPropertyName(bean, this, getMetadataUnsynchronized());
            }

            return name;
        } else {
            StampedLock lock = getValueLock();
            long stamp = 0;
            try {
                String name = null;
                if ((stamp = lock.tryOptimisticRead()) != 0) {
                    name = this.name;
                }

                if (stamp != 0 && !lock.validate(stamp)) {
                    stamp = lock.readLock();
                    name = this.name;
                }

                if (name == null) {
                    if (StampedLock.isReadLockStamp(stamp)) {
                        lock.unlockRead(stamp);
                    }

                    stamp = lock.writeLock();

                    if (this.name == null) {
                        this.name = name = PropertyHelper.getPropertyName(bean, this, getMetadataUnsynchronized());
                    }
                }

                return name;
            } finally {
                if (StampedLock.isReadLockStamp(stamp)) {
                    lock.unlockRead(stamp);
                } else if (StampedLock.isWriteLockStamp(stamp)) {
                    lock.unlockWrite(stamp);
                }
            }
        }
    }

}
