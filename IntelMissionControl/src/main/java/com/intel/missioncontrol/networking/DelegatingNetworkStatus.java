/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncSetProperty;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.NetworkStatus;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.collections.SetChangeListener;

public class DelegatingNetworkStatus implements NetworkStatus {

    private boolean offlineMode;

    private INetworkInformation networkInformation =
        new INetworkInformation() {
            @Override
            public void invalidate() {}

            @Override
            public ReadOnlyAsyncBooleanProperty networkAvailableProperty() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ReadOnlyAsyncBooleanProperty internetAvailableProperty() {
                return null;
            }

            @Override
            public ReadOnlyAsyncSetProperty<String> unreachableHostsProperty() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isHostReachable(URL url) {
                return true;
            }

            @Override
            public boolean isNetworkAvailable() {
                return false;
            }
        };

    public void setNetworkInformation(INetworkInformation networkInformation) {
        this.networkInformation = networkInformation;

        networkInformation
            .unreachableHostsProperty()
            .addListener(
                (SetChangeListener<? super String>)
                    change -> {
                        if (change.wasRemoved()) {
                            String host = change.getElementRemoved();
                            hostAvailableChanged(host, false);
                        }

                        if (change.wasAdded()) {
                            String host = change.getElementAdded();
                            hostAvailableChanged(host, true);
                        }
                    });
    }

    @Override
    public void logUnavailableHost(URL url) {}

    @Override
    public void logAvailableHost(URL url) {}

    @Override
    public boolean isHostUnavailable(URL url) {
        if (offlineMode) {
            return true;
        }

        return !networkInformation.isHostReachable(url);
    }

    @Override
    public boolean isNetworkUnavailable() {
        if (offlineMode) {
            return true;
        }

        return !networkInformation.isNetworkAvailable();
    }

    @Override
    public boolean isNetworkUnavailable(long checkInterval) {
        if (offlineMode) {
            return true;
        }

        return !networkInformation.isNetworkAvailable();
    }

    @Override
    public int getAttemptLimit() {
        return 0;
    }

    @Override
    public long getTryAgainInterval() {
        return 0;
    }

    @Override
    public boolean isOfflineMode() {
        return offlineMode;
    }

    @Override
    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    @Override
    public void setAttemptLimit(int limit) {}

    @Override
    public void setTryAgainInterval(long interval) {}

    @Override
    public List<String> getNetworkTestSites() {
        return Collections.emptyList();
    }

    @Override
    public void setNetworkTestSites(List<String> networkTestSites) {}

    @Override
    public Object setValue(String key, Object value) {
        return null;
    }

    @Override
    public AVList setValues(AVList avList) {
        return null;
    }

    @Override
    public Object getValue(String key) {
        return null;
    }

    @Override
    public Collection<Object> getValues() {
        return null;
    }

    @Override
    public String getStringValue(String key) {
        return null;
    }

    @Override
    public Set<Map.Entry<String, Object>> getEntries() {
        return null;
    }

    @Override
    public boolean hasKey(String key) {
        return false;
    }

    @Override
    public Object removeKey(String key) {
        return null;
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {}

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {}

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {}

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {}

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}

    @Override
    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent) {}

    @Override
    public AVList copy() {
        return null;
    }

    @Override
    public AVList clearList() {
        return null;
    }

    private void hostAvailableChanged(String hostName, boolean available) {
        firePropertyChange(available ? NetworkStatus.HOST_AVAILABLE : NetworkStatus.HOST_UNAVAILABLE, null, hostName);
    }

}
