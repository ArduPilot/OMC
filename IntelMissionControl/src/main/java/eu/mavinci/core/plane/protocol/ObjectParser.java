/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

/** */
package eu.mavinci.core.plane.protocol;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.MVector;

import java.lang.reflect.Field;

public class ObjectParser {

    @SuppressWarnings("rawtypes")
    public Class type;

    private String typeName;

    public String varName;
    public Object value;

    private static String INF = "inf";
    private static String NAN = "nan";

    public void decodeObject(String data) throws Exception {
        MavinciTokenizer tokenizer = new MavinciTokenizer(data);
        decodeObject(tokenizer);
        if (tokenizer.hasMoreTokens()) {
            throw new Exception(
                "Protokoll Error. Data should be ended here, but their are still tokens to parse left:\""
                    + tokenizer.nextToken()
                    + "\"");
        }
    }

    /**
     * Parse string of whole object
     *
     * @param tokenizer
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void decodeObject(MavinciTokenizer tokenizer) throws Exception {
        typeName = tokenizer.nextToken();
        Ensure.notNull(typeName, "typeName");

        tokenizer.readSpecialToken(ProtocolTokens.seppar);
        varName = tokenizer.nextToken();
        tokenizer.readSpecialToken(ProtocolTokens.seppar);
        String tmpTkn = tokenizer.nextToken();
        if (tmpTkn == null) {
            decodeObjectValueArea(tokenizer);
        } else if (tmpTkn.equals(ProtocolTokens.sbegin)) {
            // Struct or Array begins here
            tmpTkn = tokenizer.nextToken();
            if (tmpTkn == null) {
                return;
            }

            if (tmpTkn.length() == 1 && tmpTkn.equals(ProtocolTokens.separray)) {
                // is array (of structs or primitives)
                type = getType(typeName);
                MVector<Object> valueArr = new MVector<Object>(type);
                while (tmpTkn != null && tmpTkn.equals(ProtocolTokens.separray)) {
                    // as long more array elements are avaliable
                    ObjectParser pars = new ObjectParser();
                    pars.typeName = typeName;
                    pars.varName = varName;
                    pars.decodeObjectValueArea(tokenizer);
                    valueArr.add(pars.value);
                    tmpTkn = tokenizer.nextToken(); // get next seppar or send
                }
                // value = valueArr.toArray();
                value = valueArr;
                type = value.getClass(); // to make type to MVector
            } else if (tmpTkn.equals(ProtocolTokens.send)) {
                // it was an empty array, or maybe an empty struct..
                // interpret it as empty array
                value = new MVector<Object>(null);
                type = value.getClass();
            } else {
                // is single struct
                tokenizer.undoReadCurrentToken();
                decodeObjectValueArea(tokenizer);
                tokenizer.readSpecialToken(ProtocolTokens.send);
            }
        } else {
            tokenizer.undoReadCurrentToken();
            decodeObjectValueArea(tokenizer);
        }
    }

    private Class<?> getType(String typeName) throws ClassNotFoundException {
        char typeNameInt = typeName.charAt(0);

        if (typeName.length() != 1) {
            String longTypeName = ProtocolTokens.sendableObjectsPackage + "." + typeName;
            return Class.forName(longTypeName);
        }

        switch (typeNameInt) {
        case ProtocolTokens.prefixCodeInt:
            return Integer.class;

        case ProtocolTokens.prefixCodeFloat:
            return Float.class;

        case ProtocolTokens.prefixCodeDouble:
            return Double.class;

        case ProtocolTokens.prefixCodeString:
            return String.class;

        case ProtocolTokens.prefixCodeBool:
            return Boolean.class;

        case ProtocolTokens.prefixCodeChar:
            return Character.class;

        default:
            String longTypeName = ProtocolTokens.sendableObjectsPackage + "." + typeName;
            return Class.forName(longTypeName);
        }
    }

    /**
     * Parse Value Area of known object (type must allready be set before!)
     *
     * @param tokenizer
     * @throws Exception
     */
    private void decodeObjectValueArea(MavinciTokenizer tokenizer) throws Exception {
        // Primitive... parse it directly here

        if (typeName.length() != 1) {
            decodeComplexObjectValueArea(tokenizer);
            return;
        }

        char typeNameInt = typeName.charAt(0);

        String curTkn;

        switch (typeNameInt) {
        case ProtocolTokens.prefixCodeInt:
            type = Integer.class;
            value = Integer.parseInt(tokenizer.nextToken());
            return;

        case ProtocolTokens.prefixCodeFloat:
            type = Float.class;
            curTkn = tokenizer.nextToken();
            Ensure.notNull(curTkn, "curTkn");
            if (curTkn.equals(INF)) {
                value = Float.POSITIVE_INFINITY;
            } else if (curTkn.equals(NAN)) {
                value = Float.NaN;
            } else {
                value = Float.parseFloat(curTkn);
            }

            return;

        case ProtocolTokens.prefixCodeDouble:
            type = Double.class;
            curTkn = tokenizer.nextToken();
            Ensure.notNull(curTkn, "curTkn");
            if (curTkn.equals(INF)) {
                value = Double.POSITIVE_INFINITY;
            } else {
                value = Double.parseDouble(curTkn);
            }

            return;

        case ProtocolTokens.prefixCodeString:
            type = String.class;
            curTkn = tokenizer.nextToken();
            // care about empty strings
            if (curTkn == null) {
                value = "";
            } else if (ProtocolTokens.allSeperators.contains(curTkn)) {
                value = "";
                tokenizer.undoReadCurrentToken();
            } else {
                type = String.class;
                byte[] tmp = Base64.decode(curTkn, Base64.DEFAULT);
                value = new String(tmp, ProtocolTokens.encoding);
                // System.out.println(curTkn + " -> " + value);
                // value = curTkn;
            }

            return;

        case ProtocolTokens.prefixCodeBool:
            type = Boolean.class;
            curTkn = tokenizer.nextToken();
            Ensure.notNull(curTkn, "curTkn");
            if (curTkn.equals(ProtocolTokens.tokenFalse)) {
                value = Boolean.FALSE;
            } else if (curTkn.equals(ProtocolTokens.tokenTrue)) {
                value = Boolean.TRUE;
            } else {
                throw new Exception("Boolean parse error");
            }

            return;

        case ProtocolTokens.prefixCodeChar:
            type = Character.class;
            value = new Character((char)Integer.parseInt(tokenizer.nextToken()));
            return;

        default:
            decodeComplexObjectValueArea(tokenizer);
            return;
        }
    }

