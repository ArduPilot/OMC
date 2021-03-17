/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.InvalidationListener;
import org.asyncfx.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FXAsyncCollectionsTest extends TestBase {

    @Test
    void Adding_Items_To_Unmodifiable_AsyncObservableList_Should_Fail() {
        AsyncObservableList<Integer> unmodifiableList =
            FXAsyncCollections.unmodifiableObservableList(new ArrayList<>());
        try {
            unmodifiableList.add(null);
            Assertions.fail();
        } catch (Exception e) {
        }

        AsyncObservableList<Integer> modifiableList = FXAsyncCollections.observableArrayList();
        unmodifiableList = FXAsyncCollections.unmodifiableObservableList(modifiableList);
        try {
            unmodifiableList.add(null);
            Assertions.fail();
        } catch (Exception e) {
        }

        modifiableList.add(1);
        Assertions.assertEquals(1, (int)unmodifiableList.get(0));
    }

    @Test
    void AsyncObservableList_InvalidationListener_Is_Called_When_Item_Is_Added() {
        AsyncObservableList<Integer> list = FXAsyncCollections.observableArrayList();
        AtomicInteger count = new AtomicInteger();
        list.addListener((InvalidationListener)listener -> count.incrementAndGet());
        list.setAll(1);
        Assertions.assertEquals(1, count.get());
    }

    @Test
    void AsyncObservableList_SubList_Clear_Only_Works_When_List_Is_Locked() {
        AsyncObservableList<Integer> list = FXAsyncCollections.observableArrayList();
        AtomicInteger count = new AtomicInteger();
        list.addListener((InvalidationListener)listener -> count.incrementAndGet());

        list.setAll(1, 2, 3, 4, 5);
        Assertions.assertEquals(1, count.get());

        try {
            list.subList(2, 4).clear();
            Assertions.fail();
        } catch (Exception e) {
        }

        try (LockedList<Integer> lockedList = list.lock()) {
            lockedList.subList(2, 4).clear();
        }

        Assertions.assertEquals(2, count.get());
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(1, list.get(0).intValue());
        Assertions.assertEquals(5, list.get(2).intValue());
    }

    @Test
    void Adding_Items_To_Unmodifiable_AsyncObservableSet_Should_Fail() {
        AsyncObservableSet<Integer> unmodifiableSet = FXAsyncCollections.unmodifiableObservableSet(new ArraySet<>());
        try {
            unmodifiableSet.add(null);
            Assertions.fail();
        } catch (Exception e) {
        }

        AsyncObservableSet<Integer> modifiableSet = FXAsyncCollections.observableSet(new ArraySet<>());
        unmodifiableSet = FXAsyncCollections.unmodifiableObservableSet(modifiableSet);
        try {
            unmodifiableSet.add(null);
            Assertions.fail();
        } catch (Exception e) {
        }

        modifiableSet.add(1);
    }

    @Test
    void AsyncObservableSet_InvalidationListener_Is_Called_When_Item_Is_Added() {
        AsyncObservableSet<Integer> list = FXAsyncCollections.observableSet(new HashSet<>());
        AtomicInteger count = new AtomicInteger();
        list.addListener((InvalidationListener)listener -> count.incrementAndGet());
        list.add(1);
        Assertions.assertEquals(1, count.get());
    }

}
