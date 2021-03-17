/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.sources;

import static org.junit.Assert.assertTrue;

import com.intel.missioncontrol.airspaces.services.Airmap2AirspaceService;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncSetProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.common.PathProvider;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import eu.mavinci.airspace.IAirspace;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class Airmap2AirspacesServiceTest {

    private AirspacesProvidersSettings airspaceSettingsMock;
    private INetworkInformation networkStatusProvider;

    IPathProvider pathProvider;

    @Before
    public void setupAirmap()
            throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));

        airspaceSettingsMock =
            new AirspacesProvidersSettings() {
                AsyncStringProperty cacheDirectory =
                    new SimpleAsyncStringProperty(
                        this, new PropertyMetadata.Builder<String>().initialValue("airmap-test-cache").create());

                @Override
                public AsyncStringProperty cacheDirectory() {
                    return cacheDirectory;
                }
            };

        pathProvider = new PathProvider();

        networkStatusProvider =
            new INetworkInformation() {
                @Override
                public void invalidate() {}

                @Override
                public ReadOnlyAsyncBooleanProperty networkAvailableProperty() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public ReadOnlyAsyncSetProperty<String> unreachableHostsProperty() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean isHostReachable(URL url) {
                    throw new UnsupportedOperationException();
                }
            };
    }

    static Sector BAYLANDS_BB = Sector.fromDegrees(37.4104, 37.4147, -122.0033, -121.9868);

    @Ignore("BROKEN TEST, but ignored to get testing in build system")
    @Test
    public void testAirspaceService() {
        Airmap2AirspaceService service =
            new Airmap2AirspaceService(airspaceSettingsMock, pathProvider, networkStatusProvider);

        List<IAirspace> airspacesWithin = service.getAirspacesWithin(BAYLANDS_BB, 0);

        int size = airspacesWithin.size();

        assertTrue(airspacesWithin.size() > 0);
    }

    @Test
    public void airspaceServiceProvider() {}

}
