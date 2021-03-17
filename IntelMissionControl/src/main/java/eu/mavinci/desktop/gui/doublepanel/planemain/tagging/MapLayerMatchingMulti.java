/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerListener;
import eu.mavinci.desktop.helper.IRecomputeRunnable;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

public class MapLayerMatchingMulti extends AMapLayerMatching {

    public MapLayerMatchingMulti() throws Exception {
        super(null, false);
        if (parent != null) {
            parent.addMapListener(parentListener);
        }

        postConstruction();
    }

    public void setParent(IMapLayer parent) {
        if (parent != this.parent && this.parent != null) {
            this.parent.removeMapListener(parentListener);
        }

        super.setParent(parent);
        if (parentListener != null && parent != null) {
            parent.addMapListener(parentListener);
        }
    }

    public void dispose() {
        recompSyncMatches = null; // stop recomputation
        storeToSessionNow();
        if (parent != null) {
            parent.removeMapListener(parentListener);
        }

        super.dispose();
    }

    private IMapLayerListener parentListener =
        new IMapLayerListener() {

            @Override
            public void mapLayerVisibilityChanged(IMapLayer layer, boolean newVisibility) {
                // System.out.println("mapLayerVisibilityChanged"+layer);
                if (layer instanceof MapLayerMatching) {
                    picAreasChanged(true);
                    resyncMatches();
                } else if (layer instanceof MapLayerPicArea || layer instanceof MapLayerPicAreas) {
                    picAreasChanged(true);
                }
            }

            @Override
            public void mapLayerValuesChanged(IMapLayer layer) {
                // System.out.println("mapLayerValuesChanged"+layer);
                if (layer instanceof MapLayerPicArea || layer instanceof MapLayerPicAreas) {
                    picAreasChanged(false);
                }
            }

            @Override
            public void mapLayerStructureChanged(IMapLayer layer) {
                // System.out.println("mapLayerStructureChanged"+layer);
                if (layer instanceof MapLayerPics) {
                    resyncMatches();
                } else if (layer instanceof MapLayerPicArea || layer instanceof MapLayerPicAreas) {
                    picAreasChanged(true);
                }
            }

            @Override
            public void childMapLayerRemoved(int i, IMapLayer layer) {
                // System.out.println("childMapLayerRemoved"+layer);
                if (layer instanceof MapLayerMatching) {
                    resyncMatches();
                } else if (layer instanceof MapLayerPicArea || layer instanceof MapLayerPicAreas) {
                    picAreasChanged(true);
                }
            }

            @Override
            public void childMapLayerInserted(int i, IMapLayer layer) {
                // System.out.println("childMapLayerInserted"+layer);
                if (layer instanceof MapLayerMatching) {
                    resyncMatches();
                } else if (layer instanceof MapLayerPicArea || layer instanceof MapLayerPicAreas) {
                    picAreasChanged(true);
                }
            }

        };

    private void picAreasChanged(boolean reCollect) {
        if (reCollect) {
            visiblePicAreas = null;
            allPicAreas = null;
            numberOfImagesPerPosition = -1;
        }

        if (areaEnabled) {
            // System.out.println("filte changed!");
            filterChanged(); // includes an cover.recomputeCoverage();
        } else {
            // System.out.println("recomputeCoverage only!");
            cover.recomputeCoverage();
        }
    }

    private IRecomputeRunnable recompRun =
        new IRecomputeRunnable() {

            @Override
            public void run() {
                // System.out.println("sync Matches!");

                Vector<MapLayerMatch> matches = new Vector<MapLayerMatch>();
                try {
                    for (IMapLayer lay : parent.getLayers()) {
                        MapLayerMatching matching = (MapLayerMatching)lay;
                        if (!matching.isVisible()) {
                            continue;
                        }

                        for (IMapLayer subLay : matching.getPicsLayer().getLayers()) {
                            if (subLay instanceof MapLayerMatch) {
                                MapLayerMatch match = (MapLayerMatch)subLay;
                                matches.add(match.cloneMatch(matching));
                            }
                        }
                    }

                    try {
                        Collections.sort(matches, comparatorMatchesLineOrder);
                    } catch (Throwable t) {
                        comparisonContractViolationFound = true;
                        Debug.getLog().log(Level.SEVERE, "could not sort matching in multiMatching", t);
                    }

                    pics.setMute(true);
                    synchronized (pics) {
                        while (pics.sizeMapLayer() != 0) {
                            pics.removeMapLayer(0);
                        }

                        for (MapLayerMatch match : matches) {
                            pics.addMapLayer(match);
                        }
                    }

                } catch (Throwable t) {
                    Debug.getLog().log(Level.SEVERE, "could not sync match into multiMatching", t);
                }

                resetEstimatedStartingElevation();
            }

            @Override
            public void runLaterOnUIThread() {
                pics.setMute(false);
                // System.out.println("avaliable pics="+pics.sizeMapLayer());
                filterChanged();
            }
        };
    private Recomputer recompSyncMatches = new Recomputer(recompRun);

