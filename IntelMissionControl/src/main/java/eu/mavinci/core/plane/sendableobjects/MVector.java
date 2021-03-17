/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.helper.IProperties;
import eu.mavinci.core.helper.IPropertiesStoreable;
import eu.mavinci.core.obfuscation.IKeepMembers;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.core.helper.IProperties;
import eu.mavinci.core.helper.IPropertiesStoreable;
import eu.mavinci.core.obfuscation.IKeepMembers;

import java.util.Vector;
import java.util.logging.Level;

public class MVector<T> extends Vector<T> implements IPropertiesStoreable, IKeepMembers {
    private static final long serialVersionUID = 3273323533352620157L;

    Class<T> cls;

    public MVector(Class<T> cls) {
        super();
        this.cls = cls;
    }

    public MVector(int size, Class<T> cls) {
        super(size);
        this.cls = cls;
    }

    public Class<T> getClassOfMembers() {
        return cls;
    }

    @SuppressWarnings("unchecked")
    public MVector<T> clone() {
        MVector<T> clone = new MVector<T>(this.size(), cls);
        for (T elem : this) {
            if (elem instanceof MObject) {
                MObject clonable = (MObject)elem;
                try {
                    clone.add((T)clonable.clone());
                } catch (CloneNotSupportedException e) {
                    Debug.getLog().log(Level.FINE, "problem with cloning objects, this should never happen", e);
                }
            } else if (elem instanceof MVector<?>) {
                MVector<?> other = (MVector<?>)elem;
                clone.add((T)other.clone());
            } else {
                clone.add(elem);
            }
        }

        return clone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized boolean equals(Object o) {
        return super.equals(o) && (o instanceof MVector<?>) && cls.equals(((MVector<T>)o).cls);
    }

    @SuppressWarnings("unchecked")
    public void loadState(IProperties prop, String keyPrefix) {
        int i = 0;
        clear();
        while (true) {
            String singPref = keyPrefix + "." + i;
            if (!prop.containsKeyPrefix(singPref)) {
                return;
            }

            T n;
            try {
                n = (T)MObject.fromPropertiesSingle(singPref, prop, cls);
                if (n != null) {
                    add(n);
                }
            } catch (Exception e) {
                Debug.getLog().log(Level.FINE, "Problems loading Object state from Properties", e);
            }

            i++;
        }
    }

    public void storeState(IProperties prop, String keyPrefix) {
        String vecPref = keyPrefix + ".";
        prop.removeAll(vecPref);
        for (int i = 0; i != size(); i++) {
            MObject.toPropertiesSingle(vecPref + i, prop, get(i));
        }
    }
}
