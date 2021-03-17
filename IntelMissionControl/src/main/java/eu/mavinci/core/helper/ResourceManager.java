/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

import eu.mavinci.core.desktop.listener.WeakListenerList;
import java.util.Iterator;
import java.util.Vector;

@Deprecated
public class ResourceManager implements IPropertiesStoreable, Iterable<String> {

    WeakListenerList<IResourceManagerListener> listeners =
        new WeakListenerList<IResourceManagerListener>("ResourceManagerListeners");

    protected String KEY;

    public static final String FILENAME_SPLITTER = "->";

    Vector<String> references = new Vector<String>();

    public ResourceManager(String key) {
        KEY = key;
    }

    public synchronized void addListener(IResourceManagerListener listener) {
        listeners.add(listener);
    }

    public synchronized void fireResourceListChanged() {
        for (IResourceManagerListener listener : listeners) listener.resourceListChanged(this);
    }

    public synchronized String getReference(int i) {
        return references.get(i);
    }

    public synchronized int size() {
        return references.size();
    }

    public synchronized void add(String reference) {
        if (contains(reference)) {
            return;
        }

        references.add(reference);
        fireResourceListChanged();
    }

    public synchronized boolean contains(String reference) {
        return references.contains(reference);
    }

    /**
     * adds someting to the sesource manager, but take care that it will be uniqe by adding _x with x a number to the
     * string, if it is nessesary
     *
     * @param reference
     */
    public synchronized String addDistinct(String reference) {
        // System.out.println("currentSources" + references);
        if (!references.contains(reference)) {
            add(reference);
            return reference;
        }

        int x = 0;
        while (references.contains(reference + FILENAME_SPLITTER + x)) {
            x++;
        }
        // System.out.println("distinctName:" + reference+FILENAME_SPLITTER+x);
        reference = reference + FILENAME_SPLITTER + x;
        add(reference);
        return reference;
    }

    public static String getWithoutFilenameSplitter(String resourceKey){
        int pos = resourceKey.indexOf(FILENAME_SPLITTER);
        if (pos <0 ){
            return resourceKey;
        } else {
            return resourceKey.substring(0,pos);
        }
    }

    public synchronized boolean remove(String reference) {
        // System.out.println("fire removing " + reference);
        if (references.remove(reference)) {
            // System.out.println("found new State : "+references);
            fireResourceListChanged();
            return true;
        }

        return false;
    }

    public synchronized String remove(int i) {
        String s = references.remove(i);
        fireResourceListChanged();
        return s;
    }

    IProperties propOverwrite;

    public void setStorageOverwrite(IProperties prop) {
        propOverwrite = prop;
    }

    public synchronized void storeState(IProperties prop) {
        storeState(prop, "");
    }

    public synchronized void loadState(IProperties prop) {
        loadState(prop, "");
    }

    public synchronized void loadState(IProperties prop, String keyPrefix) {
        if (propOverwrite != null) prop = propOverwrite;
        keyPrefix = keyPrefix + "." + KEY + ".";
        Vector<String> referencesNew = new Vector<String>();
        int i = 0;
        while (true) {
            String key = keyPrefix + i;
            if (!prop.containsKey(key)) {
                break;
            }

            referencesNew.add(prop.getProperty(key));
            i++;
        }
        // System.out.println("resources loaded:" + keyPrefix);
        // System.out.println("references :"+ referencesNew);
        if (!referencesNew.equals(references)) {
            references = referencesNew;
            fireResourceListChanged();
        }
    }

    public synchronized boolean replace(String oldReference, String newReference) {
        for (int i = 0; i != references.size(); i++) {
            if (references.get(i).equals(oldReference)) {
                references.set(i, newReference);
                return true;
            }
        }

        return false;
    }

    public synchronized void storeState(IProperties prop, String keyPrefix) {
        if (propOverwrite != null) prop = propOverwrite;
        keyPrefix = keyPrefix + "." + KEY + ".";
        // System.out.println("store ResManager" + keyPrefix);
        // System.out.println("references :"+ references);
        int i;
        for (i = 0; i < references.size(); i++) {
            prop.setProperty(keyPrefix + i, references.get(i));
        }

        while (prop.containsKey(keyPrefix + i)) {
            prop.remove(keyPrefix + i);
            i++;
        }
    }

    public synchronized Iterator<String> iterator() {
        return references.iterator();
    }

    /** @return the references */
    public Vector<String> getReferences() {
        return references;
    }
}
