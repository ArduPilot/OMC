/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.credits;

import com.intel.missioncontrol.beans.AsyncObservable;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import java.util.LinkedList;
import javafx.collections.ListChangeListener;

public class MapCreditsManager implements IMapCreditsManager {

    private final AsyncListProperty<AsyncListProperty<MapCreditViewModel>> sources =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<AsyncListProperty<MapCreditViewModel>>>()
                .initialValue(FXAsyncCollections.observableArrayList(param -> new AsyncObservable[] {param}))
                .create());

    private final AsyncListProperty<MapCreditViewModel> credits =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<MapCreditViewModel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    public MapCreditsManager() {
        sources.addListener(
            (ListChangeListener<AsyncListProperty<MapCreditViewModel>>)
                c -> {
                    try (LockedList<AsyncListProperty<MapCreditViewModel>> sourcesLocked = sources.lock()) {
                        LinkedList<MapCreditViewModel> creditsNew = new LinkedList<>();
                        for (AsyncListProperty<MapCreditViewModel> source : sourcesLocked) {
                            try (LockedList<MapCreditViewModel> sourceLocked = source.lock()) {
                                creditsNew.addAll(sourceLocked);
                            }
                        }

                        credits.setAll(creditsNew);
                    }
                });
    }

    @Override
    public void register(IMapCreditsSource source) {
        sources.add(source.mapCreditsProperty());
    }

    @Override
    public AsyncListProperty<MapCreditViewModel> mapCreditsProperty() {
        return credits;
    }
}
