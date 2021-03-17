/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.helper.IProperties;
import eu.mavinci.core.helper.IPropertiesStoreable;
import eu.mavinci.core.obfuscation.IKeepMembers;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * Superclass for sendable object
 *
 * <p>is able to clone, equal, and toString objects, if they only contain public fields of type MObject or primitives..
 *
 * @author marco
 */
public abstract class MObject implements Cloneable, Serializable, IPropertiesStoreable, IKeepMembers {

    /** */
    private static final long serialVersionUID = 6457655370410809711L;

    /** if true, this was a value not transmitted from Connector but loaded from an old session */
    public Boolean fromSession = false;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (this.getClass().equals(obj.getClass())) {
            Field[] fields = getClass().getFields();
            for (Field field : fields) {
                try {
                    if (field.get(this) == null) {
                        if (field.get(obj) != null) {
                            return false;
                        }
                    } else if (!(field.get(this).equals(field.get(obj)))) {
                        // System.out.println("Field:"+field+ " "+field.get(this) + "<>"+field.get(obj));
                        return false;
                    }
                } catch (Exception e) {
                    Debug.getLog().log(Level.SEVERE, "problem with comparing objects, this should never happen", e);
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public MObject clone() throws CloneNotSupportedException {
        try {
            MObject obj = getClass().newInstance();
            Field[] fields = getClass().getFields();
            for (Field field : fields) {
                Object fieldValThis = field.get(this);
                if (fieldValThis instanceof MObject) {
                    MObject cloneab = (MObject)fieldValThis;
                    field.set(obj, cloneab.clone());
                } else if (fieldValThis instanceof MVector<?>) {
                    MVector<?> cloneab = (MVector<?>)fieldValThis;
                    field.set(obj, cloneab.clone());
                } else {
                    field.set(obj, fieldValThis);
                }
            }

            return obj;
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "problem with cloning objects, this should never happen", e);
            throw new CloneNotSupportedException(e.getMessage());
        }
    }

    @Override
    public String toString() {
        String str = getClass().getSimpleName() + ":";
        boolean first = true;
        Field[] fields = getClass().getFields();
        for (Field field : fields) {
            if (first) {
                first = false;
            } else {
                str += ",";
            }

            try {
                str += field.getName() + "=" + field.get(this);
            } catch (Exception e) {
                Debug.getLog().log(Level.FINE, "problem with toString object, this should never happen", e);
                return str;
            }
        }

        return str;
    }

    public void storeState(IProperties prop, String keyPrefix) {
        Class<?> c = getClass();
        for (Field f : c.getFields()) {
            String intPrefix = keyPrefix + "." + f.getName();
            try {
                toPropertiesSingle(intPrefix, prop, f.get(this));
            } catch (Exception e) {
                Debug.getLog().log(Level.FINE, "Problems writing Object state to Properties", e);
            }
        }
    }

    public void loadState(IProperties prop, String keyPrefix) {
        Class<?> c = getClass();
        for (Field f : c.getFields()) {
            try {
                if (IPropertiesStoreable.class.isAssignableFrom(f.getType())) {
                    IPropertiesStoreable o = (IPropertiesStoreable)f.get(this);
                    String oPref = keyPrefix + "." + f.getName();
                    o.loadState(prop, oPref);
                } else {
                    Object n = fromPropertiesSingle(keyPrefix + "." + f.getName(), prop, f.getType());
                    if (n != null) {
                        f.set(this, n);
                    } else {
                        throw new Exception("Could not find Parser for Class " + f.getType());
                    }
                }
            } catch (Exception e) {
                Debug.getLog().log(Level.FINEST, "Problems loading Object state from Properties", e);
            }
        }
    }

    protected static void toPropertiesSingle(String key, IProperties prop, Object o) {
        if (IPropertiesStoreable.class.isAssignableFrom(o.getClass())) {
            IPropertiesStoreable oStore = (IPropertiesStoreable)o;
            oStore.storeState(prop, key);
        } else {
            prop.setProperty(key, o.toString());
        }
    }

    protected static Object fromPropertiesSingle(String key, IProperties prop, Class<?> cls) throws Exception {
        if (cls.equals(String.class)) {
            return prop.getProperty(key);
        } else if (cls.equals(Boolean.class) || cls.equals(boolean.class)) {
            return Boolean.parseBoolean(prop.getProperty(key));
        } else if (cls.equals(Double.class) || cls.equals(double.class)) {
            return Double.parseDouble(prop.getProperty(key));
        } else if (cls.equals(Integer.class) || cls.equals(int.class)) {
            return Integer.parseInt(prop.getProperty(key));
        } else if (cls.equals(Float.class) || cls.equals(float.class)) {
            return Float.parseFloat(prop.getProperty(key));
        } else if (cls.equals(Byte.class) || cls.equals(byte.class)) {
            return Byte.parseByte(prop.getProperty(key));
        } else if (cls.equals(Character.class) || cls.equals(char.class)) {
            return prop.getProperty(key).toString().charAt(0);
        } else if (IPropertiesStoreable.class.isAssignableFrom(cls)) {
            MObject obj = (MObject)cls.newInstance();
            obj.loadState(prop, key);
            return obj;
        } else {
            throw new Exception("Could not find Parser for Class " + cls);
        }
    }

}
