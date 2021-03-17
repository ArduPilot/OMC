/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.SubInvalidationListener;
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
        helper = AsyncExpressionHelper.removeListener(helper, null, changeListener);
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
        helper = AsyncExpressionHelper.removeListener(helper, null, changeListener);
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
        helper = AsyncExpressionHelper.removeListener(helper, null, invalidationListener);
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
        helper = AsyncExpressionHelper.removeListener(helper, null, subChangeListener);
        assertTrue(helper instanceof AsyncExpressionHelper.SingleSubChange);
    }

}
