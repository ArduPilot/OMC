/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.intel.missioncontrol.airspaces.AirspaceProvider;
import com.intel.missioncontrol.common.Expect;
import eu.mavinci.core.licence.ILicenceManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.beans.property.AsyncSetProperty;
import org.asyncfx.beans.property.serialization.AsyncPropertyTypeAdapterFactory;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.collections.LockedSet;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.atteo.classindex.ClassFilter;
import org.atteo.classindex.ClassIndex;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.hildan.fxgson.FxGson;
import org.hildan.fxgson.factories.JavaFxPropertyTypeAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the settings file and maps the content to section classes (i.e. classes that are annotated with the Section
 * annotation). These section classes can be used as model classes within the MVVM pattern. When a property on a section
 * class changes (for example, because it is set via the user interface), SettingsManager will automatically update the
 * settings file on disk with the new values.
 */
public class SettingsManager implements ISettingsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsManager.class);

    @SuppressWarnings("unchecked")
    private static final Class<? extends ISettings>[] settingsClasses =
        Lists.newArrayList(
                ClassFilter.only()
                    .satisfying(ISettings.class::isAssignableFrom)
                    .from(ClassIndex.getAnnotated(SettingsMetadata.class)))
            .toArray(new Class[0]);

    private static final Gson gson =
        FxGson.coreBuilder()
            .registerTypeAdapterFactory(new JavaFxPropertyTypeAdapterFactory())
            .registerTypeAdapterFactory(new AsyncPropertyTypeAdapterFactory())
            .registerTypeAdapter(
                AsyncObservableList.class,
                (InstanceCreator<AsyncObservableList<?>>)type -> FXAsyncCollections.observableArrayList())
            .registerTypeAdapter(
                AsyncObservableSet.class,
                (InstanceCreator<AsyncObservableSet<?>>)type -> FXAsyncCollections.observableArraySet())
            .registerTypeHierarchyAdapter(
                Path.class,
                new TypeAdapter<Path>() {
                    @Override
                    public void write(JsonWriter jsonWriter, Path path) throws IOException {
                        if (path == null) {
                            jsonWriter.nullValue();
                        } else {
                            jsonWriter.value(path.toString());
                        }
                    }

                    @Override
                    public Path read(JsonReader jsonReader) throws IOException {
                        try {
                            String value = jsonReader.nextString();
                            return (value == null || value.isEmpty()) ? null : Paths.get(value);
                        } catch (RuntimeException e) {
                            jsonReader.skipValue();
                            return null;
                        }
                    }
                })
            .setExclusionStrategies()
            .serializeNulls()
            .create();

    private final Object mutex = new Object();
    private final Object saveFileMutex = new Object();
    private final List<ISettings> settingsInstances = new ArrayList<>();
    private final Injector injector;
    private final Class<?>[] simpleTypes;
    private final ILicenceManager licenceManager;

    private boolean initialized;
    private File settingsFile;
    private Future saveRequestFuture;
    private boolean saveRequestValid;

    public SettingsManager(Injector injector, Path file, Class<?>[] simpleTypes) {
        this.injector = injector;
        this.settingsFile = file.toFile();
        this.simpleTypes = simpleTypes;
        this.licenceManager = injector.getInstance(ILicenceManager.class);
    }

    private void initialize() {
        settingsInstances.addAll(createSettings(injector, SettingsMetadata.Scope.APPLICATION));

        JsonObject jsonObject = loadJsonFromFile(settingsFile.toPath());
        if (jsonObject != null) {
            populateSettingsFromJson(settingsInstances, jsonObject);
        }

        for (ISettings instance : settingsInstances) {
            instance.onLoaded();
        }

        for (ISettings instance : settingsInstances) {
            addListeners(instance);
        }

        if (getSection(GeneralSettings.class).getOperationLevel() != OperationLevel.DEBUG) {
            getSection(AirspacesProvidersSettings.class).airspaceProviderProperty().set(AirspaceProvider.AIRMAP2);
        }
    }

    public static Class<? extends ISettings>[] getSettingsClasses() {
        return settingsClasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSection(final Class<T> sectionType) {
        synchronized (mutex) {
            if (!initialized) {
                initialized = true;
                initialize();
            }
        }

        for (Object instance : settingsInstances) {
            if (instance != null && instance.getClass() == sectionType) {
                return (T)instance;
            }
        }

        throw new IllegalArgumentException("Unknown settings section: " + sectionType.getName() + " in file " + settingsFile.getAbsolutePath());
    }

    private JsonObject loadJsonFromFile(Path file) {
        boolean settingsFileExists = Files.exists(file);
        JsonObject jsonObject = null;

        if (settingsFileExists) {
            try (InputStream inputStream = new FileInputStream(this.settingsFile);
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                jsonObject = gson.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return jsonObject;
    }

    private List<ISettings> createSettings(Injector injector, SettingsMetadata.Scope scope) {
        List<ISettings> settingsList = new ArrayList<>();
        for (Class<?> section : settingsClasses) {
            SettingsMetadata metadata = section.getAnnotation(SettingsMetadata.class);
            if (metadata.scope() != scope) {
                continue;
            }

            Optional<Constructor<?>> injectingConstructor =
                Arrays.stream(section.getConstructors()).filter(c -> c.isAnnotationPresent(Inject.class)).findFirst();

            ISettings instance;
            try {
                if (injectingConstructor.isPresent()) {
                    Object[] params =
                        Arrays.stream(injectingConstructor.get().getParameterTypes())
                            .map(injector::getInstance)
                            .toArray();
                    instance = (ISettings)injectingConstructor.get().newInstance(params);
                } else {
                    instance = (ISettings)section.getConstructor().newInstance();
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            settingsList.add(instance);
        }

        return settingsList;
    }

    private void populateSettingsFromJson(List<ISettings> settings, JsonObject jsonObject) {
        for (ISettings instance : settings) {
            Class<?> instanceClass = instance.getClass();
            String sectionName = instanceClass.getAnnotation(SettingsMetadata.class).section();
            if (jsonObject != null && jsonObject.has(sectionName)) {
                try {
                    copyObjectFields(gson.fromJson(jsonObject.get(sectionName), instanceClass), instance);
                } catch (Exception e) {
                    // If a section can't be parsed, just skip it.
                    LOGGER.warn("cant load settings section:" + sectionName, e);
                }
            }
        }
    }

    private void saveToFile(@UnderInitialization SettingsManager this) {
        File dir = settingsFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (OutputStream outputStream = new FileOutputStream(settingsFile)) {
            JsonElement[] elements = settingsInstances.stream().map(gson::toJsonTree).toArray(JsonElement[]::new);

            try (Writer streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                JsonWriter writer = new JsonWriter(streamWriter);
                writer.setIndent("\t");
                writer.beginObject();
                writer.name("@comment");
                writer.value(licenceManager.getExportHeaderCore());

                for (int i = 0; i < elements.length; ++i) {
                    SettingsMetadata annotation =
                        settingsInstances.get(i).getClass().getAnnotation(SettingsMetadata.class);
                    Expect.notNull(annotation, "annotation");
                    writer.name(annotation.section());
                    gson.toJson(elements[i], writer);
                }

                writer.endObject();
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Cannot save settings to file %s. %s", settingsFile, e.getMessage()), e);
        }
    }

    private void requestSaveToFile() {
        synchronized (mutex) {
            if (saveRequestFuture != null) {
                saveRequestValid = false;
            } else {
                saveRequestValid = true;
                saveRequestFuture =
                    Dispatcher.background()
                        .runLaterAsync(this::onSaveRequestElapsed, Duration.ofMillis(500), Duration.ofMillis(250));
            }
        }
    }

    private void onSaveRequestElapsed() {
        boolean wasValid;

        synchronized (mutex) {
            wasValid = saveRequestValid;

            if (saveRequestValid) {
                saveRequestFuture.cancel(false);
                saveRequestFuture = null;
            } else {
                saveRequestValid = true;
            }
        }

        if (wasValid) {
            synchronized (saveFileMutex) {
                saveToFile();
            }
        }
    }

    private void addListeners(@UnderInitialization SettingsManager this, Object instance) {
        if (instance == null) {
            return;
        }

        try {
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                final int modifiers = field.getModifiers();
                if (Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers) || field.isSynthetic()) {
                    continue;
                }

                Class<?> fieldType = field.getType();
                boolean accessible = field.canAccess(instance);
                if (!accessible) {
                    field.setAccessible(true);
                }

                if (AsyncProperty.class.isAssignableFrom(fieldType)) {
                    AsyncProperty<?> property = (AsyncProperty<?>)field.get(instance);
                    if (property != null) {
                        property.addListener((observable, oldValue, newValue) -> requestSaveToFile());

                        if (property instanceof AsyncListProperty) {
                            AsyncObservableList<?> observableList = ((AsyncListProperty<?>)property).get();
                            observableList.addListener((ListChangeListener<Object>)change -> requestSaveToFile());
                        }

                        if (property instanceof AsyncSetProperty) {
                            AsyncObservableSet<?> observableSet = ((AsyncSetProperty<?>)property).get();
                            observableSet.addListener((SetChangeListener<Object>)change -> requestSaveToFile());
                        }
                    }
                } else if (Property.class.isAssignableFrom(fieldType)) {
                    Property<?> property = (Property<?>)field.get(instance);
                    if (property != null) {
                        property.addListener((observable, oldValue, newValue) -> requestSaveToFile());

                        if (property instanceof ListProperty) {
                            ObservableList<?> observableList = ((ListProperty<?>)property).get();
                            observableList.addListener((ListChangeListener<Object>)change -> requestSaveToFile());
                        }

                        if (property instanceof SetProperty) {
                            ObservableSet<?> observableSet = ((SetProperty<?>)property).get();
                            observableSet.addListener((SetChangeListener<Object>)change -> requestSaveToFile());
                        }

                        if (property instanceof MapProperty) {
                            ObservableMap<?, ?> observableMap = ((MapProperty<?, ?>)property).get();
                            observableMap.addListener((MapChangeListener<Object, Object>)change -> requestSaveToFile());
                        }
                    }
                } else {
                    Object innerInstance = field.get(instance);
                    if (innerInstance != null) {
                        addListeners(innerInstance);
                    }
                }

                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> void copyObjectFields(final T source, final T target) {
        for (Field field : source.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                    && !Modifier.isTransient(field.getModifiers())
                    && !field.isSynthetic()) {
                copyField(field, source, target);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void copyField(final Field field, final T source, final T target) {
        final Class<?> fieldType = field.getType();
        final boolean accessible = field.canAccess(target);
        if (!accessible) {
            field.setAccessible(true);
        }

        try {
            if (Property.class.isAssignableFrom(fieldType)) {
                Property sourceProperty = (Property)field.get(source);
                Property targetProperty = (Property)field.get(target);
                Object sourceValue;
                if (sourceProperty != null
                        && targetProperty != null
                        && (sourceValue = sourceProperty.getValue()) != null) {
                    if (targetProperty instanceof ListProperty) {
                        ((ListProperty)targetProperty).clear();
                        ((ListProperty)targetProperty).addAll((ObservableList)sourceValue);
                    } else if (targetProperty instanceof SetProperty) {
                        ((SetProperty)targetProperty).clear();
                        ((SetProperty)targetProperty).addAll((ObservableSet)sourceValue);
                    } else if (targetProperty instanceof MapProperty) {
                        ((MapProperty)targetProperty).clear();
                        ((MapProperty)targetProperty).putAll((ObservableMap)sourceValue);
                    } else {
                        targetProperty.setValue(sourceValue);
                    }
                }
            } else if (AsyncProperty.class.isAssignableFrom(fieldType)) {
                AsyncProperty sourceProperty = (AsyncProperty)field.get(source);
                AsyncProperty targetProperty = (AsyncProperty)field.get(target);
                Object sourceValue;
                if (sourceProperty != null
                        && targetProperty != null
                        && (sourceValue = sourceProperty.getValue()) != null) {
                    if (targetProperty instanceof AsyncListProperty) {
                        ((AsyncListProperty)targetProperty).clear();

                        try (LockedList list = ((AsyncObservableList)sourceValue).lock()) {
                            for (Object item : list) {
                                try {
                                    Object newInst = cloneObjectValue(item);

                                    ((AsyncListProperty)targetProperty).add(newInst);
                                } catch (ReflectiveOperationException ex) {
                                    LOGGER.warn(
                                        "Error loading list item of type "
                                            + source.getClass().getName()
                                            + ", the item will not be added to "
                                            + targetProperty.getName(),
                                        ex);
                                }
                            }
                        }
                    } else if (targetProperty instanceof AsyncSetProperty) {
                        ((AsyncSetProperty)targetProperty).clear();

                        try (LockedSet set = ((AsyncObservableSet)sourceValue).lock()) {
                            for (Object item : set) {
                                try {
                                    Object newInst = cloneObjectValue(item);
                                    ((AsyncSetProperty)targetProperty).add(newInst);
                                } catch (ReflectiveOperationException ex) {
                                    LOGGER.warn(
                                        "Error loading set item of type "
                                            + source.getClass().getName()
                                            + ", the item will not be added to "
                                            + targetProperty.getName(),
                                        ex);
                                }
                            }
                        }
                    } else if (targetProperty instanceof AsyncObjectProperty) {
                        try {
                            Object newInst = cloneObjectValue(sourceValue);
                            targetProperty.setValue(newInst);
                        } catch (ReflectiveOperationException ex) {
                            LOGGER.warn(
                                "Error loading "
                                    + source.getClass().getName()
                                    + ", skipping property "
                                    + targetProperty.getName(),
                                ex);
                        }
                    } else {
                        targetProperty.setValue(sourceValue);
                    }
                }
            } else {
                field.set(target, field.get(source));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (!accessible) {
            field.setAccessible(false);
        }
    }

    private Object cloneObjectValue(Object source) throws ReflectiveOperationException {
        Class<?> cls = source.getClass();
        if (cls.isEnum() || cls.isPrimitive() || Primitives.isWrapperType(cls)) {
            return source;
        }

        for (Class<?> simpleType : simpleTypes) {
            if (simpleType.isAssignableFrom(cls)) {
                return source;
            }
        }

        Serializable serializableAnnotation = cls.getAnnotation(Serializable.class);
        if (serializableAnnotation == null) {
            throw new IllegalArgumentException(
                "The class "
                    + cls.getName()
                    + " cannot be serialized because it is neither a simple type, nor annotated with "
                    + Serializable.class.getName()
                    + ".");
        }

        Constructor<?> ctor;
        try {
            ctor = cls.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(
                "The class "
                    + cls.getName()
                    + " does not have a no-arg constructor. "
                    + "Add a private no-arg constructor to enable serialization and deserialization.");
        }

        ctor.setAccessible(true);
        Object newInst = ctor.newInstance();
        copyObjectFields(source, newInst);

        for (Method method : cls.getDeclaredMethods()) {
            PostDeserialize postDeserializeAnnotation = method.getAnnotation(PostDeserialize.class);
            if (postDeserializeAnnotation != null) {
                method.setAccessible(true);
                method.invoke(newInst);
                break;
            }
        }

        return newInst;
    }

}
