/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.flightplan.computation.Binned3dList.DataElement;
import gov.nasa.worldwind.geom.Vec4;
import eu.mavinci.core.helper.MinMaxPair;

import java.util.Iterator;
import java.util.LinkedList;

public class Binned3dList<T> implements Iterable<DataElement<T>> {

    MinMaxPair minMaxX;
    MinMaxPair minMaxY;
    MinMaxPair minMaxZ;
    double maxSearchRadius;
    int dimX;
    int dimY;
    int dimZ;

    public static class DataElement<T> {
        Vec4 vec;
        int binX;
        int binY;
        int binZ;
        T data;
        int idxExternal;
        double distTmp2;
        Vec4 distVecTmp;

        public void updatePos(Vec4 vecNew, Binned3dList<T> bin) {
            int x = (int)((vecNew.x - bin.minMaxX.min) / bin.maxSearchRadius);
            int y = (int)((vecNew.y - bin.minMaxY.min) / bin.maxSearchRadius);
            int z = (int)((vecNew.z - bin.minMaxZ.min) / bin.maxSearchRadius);
            if (x < 0) {
                x = 0;
            }

            if (x >= bin.dimX) {
                x = bin.dimX - 1;
            }

            if (y < 0) {
                y = 0;
            }

            if (y >= bin.dimY) {
                y = bin.dimY - 1;
            }

            if (z < 0) {
                z = 0;
            }

            if (z >= bin.dimZ) {
                z = bin.dimZ - 1;
            }

            vec = vecNew;
            if (x == binX && y == binY && z == binZ) {
                return;
            }

            bin.data[binX][binY][binZ].remove(this);
            binX = x;
            binY = y;
            binZ = z;
            bin.data[binX][binY][binZ].add(this);
        }
    }

    LinkedList<DataElement<T>>[][][] data;

    LinkedList<DataElement<T>> dataAll = new LinkedList<>();

    @SuppressWarnings("unchecked")
    public Binned3dList(MinMaxPair minMaxX, MinMaxPair minMaxY, MinMaxPair minMaxZ, double maxSearchRadius) {
        this.minMaxX = minMaxX;
        this.minMaxY = minMaxY;
        this.minMaxZ = minMaxZ;
        this.maxSearchRadius = maxSearchRadius;

        dimX = (int)Math.ceil(minMaxX.size() / maxSearchRadius);
        dimY = (int)Math.ceil(minMaxY.size() / maxSearchRadius);
        dimZ = (int)Math.ceil(minMaxZ.size() / maxSearchRadius);

        data = new LinkedList[dimX][dimY][dimZ];
        for (int x = 0; x != dimX; x++) {
            for (int y = 0; y != dimY; y++) {
                for (int z = 0; z != dimZ; z++) {
                    data[x][y][z] = new LinkedList<>();
                }
            }
        }
    }

    public void add(Vec4 vec, T obj, int idxExternal) {
        int x = (int)((vec.x - minMaxX.min) / maxSearchRadius);
        int y = (int)((vec.y - minMaxY.min) / maxSearchRadius);
        int z = (int)((vec.z - minMaxZ.min) / maxSearchRadius);
        if (x < 0) {
            x = 0;
        }

        if (x >= dimX) {
            x = dimX - 1;
        }

        if (y < 0) {
            y = 0;
        }

        if (y >= dimY) {
            y = dimY - 1;
        }

        if (z < 0) {
            z = 0;
        }

        if (z >= dimZ) {
            z = dimZ - 1;
        }

        DataElement<T> dat = new DataElement<>();
        dat.vec = vec;
        dat.binX = x;
        dat.binY = y;
        dat.binZ = z;
        dat.data = obj;
        dat.idxExternal = idxExternal;
        data[x][y][z].add(dat);
        dataAll.add(dat);
    }

    public LinkedList<DataElement<T>> getNeighbours(DataElement<T> dat, double radius, int externalIdxMax) {
        LinkedList<DataElement<T>> res = new LinkedList<>();
        double radius2 = radius * radius;
        // int toSearch=0;
        for (int x = dat.binX - 1; x <= dat.binX + 1; x++) {
            if (x < 0 || x >= dimX) {
                continue;
            }

            for (int y = dat.binY - 1; y <= dat.binY + 1; y++) {
                if (y < 0 || y >= dimY) {
                    continue;
                }

                for (int z = dat.binZ - 1; z <= dat.binZ + 1; z++) {
                    if (z < 0 || z >= dimZ) {
                        continue;
                    }

                    LinkedList<DataElement<T>> cell = data[x][y][z];
                    for (DataElement<T> other : cell) {
                        if (other == dat) {
                            continue;
                        }

                        if (other.idxExternal <= externalIdxMax) {
                            continue;
                        }
                        // toSearch++;
                        Vec4 distVecTmp = dat.vec.subtract3(other.vec);
                        double distTmp2 = distVecTmp.getLengthSquared3();
                        if (distTmp2 <= radius2) {
                            // if (dat.idxExternal==53){
                            // System.out.println(" --53-> "+ other.idxExternal + " : "+Math.sqrt(distTmp2));
                            // }
                            other.distTmp2 = distTmp2;
                            other.distVecTmp = distVecTmp;
                            res.add(other);
                        }
                    }
                }
            }
        }
        // System.out.println("searched through "+toSearch + " neighb -> found "+ res.size());
        return res;
    }

    public void delete(DataElement<T> dat) {
        dataAll.remove(dat);
        data[dat.binX][dat.binY][dat.binZ].remove(dat);
    }

    @Override
    public Iterator<DataElement<T>> iterator() {
        return dataAll.iterator();
    }
}
