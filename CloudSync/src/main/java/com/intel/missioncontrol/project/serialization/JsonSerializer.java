/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.serialization;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.intel.missioncontrol.geom.Vec2;
import com.intel.missioncontrol.geom.Vec3;
import com.intel.missioncontrol.geom.Vec4;
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
import java.util.List;
import java.util.Stack;
import org.jetbrains.annotations.Nullable;

public class JsonSerializer implements Serializer {

    private final boolean prettyPrint;

    public JsonSerializer() {
        this.prettyPrint = false;
    }

    public JsonSerializer(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    @Override
    public void serialize(Serializable value, OutputStream stream) throws IOException {
        try (JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(stream)))) {
            SerializationContext context =
                new SerializationContext() {
                    @Override
                    public void writeOffsetDateTime(String name, OffsetDateTime value) {
                        writeString(name, value != null ? value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
                    }

                    @Override
                    public void writeVec2(String name, Vec2 value) {
                        if (value != null) {
                            writeString(name, value.x + "," + value.y);
                        } else {
                            writeString(name, null);
                        }
                    }

                    @Override
                    public void writeVec3(String name, Vec3 value) {
                        if (value != null) {
                            writeString(name, value.x + "," + value.y + "," + value.z);
                        } else {
                            writeString(name, null);
                        }
                    }

                    @Override
                    public void writeVec4(String name, Vec4 value) {
                        if (value != null) {
                            writeString(name, value.x + "," + value.y + "," + value.z + "," + value.w);
                        } else {
                            writeString(name, null);
                        }
                    }

                    @Override
                    public void writeValue(String name, Object value, Class<?> type, boolean writeTypeInfo) {
                        try {
                            writer.name(name);
                            writeValue(value, type, writeTypeInfo);
                        } catch (IOException ex) {
                            throw new SerializationException("Error occurred during serialization.", ex);
                        }
                    }

                    @Override
                    public void writeBytes(String name, byte[] value) {
                        writeString(name, Base64.getMimeEncoder().encodeToString(value));
                    }

                    @Override
                    public <T> void writeCollection(
                            String name, Collection<T> value, Class<? super T> type, boolean writeTypeInfo) {
                        try {
                            writer.name(name);
                            writer.beginArray();

                            for (T item : value) {
                                writeValue(item, type, writeTypeInfo);
                            }

                            writer.endArray();
                        } catch (IOException ex) {
                            throw new SerializationException("Error occurred during serialization.", ex);
                        }
                    }

                    private void writeValue(Object value, Class<?> type, boolean writeTypeInfo) throws IOException {
                        if (type == Boolean.class) {
                            writer.value((boolean)value);
                        } else if (type == Byte.class) {
                            writer.value((byte)value);
                        } else if (type == Short.class) {
                            writer.value((short)value);
                        } else if (type == Integer.class) {
                            writer.value((int)value);
                        } else if (type == Long.class) {
                            writer.value((long)value);
                        } else if (type == Float.class) {
                            writer.value((float)value);
                        } else if (type == Double.class) {
                            writer.value((double)value);
                        } else if (type == String.class) {
                            writer.value((String)value);
                        } else if (value instanceof Serializable) {
                            writer.beginObject();

                            if (writeTypeInfo) {
                                writer.name("@type");
                                writer.value(value.getClass().getSimpleName());
                            }

                            ((Serializable)value).getObjectData(this);
                            writer.endObject();
                        } else if (value == null) {
                            writer.nullValue();
                        } else {
                            throw new SerializationException(type.getName() + " cannot be serialized.");
                        }
                    }
                };

            if (prettyPrint) {
                writer.setIndent("    ");
            }

            writer.beginObject();
            value.getObjectData(context);
            writer.endObject();
        }
    }

