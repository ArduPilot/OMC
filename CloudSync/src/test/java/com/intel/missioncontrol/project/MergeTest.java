/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import static com.intel.missioncontrol.PropertyHelper.asMutable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intel.missioncontrol.TestBase;
import com.intel.missioncontrol.UUIDHelper;
import com.intel.missioncontrol.geom.Vec4;
import com.intel.missioncontrol.project.property.MergeStrategy;
import com.intel.missioncontrol.project.property.PlatformSynchronizationContext;
import com.intel.missioncontrol.project.property.TrackingAsyncDoubleProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncListProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncObjectProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncStringProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.asyncfx.AsyncFX;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("InnerClassMayBeStatic")
class MergeTest extends TestBase {

    @BeforeAll
    static void setup() {
        AsyncFX.setVerifyPropertyAccess(false);
    }

    @AfterAll
    static void teardown() {
        AsyncFX.setVerifyPropertyAccess(true);
    }

    @Nested
    class ValueMergeTests {
        @Nested
        class NonConflictingChangesTest {

            @Test
            void test_SimpleProperty_Unchanged_Merge_NoConflict() {
                double val = 10.01;
                TrackingAsyncDoubleProperty double1 = new TrackingAsyncDoubleProperty(this);
                double1.update(val);
                var strategy = new MergeStrategy.DryRun();
                double1.merge(val, strategy);
                assertEquals(val, double1.get());
                assertEquals(0, strategy.getConflicts().size());
            }

            @Test
            void test_ObjectProperty_Unchanged_Merge_NoConflict() {
                String val = "Hello1";
                String val2 = "Hello1";
                TrackingAsyncStringProperty string1 = new TrackingAsyncStringProperty(this);
                string1.update(val);
                var strategy = new MergeStrategy.DryRun();
                string1.merge(val2, strategy);
                assertEquals(val, string1.get());
                assertEquals(0, strategy.getConflicts().size());
            }

            @Test
            void test_IMergableObjectProperty_Unchanged_Merge_NoConflict() {
                String name = "Hello FlightPlan";
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                TrackingAsyncObjectProperty<IFlightPlan> flightPlanProp = new TrackingAsyncObjectProperty<>(this);
                flightPlanProp.update(flightPlan);

                FlightPlan flightPlan2 = new FlightPlan(flightPlan);
                var strategy = new MergeStrategy.DryRun();
                flightPlanProp.merge(flightPlanSnapshot, strategy);
                assertEquals(flightPlan, flightPlanProp.get());
                assertEquals(0, strategy.getConflicts().size());

                flightPlanProp.merge(flightPlan2, strategy);
                assertEquals(flightPlan, flightPlanProp.get());
                assertEquals(0, strategy.getConflicts().size());
            }

            @Test
            void test_ListProperty_Unchanged_Merge_NoConflict() {
                String name = "Hello FlightPlan";

                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                flightPlansListProperty1.add(flightPlan);
                flightPlansListProperty2.add(new FlightPlan(flightPlan));

                var strategy = new MergeStrategy.DryRun();
                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                assertEquals(flightPlan, flightPlansListProperty1.get(0));
                assertEquals(0, strategy.getConflicts().size());
            }

            @Test
            void test_SimpleProperty_LeftChangedRightNotChanged_Merge_NoConflict() {
                double val = 10.01;
                TrackingAsyncDoubleProperty double1 = new TrackingAsyncDoubleProperty(this);
                double1.update(val);

                double val1 = 10.02;
                double1.set(val1);

                var strategy = new MergeStrategy.DryRun();
                double1.merge(val, strategy);
                assertEquals(val1, double1.get());
                assertEquals(0, strategy.getConflicts().size());
            }

            @Test
            void test_ObjectProperty_LeftChangedRightNotChanged_Merge_NoConflict() {
                String val = "Hello1";
                String val2 = "Hello2";
                TrackingAsyncStringProperty string1 = new TrackingAsyncStringProperty(this);
                string1.update(val);
                string1.set(val2);

                var strategy = new MergeStrategy.DryRun();
                string1.merge(val, strategy);
                assertEquals(val2, string1.get());
                assertEquals(0, strategy.getConflicts().size());
            }

