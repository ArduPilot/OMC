/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

import static com.intel.missioncontrol.PropertyHelper.asMutable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intel.missioncontrol.project.ExtrudedPolygonGoal;
import com.intel.missioncontrol.project.Mission;
import com.intel.missioncontrol.project.Project;
import com.intel.missioncontrol.project.ProjectSerializationOptions;
import com.intel.missioncontrol.project.ProjectSnapshot;
import com.intel.missioncontrol.project.property.Identifiable;
import com.intel.missioncontrol.project.property.PropertySerializationHelper;
import com.intel.missioncontrol.project.property.TrackingAsyncBooleanProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncDoubleProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncFloatProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncIntegerProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncListProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncLongProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncObjectProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncStringProperty;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;
import org.asyncfx.AsyncFX;
import org.asyncfx.TestBase;
import org.asyncfx.collections.FXAsyncCollections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SerializationTest extends TestBase {

    @BeforeAll
    static void setup() {
        AsyncFX.setVerifyPropertyAccess(false);
    }

    @AfterAll
    static void teardown() {
        AsyncFX.setVerifyPropertyAccess(true);
    }

    enum TestEnum {
        VALUE_1,
        VALUE_2
    }

    static class SimpleTestObj implements CompositeSerializable, Identifiable {
        private final UUID id;
        final TrackingAsyncBooleanProperty boolProp = new TrackingAsyncBooleanProperty(this);
        final TrackingAsyncFloatProperty floatProp = new TrackingAsyncFloatProperty(this);
        final TrackingAsyncDoubleProperty doubleProp = new TrackingAsyncDoubleProperty(this);
        final TrackingAsyncIntegerProperty intProp = new TrackingAsyncIntegerProperty(this);
        final TrackingAsyncLongProperty longProp = new TrackingAsyncLongProperty(this);
        final TrackingAsyncStringProperty stringProp = new TrackingAsyncStringProperty(this);
        final TrackingAsyncObjectProperty<TestEnum> enumProp = new TrackingAsyncObjectProperty<>(this);

        SimpleTestObj(UUID id) {
            this.id = id;
        }

        @SuppressWarnings("WeakerAccess")
        public SimpleTestObj(CompositeDeserializationContext context) {
            id = UUID.fromString(context.readString("id"));
            PropertySerializationHelper.readBoolean(context, boolProp);
            PropertySerializationHelper.readFloat(context, floatProp);
            PropertySerializationHelper.readDouble(context, doubleProp);
            PropertySerializationHelper.readInteger(context, intProp);
            PropertySerializationHelper.readLong(context, longProp);
            PropertySerializationHelper.readString(context, stringProp);
            PropertySerializationHelper.readEnum(context, enumProp, TestEnum.class);
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public void serialize(CompositeSerializationContext context) {
            context.writeString("id", id.toString());
            PropertySerializationHelper.writeBoolean(context, boolProp);
            PropertySerializationHelper.writeFloat(context, floatProp);
            PropertySerializationHelper.writeDouble(context, doubleProp);
            PropertySerializationHelper.writeInteger(context, intProp);
            PropertySerializationHelper.writeLong(context, longProp);
            PropertySerializationHelper.writeString(context, stringProp);
            PropertySerializationHelper.writeEnum(context, enumProp, TestEnum.class);
        }

        static SimpleTestObj createRandom(UUID id) {
            SimpleTestObj res = new SimpleTestObj(id);
            Random random = new Random();
            res.boolProp.init(random.nextBoolean(), random.nextBoolean());
            res.floatProp.init(random.nextFloat(), random.nextFloat());
            res.doubleProp.init(random.nextDouble(), random.nextDouble());
            res.intProp.init(random.nextInt(), random.nextInt());
            res.longProp.init(random.nextLong(), random.nextLong());
            res.stringProp.init(Integer.toString(random.nextInt()), Integer.toString(random.nextInt()));
            res.enumProp.init(
                random.nextBoolean() ? TestEnum.VALUE_1 : TestEnum.VALUE_2,
                random.nextBoolean() ? TestEnum.VALUE_1 : TestEnum.VALUE_2);
            return res;
        }

        static void assertEquals(SimpleTestObj obj0, SimpleTestObj obj1) {
            Assertions.assertEquals(obj0.boolProp.get(), obj1.boolProp.get());
            Assertions.assertEquals(obj0.boolProp.getCleanValue(), obj1.boolProp.getCleanValue());

            Assertions.assertEquals(obj0.floatProp.get(), obj1.floatProp.get());
            Assertions.assertEquals(obj0.floatProp.getCleanValue(), obj1.floatProp.getCleanValue());

            Assertions.assertEquals(obj0.doubleProp.get(), obj1.doubleProp.get());
            Assertions.assertEquals(obj0.doubleProp.getCleanValue(), obj1.doubleProp.getCleanValue());

            Assertions.assertEquals(obj0.intProp.get(), obj1.intProp.get());
            Assertions.assertEquals(obj0.intProp.getCleanValue(), obj1.intProp.getCleanValue());

            Assertions.assertEquals(obj0.longProp.get(), obj1.longProp.get());
            Assertions.assertEquals(obj0.longProp.getCleanValue(), obj1.longProp.getCleanValue());

            Assertions.assertEquals(obj0.stringProp.get(), obj1.stringProp.get());
            Assertions.assertEquals(obj0.stringProp.getCleanValue(), obj1.stringProp.getCleanValue());

            Assertions.assertEquals(obj0.enumProp.get(), obj1.enumProp.get());
            Assertions.assertEquals(obj0.enumProp.getCleanValue(), obj1.enumProp.getCleanValue());
        }
    }

    static class DerivedTestObj extends SimpleTestObj {
        final TrackingAsyncStringProperty string2Prop = new TrackingAsyncStringProperty(this);

        DerivedTestObj(UUID id) {
            super(id);
        }

        @SuppressWarnings("unused")
        public DerivedTestObj(CompositeDeserializationContext context) {
            super(context);
            PropertySerializationHelper.readString(context, string2Prop);
        }

        @Override
        public void serialize(CompositeSerializationContext context) {
            super.serialize(context);
            PropertySerializationHelper.writeString(context, string2Prop);
        }

        static DerivedTestObj createRandom(UUID id) {
            DerivedTestObj res = new DerivedTestObj(id);
            Random random = new Random();
            res.boolProp.init(random.nextBoolean(), random.nextBoolean());
            res.floatProp.init(random.nextFloat(), random.nextFloat());
            res.doubleProp.init(random.nextDouble(), random.nextDouble());
            res.intProp.init(random.nextInt(), random.nextInt());
            res.longProp.init(random.nextLong(), random.nextLong());
            res.stringProp.init(Integer.toString(random.nextInt()), Integer.toString(random.nextInt()));
            res.string2Prop.init(Integer.toString(random.nextInt()), Integer.toString(random.nextInt()));
            res.enumProp.init(
                random.nextBoolean() ? TestEnum.VALUE_1 : TestEnum.VALUE_2,
                random.nextBoolean() ? TestEnum.VALUE_1 : TestEnum.VALUE_2);
            return res;
        }

        static void assertEquals(DerivedTestObj obj0, DerivedTestObj obj1) {
            SimpleTestObj.assertEquals(obj0, obj1);
            Assertions.assertEquals(obj0.string2Prop.get(), obj1.string2Prop.get());
            Assertions.assertEquals(obj0.string2Prop.getCleanValue(), obj1.string2Prop.getCleanValue());
        }
    }

    static class ComplexTestObj implements CompositeSerializable, Identifiable {
        private final TrackingAsyncObjectProperty<SimpleTestObj> objectProp = new TrackingAsyncObjectProperty<>(this);
        private final TrackingAsyncObjectProperty<SimpleTestObj> derivedObjectProp =
            new TrackingAsyncObjectProperty<>(this);
        private final TrackingAsyncListProperty<SimpleTestObj> listProp = new TrackingAsyncListProperty<>(this);
        private final TrackingAsyncListProperty<SimpleTestObj> polymorphicListProp =
            new TrackingAsyncListProperty<>(this);
        private final UUID id;

        ComplexTestObj(UUID id) {
            this.id = id;
            listProp.set(FXAsyncCollections.observableArrayList());
            polymorphicListProp.set(FXAsyncCollections.observableArrayList());
        }

        @SuppressWarnings("unused")
        public ComplexTestObj(CompositeDeserializationContext context) {
            this(UUID.fromString(context.readString("id")));
            PropertySerializationHelper.readObject(context, objectProp, SimpleTestObj.class);
            PropertySerializationHelper.readPolymorphicObject(
                context, derivedObjectProp, SimpleTestObj.class, SimpleTestObj.class, DerivedTestObj.class);
            PropertySerializationHelper.readList(context, listProp, SimpleTestObj.class);
            PropertySerializationHelper.readPolymorphicList(
                context, polymorphicListProp, SimpleTestObj.class, SimpleTestObj.class, DerivedTestObj.class);
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public void serialize(CompositeSerializationContext context) {
            context.writeString("id", id.toString());
            PropertySerializationHelper.writeObject(context, objectProp);
            PropertySerializationHelper.writePolymorphicObject(context, derivedObjectProp);
            PropertySerializationHelper.writeList(context, listProp, SimpleTestObj.class);
            PropertySerializationHelper.writePolymorphicList(context, polymorphicListProp, SimpleTestObj.class);
        }

        static ComplexTestObj createRandom(UUID id) {
            ComplexTestObj res = new ComplexTestObj(id);

            id = UUID.randomUUID();
            SimpleTestObj obj0 = SimpleTestObj.createRandom(id);
            res.objectProp.init(obj0, obj0);

            id = UUID.randomUUID();
            res.derivedObjectProp.init(DerivedTestObj.createRandom(id), DerivedTestObj.createRandom(id));

            res.listProp.add(SimpleTestObj.createRandom(UUID.randomUUID()));
            res.listProp.add(SimpleTestObj.createRandom(UUID.randomUUID()));
            res.listProp.add(SimpleTestObj.createRandom(UUID.randomUUID()));
            res.listProp.clean();

            res.polymorphicListProp.add(SimpleTestObj.createRandom(UUID.randomUUID()));
            res.polymorphicListProp.add(DerivedTestObj.createRandom(UUID.randomUUID()));
            res.polymorphicListProp.add(DerivedTestObj.createRandom(UUID.randomUUID()));

            return res;
        }

        static void assertEquals(ComplexTestObj obj0, ComplexTestObj obj1) {
            SimpleTestObj.assertEquals(obj0.objectProp.get(), obj1.objectProp.get());
            DerivedTestObj.assertEquals(
                (DerivedTestObj)obj0.derivedObjectProp.get(), (DerivedTestObj)obj1.derivedObjectProp.get());

            SimpleTestObj.assertEquals(obj0.listProp.get(0), obj1.listProp.get(0));
            SimpleTestObj.assertEquals(obj0.listProp.get(1), obj1.listProp.get(1));
            SimpleTestObj.assertEquals(obj0.listProp.get(2), obj1.listProp.get(2));

            SimpleTestObj.assertEquals(obj0.polymorphicListProp.get(0), obj1.polymorphicListProp.get(0));
            DerivedTestObj.assertEquals(
                (DerivedTestObj)obj0.polymorphicListProp.get(1), (DerivedTestObj)obj1.polymorphicListProp.get(1));
            DerivedTestObj.assertEquals(
                (DerivedTestObj)obj0.polymorphicListProp.get(2), (DerivedTestObj)obj1.polymorphicListProp.get(2));
        }
    }

    private static <T extends CompositeSerializable> String serialize(T obj, boolean internals) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            JsonSerializer serializer = new JsonSerializer();
            serializer.setPrettyPrint(true);
            serializer.setPreserveReferences(true);
            serializer.serialize(obj, new ProjectSerializationOptions(internals), stream);
            return stream.toString();
        } catch (IOException ex) {
            Assertions.fail(ex);
            return null;
        }
    }

    private static <T extends CompositeSerializable> T deserialize(String document, Class<T> type, boolean internals) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(document.getBytes())) {
            JsonSerializer serializer = new JsonSerializer();
            serializer.setPreserveReferences(true);
            return serializer.deserialize(stream, type, new ProjectSerializationOptions(internals));
        } catch (IOException ex) {
            Assertions.fail(ex);
            return null;
        }
    }

    @Test
    void Simple_TrackingProperties_Retain_Internal_State() {
        SimpleTestObj obj0 = SimpleTestObj.createRandom(UUID.randomUUID());
        String document = serialize(obj0, true);
        SimpleTestObj obj1 = deserialize(document, SimpleTestObj.class, true);
        SimpleTestObj.assertEquals(obj0, obj1);
    }

    @Test
    void Derived_Simple_TrackingProperties_Retain_Internal_State() {
        DerivedTestObj obj0 = DerivedTestObj.createRandom(UUID.randomUUID());
        String document = serialize(obj0, true);
        DerivedTestObj obj1 = deserialize(document, DerivedTestObj.class, true);
        DerivedTestObj.assertEquals(obj0, obj1);
    }

    @Test
    void Complex_TrackingProperties_Retain_Internal_State() {
        ComplexTestObj obj0 = ComplexTestObj.createRandom(UUID.randomUUID());
        String document = serialize(obj0, true);
        ComplexTestObj obj1 = deserialize(document, ComplexTestObj.class, true);
        ComplexTestObj.assertEquals(obj0, obj1);
    }

    @Test
    void Project_Is_Equal_When_Serialized_And_Deserialized() {
        OffsetDateTime date = OffsetDateTime.parse("2019-01-01T00:00:00Z");

        Project project = new Project();
        project.nameProperty().set("Project0");
        asMutable(project.creationDateProperty()).set(date);
        asMutable(project.lastModifiedDateProperty()).set(date);

        Mission mission = new Mission();
        mission.nameProperty().set("Mission0");
        project.missionsProperty().add(mission);
        mission.getPlaceables().add(new ExtrudedPolygonGoal());

        mission = new Mission();
        mission.nameProperty().set("Mission1");
        project.missionsProperty().add(mission);

        String document = serialize(project, true);
        Project deserializedProject = deserialize(document, Project.class, true);

        assertEquals(project, deserializedProject);
    }

    @Test
    void ProjectSnapshot_Is_Equal_When_Serialized_And_Deserialized() {
        OffsetDateTime date = OffsetDateTime.parse("2019-01-01T00:00:00Z");

        Project project = new Project();
        project.nameProperty().set("Project0");
        asMutable(project.creationDateProperty()).set(date);
        asMutable(project.lastModifiedDateProperty()).set(date);

        Mission mission = new Mission();
        mission.nameProperty().set("Mission0");
        project.missionsProperty().add(mission);
        mission.getPlaceables().add(new ExtrudedPolygonGoal());

        mission = new Mission();
        mission.nameProperty().set("Mission1");
        project.missionsProperty().add(mission);

        ProjectSnapshot projectSnapshot = new ProjectSnapshot(project);

        String document = serialize(projectSnapshot, true);
        ProjectSnapshot deserializedProject = deserialize(document, ProjectSnapshot.class, true);

        assertEquals(projectSnapshot, deserializedProject);
    }

}