    private void resyncMatches() {
        if (recompRun == null) {
            return;
        }

        recompSyncMatches.tryStartRecomp();
    }

    @Override
    protected void postConstruction() throws Exception {
        load();
        super.postConstruction();
    }

    @Override
    public File getResourceFile() {
        return null;
    }

    public void storeToSessionNow() {
        // TODO IMPLEMENT ME
        /*IProperties session = controller.getPlane().getSession();

        session.setProperty(KEY_ALTAGLENABLE, "" + altitudeAGLEnabled);
        session.setProperty(KEY_ALTVALUE, "" + altitudeValue);
        session.setProperty(KEY_ALTSPREAD, "" + altitudeSpread);
        session.setProperty(KEY_ROLLENABLE, "" + rollEnabled);
        session.setProperty(KEY_ROLLVALUE, "" + rollValue);
        session.setProperty(KEY_ROLLSPREAD, "" + rollSpread);
        session.setProperty(KEY_PITCHENABLE, "" + pitchEnabled);
        session.setProperty(KEY_PITCHVALUE, "" + pitchValue);
        session.setProperty(KEY_PITCHSPREAD, "" + pitchSpread);
        session.setProperty(KEY_YAWENABLE, "" + yawEnabled);
        session.setProperty(KEY_YAWVALUE, "" + yawValue);
        session.setProperty(KEY_YAWSPREAD, "" + yawSpread);

        session.setProperty(KEY_OnlyInPicArea, "" + onlyInPicArea);
        session.setProperty(KEY_UseAll, "" + useAll);
        session.setProperty(MapLayerCoverageMatching.KEY_Resolution, "" + cover.resolution);

        super.storeToSessionNow();
        */
    }

    @Override
    protected void load() {
        // TODO IMPLEMENT ME
        /*
        IProperties session = controller.getPlane().getSession();
        try {
            setMute(true);

            altitudeAGLEnabled = Boolean.parseBoolean(session.getProperty(KEY_ALTAGLENABLE, "" + altitudeAGLEnabled));
            altitudeValue = Double.parseDouble(session.getProperty(KEY_ALTVALUE, "" + altitudeValue));
            altitudeSpread = Double.parseDouble(session.getProperty(KEY_ALTSPREAD, "" + altitudeSpread));
            rollEnabled = Boolean.parseBoolean(session.getProperty(KEY_ROLLENABLE, "" + rollEnabled));
            rollValue = Double.parseDouble(session.getProperty(KEY_ROLLVALUE, "" + rollValue));
            rollSpread = Double.parseDouble(session.getProperty(KEY_ROLLSPREAD, "" + rollSpread));
            pitchEnabled = Boolean.parseBoolean(session.getProperty(KEY_PITCHENABLE, "" + pitchEnabled));
            pitchValue = Double.parseDouble(session.getProperty(KEY_PITCHVALUE, "" + pitchValue));
            pitchSpread = Double.parseDouble(session.getProperty(KEY_PITCHSPREAD, "" + pitchSpread));
            yawEnabled = Boolean.parseBoolean(session.getProperty(KEY_YAWENABLE, "" + yawEnabled));
            yawValue = Double.parseDouble(session.getProperty(KEY_YAWVALUE, "" + yawValue));
            yawSpread = Double.parseDouble(session.getProperty(KEY_YAWSPREAD, "" + yawSpread));

            onlyInPicArea = Boolean.parseBoolean(session.getProperty(KEY_OnlyInPicArea, "" + onlyInPicArea));
            useAll = Boolean.parseBoolean(session.getProperty(KEY_UseAll, "" + useAll));
            cover.resolution =
                Double.parseDouble(session.getProperty(MapLayerCoverageMatching.KEY_Resolution, "" + cover.resolution));
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "Could not load Matching File", e);
        } finally {
            setMute(false);
            mapLayerStructureChanged(this);
            filterChanged();
            setChanged(false);
        }*/
    }