            @Test
            void test_IMergableObjectProperty__LeftChangedRightNotChanged_Merge_NoConflict() {
                var strategy = new MergeStrategy.DryRun();
                String name = "Hello FlightPlan";

                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);
                FlightPlan flightPlan1 = new FlightPlan(flightPlan);

                TrackingAsyncObjectProperty<IFlightPlan> flightPlanProp = new TrackingAsyncObjectProperty<>(this);

                // no factual change - no practical change
                flightPlanProp.update(flightPlan);

                // no factual change - practical change
                flightPlan1.nameProperty().set("Hello 111");
                flightPlanProp.set(flightPlan1);

                // works
                flightPlanProp.merge(flightPlanSnapshot, strategy);
                assertEquals(flightPlan1, flightPlanProp.get());
                assertEquals(0, strategy.getConflicts().size());
            }

            @Test
            void test_ListProperty_LeftChangedRightNotChanged_Merge_NoConflict() {
                String name = "Hello FlightPlan";
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                flightPlansListProperty1.add(flightPlan);
                flightPlansListProperty2.add(new FlightPlan(flightPlan));

                flightPlansListProperty1.clear();
                FlightPlan fp3 = new FlightPlan(flightPlan);
                flightPlansListProperty1.add(fp3);
                fp3.nameProperty().set("New name");

                var strategy = new MergeStrategy.DryRun();
                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                assertEquals(fp3, flightPlansListProperty1.get(0));
                assertEquals(0, strategy.getConflicts().size());
            }

            @Test
            void test_SimpleProperty_LeftNoChangeRightChanged_Merge_NoConflict() {
                double val = 10.01;
                double val1 = 10.02;
                double val2 = 10.03;
                TrackingAsyncDoubleProperty double1 = new TrackingAsyncDoubleProperty(this);
                double1.update(val);

                // if both strategies give the same result in this case which to choose by default ??
                var strategy = new MergeStrategy.KeepOurs();
                double1.merge(val1, strategy);
                assertEquals(val1, double1.get());

                var strategy2 = new MergeStrategy.KeepTheirs();
                double1.merge(val2, strategy2);
                assertEquals(val2, double1.get());
            }

            @Test
            void test_ObjectProperty_LeftNoChangeRightChanged_Merge_NoConflict() {
                String val = "Hello1";
                String val2 = "Hello2";
                TrackingAsyncStringProperty string1 = new TrackingAsyncStringProperty(this);
                string1.update(val);
                var strategy = new MergeStrategy.KeepOurs();
                string1.merge(val2, strategy);
                assertEquals(val2, string1.get());
            }

            @Test
            void test_IMergableObjectProperty_LeftNoChangeRightChanged_Merge_NoConflict() {
                String name = "Hello FlightPlan";
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                TrackingAsyncObjectProperty<IFlightPlan> flightPlanProp = new TrackingAsyncObjectProperty<>(this);
                flightPlanProp.update(flightPlan);

                FlightPlan flightPlan2 = new FlightPlan(flightPlan);
                var strategy = new MergeStrategy.KeepOurs();

                flightPlan2.nameProperty().set("Hello FlightPlan 2!");
                flightPlanProp.merge(flightPlan2, strategy);
                assertEquals(flightPlanProp.get().getName(), flightPlan2.nameProperty().get());
            }

            @Test
            void test_ListProperty_LeftNoChangeRightChanged_Merge_NoConflict() {
                String name = "Hello FlightPlan";
                String name2 = "Hello FlightPlan2";
                OffsetDateTime modifiedDate = OffsetDateTime.now();
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                FlightPlan flightPlan1 = new FlightPlan(flightPlan);
                flightPlan1.nameProperty().set(name2);

                FlightPlanSnapshot flightPlanSnapshot2 =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan2 = new FlightPlan(flightPlanSnapshot2);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                var list = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list.add(flightPlan);
                flightPlansListProperty1.update(list);

                var list2 = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list2.add(flightPlan2);
                list2.add(flightPlan1);
                flightPlansListProperty1.update(list);
                flightPlansListProperty2.update(list2);

                var strategy = new MergeStrategy.KeepOurs();

                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                assertEquals(flightPlan1.getName(), flightPlansListProperty1.get(0).getName());
            }