    @Override
    public <T extends Serializable> T deserialize(InputStream stream, Class<T> type) throws IOException {
        try (JsonReader reader = new JsonReader(new BufferedReader((new InputStreamReader(stream))))) {
            JsonObject root = new JsonObject();
            readJsonObject(root, reader);

            Stack<JsonObject> scope = new Stack<>();
            scope.push(root);

            DeserializationContext context =
                new DeserializationContext() {
                    @Override
                    public OffsetDateTime readOffsetDateTime(String name) {
                        String value = readString(name);
                        return value != null
                            ? OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            : null;
                    }

                    @Override
                    public Vec2 readVec2(String name) {
                        String value = readString(name);
                        if (value != null) {
                            String[] coords = value.split(",");
                            return new Vec2(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
                        }

                        return null;
                    }

                    @Override
                    public Vec3 readVec3(String name) {
                        String value = readString(name);
                        if (value != null) {
                            String[] coords = value.split(",");
                            return new Vec3(
                                Double.parseDouble(coords[0]),
                                Double.parseDouble(coords[1]),
                                Double.parseDouble(coords[2]));
                        }

                        return null;
                    }

                    @Override
                    public Vec4 readVec4(String name) {
                        String value = readString(name);
                        if (value != null) {
                            String[] coords = value.split(",");
                            return new Vec4(
                                Double.parseDouble(coords[0]),
                                Double.parseDouble(coords[1]),
                                Double.parseDouble(coords[2]),
                                Double.parseDouble(coords[3]));
                        }

                        return null;
                    }

                    @Override
                    public byte[] readBytes(String name) {
                        String value = readString(name);
                        return value != null ? Base64.getMimeDecoder().decode(value) : null;
                    }

                    @Override
                    public <T0> void readCollection(
                            String name,
                            Collection<? super T0> list,
                            Class<T0> targetType,
                            @Nullable Class<? extends T0>[] potentialTypes) {
                        JsonObject current = scope.peek();

                        for (JsonPair entity : current.fields) {
                            if (!entity.name.equals(name)) {
                                continue;
                            }

                            if (entity.value instanceof JsonArray) {
                                for (JsonEntity itemEntity : ((JsonArray)entity.value).items) {
                                    list.add(readItem(itemEntity, targetType, potentialTypes));
                                }

                                return;
                            } else if (entity.value instanceof JsonNull) {
                                return;
                            } else {
                                throw new SerializationException("'" + name + "': expected array.");
                            }
                        }

                        throw new SerializationException("'" + name + "' not found.");
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    protected <T0> T0 readValue(
                            String name, Class<T0> targetType, @Nullable Class<? extends T0>[] potentialTypes) {
                        JsonObject current = scope.peek();

                        for (JsonPair pair : current.fields) {
                            if (!pair.name.equals(name)) {
                                continue;
                            }

                            pair.consumed = true;

                            if (pair.value instanceof JsonBoolean) {
                                if (targetType != Boolean.class) {
                                    throwTypeMismatch(name, Boolean.class, targetType);
                                }

                                return (T0)(Boolean)((JsonBoolean)pair.value).value;
                            } else if (pair.value instanceof JsonNumber) {
                                if (targetType == Byte.class) {
                                    return (T0)(Byte)Byte.parseByte(((JsonNumber)pair.value).value);
                                } else if (targetType == Short.class) {
                                    return (T0)(Short)Short.parseShort(((JsonNumber)pair.value).value);
                                } else if (targetType == Integer.class) {
                                    return (T0)(Integer)Integer.parseInt(((JsonNumber)pair.value).value);
                                } else if (targetType == Long.class) {
                                    return (T0)(Long)Long.parseLong(((JsonNumber)pair.value).value);
                                } else if (targetType == Float.class) {
                                    return (T0)(Float)Float.parseFloat(((JsonNumber)pair.value).value);
                                } else if (targetType == Double.class) {
                                    return (T0)(Double)Double.parseDouble(((JsonNumber)pair.value).value);
                                } else {
                                    throwTypeMismatch(name, Number.class, targetType);
                                }
                            } else if (pair.value instanceof JsonString) {
                                if (targetType != String.class) {
                                    throwTypeMismatch(name, String.class, targetType);
                                }

                                return (T0)((JsonString)pair.value).value;
                            } else if (pair.value instanceof JsonNull) {
                                return null;
                            } else if (pair.value instanceof JsonObject) {
                                scope.push((JsonObject)pair.value);
                                T0 object = createObject(this, targetType, potentialTypes, (JsonObject)pair.value);
                                scope.pop();
                                return object;
                            } else if (pair.value instanceof JsonArray) {
                                throw new SerializationException("'" + name + "': unexpected array value.");
                            }
                        }

                        throw new SerializationException("'" + name + "' not found.");
                    }

                    @SuppressWarnings("unchecked")
                    private <T0> T0 readItem(
                            JsonEntity value, Class<T0> targetType, @Nullable Class<? extends T0>[] potentialTypes) {
                        if (value instanceof JsonBoolean) {
                            return (T0)(Boolean)((JsonBoolean)value).value;
                        } else if (value instanceof JsonNumber) {
                            Integer i = Ints.tryParse(((JsonNumber)value).value);
                            if (i != null) {
                                return (T0)i;
                            }

                            Long l = Longs.tryParse(((JsonNumber)value).value);
                            if (l == null) {
                                return (T0)l;
                            }

                            return (T0)(Double)Double.parseDouble(((JsonNumber)value).value);
                        } else if (value instanceof JsonString) {
                            return (T0)((JsonString)value).value;
                        } else if (value instanceof JsonNull) {
                            return null;
                        } else if (value instanceof JsonObject) {
                            scope.push((JsonObject)value);
                            T0 object = createObject(this, targetType, potentialTypes, (JsonObject)value);
                            scope.pop();
                            return object;
                        } else if (value instanceof JsonArray) {
                            throw new SerializationException("Unexpected array value.");
                        }

                        return null;
                    }

                    private void throwTypeMismatch(String name, Class<?> expected, Class<?> actual) {
                        throw new SerializationException(
                            "Type mismatch: '"
                                + name
                                + "' (expected = "
                                + expected.getSimpleName()
                                + ", actual = "
                                + actual.getSimpleName()
                                + ")");
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

    private <T> T createObject(
            DeserializationContext context,
            Class<T> type,
            @Nullable Class<? extends T>[] potentialTypes,
            JsonObject jsonObject) {
        T object;
        try {
            String typeValue = null;
            if (jsonObject != null) {
                for (JsonPair pair : jsonObject.fields) {
                    if ("@type".equals(pair.name)) {
                        if (!(pair.value instanceof JsonString)) {
                            throw new SerializationException("'@type': unexpected value");
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
                    throw new SerializationException("'@type' was encountered, but no suitable type found.");
                }
            } else {
                polymorphicType = type;
            }

            Constructor<? extends T> ctor = polymorphicType.getConstructor(DeserializationContext.class);
            ctor.setAccessible(true);
            object = ctor.newInstance(context);
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new SerializationException(
                type.getName()
                    + " requires a public constructor with the signature "
                    + type.getSimpleName()
                    + "("
                    + DeserializationContext.class.getSimpleName()
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

}
