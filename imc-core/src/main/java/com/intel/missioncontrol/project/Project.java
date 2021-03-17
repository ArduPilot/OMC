/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.persistence.Repository;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;

public class Project extends AbstractProject {

    private final transient AsyncObjectProperty<Repository> repository = new SimpleAsyncObjectProperty<>(this);

    private final transient AsyncListProperty<MergeConflict> mergeConflicts =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<MergeConflict>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    public Project() {
        super();
    }

    public Project(Project other) {
        super(other);
        this.repository.set(other.repository.get());
        this.mergeConflicts.setAll(other.mergeConflicts.get());
    }

    public Project(ProjectSnapshot other) {
        super(other);
    }

    public Project(CompositeDeserializationContext context) {
        super(context);
    }

    public ReadOnlyAsyncObjectProperty<Repository> repositoryProperty() {
        return repository;
    }

    public ReadOnlyAsyncListProperty<MergeConflict> mergeConflictsProperty() {
        return mergeConflicts;
    }

    public Repository getRepository() {
        return repository.get();
    }

    public AsyncObservableList<MergeConflict> getMergeConflicts() {
        return mergeConflicts.get();
    }

}
