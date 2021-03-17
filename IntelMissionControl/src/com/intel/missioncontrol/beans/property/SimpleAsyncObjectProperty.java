/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.diagnostics.Debugger;
import java.util.concurrent.locks.StampedLock;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class SimpleAsyncObjectProperty<T> extends AsyncObjectPropertyBase<T> {

    private final Object bean;
    private String name;

    public SimpleAsyncObjectProperty(Object bean) {
        this(bean, new PropertyMetadata.Builder<T>().create());
    }

    public SimpleAsyncObjectProperty(Object bean, PropertyMetadata<T> metadata) {
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
