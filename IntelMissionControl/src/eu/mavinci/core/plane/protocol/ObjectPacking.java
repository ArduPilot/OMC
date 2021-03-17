/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

/** */
package eu.mavinci.core.plane.protocol;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Vector;

public class ObjectPacking {

    public static String encodeFkt(String fktName) throws Exception {
        StringBuffer builder = new StringBuffer(100);
        builder.append(ProtocolTokens.mbegin);
        builder.append(fktName);
        // builder.append(ProtocolTokens.mend); //moved into TCP stack
        return builder.toString();
    }

    public static String encodeFkt(String fktName, Object arg, String name) throws Exception {
        StringBuffer builder = new StringBuffer(400);
        builder.append(ProtocolTokens.mbegin);
        builder.append(fktName);
        builder.append(ProtocolTokens.sep);
        ObjectPacking pack = new ObjectPacking();
        pack.encodeObject(arg, name, builder);
        // builder.append(ProtocolTokens.mend); //moved into TCP stack
        return builder.toString();
    }

    public static String encodeFkt(String fktName, Vector<Object> args, Vector<String> names) throws Exception {
        StringBuffer builder = new StringBuffer(400);
        builder.append(ProtocolTokens.mbegin);

        builder.append(fktName);
        for (int i = 0; i != args.size(); i++) {
            builder.append(ProtocolTokens.sep);
            ObjectPacking pack = new ObjectPacking();
            pack.encodeObject(args.get(i), names.get(i), builder);
        }
        // builder.append(ProtocolTokens.mend); //moved into TCP stack
        return builder.toString();
    }

    public StringBuffer encodeObject(Object o, String _name, StringBuffer builder) throws Exception {
        varName = _name;

        valueStringBuilder = new StringBuffer(200);
        encodeObjectValueArea(o, valueStringBuilder);

        builder.append(typeName);
        builder.append(ProtocolTokens.seppar);

        builder.append(varName);
        builder.append(ProtocolTokens.seppar);

        if (needBrackets) {
            builder.append(ProtocolTokens.sbegin);
        }

        builder.append(valueStringBuilder);
        if (needBrackets) {
            builder.append(ProtocolTokens.send);
        }

        return builder;
    }

    @SuppressWarnings("unchecked")
    public StringBuffer encodeObjectValueArea(Object o, StringBuffer valueStringBuilder) throws Exception {
        if (o instanceof Integer) {
            Integer tmp = (Integer)o;
            typeName = ProtocolTokens.prefixInt;
            valueStringBuilder.append(tmp.toString());
        } else if (o instanceof Character) {
            Character tmp = (Character)o;
            typeName = ProtocolTokens.prefixChar;
            valueStringBuilder.append(Integer.toString(tmp.toString().codePointAt(0)));
        } else if (o instanceof Float) {
            Float tmp = (Float)o;
            typeName = ProtocolTokens.prefixFloat;
            valueStringBuilder.append(tmp.toString());
        } else if (o instanceof Double) {
            Double tmp = (Double)o;
            typeName = ProtocolTokens.prefixDouble;
            valueStringBuilder.append(tmp.toString());
        } else if (o instanceof Boolean) {
            Boolean tmp = (Boolean)o;
            typeName = ProtocolTokens.prefixBool;
            if (tmp.equals(true)) {
                valueStringBuilder.append(ProtocolTokens.tokenTrue);
            } else {
                valueStringBuilder.append(ProtocolTokens.tokenFalse);
            }
        } else if (o instanceof String) {
            String tmp = (String)o;
            typeName = ProtocolTokens.prefixString;
            valueStringBuilder.append(Base64.encodeString(tmp));
        } else if (o instanceof Vector) {
            Vector<Object> vec = (Vector<Object>)o;
            needBrackets = true;
            typeName = ""; // FIXME! this works only for not empty arrays!
            if (vec.isEmpty()) {
                throw new Exception("Sorry, but the ObjectPacking is not able to pack empty arrays!");
            }

            for (Iterator<Object> it = vec.iterator(); it.hasNext(); ) {
                valueStringBuilder.append(ProtocolTokens.separray);
                ObjectPacking pack = new ObjectPacking();
                pack.encodeObjectValueArea(it.next(), valueStringBuilder);
                typeName = pack.typeName;
            }
        } else {
            Class<?> c = o.getClass();
            needBrackets = true;
            typeName = c.getSimpleName();

            Field[] publicFields = c.getFields();
            for (int i = 0; i < publicFields.length; i++) {
                String fieldName = publicFields[i].getName();
                Object obj = publicFields[i].get(o);
                if (obj == null) {
                    continue;
                }

                if (i > 0) {
                    valueStringBuilder.append(ProtocolTokens.sepael);
                }

                ObjectPacking pack = new ObjectPacking();
                pack.encodeObject(obj, fieldName, valueStringBuilder);
            }
        }

        return valueStringBuilder;
    }

    private boolean needBrackets = false;
    private String varName = null;
    private String typeName = null;
    private StringBuffer valueStringBuilder = null;

}
