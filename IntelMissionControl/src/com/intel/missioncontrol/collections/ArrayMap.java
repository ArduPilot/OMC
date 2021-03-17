/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class ArrayMap<K, V> extends AbstractMap<K, V> {

    private static class MapList<K, V> extends ArrayList<Entry<K, V>> implements Set<Entry<K, V>> {
        private boolean flag;

        @Override
        public boolean add(Entry<K, V> kvEntry) {
            if (!flag) {
                throw new UnsupportedOperationException();
            }

            return super.add(kvEntry);
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            throw new UnsupportedOperationException();
        }
    }

    private final MapList<K, V> list = new MapList<>();

    @Override
    public Set<Entry<K, V>> entrySet() {
        return list;
    }

    @Override
    public V put(K key, V value) {
        for (var item : list) {
            if (item.getKey().equals(key)) {
                item.setValue(value);
                return value;
            }
        }

        list.flag = true;
        list.add(new SimpleEntry<>(key, value));
        list.flag = false;
        return value;
    }

}
