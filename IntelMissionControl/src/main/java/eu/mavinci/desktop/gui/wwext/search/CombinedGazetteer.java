/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.search;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.settings.ExpertSettings;
import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.NoItemException;
import gov.nasa.worldwind.exception.ServiceException;
import gov.nasa.worldwind.poi.Gazetteer;
import gov.nasa.worldwind.poi.PointOfInterest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.asyncfx.concurrent.Dispatcher;

public class CombinedGazetteer implements Gazetteer, IKeepAll {

    public static final String SEARCH_PRIO = "SearchPrio";

    List<Gazetteer> slaves = new Vector<Gazetteer>();
    List<Gazetteer> slavesWithCoordinates = new Vector<Gazetteer>();
    AtomicInteger resultsToWait = new AtomicInteger(0);
    Queue<PointOfInterest> results = new ConcurrentLinkedQueue<PointOfInterest>();

    public boolean isGetCoordinates() {
        return getCoordinates;
    }

    public void setGetCoordinates(boolean getCoordinates) {
        this.getCoordinates = getCoordinates;
    }

    private boolean getCoordinates = false;

    public static final String GEOCODER_PROVIDER_HEREMAPS = "here";
    public static final String GEOCODER_PROVIDER_GOOGLE = "google";
    public static final String GEOCODER_PROVIDER_MAPBOX = "mapbox";

    public void addSlaveGazetteer(Gazetteer slave) {
        slaves.add(slave);
    }

    public void addSlaveWithCoordinatesGazetter(Gazetteer slave) {
        slavesWithCoordinates.add(slave);
    }

    public static CombinedGazetteer createAllGazeteer() {
        CombinedGazetteer gz = new CombinedGazetteer();

        String config = StaticInjector.getInstance(ExpertSettings.class).getGeocoderProvider();

        if (config.equalsIgnoreCase(GEOCODER_PROVIDER_GOOGLE)) {
            gz.addSlaveWithCoordinatesGazetter(new GoogleGazetteer());
            gz.addSlaveGazetteer(new GoogleAutoCompleteGazetteer());
        } else if (config.equalsIgnoreCase(GEOCODER_PROVIDER_HEREMAPS)) {
            gz.addSlaveWithCoordinatesGazetter(new HereGazetteer());
            gz.addSlaveGazetteer(new HereAutoCompleteGazetteer());
        } else {
            gz.addSlaveGazetteer(new MapboxAutoCompleteGazetteer());
            gz.addSlaveWithCoordinatesGazetter(new MapboxGazetteer());
        }

        return gz;
    }

    @Override
    public List<PointOfInterest> findPlaces(final String placeInfo) throws NoItemException, ServiceException {
        results.clear();
        resultsToWait.set(slaves.size());
        Dispatcher dispatcher = Dispatcher.background();
        for (final Gazetteer slave : (getCoordinates ? slavesWithCoordinates : slaves)) {
            dispatcher.run(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<PointOfInterest> tmp = slave.findPlaces(placeInfo);
                            if (tmp != null) {
                                if (tmp.size() <= 1000) {
                                    results.addAll(tmp);
                                } else {
                                    Debug.getLog()
                                        .log(Level.INFO, "found too much places in gazetteer, will be reduced");
                                }
                            }
                        } catch (ServiceException e) {
                            Debug.getLog().log(Level.FINE, "Problems with gazetteer service", e);
                        } finally {
                            resultsToWait.decrementAndGet();
                        }
                    }
                });
        }

        while (resultsToWait.get() > 0) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }

        List<PointOfInterest> list = new ArrayList<PointOfInterest>(results);
        Collections.sort(
            list,
            new Comparator<PointOfInterest>() {

                @Override
                public int compare(PointOfInterest o1, PointOfInterest o2) {
                    if (o2.getStringValue(SEARCH_PRIO).equals(o1.getStringValue(SEARCH_PRIO))) {
                        return (o2.getStringValue(AVKey.DISPLAY_NAME) + o2.getLatlon().getLatitude())
                            .compareTo(o1.getStringValue(AVKey.DISPLAY_NAME) + o1.getLatlon().getLatitude());
                    } else {
                        return (Double.valueOf(o2.getValue(CombinedGazetteer.SEARCH_PRIO).toString()))
                            .compareTo(Double.valueOf(o1.getValue(CombinedGazetteer.SEARCH_PRIO).toString()));
                    }
                }

            });
        return list;
    }

}