            @Test
            void test_CompositeObject_LeftNoChangeRightChanged_Merge_NoConflict() {
                var date1 = OffsetDateTime.parse("2019-01-01T00:00:01Z");
                Project left = new Project();
                asMutable(left.creationDateProperty()).set(date1);
                asMutable(left.lastModifiedDateProperty()).set(date1);

                String userName = "Katerina";
                var userLeft = new User();
                asMutable(userLeft.nameProperty()).update(userName);
                var userRight = new User(userLeft);
                asMutable(userRight.nameProperty()).update(userName);

                asMutable(left.userProperty()).update(userLeft);

                Project right = new Project(left);
                asMutable(right.creationDateProperty()).set(date1);
                asMutable(right.lastModifiedDateProperty()).set(date1);

                asMutable(right.userProperty()).update(userRight);
                ((User)right.userProperty().get()).nameProperty().set("Filona");

                var strategy = new MergeStrategy.DryRun();
                left.merge(right, strategy);

                assertEquals(0, strategy.getConflicts().size());
            }
        }

        @Nested
        class ConflictingChangesTest {
            @Nested
            class ValueConflicts {
                private Object[] createSimpleProject0() {
                    var date0 = OffsetDateTime.parse("2019-01-01T00:00:00Z");
                    var date1 = OffsetDateTime.parse("2019-01-01T00:00:01Z");

                    Project project = new Project();
                    asMutable(project.creationDateProperty()).set(date0);
                    asMutable(project.lastModifiedDateProperty()).set(date0);

                    ProjectSnapshot projectSnapshot =
                        new ProjectSnapshot(
                            project.getId(),
                            "SimpleProject0",
                            RepositoryType.LOCAL,
                            date1,
                            date1,
                            new UserSnapshot(UUID.randomUUID(), "Ivan"),
                            null,
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new ArrayList<>());

                    return new Object[] {project, projectSnapshot};
                }

                @Test
                void DryRun_Fails_With_Conflicts() {
                    var temp = createSimpleProject0();
                    Project project = (Project)temp[0];
                    ProjectSnapshot projectSnapshot = (ProjectSnapshot)temp[1];

                    MergeStrategy.DryRun strategy = new MergeStrategy.DryRun();
                    project.merge(projectSnapshot, strategy);

                    // creation and modification dates
                    assertEquals(2, strategy.getConflicts().size());
                }

                @Test
                void KeepOurs_Succeeds() {
                    var temp = createSimpleProject0();
                    Project project = (Project)temp[0];
                    ProjectSnapshot projectSnapshot = (ProjectSnapshot)temp[1];

                    MergeStrategy.KeepOurs strategy = new MergeStrategy.KeepOurs();
                    project.merge(projectSnapshot, strategy);

                    assertEquals("SimpleProject0", project.getName());

                    var expectedDate = OffsetDateTime.parse("2019-01-01T00:00:00Z");
                    assertEquals(expectedDate, project.getCreationDate());
                    assertEquals(expectedDate, project.getLastModifiedDate());
                }

                @Test
                void KeepTheirs_Succeeds() {
                    var temp = createSimpleProject0();
                    Project project = (Project)temp[0];
                    ProjectSnapshot projectSnapshot = (ProjectSnapshot)temp[1];

                    MergeStrategy.KeepTheirs strategy = new MergeStrategy.KeepTheirs();
                    project.merge(projectSnapshot, strategy);

                    assertEquals("SimpleProject0", project.getName());

                    var expectedDate = OffsetDateTime.parse("2019-01-01T00:00:01Z");
                    assertEquals(expectedDate, project.getCreationDate());
                    assertEquals(expectedDate, project.getLastModifiedDate());
                }