    private ArrayList<MapLayerPicArea> visiblePicAreas = null;

    @Override
    public List<MapLayerPicArea> getVisiblePicAreas() {
        if (parent == null) {
            return null;
        }

        if (visiblePicAreas == null) {
            visiblePicAreas = new ArrayList<MapLayerPicArea>();
            for (IMapLayer lay : parent.getLayers()) {
                if (lay instanceof MapLayerMatching) {
                    MapLayerMatching matching = (MapLayerMatching)lay;
                    if (matching.isVisible()) {
                        visiblePicAreas.addAll(matching.getVisiblePicAreas());
                    }
                }
            }
            // System.out.println("visiblePicAreas="+visiblePicAreas);
        }

        return visiblePicAreas;
    }

    private ArrayList<MapLayerPicArea> allPicAreas = null;

    @Override
    public List<MapLayerPicArea> getPicAreas() {
        if (allPicAreas == null) {
            allPicAreas = new ArrayList<MapLayerPicArea>();

            for (IMapLayer lay : parent.getLayers()) {
                if (lay instanceof MapLayerMatching) {
                    MapLayerMatching matching = (MapLayerMatching)lay;
                    if (matching.isVisible()) {
                        for (IMapLayer subLay : matching.getPicAreasLayer().getLayers()) {
                            if (subLay instanceof MapLayerPicArea) {
                                MapLayerPicArea picArea = (MapLayerPicArea)subLay;
                                allPicAreas.add(picArea);
                            }
                        }
                    }
                }
            }
            // System.out.println("visiblePicAreas="+visiblePicAreas);
        }

        return allPicAreas;
    }

    @Override
    public File getMatchingFolder() {
        // TODO IMPLEMENT ME
        return null;
    }

    public void cloneMyValuesTo(MapLayerMatching other) throws Exception {
        //TODO add other values?
        other.areaEnabled= areaEnabled;
        other.cover.resolution = cover.resolution;
        other.bandNames = getBandNames();
        other.currentBandNo = currentBandNo;

        List<MapLayerPicArea> listMapLayerPicArea = getVisiblePicAreas();
        Ensure.notNull(listMapLayerPicArea, "listMapLayerPicArea");
        for (MapLayerPicArea picArea : listMapLayerPicArea) {
            other.getPicAreasLayer().addToFlightplanContainer(picArea.clone(other));
        }

        other.filterChanged();
    }

    /**
     * PLEASE ONLY CALL THIS, if you need an average over all images in the matching otherwise go to the
     * getLegacyMatching() function of single matches to get their individual ground reference
     */
    @Override
    public double getEstimatedStartingElevationInMoverWGS84() {
        // Debug.getLog().log(Level.SEVERE,"This function shoul never be called. blease use the Estimated elevation of
        // the individual
        // Matchings instead");
        return super.getEstimatedStartingElevationInMoverWGS84();
    }

    private int numberOfImagesPerPosition = -1;
    private String[] bandNames = null;

    @Override
    public synchronized int getNumberOfImagesPerPosition() {
        if (numberOfImagesPerPosition == -1) {
            allPicAreas = new ArrayList<MapLayerPicArea>();
            int tmp = Integer.MAX_VALUE;
            for (IMapLayer lay : parent.getLayers()) {
                if (lay instanceof MapLayerMatching) {
                    MapLayerMatching matching = (MapLayerMatching)lay;
                    if (matching.isVisible()) {
                        tmp = Math.min(tmp, matching.getNumberOfImagesPerPosition());
                    }
                }
            }

            if (tmp == Integer.MAX_VALUE) {
                tmp = 1;
            }

            String[] bandNamesTmp = new String[tmp];
            bandNamesTmp[0] = "rgb";
            for (IMapLayer lay : parent.getLayers()) {
                if (lay instanceof MapLayerMatching) {
                    MapLayerMatching matching = (MapLayerMatching)lay;
                    if (matching.isVisible()) {
                        for (int i = 0; i != tmp; i++) {
                            bandNamesTmp[i] = matching.getBandNames()[i];
                        }
                    }
                }
            }

            bandNames = bandNamesTmp;
            numberOfImagesPerPosition = tmp;
        }

        return numberOfImagesPerPosition;
    }

    @Override
    public String[] getBandNames() {
        if (numberOfImagesPerPosition < 0) {
            getNumberOfImagesPerPosition();
        }

        return bandNames;
    }

    @Override
    public String getName() {
        return "multi";
    }
}
