/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.persistence.ProjectOutdatedException;
import com.intel.missioncontrol.persistence.Query;
import com.intel.missioncontrol.persistence.Repository;
import com.intel.missioncontrol.project.property.Hierarchical;
import com.intel.missioncontrol.project.property.MergeStrategy;
import com.intel.missioncontrol.project.property.TrackingAsyncObjectProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncSetProperty;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.beans.property.AttachedProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.PropertyObject;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncSetProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.value.RateControlledSubChangeListenerWrapper;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedCollection;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.AcceptAsyncDebouncer;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.asyncfx.concurrent.RunAsyncDebouncer;
import org.asyncfx.concurrent.Strand;

public class ProjectManager implements IProjectManager, Repository.ChangeListener, SubChangeListener {

    static final Duration CHANGE_DETECTION_PERIOD = Duration.ofMillis(250);

    private static AttachedProperty<AcceptAsyncDebouncer> DEBOUNCER_PROPERTY =
        AttachedProperty.register("DEBOUNCER_PROPERTY", AcceptAsyncDebouncer.class);

    private Function<List<? extends MergeStrategy.ConflictEntry>, MergeStrategy> conflictCallback;

    private final AsyncListProperty<Repository> repositories =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<Repository>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .dispatcher(Dispatcher.platform())
                .create());

    private final TrackingAsyncSetProperty<Project> availableProjects =
        new TrackingAsyncSetProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableSet<Project>>().dispatcher(Dispatcher.platform()).create());

    private final TrackingAsyncObjectProperty<Project> currentProject = new TrackingAsyncObjectProperty<>(this);

    private final Strand mergeStrand = new Strand();
    private final Strand allOperationsStrand = new Strand();

    private final RunAsyncDebouncer refreshDebouncer =
        new RunAsyncDebouncer(this::refreshInternalAsync, allOperationsStrand, true);

    /** Flag that disables projectChangeListener when merging internally in synchronizeAsync */
    private volatile boolean internalMergeIsActive = false;

    private final RateControlledSubChangeListenerWrapper projectChangeListener =
        new RateControlledSubChangeListenerWrapper(
            (observable, oldValue, newValue, subChange) -> {
                if (subChange && !internalMergeIsActive) {
                    Project project = getProject(newValue);

                    // For ease of use, we attach the debouncer instance to the project.
                    @SuppressWarnings("unchecked")
                    AcceptAsyncDebouncer<Project> debouncer =
                        (AcceptAsyncDebouncer<Project>)project.getValue(DEBOUNCER_PROPERTY, null);

                    if (debouncer == null) {
                        debouncer = new AcceptAsyncDebouncer<>(value -> synchronizeAsync(value));

                        project.setValue(DEBOUNCER_PROPERTY, debouncer);
                    }
                    // TODO check the result of the operation
                    debouncer.acceptAsync(project);
                }
            },
            CHANGE_DETECTION_PERIOD);

    @Inject
    public ProjectManager(
            @Named("local-repository") Repository localRepository,
            @Named("cloud-repository") Repository cloudRepository) {
        repositories.add(localRepository);
        repositories.add(cloudRepository);

        localRepository.addListener(this);
        cloudRepository.addListener(this);

        // If the current project changes, attach it to the platform dispatcher.
        currentProject.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null && !availableProjects.contains(newValue)) {
                    throw new IllegalArgumentException("The project is not contained in the available projects list.");
                }

                if (oldValue != null) {
                    oldValue.detachFromDispatcher();
                }

                if (newValue != null) {
                    newValue.attachToDispatcher(Dispatcher.platform());
                }
            });

        // If any of the sub-properties of the current project changes, save the project to the repository. Requests to
        // save the project are debounced, which means that only one request per project can be active at any given
        // time. If requests come in faster than the repository can fulfill them, intermediate requests will be dropped.
        // The last request of a series will always be accepted, so the last state of the project will be saved.
        currentProject.addListener((SubChangeListener)this);

        refreshAsync();
    }

    @Override
    public ReadOnlyAsyncListProperty<Repository> repositoriesProperty() {
        return repositories;
    }

    @Override
    public ReadOnlyAsyncSetProperty<Project> availableProjectsProperty() {
        return availableProjects;
    }

    @Override
    public AsyncObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }

    /**
     * Refreshes the available project list by merging all projects of all repositories into the current list. If a
     * merge conflict occurs, it will be recorded in {@link Project#mergeConflictsProperty()} of the corresponding
     * project instance.
     */
    @Override
    public Future<Void> refreshAsync() {
        // By deferring to the debouncer, we make sure that only a single refresh operation is running at any time.
        return refreshDebouncer.runAsync();
    }

    // This method is called by a debouncer.
    @SuppressWarnings("unchecked")
    private Future<Void> refreshInternalAsync() {
        return Dispatcher.background()
            .getLaterAsync(
                () -> {
                    // Make a copy of the available projects list first to keep the time we need to lock the list short.
                    List<ProjectSnapshot> availableProjectSnapshots;
                    try (LockedCollection<Project> list = this.availableProjects.lock()) {
                        availableProjectSnapshots = new ArrayList<>(list.size());
                        for (Project project : list) {
                            availableProjectSnapshots.add(new ProjectSnapshot(project));
                        }
                    }

                    // Expand the list of project snapshots into a list of projects.
                    TrackingAsyncSetProperty<Project> mergedProjectList = new TrackingAsyncSetProperty<>(null);
                    mergedProjectList.set(
                        FXAsyncCollections.observableSet(new TreeSet<>(Comparator.comparing(AbstractProject::getId))));
                    try (LockedCollection<Project> list = mergedProjectList.lock()) {
                        for (ProjectSnapshot project : availableProjectSnapshots) {
                            list.add(new Project(project));
                        }
                    }

                    // Now get all projects from all repositories, and merge them into our expanded project list.
                    Future[] futures = new Future[repositories.size()];
                    try (LockedList<Repository> repositories = this.repositories.lock()) {
                        for (int i = 0; i < repositories.size(); ++i) {
                            futures[i] =
                                querySingleRepositoryAsync(repositories.get(i), new Query.All())
                                    .thenAcceptAsync(projects -> mergeIntoProjectListAsync(mergedProjectList, projects))
                                    // to fill the list of available projects gradually
                                    .thenRunAsync(ex -> applyMergedListAsync(mergedProjectList));
                        }
                    }

                    return futures;
                })
            .thenApplyAsync(res -> Futures.whenAll(res))
            .cast();
    }

    private Future<Set<Project>> querySingleRepositoryAsync(Repository repository, Query query) {
        return repository
            .queryAsync(query)
            .thenApply(
                projects -> {
                    for (Project project : projects) {
                        ((AsyncObjectProperty<Repository>)project.repositoryProperty()).set(repository);
                    }

                    return projects;
                });
    }

    private Future<Void> mergeIntoProjectListAsync(
            TrackingAsyncSetProperty<Project> mergedProjects, Set<Project> projects) {
        return mergeStrand.runLater(
            () -> {
                // Merge with default strategy, which applies all non-conflicting changes. Conflicting changes are
                // recorded, so we can present them in the user interface.
                MergeStrategy.Default strategy = new MergeStrategy.Default();
                mergedProjects.merge(projects, strategy, Project::new, (s, t) -> s.merge(t, strategy));

                try (LockedCollection<Project> list = mergedProjects.lock()) {
                    for (Project project : list) {
                        project.mergeConflictsProperty().clear();
                    }
                }

                // For each conflict, find the corresponding project and add it to the list of conflicts for this
                // project.
                for (MergeStrategy.ConflictEntry entry : strategy.getConflicts()) {
                    List<Object> path = new ArrayList<>();
                    path.add(entry.getProperty());

                    Object parent = entry.getProperty().getBean();
                    path.add(parent);

                    while (parent instanceof Hierarchical) {
                        parent = ((Hierarchical)parent).getParent();
                        path.add(parent);
                    }

                    if (parent instanceof Project) {
                        Project project = (Project)parent;

                        if (entry instanceof MergeStrategy.ValueConflictEntry) {
                            MergeStrategy.ValueConflictEntry value = (MergeStrategy.ValueConflictEntry)entry;
                            project.mergeConflictsProperty()
                                .add(new MergeConflict(path, value.getOurValue(), value.getTheirValue()));
                        } else if (entry instanceof MergeStrategy.ItemRemovedConflictEntry) {
                            project.mergeConflictsProperty().add(new MergeConflict(path));
                        }
                    }
                }
            });
    }

    private Future<Void> applyMergedListAsync(TrackingAsyncSetProperty<Project> mergedProjects) {
        return Dispatcher.platform()
            .runLaterAsync(
                () -> {
                    System.out.println("setting projects to the list of available projects");
                    this.availableProjects.set(mergedProjects.get());
                });
    }

    @Override
    public void addRepository(Repository repository) {
        repositories.add(repository);
    }

    @Override
    /** Method operates on a Project's copy and applies the resulting project back to the original */
    public Future<Void> synchronizeAsync(Project originalProject) {
        Repository repository = originalProject.repositoryProperty().get();
        return synchronizeInternal(originalProject, repository);
    }

    private Future<Void> synchronizeInternal(Project originalProject, Repository repository) {
        MergeStrategy.DryRun strategy = new MergeStrategy.DryRun();
        Project projectCopy = new Project(originalProject);
        return repository
            /* 1. Getting a project from the repo * */
            .queryAsync(new Query.ById(projectCopy.getId()))
            .thenFinallyApplyAsync(
                (value, exception) -> {
                    if (exception != null && exception.getThrowables().length != 0) {
                        return Futures.failed(exception);
                    }

                    if (value.size() > 1) {
                        return Futures.failed(new IllegalStateException("project is not unique in the repository"));
                    }

                    Iterator<Project> it = value.iterator();
                    if (it.hasNext()) {
                        return Futures.successful(it.next());
                    }
                    // no project found in the repo - would mean that project has never been saved
                    return Futures.successful(projectCopy);
                })
            /* *****************************************/
            /* 2. Merging remote project into copy of the current project **/
            .thenFinallyApplyAsync(
                (p, exception) -> {
                    if (projectCopy.equals(p)) {
                        return Futures.successful(projectCopy);
                    }

                    projectCopy.merge(p, strategy);
                    MergeStrategy userSelectedMergeStrategy = null;
                    if (strategy.getConflicts().size() > 0) {
                        userSelectedMergeStrategy = conflictCallback.apply(strategy.getConflicts());
                        projectCopy.merge(p, userSelectedMergeStrategy);
                    } else {
                        // no conflicts
                        projectCopy.merge(p, new MergeStrategy.Default());
                    }

                    if (userSelectedMergeStrategy instanceof MergeStrategy.DryRun
                            || userSelectedMergeStrategy instanceof MergeStrategy.Default) {
                        return Futures.failed(new IllegalStateException());
                    }

                    return Futures.successful(projectCopy);
                })
            /* **************************************** */
            /* 3. Saving merged version into the repo. In case if in the repo there are new changes - run synchronizeInternal again**/
            .thenFinallyApplyAsync((value, exception) -> repository.saveAsync(projectCopy))
            .thenFinallyAcceptAsync(
                (value, exception) -> {
                    if (exception != null) {
                        var exceptions = exception.getThrowables();
                        for (Throwable t : exceptions) {
                            if (t instanceof ProjectOutdatedException) {
                                return synchronizeInternal(originalProject, repository);
                            }
                        }
                    }

                    return Futures.successful(null);
                })
            /* **************************************** */
            /* 4. Merging synced copy into current project on the UI thread **/
            .thenFinallyApplyAsync(
                (value, exception) ->
                    Dispatcher.platform()
                        .getLaterAsync(
                            () -> {
                                MergeStrategy.DryRun strategy1 = new MergeStrategy.DryRun();
                                // trying to apply merged and synced project copy to the original
                                if (exception == null) {
                                    internalMergeIsActive = true;
                                    originalProject.merge(projectCopy, strategy1);
                                    internalMergeIsActive = false;
                                }

                                if (strategy1.getConflicts().isEmpty()
                                        || areConflictsTheSame(strategy1.getConflicts(), strategy.getConflicts())) {
                                    internalMergeIsActive = true;
                                    originalProject.merge(projectCopy, new MergeStrategy.KeepTheirs());
                                    internalMergeIsActive = false;
                                }

                                return strategy1;
                            }))
            /* **************************************** */
            /* 5. In case if there were changes in the current project during sync process - rerun synchronizeInternal **/
            .thenFinallyAcceptAsync(
                (value, exception) -> {
                    if (!value.getConflicts().isEmpty()) {
                        return synchronizeInternal(originalProject, repository);
                    }

                    return Futures.successful(null);
                });
    }

    private boolean areConflictsTheSame(
            List<MergeStrategy.ConflictEntry> conflicts, List<MergeStrategy.ConflictEntry> conflicts1) {
        if (conflicts.size() != conflicts1.size()) {
            return false;
        }

        for (int i = 0; i < conflicts.size(); i++) {
            var conflict1 = conflicts.get(i);
            var conflict2 = conflicts1.get(i);
            if (conflict1.getProperty().getName().equals(conflict2.getProperty().getName())) {
                if (conflict1 instanceof MergeStrategy.ValueConflictEntry
                        && conflict2 instanceof MergeStrategy.ValueConflictEntry) {
                    if (((MergeStrategy.ValueConflictEntry)conflict1)
                                .getOurValue()
                                .equals(((MergeStrategy.ValueConflictEntry)conflict2).getOurValue())
                            && ((MergeStrategy.ValueConflictEntry)conflict1)
                                .getTheirValue()
                                .equals(((MergeStrategy.ValueConflictEntry)conflict2).getTheirValue())) {
                        continue;
                    }
                }

                if (conflict1 instanceof MergeStrategy.ItemRemovedConflictEntry
                        && conflict2 instanceof MergeStrategy.ItemRemovedConflictEntry) {
                    if (((MergeStrategy.ItemRemovedConflictEntry)conflict1).isRemovedByUs()
                            == ((MergeStrategy.ItemRemovedConflictEntry)conflict2).isRemovedByUs()) {
                        continue;
                    }
                }
            }

            return false;
        }

        return true;
    }

    //    @Override
    //    public Future<Void> deleteAsync(UUID info) {
    //        return null;
    //    }

    @Override
    public Future<Void> moveAsync(Project project, Repository repository) {
        return repository
            .saveAsync(project)
            .thenApplyAsync(
                value -> {
                    project.repositoryProperty().get().deleteAsync(project.getId());
                    return Futures.successful(value);
                });
    }

    @Override
    public void registerCallback(Function<List<? extends MergeStrategy.ConflictEntry>, MergeStrategy> callback) {
        this.conflictCallback = callback;
    }

    private Project getProject(Object propertyOrBean) {
        if (propertyOrBean instanceof Project) {
            return (Project)propertyOrBean;
        }

        Hierarchical bean = null;

        if (propertyOrBean instanceof AsyncProperty) {
            Object obj = ((AsyncProperty)propertyOrBean).getBean();
            if (obj instanceof Hierarchical) {
                bean = (Hierarchical)obj;
            }
        } else if (propertyOrBean instanceof PropertyObject) {
            bean = (Hierarchical)propertyOrBean;
        }

        if (bean == null) {
            throw new IllegalArgumentException("propertyOrBean");
        }

        Object parent = bean.getParent();
        while (parent instanceof Hierarchical) {
            parent = ((Hierarchical)parent).getParent();
        }

        if (!(parent instanceof Project)) {
            throw new IllegalArgumentException("propertyOrBean");
        }

        return (Project)parent;
    }

    /** Change coming from a repository */
    @Override
    public void changed(Project project) {
        if (project.getId().equals(currentProject.get().getId())) {
            projectChangeListener.changed(currentProject, project, project, true);
        } else {
            refreshAsync();
        }
    }

    /** Change coming from the current project modification */
    @Override
    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue, boolean subChange) {
        projectChangeListener.changed(observable, oldValue, newValue, subChange);
    }
}
