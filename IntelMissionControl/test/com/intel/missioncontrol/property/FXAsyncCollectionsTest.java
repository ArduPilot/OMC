/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.property;

import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.InvalidationListener;
import org.junit.Assert;
import org.junit.Test;

public class FXAsyncCollectionsTest {

    @Test
    public void Adding_Items_To_Unmodifiable_AsyncObservableList_Should_Fail() {
        AsyncObservableList<Integer> unmodifiableList =
            FXAsyncCollections.unmodifiableObservableList(new ArrayList<>());
        try {
            unmodifiableList.add(null);
            Assert.fail();
        } catch (Exception e) {
        }

        AsyncObservableList<Integer> modifiableList = FXAsyncCollections.observableArrayList();
        unmodifiableList = FXAsyncCollections.unmodifiableObservableList(modifiableList);
        try {
            unmodifiableList.add(null);
            Assert.fail();
        } catch (Exception e) {
        }

        modifiableList.add(1);
        Assert.assertEquals(1, (int)unmodifiableList.get(0));
    }

    @Test
    public void AsyncObservableList_InvalidationListener_Is_Called_When_Item_Is_Added() {
        AsyncObservableList<Integer> list = FXAsyncCollections.observableArrayList();
        AtomicInteger count = new AtomicInteger();
        list.addListener((InvalidationListener)listener -> count.incrementAndGet());
        list.setAll(1);
        Assert.assertEquals(1, count.get());
    }

    @Test
    public void AsyncObservableList_SubList_Clear_Only_Works_When_List_Is_Locked() {
        AsyncObservableList<Integer> list = FXAsyncCollections.observableArrayList();
        AtomicInteger count = new AtomicInteger();
        list.addListener((InvalidationListener)listener -> count.incrementAndGet());

        list.setAll(1, 2, 3, 4, 5);
        Assert.assertEquals(1, count.get());

        try {
            list.subList(2, 4).clear();
            Assert.fail();
        } catch (Exception e) {
        }

        try (LockedList<Integer> lockedList = list.lock()) {
            lockedList.subList(2, 4).clear();
        }

        Assert.assertEquals(2, count.get());
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(1, list.get(0).intValue());
        Assert.assertEquals(5, list.get(2).intValue());
    }

}