                @Test
                void test_CompositeObject_LeftChangedRightChanged_Merge_Conflict() {
                    var date1 = OffsetDateTime.parse("2019-01-01T00:00:01Z");
                    Project left = new Project();
                    asMutable(left.creationDateProperty()).set(date1);
                    asMutable(left.lastModifiedDateProperty()).set(date1);

                    String userName = "Katerina";
                    var userLeft = new User();
                    asMutable(userLeft.nameProperty()).update(userName);
                    var userRight = new User(userLeft);
                    asMutable(userRight.nameProperty()).update(userName);

                    asMutable(left.userProperty()).update(userLeft);
                    ((User)left.userProperty().get()).nameProperty().set("Sara");

                    Project right = new Project(left);
                    asMutable(right.creationDateProperty()).set(date1);
                    asMutable(right.lastModifiedDateProperty()).set(date1);

                    asMutable(right.userProperty()).update(userRight);
                    ((User)right.userProperty().get()).nameProperty().set("Filona");

                    var strategy = new MergeStrategy.DryRun();
                    left.merge(right, strategy);

                    assertEquals(1, strategy.getConflicts().size());
                }
            }
        }
    }

    @Nested
    class TreeMergeTests {
        @Nested
        class NonConflictingChangesTest {
            @Test
            void test_ListProperty_LeftChangedRightNotChanged_Merge_NoConflict() {
                String name = "Hello FlightPlan";
                var modifiedDate = OffsetDateTime.now();
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                FlightPlanSnapshot flightPlanSnapshot2 =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan2 = new FlightPlan(flightPlanSnapshot2);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                var list = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list.add(flightPlan);
                flightPlansListProperty1.update(list);
                flightPlansListProperty1.clear();

                var list2 = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list2.add(flightPlan);
                flightPlansListProperty2.update(list);

                var strategy = new MergeStrategy.KeepOurs();

                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                assertEquals(0, flightPlansListProperty1.size());
            }

            @Test
            void test_ListProperty_LeftNoChangeRightChanged_Merge_NoConflict() {
                String name = "Hello FlightPlan";
                var modifiedDate = OffsetDateTime.now();
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                FlightPlanSnapshot flightPlanSnapshot2 =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan2 = new FlightPlan(flightPlanSnapshot2);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                var list = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list.add(flightPlan);
                flightPlansListProperty1.update(list);

                var list2 = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list2.add(flightPlan);
                list2.add(flightPlan2);
                flightPlansListProperty2.update(list2);

                var strategy = new MergeStrategy.KeepOurs();

                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                assertEquals(2, flightPlansListProperty1.size());
                assertEquals(flightPlan, flightPlansListProperty1.get(0));
            }
        }

        @Nested
        class ConflictingChangesTest {
            @Test
            void test_ListProperty_LeftTreeChangedRightTreeChangedPlusValueChanged_Merge_Conflict() {
                String name = "Hello FlightPlan";
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                FlightPlanSnapshot flightPlanSnapshot2 =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlanSnapshot flightPlanSnapshot3 =
                    new FlightPlanSnapshot(
                        flightPlanSnapshot2.getId(),
                        name + "111",
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan2 = new FlightPlan(flightPlanSnapshot2);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                var list = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list.add(flightPlan);
                list.add(flightPlan2);
                flightPlansListProperty1.update(list);
                flightPlansListProperty1.get().remove(flightPlan2);

                var list2 = FXAsyncCollections.<IFlightPlan>observableArrayList();
                // list2.add(flightPlan2);
                list2.add(flightPlanSnapshot3);
                // ((TrackingAsyncStringProperty)flightPlan2.nameProperty()).update("Hello new name!");
                flightPlansListProperty2.update(list2);

                var strategy = new MergeStrategy.DryRun();

                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                // TODO should be a conflict here ?!
                assertEquals(0, strategy.getConflicts().size());
                assertEquals(1, flightPlansListProperty1.get().size());
            }

            @Test
            void test_ListProperty_LeftTreeChangedRightTreeChangedPlusValueChanged_Merge_KeepOur_ConflictResolved() {
                String name = "Hello FlightPlan";
                var modifiedDate = OffsetDateTime.now();
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                FlightPlanSnapshot flightPlanSnapshot2 =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan2 = new FlightPlan(flightPlanSnapshot2);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                var list = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list.add(flightPlan);
                list.add(flightPlan2);
                flightPlansListProperty1.update(list);
                flightPlansListProperty1.get().remove(flightPlan2);

                var list2 = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list2.add(flightPlan2);
                flightPlan2.nameProperty().set("Hello new name!");
                flightPlansListProperty2.update(list2);

                var strategy = new MergeStrategy.KeepOurs();

                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                // first element was deleted on our side
                assertEquals(0, flightPlansListProperty1.size());
            }

            @Test
            void test_ListProperty_LeftTreeChangedRightTreeChangedPlusValueChanged_Merge_KeepTheir_ConflictResolved() {
                String name = "Hello FlightPlan";
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                FlightPlanSnapshot flightPlanSnapshot2 =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan2 = new FlightPlan(flightPlanSnapshot2);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                var list = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list.add(flightPlan);
                list.add(flightPlan2);
                flightPlansListProperty1.update(list);
                flightPlansListProperty1.get().remove(flightPlan2);

                var list2 = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list2.add(flightPlan2);
                flightPlan2.nameProperty().set("Hello new name!");
                flightPlansListProperty2.update(list2);

                var strategy = new MergeStrategy.KeepTheirs();

                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);
                // first element got deleted(no conflict) second was modified on their side - so it is added back to the
                // first list
                assertEquals(1, flightPlansListProperty1.get().size());
            }

            @Test
            void test_ListProperty_LeftTreeChangedPlusValueChangedRightTreeChanged_Merge_Conflict() {
                String name = "Hello FlightPlan";
                var modifiedDate = OffsetDateTime.now();
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                FlightPlanSnapshot flightPlanSnapshot2 =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan2 = new FlightPlan(flightPlanSnapshot2);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                var list = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list.add(flightPlan);
                flightPlansListProperty1.update(list);
                flightPlan.nameProperty().set("Hello new name!");

                var list2 = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list2.add(flightPlan2);
                flightPlansListProperty2.update(list2);

                var strategy = new MergeStrategy.DryRun();

                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                assertEquals(1, strategy.getConflicts().size());
                assertEquals(1, flightPlansListProperty1.get().size());
            }

            @Test
            void test_ListProperty_LeftTreeChangedPlusValueChangedRightTreeChanged_Merge_KeepOurs_ConflictResolved() {
                String name = "Hello FlightPlan";
                var modifiedDate = OffsetDateTime.now();
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                FlightPlanSnapshot flightPlanSnapshot2 =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan2 = new FlightPlan(flightPlanSnapshot2);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                var list = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list.add(flightPlan);
                flightPlansListProperty1.update(list);
                flightPlan.nameProperty().set("Hello new name!");

                var list2 = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list2.add(flightPlan2);
                flightPlansListProperty2.update(list2);

                var strategy = new MergeStrategy.KeepOurs();

                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                assertEquals(2, flightPlansListProperty1.get().size());
            }

            @Test
            void test_ListProperty_LeftTreeChangedPlusValueChangedRightTreeChanged_Merge_KeepTheir_ConflictResolved() {
                String name = "Hello FlightPlan";
                var modifiedDate = OffsetDateTime.now();
                FlightPlanSnapshot flightPlanSnapshot =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan = new FlightPlan(flightPlanSnapshot);

                FlightPlanSnapshot flightPlanSnapshot2 =
                    new FlightPlanSnapshot(
                        UUID.randomUUID(),
                        name,
                        OffsetDateTime.now(),
                        new Vec4(0, 0, 0, 0),
                        new Vec4(0, 0, 0, 0),
                        0.0,
                        0.0,
                        0.0,
                        true,
                        true,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        new ArrayList<>());
                FlightPlan flightPlan2 = new FlightPlan(flightPlanSnapshot2);

                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty1 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());
                TrackingAsyncListProperty<IFlightPlan> flightPlansListProperty2 =
                    new TrackingAsyncListProperty<>(
                        this,
                        new PropertyMetadata.Builder<AsyncObservableList<IFlightPlan>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .synchronizationContext(PlatformSynchronizationContext.getInstance())
                            .create());

                var list = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list.add(flightPlan);
                flightPlansListProperty1.update(list);
                flightPlan.nameProperty().set("Hello new name!");

                var list2 = FXAsyncCollections.<IFlightPlan>observableArrayList();
                list2.add(flightPlan2);
                flightPlansListProperty2.update(list2);

                var strategy = new MergeStrategy.KeepTheirs();

                flightPlansListProperty1.merge(flightPlansListProperty2, strategy);

                assertEquals(1, flightPlansListProperty1.get().size());
            }
        }
    }
}
