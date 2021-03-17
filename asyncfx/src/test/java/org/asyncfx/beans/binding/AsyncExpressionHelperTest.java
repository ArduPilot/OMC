/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.asyncfx.beans.value.SubChangeListener;
import org.junit.jupiter.api.Test;

class AsyncExpressionHelperTest {

    private InvalidationListener invalidationListener = observable -> {};
    private SubInvalidationListener subInvalidationListener = (observable, subChange) -> {};
    private ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {};
    private SubChangeListener subChangeListener = (observable, oldValue, newValue, subChange) -> {};

    @Test
    void Generic_Listener_Reverts_To_SingleInvalidation_Listener() {
        ObservableValue<String> observable = new SimpleStringProperty();
        AsyncExpressionHelper<String> helper = null;
        helper = AsyncExpressionHelper.addListener(helper, observable, null, invalidationListener);
        assertTrue(helper instanceof AsyncExpressionHelper.SingleInvalidation);
        helper = AsyncExpressionHelper.addListener(helper, observable, null, changeListener);
        assertTrue(helper instanceof AsyncExpressionHelper.Generic);
        helper = AsyncExpressionHelper.removeListener(helper, changeListener);
        assertTrue(helper instanceof AsyncExpressionHelper.SingleInvalidation);
    }

    @Test
    void Generic_Listener_Reverts_To_SingleSubInvalidation_Listener() {
        ObservableValue<String> observable = new SimpleStringProperty();
        AsyncExpressionHelper<String> helper = null;
        helper = AsyncExpressionHelper.addListener(helper, observable, null, subInvalidationListener);
        assertTrue(helper instanceof AsyncExpressionHelper.SingleSubInvalidation);
        helper = AsyncExpressionHelper.addListener(helper, observable, null, changeListener);
        assertTrue(helper instanceof AsyncExpressionHelper.Generic);
        helper = AsyncExpressionHelper.removeListener(helper, changeListener);
        assertTrue(helper instanceof AsyncExpressionHelper.SingleSubInvalidation);
    }

    @Test
    void Generic_Listener_Reverts_To_SingleChange_Listener() {
        ObservableValue<String> observable = new SimpleStringProperty();
        AsyncExpressionHelper<String> helper = null;
        helper = AsyncExpressionHelper.addListener(helper, observable, null, invalidationListener);
        assertTrue(helper instanceof AsyncExpressionHelper.SingleInvalidation);
        helper = AsyncExpressionHelper.addListener(helper, observable, null, changeListener);
        assertTrue(helper instanceof AsyncExpressionHelper.Generic);
        helper = AsyncExpressionHelper.removeListener(helper, invalidationListener);
        assertTrue(helper instanceof AsyncExpressionHelper.SingleChange);
    }

    @Test
    void Generic_Listener_Reverts_To_SingleSubChange_Listener() {
        ObservableValue<String> observable = new SimpleStringProperty();
        AsyncExpressionHelper<String> helper = null;
        helper = AsyncExpressionHelper.addListener(helper, observable, null, subChangeListener);
        assertTrue(helper instanceof AsyncExpressionHelper.SingleSubChange);
        helper = AsyncExpressionHelper.addListener(helper, observable, null, subChangeListener);
        assertTrue(helper instanceof AsyncExpressionHelper.Generic);
        helper = AsyncExpressionHelper.removeListener(helper, subChangeListener);
        assertTrue(helper instanceof AsyncExpressionHelper.SingleSubChange);
    }

    @Test
    void InvalidationListener_Is_Added_To_ChangeListener() {
        AsyncStringProperty observable =
            new SimpleAsyncStringProperty(null, new PropertyMetadata.Builder<String>().customBean(true).create());

        int[] invalidationCount = new int[1], changeCount = new int[1];
        ChangeListener<String> changeListener = (o, oldValue, newValue) -> changeCount[0]++;
        InvalidationListener invalidationListener = o -> invalidationCount[0]++;

        observable.addListener(changeListener);
        observable.set("a");
        assertEquals(1, changeCount[0]);

        observable.addListener(invalidationListener);
        observable.set("b");
        assertEquals(2, changeCount[0]);
        assertEquals(1, invalidationCount[0]);
    }

}
