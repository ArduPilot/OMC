/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.serialization;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;
import org.asyncfx.collections.ArraySet;
import org.jetbrains.annotations.Nullable;

/** A JSON serializer that supports value-base and reference-based serialization. */
public class JsonSerializer implements Serializer {

    private boolean prettyPrint;
    private boolean preserveReferences;

    public JsonSerializer() {}

    /** Adds whitespace formatting to the generated JSON document. */
    public void setPrettyPrint(boolean value) {
        this.prettyPrint = value;
    }

    /**
     * Preserves object references in the JSON file. If an object instance is referenced multiple times, it will only be
     * serialized once, while all other occurrences will refer to the single serialized instance. When the JSON document
     * is deserialized, all occurrences of the reference will point to the same object instance.
     */
    public void setPreserveReferences(boolean value) {
        this.preserveReferences = value;
    }

    @Override
    public void serialize(CompositeSerializable value, OutputStream stream) throws IOException {
        serialize(value, null, stream);
    }

    @Override
    @SuppressWarnings("Convert2Lambda")
    public void serialize(CompositeSerializable value, SerializationOptions options, OutputStream stream)
            throws IOException {
        try (JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(stream)))) {
            PrimitiveSerializationContext primitiveContext =
                new PrimitiveSerializationContext() {
                    @Override
                    public SerializationOptions getOptions() {
                        return options;
                    }

                    @Override
                    public void write(String value) {
                        try {
                            writer.value(value);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };

            CompositeSerializationContext context =
                new CompositeSerializationContext() {
                    private final Map<Class<?>, WriteFunc> writeFuncs = new HashMap<>();
                    private final Map<CompositeSerializable, Integer> knownObjects = new HashMap<>();
                    private int currentObjectId;

                    {
                        writeFuncs.put(Boolean.class, (WriteFunc<Boolean>)writer::value);
                        writeFuncs.put(Byte.class, (WriteFunc<Byte>)writer::value);
                        writeFuncs.put(Short.class, (WriteFunc<Short>)writer::value);
                        writeFuncs.put(Integer.class, (WriteFunc<Integer>)writer::value);
                        writeFuncs.put(Long.class, (WriteFunc<Long>)writer::value);
                        writeFuncs.put(Float.class, (WriteFunc<Float>)writer::value);
                        writeFuncs.put(Double.class, (WriteFunc<Double>)writer::value);
                        writeFuncs.put(String.class, (WriteFunc<String>)writer::value);
                        writeFuncs.put(
                            Enum.class, (WriteFunc<Enum>)value -> writer.value(value != null ? value.name() : null));
                        writeFuncs.put(
                            OffsetDateTime.class,
                            new WriteFunc<OffsetDateTime>() {
                                @Override
                                public void write(OffsetDateTime value) throws IOException {
                                    writer.value(
                                        value != null ? value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
                                }
                            });
                    }

                    @Override
                    public SerializationOptions getOptions() {
                        return options;
                    }

                    @Override
                    public void writeBoolean(String name, boolean value) {
                        try {
                            writer.name(name);
                            writer.value(value);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public void writeByte(String name, byte value) {
                        try {
                            writer.name(name);
                            writer.value(value);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public void writeShort(String name, short value) {
                        try {
                            writer.name(name);
                            writer.value(value);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public void writeInteger(String name, int value) {
                        try {
                            writer.name(name);
                            writer.value(value);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public void writeLong(String name, long value) {
                        try {
                            writer.name(name);
                            writer.value(value);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public void writeFloat(String name, float value) {
                        try {
                            writer.name(name);
                            writer.value(value);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public void writeDouble(String name, double value) {
                        try {
                            writer.name(name);
                            writer.value(value);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public void writeString(String name, String value) {
                        try {
                            writer.name(name);

                            if (value == null) {
                                writer.nullValue();
                            } else {
                                writer.value(value);
                            }
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void writeOffsetDateTime(String name, OffsetDateTime value) {
                        try {
                            writer.name(name);
                            writeFuncs.get(OffsetDateTime.class).write(value);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public void writeBytes(String name, byte[] value) {
                        writeString(name, Base64.getMimeEncoder().encodeToString(value));
                    }

                    @Override
                    public <E extends Enum<E>> void writeEnum(String name, E value) {
                        writeString(name, value != null ? value.name() : null);
                    }

                    @Override
                    public void writeObject(String name, Serializable value) {
                        try {
                            writer.name(name);
                            writeObjectValue(value, false);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public void writePolymorphicObject(String name, Serializable value) {
                        try {
                            writer.name(name);
                            writeObjectValue(value, true);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public <T> void writeCollection(String name, Collection<T> value, Class<? super T> type) {
                        try {
                            writer.name(name);
                            writeCollectionValue(value, false);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @Override
                    public <T> void writePolymorphicCollection(
                            String name, Collection<T> value, Class<? super T> type) {
                        try {
                            writer.name(name);
                            writeCollectionValue(value, true);
                        } catch (IOException ex) {
                            throw serializationFailed(name, ex);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    private <T> void writeCollectionValue(Collection<T> value, boolean writeTypeInfo)
                            throws IOException {
                        if (value == null) {
                            writer.nullValue();
                            return;
                        }

                        writer.beginArray();

                        for (T item : value) {
                            if (item == null) {
                                writer.nullValue();
                            } else {
                                Class<?> itemType = item.getClass();
                                WriteFunc writeFunc = writeFuncs.get(itemType);
                                if (writeFunc != null) {
                                    writeFunc.write(item);
                                } else if (item instanceof Serializable) {
                                    writeObjectValue((Serializable)item, writeTypeInfo);
                                } else if (item instanceof Collection) {
                                    writeCollectionValue((Collection)item, writeTypeInfo);
                                } else {
                                    throw new SerializationException("Unexpected type: " + itemType.getName());
                                }
                            }
                        }

                        writer.endArray();
                    }

                    private <T extends Serializable> void writeObjectValue(T value, boolean writeTypeInfo)
                            throws IOException {
                        if (value == null) {
                            writer.nullValue();
                            return;
                        }

                        if (value instanceof PrimitiveSerializable) {
                            try {
                                ((PrimitiveSerializable)value).serialize(primitiveContext);
                            } catch (RuntimeException ex) {
                                throw new IOException(ex.getCause());
                            }
                        } else {
                            CompositeSerializable serializable = (CompositeSerializable)value;

                            writer.beginObject();

                            boolean serializeObject = true;
                            if (preserveReferences) {
                                Integer id = knownObjects.get(value);
                                if (id != null) {
                                    writer.name("$ref");
                                    writer.value(id.toString());
                                    serializeObject = false;
                                } else {
                                    id = currentObjectId++;
                                    knownObjects.put(serializable, id);
                                    writer.name("$id");
                                    writer.value(id.toString());
                                }
                            }

                            if (serializeObject) {
                                if (writeTypeInfo) {
                                    writer.name("$type");
                                    writer.value(value.getClass().getSimpleName());
                                }

                                serializable.serialize(this);
                            }

                            writer.endObject();
                        }
                    }
                };

            if (prettyPrint) {
                writer.setIndent("    ");
            }

            writer.beginObject();
            value.serialize(context);
            writer.endObject();
        }
    }

    @Override
    public <T extends CompositeSerializable> T deserialize(InputStream stream, Class<T> type) throws IOException {
        return deserialize(stream, type, null);
    }

    @Override
    @SuppressWarnings("Convert2Lambda")
    public <T extends CompositeSerializable> T deserialize(
            InputStream stream, Class<T> type, SerializationOptions options) throws IOException {
        try (JsonReader reader = new JsonReader(new BufferedReader((new InputStreamReader(stream))))) {
            JsonObject root = new JsonObject();
            readJsonObject(root, reader);

            Stack<JsonObject> scope = new Stack<>();
            scope.push(root);

            CompositeDeserializationContext context =
                new CompositeDeserializationContext() {
                    private final Map<Class<?>, ReadFunc> readFuncs = new HashMap<>();
                    private final Map<String, CompositeSerializable> knownObjects = new HashMap<>();

                    {
                        readFuncs.put(Boolean.class, entity -> castEntity(entity, JsonBoolean.class).value);
                        readFuncs.put(Byte.class, entity -> Byte.parseByte(castEntity(entity, JsonNumber.class).value));
                        readFuncs.put(
                            Short.class, entity -> Short.parseShort(castEntity(entity, JsonNumber.class).value));
                        readFuncs.put(
                            Integer.class, entity -> Integer.parseInt(castEntity(entity, JsonNumber.class).value));
                        readFuncs.put(Long.class, entity -> Long.parseLong(castEntity(entity, JsonNumber.class).value));
                        readFuncs.put(
                            Float.class, entity -> Float.parseFloat(castEntity(entity, JsonNumber.class).value));
                        readFuncs.put(
                            Double.class, entity -> Double.parseDouble(castEntity(entity, JsonNumber.class).value));
                        readFuncs.put(
                            String.class,
                            entity -> entity instanceof JsonNull ? null : castEntity(entity, JsonString.class).value);
                        readFuncs.put(
                            OffsetDateTime.class,
                            new ReadFunc<OffsetDateTime>() {
                                @Override
                                public OffsetDateTime read(JsonEntity entity) {
                                    if (entity instanceof JsonNull) {
                                        return null;
                                    }

                                    String value = castEntity(entity, JsonString.class).value;
                                    return value != null
                                        ? OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                        : null;
                                }
                            });
                    }

                    @SuppressWarnings("unchecked")
                    private <U extends JsonEntity> U castEntity(JsonEntity entity, Class<U> type) {
                        if (type.isInstance(entity)) {
                            return (U)entity;
                        }

                        throw new SerializationException(
                            "Expected "
                                + type.getSimpleName()
                                + ", but encountered "
                                + entity.getClass().getSimpleName()
                                + ".");
                    }

                    private JsonEntity optionalFieldValue(
                            String name, Class<? extends JsonEntity> type, boolean maybeNull) {
                        JsonObject current = scope.peek();
                        for (JsonPair field : current.fields) {
                            if (!field.name.equals(name)) {
                                continue;
                            }

                            if ((maybeNull && field.value instanceof JsonNull) || field.value.getClass() == type) {
                                field.consumed = true;
                                return field.value;
                            }

                            throw new SerializationException(
                                "Unexpected type: "
                                    + name
                                    + " (expected = "
                                    + type.getSimpleName()
                                    + ", actual = "
                                    + field.value.getClass().getSimpleName()
                                    + ").");
                        }

                        return null;
                    }

                    private JsonEntity requiredFieldValue(
                            String name, Class<? extends JsonEntity> type, boolean maybeNull) {
                        JsonEntity field = optionalFieldValue(name, type, maybeNull);
                        if (field == null) {
                            throw new SerializationException("A required field was not found: " + name);
                        }

                        return field;
                    }

                    @Override
                    public SerializationOptions getOptions() {
                        return options;
                    }

                    @Override
                    public boolean readBoolean(String name) {
                        return ((JsonBoolean)requiredFieldValue(name, JsonBoolean.class, false)).value;
                    }

                    @Override
                    public boolean readBoolean(String name, boolean fallbackValue) {
                        JsonBoolean value = (JsonBoolean)optionalFieldValue(name, JsonBoolean.class, false);
                        return value != null ? value.value : fallbackValue;
                    }

                    @Override
                    public byte readByte(String name) {
                        return Byte.parseByte(((JsonNumber)requiredFieldValue(name, JsonNumber.class, false)).value);
                    }

                    @Override
                    public byte readByte(String name, byte fallbackValue) {
                        JsonNumber value = (JsonNumber)optionalFieldValue(name, JsonNumber.class, false);
                        return value != null ? Byte.parseByte(value.value) : fallbackValue;
                    }

                    @Override
                    public short readShort(String name) {
                        return Short.parseShort(((JsonNumber)requiredFieldValue(name, JsonNumber.class, false)).value);
                    }

                    @Override
                    public short readShort(String name, short fallbackValue) {
                        JsonNumber value = (JsonNumber)optionalFieldValue(name, JsonNumber.class, false);
                        return value != null ? Short.parseShort(value.value) : fallbackValue;
                    }

                    @Override
                    public int readInteger(String name) {
                        return Integer.parseInt(((JsonNumber)requiredFieldValue(name, JsonNumber.class, false)).value);
                    }

                    @Override
                    public int readInteger(String name, int fallbackValue) {
                        JsonNumber value = (JsonNumber)optionalFieldValue(name, JsonNumber.class, false);
                        return value != null ? Integer.parseInt(value.value) : fallbackValue;
                    }

                    @Override
                    public long readLong(String name) {
                        return Long.parseLong(((JsonNumber)requiredFieldValue(name, JsonNumber.class, false)).value);
                    }

                    @Override
                    public long readLong(String name, long fallbackValue) {
                        JsonNumber value = (JsonNumber)optionalFieldValue(name, JsonNumber.class, false);
                        return value != null ? Long.parseLong(value.value) : fallbackValue;
                    }

                    @Override
                    public float readFloat(String name) {
                        return Float.parseFloat(((JsonNumber)requiredFieldValue(name, JsonNumber.class, false)).value);
                    }

                    @Override
                    public float readFloat(String name, float fallbackValue) {
                        JsonNumber value = (JsonNumber)optionalFieldValue(name, JsonNumber.class, false);
                        return value != null ? Float.parseFloat(value.value) : fallbackValue;
                    }

                    @Override
                    public double readDouble(String name) {
                        return Double.parseDouble(
                            ((JsonNumber)requiredFieldValue(name, JsonNumber.class, false)).value);
                    }

                    @Override
                    public double readDouble(String name, double fallbackValue) {
                        JsonNumber value = (JsonNumber)optionalFieldValue(name, JsonNumber.class, false);
                        return value != null ? Double.parseDouble(value.value) : fallbackValue;
                    }

                    @Override
                    public String readString(String name) {
                        JsonEntity value = requiredFieldValue(name, JsonString.class, true);
                        return value instanceof JsonNull ? null : ((JsonString)value).value;
                    }

                    @Override
                    public String readString(String name, String fallbackValue) {
                        JsonEntity value = optionalFieldValue(name, JsonString.class, true);
                        return value != null
                            ? (value instanceof JsonNull ? null : ((JsonString)value).value)
                            : fallbackValue;
                    }

                    @Override
                    public OffsetDateTime readOffsetDateTime(String name) {
                        return (OffsetDateTime)
                            readFuncs.get(OffsetDateTime.class).read(requiredFieldValue(name, JsonString.class, true));
                    }

                    @Override
                    public OffsetDateTime readOffsetDateTime(String name, OffsetDateTime fallbackValue) {
                        JsonEntity value = optionalFieldValue(name, JsonString.class, true);
                        return value == null
                            ? fallbackValue
                            : (OffsetDateTime)readFuncs.get(OffsetDateTime.class).read(value);
                    }

                    @Override
                    public byte[] readBytes(String name) {
                        String value = readString(name);
                        return value != null ? Base64.getMimeDecoder().decode(value) : null;
                    }

                    @Override
                    @SuppressWarnings("StringEquality")
                    public byte[] readBytes(String name, byte[] fallbackValue) {
                        String fallback = "";
                        String value = readString(name, fallback);
                        if (value == fallback) {
                            return fallbackValue;
                        }

                        return value != null ? Base64.getMimeDecoder().decode(value) : null;
                    }

                    @Override
                    public <E extends Enum<E>> E readEnum(String name, Class<E> type) {
                        String value = readString(name);
                        return value != null ? Enum.valueOf(type, value) : null;
                    }

                    @Override
                    @SuppressWarnings("StringEquality")
                    public <E extends Enum<E>> E readEnum(String name, Class<E> type, E fallbackValue) {
                        String fallback = "";
                        String value = readString(name, fallback);
                        if (value == fallback) {
                            return fallbackValue;
                        }

                        return value != null ? Enum.valueOf(type, value) : null;
                    }

                    @Override
                    public <U extends Serializable> U readObject(String name, Class<U> type) {
                        return readObjectInternal(name, type, null, null, false);
                    }

                    @Override
                    public <U extends Serializable> U readObject(String name, Class<U> type, U fallbackValue) {
                        return readObjectInternal(name, type, null, fallbackValue, true);
                    }

                    @Override
                    @SafeVarargs
                    public final <U extends Serializable> U readPolymorphicObject(
                            String name, Class<U> targetType, Class<? extends U>... potentialTypes) {
                        return readObjectInternal(name, targetType, potentialTypes, null, false);
                    }

                    @Override
                    @SafeVarargs
                    public final <U extends Serializable> U readPolymorphicObject(
                            String name, Class<U> targetType, U fallbackValue, Class<? extends U>... potentialTypes) {
                        return readObjectInternal(name, targetType, potentialTypes, fallbackValue, false);
                    }

                    @Override
                    public <U> void readList(String name, List<? super U> list, Class<U> targetType) {
                        readCollectionInternal(name, list, ArrayList::new, targetType, null, false, null);
                    }

                    @Override
                    public <U> void readList(
                            String name, List<? super U> list, Class<U> targetType, List<U> fallbackValue) {
                        readCollectionInternal(name, list, ArrayList::new, targetType, fallbackValue, true, null);
                    }

                    @Override
                    @SafeVarargs
                    public final <U> void readPolymorphicList(
                            String name,
                            List<? super U> list,
                            Class<U> targetType,
                            Class<? extends U>... potentialTypes) {
                        readCollectionInternal(name, list, ArrayList::new, targetType, null, false, potentialTypes);
                    }

                    @Override
                    @SafeVarargs
                    public final <U> void readPolymorphicList(
                            String name,
                            List<? super U> list,
                            Class<U> targetType,
                            List<U> fallbackValue,
                            Class<? extends U>... potentialTypes) {
                        readCollectionInternal(
                            name, list, ArrayList::new, targetType, fallbackValue, true, potentialTypes);
                    }

                    @Override
                    public <U> void readSet(String name, Set<? super U> list, Class<U> targetType) {
                        readCollectionInternal(name, list, ArraySet::new, targetType, null, false, null);
                    }

                    @Override
                    public <U> void readSet(
                            String name, Set<? super U> list, Class<U> targetType, Set<U> fallbackValue) {
                        readCollectionInternal(name, list, ArraySet::new, targetType, fallbackValue, true, null);
                    }

                    @Override
                    @SafeVarargs
                    public final <U> void readPolymorphicSet(
                            String name,
                            Set<? super U> list,
                            Class<U> targetType,
                            Class<? extends U>... potentialTypes) {
                        readCollectionInternal(name, list, ArraySet::new, targetType, null, false, potentialTypes);
                    }

                    @Override
                    @SafeVarargs
                    public final <U> void readPolymorphicSet(
                            String name,
                            Set<? super U> list,
                            Class<U> targetType,
                            Set<U> fallbackValue,
                            Class<? extends U>... potentialTypes) {
                        readCollectionInternal(
                            name, list, ArraySet::new, targetType, fallbackValue, true, potentialTypes);
                    }

                    private <U extends Serializable> U readObjectInternal(
                            String name,
                            Class<U> targetType,
                            Class<? extends U>[] potentialTypes,
                            U fallbackValue,
                            boolean optional) {
                        if (PrimitiveSerializable.class.isAssignableFrom(targetType)) {
                            JsonEntity fieldValue =
                                optional
                                    ? optionalFieldValue(name, JsonString.class, true)
                                    : requiredFieldValue(name, JsonString.class, true);

                            if (fieldValue == null) {
                                return fallbackValue;
                            }

                            if (fieldValue instanceof JsonNull) {
                                return null;
                            }

                            return createPrimitiveObject(
                                new PrimitiveDeserializationContext() {
                                    @Override
                                    public SerializationOptions getOptions() {
                                        return options;
                                    }

                                    @Override
                                    public String read() {
                                        return ((JsonString)fieldValue).value;
                                    }
                                },
                                targetType);
                        }

                        JsonEntity fieldValue =
                            optional
                                ? optionalFieldValue(name, JsonObject.class, true)
                                : requiredFieldValue(name, JsonObject.class, true);

                        if (fieldValue == null) {
                            return fallbackValue;
                        }

                        if (fieldValue instanceof JsonNull) {
                            return null;
                        }

                        return readObjectValue(targetType, potentialTypes, (JsonObject)fieldValue);
                    }

                    @SuppressWarnings("unchecked")
                    private <U> U readObjectValue(
                            Class<U> targetType, Class<? extends U>[] potentialTypes, JsonObject jsonObject) {
                        if (preserveReferences) {
                            JsonEntity ref = optionalFieldValue("$ref", JsonString.class, false);
                            if (ref instanceof JsonString) {
                                String refValue = ((JsonString)ref).value;
                                CompositeSerializable knownObject = knownObjects.get(refValue);
                                if (knownObject == null) {
                                    throw new SerializationException("Object reference not found:" + refValue);
                                }

                                if (!targetType.isInstance(knownObject)) {
                                    throw new SerializationException(
                                        "Object reference to unexpected type: "
                                            + refValue
                                            + " (expected = "
                                            + targetType.getSimpleName()
                                            + ", actual = "
                                            + knownObject.getClass().getSimpleName()
                                            + ").");
                                }

                                return (U)knownObject;
                            }

                            scope.push(jsonObject);
                            U object = createObject(this, targetType, potentialTypes, jsonObject);
                            JsonEntity id = optionalFieldValue("$id", JsonString.class, false);
                            scope.pop();

                            if (id instanceof JsonString) {
                                knownObjects.put(((JsonString)id).value, (CompositeSerializable)object);
                            }

                            return object;
                        }

                        scope.push(jsonObject);
                        U object = createObject(this, targetType, potentialTypes, jsonObject);
                        scope.pop();
                        return object;
                    }

                    private <U> void readCollectionInternal(
                            String name,
                            Collection<? super U> collection,
                            Supplier<Collection<U>> createCollection,
                            Class<U> targetType,
                            Collection<U> fallbackValue,
                            boolean optional,
                            @Nullable Class<? extends U>[] potentialTypes) {
                        JsonEntity fieldValue =
                            optional
                                ? optionalFieldValue(name, JsonArray.class, true)
                                : requiredFieldValue(name, JsonArray.class, true);

                        if (fieldValue == null) {
                            collection.clear();
                            collection.addAll(fallbackValue);
                        } else if (fieldValue instanceof JsonNull) {
                            collection.clear();
                        } else if (fieldValue instanceof JsonArray) {
                            readCollectionValue(
                                (JsonArray)fieldValue, collection, createCollection, targetType, potentialTypes);
                        } else {
                            throw new SerializationException(
                                "Unexpected type: " + fieldValue.getClass().getSimpleName());
                        }
                    }

                    @SuppressWarnings("unchecked")
                    private <U> void readCollectionValue(
                            JsonArray array,
                            Collection<? super U> collection,
                            Supplier<Collection<U>> createCollection,
                            Class<U> targetType,
                            @Nullable Class<? extends U>[] potentialTypes) {
                        ReadFunc readFunc = readFuncs.get(targetType);

                        for (JsonEntity item : array.items) {
                            if (item instanceof JsonNull) {
                                collection.add(null);
                            } else if (item instanceof JsonArray) {
                                Collection<U> subCollection = createCollection.get();
                                readCollectionValue(
                                    (JsonArray)item, subCollection, createCollection, targetType, potentialTypes);
                                collection.add((U)subCollection);
                            } else if (readFunc != null) {
                                collection.add((U)readFunc.read(item));
                            } else if (targetType.isEnum()) {
                                String enumValue = (String)readFuncs.get(String.class).read(item);
                                collection.add(
                                    enumValue == null ? null : (U)Enum.valueOf((Class)targetType, enumValue));
                            } else if (PrimitiveSerializable.class.isAssignableFrom(targetType)) {
                                if (item instanceof JsonString) {
                                    collection.add(
                                        createPrimitiveObject(
                                            new PrimitiveDeserializationContext() {
                                                @Override
                                                public SerializationOptions getOptions() {
                                                    return options;
                                                }

                                                @Override
                                                public String read() {
                                                    return ((JsonString)item).value;
                                                }
                                            },
                                            targetType));
                                } else {
                                    throw new SerializationException(
                                        "Unexpected type: " + item.getClass().getSimpleName());
                                }
                            } else if (CompositeSerializable.class.isAssignableFrom(targetType)) {
                                if (item instanceof JsonObject) {
                                    scope.push((JsonObject)item);
                                    collection.add(readObjectValue(targetType, potentialTypes, (JsonObject)item));
                                    scope.pop();
                                } else {
                                    throw new SerializationException(
                                        "Unexpected type: " + item.getClass().getSimpleName());
                                }
                            }
                        }
                    }
                };

            return createObject(context, type, null, null);
        }
    }

    private static void readJsonObject(JsonObject object, JsonReader reader) throws IOException {
        reader.beginObject();

        while (reader.peek() != JsonToken.END_OBJECT) {
            String name = reader.nextName();

            switch (reader.peek()) {
            case BOOLEAN:
                object.fields.add(new JsonPair(name, new JsonBoolean(reader.nextBoolean())));
                break;
            case NUMBER:
                object.fields.add(new JsonPair(name, new JsonNumber(reader.nextString())));
                break;
            case STRING:
                object.fields.add(new JsonPair(name, new JsonString(reader.nextString())));
                break;
            case NULL:
                reader.nextNull();
                object.fields.add(new JsonPair(name, new JsonNull()));
                break;
            case BEGIN_OBJECT:
                JsonObject object2 = new JsonObject();
                readJsonObject(object2, reader);
                object.fields.add(new JsonPair(name, object2));
                break;
            case BEGIN_ARRAY:
                JsonArray array = new JsonArray();
                readJsonArray(array, reader);
                object.fields.add(new JsonPair(name, array));
                break;
            }
        }

        reader.endObject();
    }

    private static void readJsonArray(JsonArray array, JsonReader reader) throws IOException {
        reader.beginArray();

        while (reader.peek() != JsonToken.END_ARRAY) {
            switch (reader.peek()) {
            case BOOLEAN:
                array.items.add(new JsonBoolean(reader.nextBoolean()));
                break;
            case NUMBER:
                array.items.add(new JsonNumber(reader.nextString()));
                break;
            case STRING:
                array.items.add(new JsonString(reader.nextString()));
                break;
            case NULL:
                reader.nextNull();
                array.items.add(new JsonNull());
                break;
            case BEGIN_OBJECT:
                JsonObject object = new JsonObject();
                readJsonObject(object, reader);
                array.items.add(object);
                break;
            case BEGIN_ARRAY:
                JsonArray array2 = new JsonArray();
                readJsonArray(array2, reader);
                array.items.add(array2);
                break;
            }
        }

        reader.endArray();
    }

    private final HashMap<Class, Constructor> primitiveConstructors = new HashMap<>();
    private final HashMap<Class, Constructor> compositeConstructors = new HashMap<>();

    @SuppressWarnings("unchecked")
    private <T> T createPrimitiveObject(PrimitiveDeserializationContext context, Class<T> type) {
        T object;
        try {
            Constructor ctor = primitiveConstructors.get(type);
            if (ctor == null) {
                ctor = type.getConstructor(PrimitiveDeserializationContext.class);
                primitiveConstructors.put(type, ctor);
            }

            ctor.setAccessible(true);
            object = ((Constructor<T>)ctor).newInstance(context);
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new SerializationException(
                type.getName()
                    + " requires a public constructor with the signature "
                    + type.getSimpleName()
                    + "("
                    + PrimitiveDeserializationContext.class.getSimpleName()
                    + ") to be deserializable.");
        } catch (InvocationTargetException ex) {
            throw new SerializationException("Constructor of type " + type.getName() + " threw an exception.", ex);
        } catch (InstantiationException ex) {
            throw new SerializationException(type.getName() + " cannot be instantiated.", ex);
        }

        return object;
    }

    @SuppressWarnings("unchecked")
    private <T> T createObject(
            CompositeDeserializationContext context,
            Class<T> type,
            @Nullable Class<? extends T>[] potentialTypes,
            JsonObject jsonObject) {
        T object;
        try {
            String typeValue = null;
            if (jsonObject != null) {
                for (JsonPair pair : jsonObject.fields) {
                    if ("$type".equals(pair.name)) {
                        if (!(pair.value instanceof JsonString)) {
                            throw new SerializationException("'$type': unexpected value");
                        }

                        typeValue = ((JsonString)pair.value).value;
                        break;
                    }
                }
            }

            Class<? extends T> polymorphicType = null;
            if (typeValue != null) {
                if (type.getSimpleName().equals(typeValue)) {
                    polymorphicType = type;
                } else if (potentialTypes != null) {
                    for (Class<? extends T> potentialType : potentialTypes) {
                        if (potentialType != null && potentialType.getSimpleName().equals(typeValue)) {
                            polymorphicType = potentialType;
                            break;
                        }
                    }
                }

                if (polymorphicType == null) {
                    throw new SerializationException("'$type' was encountered, but no suitable type was found.");
                }
            } else {
                polymorphicType = type;
            }

            Constructor ctor = compositeConstructors.get(polymorphicType);
            if (ctor == null) {
                ctor = polymorphicType.getConstructor(CompositeDeserializationContext.class);
                compositeConstructors.put(polymorphicType, ctor);
            }

            ctor.setAccessible(true);
            object = ((Constructor<T>)ctor).newInstance(context);
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new SerializationException(
                type.getName()
                    + " requires a public constructor with the signature "
                    + type.getSimpleName()
                    + "("
                    + CompositeDeserializationContext.class.getSimpleName()
                    + ") to be deserializable.");
        } catch (InvocationTargetException ex) {
            throw new SerializationException("Constructor of type " + type.getName() + " threw an exception.", ex);
        } catch (InstantiationException ex) {
            throw new SerializationException(type.getName() + " cannot be instantiated.", ex);
        }

        return object;
    }

    private interface JsonEntity {}

    private static class JsonNumber implements JsonEntity {
        final String value;

        JsonNumber(String value) {
            this.value = value;
        }
    }

    private static class JsonBoolean implements JsonEntity {
        final boolean value;

        JsonBoolean(boolean value) {
            this.value = value;
        }
    }

    private static class JsonString implements JsonEntity {
        final String value;

        JsonString(String value) {
            this.value = value;
        }
    }

    private static class JsonNull implements JsonEntity {}

    private static class JsonPair {
        final String name;
        final JsonEntity value;
        boolean consumed;

        JsonPair(String name, JsonEntity value) {
            this.name = name;
            this.value = value;
        }
    }

    private static class JsonObject implements JsonEntity {
        final List<JsonPair> fields = new ArrayList<>(3);
    }

    private static class JsonArray implements JsonEntity {
        final List<JsonEntity> items = new ArrayList<>(3);
    }

    private static SerializationException serializationFailed(String name, IOException ex) {
        throw new SerializationException("Failed to serialize field '" + name + "': " + ex.getMessage());
    }

    private interface WriteFunc<T> {
        void write(T value) throws IOException;
    }

    private interface ReadFunc<T> {
        T read(JsonEntity entity);
    }

}