    /**
     * Parse Value Area of known not primitive object (type must allready be set before!)
     *
     * @param tokenizer
     * @throws Exception
     */
    private void decodeComplexObjectValueArea(MavinciTokenizer tokenizer) throws Exception {
        String longTypeName = ProtocolTokens.sendableObjectsPackage + "." + typeName;

        try {
            type = Class.forName(longTypeName);
            value = type.newInstance();
        } catch (ClassNotFoundException e) {
            // making fist caracter to upper case for compatibility
            String firstChar = typeName.substring(0, 1).toUpperCase();
            typeName = firstChar + typeName.substring(1);

            longTypeName = ProtocolTokens.sendableObjectsPackage + "." + typeName;

            type = Class.forName(longTypeName);
            value = type.newInstance();
        } catch (NoClassDefFoundError e) { // needed for windows
            // making fist caracter to upper case for compatibility
            String firstChar = typeName.substring(0, 1).toUpperCase();
            typeName = firstChar + typeName.substring(1);

            longTypeName = ProtocolTokens.sendableObjectsPackage + "." + typeName;

            type = Class.forName(longTypeName);
            value = type.newInstance();
        }

        // setting members
        for (; ; ) { // as long more array elements are avaliable
            ObjectParser pars = new ObjectParser();

            pars.decodeObject(tokenizer);

            // set member
            Field member = type.getField(pars.varName);
            member.set(value, pars.value);

            String nextToken = tokenizer.nextToken();
            if (nextToken == null || !nextToken.equals(ProtocolTokens.sepael)) {
                break;
            }
        }

        tokenizer.undoReadCurrentToken();
    }

    public static final String recv_orientationToken = "recv_orientation";
    public static final String recv_positionToken = "recv_position";

}
