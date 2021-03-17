/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;

/**
 * Version of Properties object, what saves data ordered by key's
 *
 * @author caller
 */
public class MProperties extends Properties implements IProperties {

    public class LoadErrorException extends IOException {

        private static final long serialVersionUID = 5376735010295390011L;

    };

    private static final long serialVersionUID = -3406930777595539230L;

    public static final String KEY_TESTCOMPLEATNESS = "zzzzzzzzzzzzzzzzzzz";

    protected MProperties beforeChanges;

    File file = null;

    File fileBackup = null;

    File fileCur = null;

    public synchronized boolean isChanged() {
        // System.out.println("-----------");

        // System.out.println(this);
        // System.out.println("<<");
        // System.out.println(beforeChanges);

        // System.out.println("sizes this" + size() + " org="+beforeChanges.size());
        //
        // Iterator<java.util.Map.Entry<Object, Object>> i = entrySet().iterator();
        // while (i.hasNext()) {
        // Map.Entry<Object,Object> e = i.next();
        // Object key = e.getKey();
        // Object value = e.getValue();
        // if (value == null) {
        // if (!(beforeChanges.get(key)==null && beforeChanges.containsKey(key)))
        // System.out.println("this is null, but other isnt for key " + key + " other="+beforeChanges.get(key));
        // } else {
        // if (!value.equals(beforeChanges.get(key)))
        // System.out.println("key="+key + " this="+value + " org="+beforeChanges.get(key));
        // }
        // }

        return !equals(beforeChanges);
    }

    public MProperties() {
        setUnchanged();
    }

