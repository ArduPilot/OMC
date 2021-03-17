/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.intel.missioncontrol.helper.Expect;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 *
 * @param <V> view extended from {@link FxmlView}
 * @param <M> view model extended from {@link ViewModel}
 */
public abstract class GridComponentBase<V extends FxmlView<M>, M extends ViewModel> extends VBox {

    private final ViewTuple<V, M> viewModelTuple;

    public GridComponentBase(Class<V> viewClass, Context context) {
        Expect.notNull(viewClass, "viewClass", context, "context");
        viewModelTuple = FluentViewLoader.fxmlView(viewClass).root(this).context(context).load();
    }

    public V getView() {
        return viewModelTuple.getCodeBehind();
    }

    public M getViewModel() {
        return viewModelTuple.getViewModel();
    }

}
