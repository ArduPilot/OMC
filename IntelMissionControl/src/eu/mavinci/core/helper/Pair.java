/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

public class Pair<T, V> {
    public T first = null;
    public V second = null;

    public Pair() {}

    public Pair(T first, V second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof Pair) {
            Pair<?, ?> p = (Pair<?, ?>)obj;
            if (first == p.first && second == p.second) {
                return true;
            }

            if (first == null && p.first != null) {
                return false;
            }

            if (second == null && p.second != null) {
                return false;
            }

            if (first != null && second != null) {
                return (first.equals(p.first) && second.equals(p.second));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new Pair<T, V>(first, second);
    }

    @Override
    public int hashCode() {
        if (first == null) {
            if (second == null) {
                return 0;
            }

            return 31 * second.hashCode();
        } else {
            if (second == null) {
                return first.hashCode();
            }

            return first.hashCode() + 31 * second.hashCode();
        }
    }
}