    public MProperties(String resourcePath) throws IOException {
        this();
        try (InputStream inputResourcePath = ClassLoader.getSystemResourceAsStream(resourcePath)) {
            PropertyResourceBundle tmp = new PropertyResourceBundle(new InputStreamReader(inputResourcePath, "UTF-8"));
            Enumeration<String> keys = tmp.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                setProperty(key, tmp.getString(key));
            }

            setUnchanged();
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "MProperties resourcePath", e);
        }
    }

    @Override
    @Deprecated
    public synchronized void store(OutputStream out, String comments) throws IOException {
        super.store(out, comments);
        setUnchanged();
    }

    public synchronized void store(File file, String comments) throws IOException {
        this.file = file;

        // store to another file instead
        File f = new File(file.getAbsoluteFile() + "-");
        FileOutputStream os = new FileOutputStream(f);
        try {
            super.store(os, comments);
            os.close();

            // rename it to the destination filename (is faster than storing, is more secure to be done without failure
            if (!f.renameTo(file)) {
                file.delete();
                f.renameTo(file);
            }

            setUnchanged();
        } finally {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    @Deprecated
    public synchronized void store(Writer writer, String comments) throws IOException {
        super.store(writer, comments);
        setUnchanged();
    }

    @Override
    @Deprecated
    public synchronized void storeToXML(OutputStream os, String comment) throws IOException {
        super.storeToXML(os, comment);
        setUnchanged();
    }

    @Override
    @Deprecated
    public synchronized void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        super.storeToXML(os, comment, encoding);
        setUnchanged();
    }

    public synchronized void storeToXML(File file) throws IOException {
        storeToXML(
            file, DependencyInjector.getInstance().getInstanceOf(ILicenceManager.class).getExportHeaderCore(), "UTF-8");
    }

    public synchronized void storeToXML(File file, String comment, String encoding) throws IOException {
        this.file = file;

        // store to another file instead
        File f = new File(file.getAbsoluteFile() + "-");
        FileOutputStream os = new FileOutputStream(f);
        try {
            super.storeToXML(os, comment, encoding);
            os.close();

            // rename it to the destination filename (is faster than storing, is more secure to be done without failure
            if (!f.renameTo(file)) {
                file.delete();
                f.renameTo(file);
            }

            setUnchanged();
        } finally {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    @Deprecated
    public synchronized void load(InputStream inStream) throws IOException {
        super.load(inStream);
        setUnchanged();
    }

    public synchronized void load(File file) throws IOException {
        this.file = file;
        this.fileBackup = new File(file.getAbsolutePath() + "~");
        try {
            fileCur = file;
            doLoad();
            // org file was fine! store it as backup!
            FileHelper.copyFile(fileCur, fileBackup,false);
        } catch (LoadErrorException e) {
            // try loading backup if org seams corrupt
            fileCur = fileBackup;
            try {
                doLoad();
            } catch (Exception e2) {
                // better load incompleat org. file than corrupt / brokenbackup file!
                fileCur = file;
                doLoad();
            }
        } catch (IOException e) {
            this.fileCur = fileBackup;
            doLoad();
        }
    }

    private void doLoad() throws IOException {
        FileInputStream in = new FileInputStream(fileCur);
        try {
            super.load(in);
            if (!containsKey(KEY_TESTCOMPLEATNESS) || !getProperty(KEY_TESTCOMPLEATNESS).equals(KEY_TESTCOMPLEATNESS)) {
                throw new LoadErrorException();
            }

            setUnchanged();
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    @Deprecated
    public synchronized void load(Reader reader) throws IOException {
        super.load(reader);
        setUnchanged();
    }

    @Override
    @Deprecated
    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        super.loadFromXML(in);
        setUnchanged();
    }

    public synchronized void loadFromXML(File file) throws IOException, InvalidPropertiesFormatException {
        this.file = file;
        this.fileBackup = new File(file.getAbsolutePath() + "~");
        if (!file.exists() && !fileBackup.exists()) {
            throw new FileNotFoundException(
                "session settings file not found: " + file + " not its backup:" + fileBackup);
        }

        try {
            fileCur = file;
            doLoadXML();
            // org file was fine! store it as backup!
            FileHelper.copyFile(fileCur, fileBackup,false);
        } catch (LoadErrorException e) {
            // try loading backup if org seams corrupt
            fileCur = fileBackup;
            try {
                doLoadXML();
            } catch (Exception e2) {
                // better load incompleat org. file than corrupt / brokenbackup file!
                fileCur = file;
                doLoadXML();
            }
        } catch (IOException e) {
            Debug.getLog()
                .log(Level.FINE, "problems loading " + file + " file, trying backup file " + fileBackup + " now!", e);
            this.fileCur = fileBackup;
            doLoadXML();
        }
    }

    private void doLoadXML() throws IOException {
        FileInputStream in = new FileInputStream(fileCur);
        try {
            super.loadFromXML(in);
            setUnchanged();
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
    }

    /*
     * @SuppressWarnings("unchecked")
     * @Override public synchronized Enumeration<Object> keys() {
     * @SuppressWarnings("rawtypes") final Vector vec = new Vector<Object>(); vec.addAll(keySet()); //this one is sorted! //
     * Collections.sort(vec); // vec.addAll(super.keySet()); return vec.elements(); }
     */

    @Override
    public synchronized Enumeration<Object> keys() {
        // Debug.printStackTrace("keys");
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
    }

    @Override
    public Set<java.util.Map.Entry<Object, Object>> entrySet() {
        TreeSet<java.util.Map.Entry<Object, Object>> tmp =
            new TreeSet<java.util.Map.Entry<Object, Object>>(
                new Comparator<java.util.Map.Entry<Object, Object>>() {
                    @Override
                    public int compare(
                            java.util.Map.Entry<Object, Object> entry1, java.util.Map.Entry<Object, Object> entry2) {
                        String key1 = entry1.getKey().toString();
                        String key2 = entry2.getKey().toString();
                        return key1.compareTo(key2);
                    }
                });

        tmp.addAll(super.entrySet());

        return Collections.unmodifiableSet(tmp);
    }

    @Override
    public Set<Object> keySet() {
        // Debug.printStackTrace("keySet");
        return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
    }

    /*
     * @Override public synchronized TreeSet<Object> keySet() { final TreeSet<Object> setKeys = new TreeSet<Object>();
     * setKeys.addAll(super.keySet()); return setKeys; }
     */

    @Override
    public synchronized Set<String> stringPropertyNames() {
        final TreeSet<String> setKeys = new TreeSet<String>();
        setKeys.addAll(super.stringPropertyNames());
        return setKeys;
    }

    @Override
    public synchronized String getProperty(String key, String defaultValue) {
        if (!containsKey(key)) {
            setProperty(key, defaultValue);
        }

        return super.getProperty(key, defaultValue);
    }

    private final ConcurrentLinkedDeque<PropertyChangeListenerPair> listeners =
        new ConcurrentLinkedDeque<PropertyChangeListenerPair>();

    public synchronized Object setProperty(String key, String value) {
        Object oldVal = super.setProperty(key, value);
        if ((oldVal == null && value != null) || (oldVal != null && !oldVal.equals(value))) {
            firePropertyChange(key, value);
        }

        return oldVal;
    }

    public synchronized Object remove(String key) {
        Object oldVal = super.remove(key);
        if (oldVal != null) {
            firePropertyChange(key, null);
        }

        return oldVal;
    }

    public synchronized void removeAll(String prefix) {
        final Set<Object> setKeys = keySet(); // not using this from super,
        // so I have a cloned list and not getting concurrent modification exception
        for (Object key : setKeys) {
            if (key.toString().startsWith(prefix)) {
                remove(key);
            }
        }
    }

    public static class PropertyChangeListenerPair {

        public final WeakReference<IPropertyChangeListener> slave;
        public final String keyPrefixForFiltering;

        public PropertyChangeListenerPair(String keyPrefixForFiltering, IPropertyChangeListener slave) {
            this.slave = new WeakReference<IPropertyChangeListener>(slave);
            if (keyPrefixForFiltering == null || keyPrefixForFiltering.isEmpty()) {
                this.keyPrefixForFiltering = null;
            } else {
                this.keyPrefixForFiltering = keyPrefixForFiltering;
            }
        }

    }

    public synchronized void addWeakPropertyChangeListener(
            String keyPrefixForFiltering, IPropertyChangeListener listener) {
        listeners.add(new PropertyChangeListenerPair(keyPrefixForFiltering, listener));
    }

    public synchronized void removeWeakPropertyChangeListener(IPropertyChangeListener listener) {
        Iterator<PropertyChangeListenerPair> it = listeners.iterator();
        while (it.hasNext()){
            PropertyChangeListenerPair ll = it.next();
            if (ll.slave.get() == listener) {
                it.remove();
            }
        }
    }

    public synchronized void firePropertyChange(String key, String newValue) {
        Iterator<PropertyChangeListenerPair> it = listeners.iterator();
        while (it.hasNext()) {
            PropertyChangeListenerPair listener = it.next();
            try {
                IPropertyChangeListener slave = listener.slave.get();
                if (slave == null) {
                    it.remove();
                } else {
                    // if (slave instanceof SessionChooserWidget)
                    // System.out.println("key:" + key + " filter:" + keyPrefixForFiltering + " value:" + newValue + "
                    // slave:" + slave);

                    if (listener.keyPrefixForFiltering == null || key.startsWith(listener.keyPrefixForFiltering)) {
                        // if (slave instanceof SessionChooserWidget) {
                        // (new Exception()).printStackTrace();
                        // System.out.println("fired");
                        // }
                        slave.propertyChanged(key, newValue);
                    }
                }
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "Problems invoking Listener " + listener.slave, e);
            }
        }
    }

    @Override
    public synchronized boolean containsKey(String key) {
        return super.containsKey(key);
    }

    @Override
    public synchronized boolean containsKeyPrefix(String prefix) {
        for (Object key : super.keySet()) {
            if (key.toString().startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    public synchronized void setUnchanged() {
        setProperty(KEY_TESTCOMPLEATNESS, KEY_TESTCOMPLEATNESS);
        beforeChanges = null; // prevent memroy leak, otherwise we will become an infinite chain of such blocks
        beforeChanges = (MProperties)clone();
        // System.out.println("beforeChanges.beforeChanges:"+beforeChanges.beforeChanges);
        // beforeChanges.beforeChanges=null;//memory leak!
    }
}
