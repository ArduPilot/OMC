/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.geo.CountryDetector.RadioRegulation;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import eu.mavinci.core.helper.MinMaxPair;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class QuadTile extends GeoRestriction {
    public Sector sector;
    double radius;
    LatLon center;
    int level;
    String id;
    boolean regulationLocked;
    boolean isDefined;

    QuadTile[] childs;
    public static int count = 0;
    public static int countEffective = 0;
    public static int maxLevel = 0;

    public static final double MAX_RESOLUTION = 2000;
    // public static final double MAX_RESOLUTION=50000;//minimal cell radius we will archive. good default value = 2000m
    Collection<Country> countriesToCheck;
    QuadTile parent;

    public QuadTile(
            String id,
            Sector sector,
            Collection<Country> countriesToCheck,
            boolean isEUdef,
            boolean isRestrictedDef,
            CountryDetector.RadioRegulation radioRegulationDef) {
        this.sector = sector;
        this.id = id;
        this.countriesToCheck = countriesToCheck;
        count++;
        countEffective++;

        isRestricted = isRestrictedDef;
        radioRegulation = radioRegulationDef;
        isEU = isEUdef;
        level = 0;
        check();
    }

    public QuadTile(String id, Sector sector, Collection<Country> countriesToCheck, QuadTile parent) {
        this.sector = sector;
        this.id = id;
        this.countriesToCheck = countriesToCheck;
        this.parent = parent;
        count++;
        countEffective++;

        if (parent != null) {
            level = parent.level + 1;
            if (parent.isDefined) {
                overwriteRestrictions(parent);
                isDefined = true;
            }

            if (level > maxLevel) {
                maxLevel = level;
            }
        } else {
            level = 0;
        }

        check();
    }

    public static final int UNCERTAIN_LEVEL = 6;

    public void check() {
        center = sector.getCentroid();
        // boolean doProbe = sector.contains(CountryTransformator.probeLocation) & id.startsWith("32312");
        // boolean doProbe = id.startsWith("32312");
        // if (doProbe) {
        // System.out.println("\n\nID:"+id+" (lev "+level+")\nsector:" + sector);
        // }

        MinMaxPair radius = new MinMaxPair();
        for (LatLon corner : sector.getCorners()) {
            radius.update(
                Position.ellipsoidalDistance(center, corner, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS));
        }

        this.radius = radius.max;
        if (level <= UNCERTAIN_LEVEL) {
            // dirty fix. some southern countries are otherwise non contained in
            // the world at all due to different distance computation approaches
            // this will not harm compartness of quadtile pyramid, since it will be reduced afterwords anyway
            // but it might slow down generation of the pyramid a bit
            this.radius *= 4; // 2 is definitly too less
        }

        List<Country> countriesTotalCovering = null;
        List<Country> countriesPartlyCovering = null;
        // if (doProbe) System.out.println("radius:"+radius);
        for (Country c : countriesToCheck) {
            double dist = c.distance(center);
            // doProbe = doProbe&& c==CountryTransformator.cProbe;
            // doProbe = doProbe|| c==CountryTransformator.cProbe;
            // if (doProbe){
            // System.out.println(level+" sector:"+sector);
            // System.out.println("radius:"+radius + " dist:"+dist);
            // }
            // if (doProbe) System.out.println(c+"\t"+dist );
            // if (doProbe&& id.equals("32312")){
            // System.out.println(c);
            // System.out.println("border:"+c.borders.get(0));
            // }
            if (-dist
                    >= this.radius
                        + c.dataQuality) { // negative distance means we are inside, abs value is the distance to the
                                           // next
                // border
                if (countriesTotalCovering == null) {
                    countriesTotalCovering = new LinkedList<Country>();
                }

                countriesTotalCovering.add(c);
                // if (doProbe) System.out.println("inside");
            } else if (dist > this.radius + c.dataQuality) {
                // definitly outside
                // if (doProbe) System.out.println("def outside");
            } else if (Math.abs(dist) <= c.dataQuality - this.radius) {
                // definitly inside uncertency band along the border, refining in this band would not help!!
                // this countries will be handled as they are covered!
                if (countriesTotalCovering == null) {
                    countriesTotalCovering = new LinkedList<Country>();
                }

                countriesTotalCovering.add(c);
                // if (doProbe) System.out.println("def. inside uncertaincy of border");
            } else {
                if (countriesPartlyCovering == null) {
                    countriesPartlyCovering = new LinkedList<Country>();
                }

                countriesPartlyCovering.add(c);
                // if (doProbe) System.out.println("def. partly covered");
            }
        }
        // since until this level the radius value is much too large, total coverage will be wrong anyway!
        if (level <= UNCERTAIN_LEVEL && countriesTotalCovering != null) {
            if (countriesPartlyCovering == null) {
                countriesPartlyCovering = countriesTotalCovering;
            } else {
                countriesPartlyCovering.addAll(countriesTotalCovering);
            }

            countriesTotalCovering = null;
        }

        if (this.radius <= MAX_RESOLUTION && countriesPartlyCovering != null) {
            // it would not make sense to split this cell, handle all countries as totally covered
            if (countriesTotalCovering == null) {
                countriesTotalCovering = countriesPartlyCovering;
            } else {
                countriesTotalCovering.addAll(countriesPartlyCovering);
            }

            countriesPartlyCovering = null;
        }

        if (countriesTotalCovering != null) {
            isDefined = true;
            for (Country c : countriesTotalCovering) {
                // if (doProbe) System.out.println("totallyCovered:"+c);
                updateRestrictions(c);
            }
        }

        // if (doProbe) System.out.println("lev:"+level+"\t"+count+"\t"+sector+" restricted:"+isRestricted+" " +
        // radioRegulation);
        if (countriesPartlyCovering != null) {
            childs = new QuadTile[4];
            childs[0] =
                new QuadTile(
                    id + "0",
                    new Sector(
                        sector.getMinLatitude(), center.getLatitude(), sector.getMinLongitude(), center.getLongitude()),
                    countriesPartlyCovering,
                    this);
            childs[1] =
                new QuadTile(
                    id + "1",
                    new Sector(
                        sector.getMinLatitude(), center.getLatitude(), center.getLongitude(), sector.getMaxLongitude()),
                    countriesPartlyCovering,
                    this);
            childs[2] =
                new QuadTile(
                    id + "2",
                    new Sector(
                        center.getLatitude(), sector.getMaxLatitude(), sector.getMinLongitude(), center.getLongitude()),
                    countriesPartlyCovering,
                    this);
            childs[3] =
                new QuadTile(
                    id + "3",
                    new Sector(
                        center.getLatitude(), sector.getMaxLatitude(), center.getLongitude(), sector.getMaxLongitude()),
                    countriesPartlyCovering,
                    this);

            // its important to do this AFTER generating the childs, since they will derive the definitly
            // restricted and regulation style from this object, and the maybe stuff should
            // not be included at that moment
            // for (Country c :countriesPartlyCovering){
            // updateRestrictions(c);
            // }

            boolean needChilds = false;
            boolean firstChild = true;
            for (QuadTile child : childs) {
                if (!child.isDefined) {
                    continue;
                }

                isDefined = true;
                if (firstChild) {
                    updateRestrictions(child);
                    if (child.childs != null) {
                        needChilds = true;
                        break;
                    }

                    firstChild = false;
                } else if (!isSimilarRegulated(child) || child.childs != null) {
                    needChilds = true;
                    break;
                }
            }

            if (!needChilds) {
                childs = null;
                countEffective -= 4;
                // if (doProbe) System.out.println("delete" + sector);
            }
        }

        // this is not nessesary, but makes final result less random, better compressable, and nicer to look on..
        if (!isDefined) {
            isRestricted = false;
            radioRegulation = CountryDetector.RadioRegulation.other;
            isEU = false;
            // if (doProbe) System.out.println("final cleanup of non isDefined tile");
        }

        // if (doProbe) System.out.println( this + " FINAL");

        // if (id.equals("3231")){
        // int i=0;
        // for (QuadTile child:childs){
        // System.out.println("child " + i + " " + child);
        // i++;
        // }
        // }
        // if (doProbe) System.out.println("count:" + count + " countEffective: "+countEffective);
    }

    @Override
    public String toString() {
        return "lev:"
            + level
            + "\t"
            + id
            + "\t"
            + sector
            + "  restricted:"
            + isRestricted
            + " "
            + radioRegulation
            + "\tisEU:"
            + isEU
            + "\tisDefined:"
            + isDefined
            + "\tchilds:"
            + (childs != null);
    }

    public void updateRestrictions(Country c) {
        if (regulationLocked) {
            return;
        }

        if (c.iso2.equals(CountryDetector.MTR)) {
            super.overwriteRestrictions(c);
            regulationLocked = true;
        } else {
            super.updateRestrictions(c);
        }
    }

    public int getInt() {
        // if (id.startsWith("32312")) return 55;
        return radioRegulation.ordinal() + (isRestricted ? 128 : 0) + (isEU ? 64 : 0); // +(isDefined?32:0);
    }

    public int dumpToArray(int[] tileArray) {
        return dumpChildsToArray(tileArray, 0);
    }

    private int dumpChildsToArray(int[] tileArray, int nextFreeOffset) {
        int dumpPos = nextFreeOffset;
        nextFreeOffset += 4;
        for (int i = 0; i != 4; i++) {
            if (childs[i].childs == null) {
                tileArray[dumpPos + i] = childs[i].getInt();
            } else {
                tileArray[dumpPos + i] = -nextFreeOffset;
                nextFreeOffset = childs[i].dumpChildsToArray(tileArray, nextFreeOffset);
            }
        }

        return nextFreeOffset;
    }

    public int count() {
        if (childs == null) {
            return 1;
        }

        int count = 1;
        for (int i = 0; i != 4; i++) {
            count += childs[i].count();
        }

        return count;
    }

}
