/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.property.Hierarchical;
import java.util.Collection;
import org.asyncfx.collections.AsyncCollection;
import org.asyncfx.collections.AsyncListChangeListener;
import org.asyncfx.collections.LockedCollection;

class HierarchyHelper {

    static class Listener<T extends Hierarchical, P> implements AsyncListChangeListener<T> {
        private final P parent;

        Listener(P parent) {
            this.parent = parent;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onChanged(AsyncChange<? extends T> change) {
            while (change.next()) {
                for (T item : change.getRemoved()) {
                    setParent(item, null);
                }

                for (T item : change.getAddedSubList()) {
                    setParent(item, parent);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T> void setParent(Collection<? extends Hierarchical> collection, T parent) {
        if (collection instanceof AsyncCollection) {
            try (LockedCollection<Hierarchical<T>> lockedCollection =
                ((AsyncCollection<Hierarchical<T>>)collection).lock()) {
                for (Hierarchical<T> hierarchical : lockedCollection) {
                    setParent(hierarchical, parent);
                }
            }
        } else {
            for (Hierarchical<T> hierarchical : collection) {
                setParent(hierarchical, parent);
            }
        }
    }

    private static <T> void setParent(Hierarchical<T> hierarchical, T parent) {
        if (hierarchical instanceof ResourceReference) {
            ((ResourceReference)hierarchical).setParent((Project)parent);
        } else if (hierarchical instanceof Mission) {
            ((Mission)hierarchical).setParent((Project)parent);
        } else if (hierarchical instanceof Placeable) {
            ((Placeable)hierarchical).setParent((Mission)parent);
        }
    }

}
