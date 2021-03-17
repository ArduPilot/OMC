/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

import java.util.Enumeration;
import java.util.Set;

public interface IProperties {

    public String getProperty(String key, String defaultValue);

    public String getProperty(String key);

    public Object setProperty(String key, String value);

    public Object remove(String key);

    public void removeAll(String prefix);

    public void addWeakPropertyChangeListener(String keyPrefixForFiltering, IPropertyChangeListener listener);

    public void removeWeakPropertyChangeListener(IPropertyChangeListener listener);

    public Set<Object> keySet();

    public Enumeration<Object> keys();

    public boolean containsKey(String key);

    public boolean containsKeyPrefix(String prefix);
}
