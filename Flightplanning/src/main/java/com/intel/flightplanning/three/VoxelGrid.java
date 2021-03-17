/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.three;


public class VoxelGrid {
//
//    public PlanVoxel[][][] voxels;
//    public int stepsX;
//    public int stepsY;
//    public int stepsZ;
//    public MinMaxPair minMaxX;
//    public MinMaxPair minMaxZ;
//    public MinMaxPair minMaxY;
//    public double rasterSize;
//
//    public int normalAvgSize;
//    public int normalAvgSize2;
//    public int[] normalRadiusY;
//    public int[][] normalRadiusZ;
//    public static final double sqrt12 = Math.sqrt(1. / 2);
//
//    public ITransformationProvider trafo;
//    private static final IElevationModel elevationModel =
//        DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
//
//    public VoxelGrid(
//            MMesh mesh, double sizeParallel, double sizeInFlight, double camAlt, ITransformationProvider trafo) {
//        Vector<MTriangle> triangles = mesh.triangles;
//        rasterSize = Math.min(Math.min(camAlt, sizeParallel), sizeInFlight) / 5;
//        System.out.println("rasterSize:" + rasterSize);
//        this.trafo = trafo;
//        minMaxX = new MinMaxPair();
//        minMaxZ = new MinMaxPair();
//        minMaxY = new MinMaxPair();
//        for (MTriangle triangle : triangles) {
//            minMaxX.enlarge(triangle.minMaxX);
//            minMaxY.enlarge(triangle.minMaxY);
//            minMaxZ.enlarge(triangle.minMaxZ);
//        }
//
//        /*System.out.println("init voxel grid");
//        System.out.println("minMaxX:" + minMaxX);
//        System.out.println("minMaxY:" + minMaxY + "  " + minMaxY.mean());
//        System.out.println("minMaxZ:" + minMaxZ);*/
//        minMaxX.enlarge(camAlt);
//        minMaxY.enlarge(camAlt);
//        minMaxZ.enlarge(camAlt); // ATTENTION, no one will fly below the ground anyway!!
//        //		MinMaxPair minMaxZOrg=minMaxZ;
//        //		minMaxZ = new MinMaxPair(Math.max(minAlt, minMaxZ.min),minMaxZ.max);
//
//        minMaxX.enlarge(2 * rasterSize);
//        minMaxY.enlarge(2 * rasterSize);
//        minMaxZ.enlarge(2 * rasterSize);
//        stepsX = (int)Math.ceil(minMaxX.size() / rasterSize) + 1;
//        stepsY = (int)Math.ceil(minMaxY.size() / rasterSize) + 1;
//        stepsZ = (int)Math.ceil(minMaxZ.size() / rasterSize) + 1;
//        initGrid();
//
//        /*System.out.println("init voxel grid");
//        System.out.println("minMaxX:" + minMaxX);
//        System.out.println("minMaxY:" + minMaxY + "  " + minMaxY.mean());
//        System.out.println("minMaxZ:" + minMaxZ);*/
//        // here we traid voxels as boxes, and the steps are describing the voxel boundary
//        fillVoxelsBasedOnTriangleIntersection(triangles);
//
//        // so now we can use this for the voxel centers in the future, so voxels are just the center dot.
//        minMaxX.shift(0.5 * rasterSize);
//        minMaxY.shift(0.5 * rasterSize);
//        minMaxZ.shift(0.5 * rasterSize);
//        detectOuterCluster();
//    }
//
//    public VoxelGrid(
//            Vector<Vec4> cropPolygon,
//            double sizeParallel,
//            double sizeInFlight,
//            double camAlt,
//            ITransformationProvider trafo) {
//        this.trafo = trafo;
//        rasterSize = Math.min(Math.min(camAlt, sizeParallel), sizeInFlight) / 5;
//        System.out.println("cropPoly:" + cropPolygon);
//        System.out.println("rasterSize:" + rasterSize);
//
//        minMaxX = new MinMaxPair();
//        minMaxY = new MinMaxPair();
//        for (Vec4 v : cropPolygon) {
//            minMaxX.update(v.x);
//            minMaxY.update(v.y);
//        }
//
//        minMaxX.enlarge(camAlt);
//        minMaxY.enlarge(camAlt);
//        minMaxX.enlarge(2 * rasterSize);
//        minMaxY.enlarge(2 * rasterSize);
//
//        stepsX = (int)Math.ceil(minMaxX.size() / rasterSize) + 1;
//        stepsY = (int)Math.ceil(minMaxY.size() / rasterSize) + 1;
//        // so now we can use this for the voxel centers in the future, so voxels are just the center dot.
//        minMaxX.shift(0.5 * rasterSize);
//        minMaxY.shift(0.5 * rasterSize);
//        List<LatLon> latLons = new ArrayList<LatLon>(stepsX * stepsY);
//        System.out.println("minMaxX:" + minMaxX);
//        System.out.println("minMaxY:" + minMaxY);
//        System.out.println("latLonRaster:" + (stepsX * stepsY));
//        if (stepsX * stepsY > 10_000_000) throw new RuntimeException("too large XY raster:" + stepsX * stepsY);
//        for (int x = 0; x != stepsX; x++) {
//            double dx = minMaxX.min + x * rasterSize;
//            for (int y = 0; y != stepsY; y++) {
//                double dy = minMaxY.min + y * rasterSize;
//                LatLon latLon = trafo.transformToGlobe(new Vec4(dx, dy));
//                latLons.add(latLon);
//            }
//        }
//
//        Sector sector = Sector.boundingSector(latLons);
//        System.out.println("sector:" + sector);
//        double[] elevations = new double[stepsX * stepsY];
//        double resTarget = rasterSize / Earth.WGS84_EQUATORIAL_RADIUS; // from meters to radians
//        double resAchieved = elevationModel.getElevations(sector, latLons, resTarget, elevations, null);
//        System.out.println("resAchieved:" + resAchieved);
//        minMaxZ = new MinMaxPair();
//        double offset = trafo.getHeightOffsetToGlobal();
//        for (double elevation : elevations) {
//            minMaxZ.update(elevation - offset);
//        }
//
//        System.out.println("minMaxHeight:" + minMaxZ);
//        minMaxZ.enlarge(camAlt);
//        minMaxZ.enlarge(2 * rasterSize);
//        stepsZ = (int)Math.ceil(minMaxZ.size() / rasterSize) + 1;
//        minMaxZ.shift(0.5 * rasterSize);
//
//        System.out.println("minMaxHeight:" + minMaxZ + " steps:" + stepsZ);
//        initGrid();
//
//        // fill grid
//        int elevIdx = 0;
//        for (int x = 0; x != stepsX; x++) {
//            for (int y = 0; y != stepsY; y++) {
//                double pZ = elevations[elevIdx];
//
//                int zMax = Math.min((int)Math.round((pZ - offset - minMaxZ.min) / rasterSize), stepsZ - 1);
//                for (int z = 0; z <= zMax; z++) {
//                    voxels[x][y][z].coreModel = true;
//                }
//
//                elevIdx++;
//            }
//        }
//
//        System.out.println("init voxel grid");
//        System.out.println("minMaxX:" + minMaxX);
//        System.out.println("minMaxY:" + minMaxY + "  " + minMaxY.mean());
//        System.out.println("minMaxZ:" + minMaxZ);
//        detectOuterCluster();
//    }
//
//    void initGrid() {
//        System.out.println("number voxels:" + stepsX * stepsY * stepsZ);
//
//        if (stepsX * stepsY * stepsZ > 300_000_000)
//            throw new RuntimeException("too large XYZ raster:" + stepsX * stepsY * stepsZ);
//        voxels = new PlanVoxel[stepsX][stepsY][stepsZ];
//
//        for (int x = 0; x != stepsX; x++) {
//            for (int y = 0; y != stepsY; y++) {
//                for (int z = 0; z != stepsZ; z++) {
//                    voxels[x][y][z] = new PlanVoxel();
//                }
//            }
//        }
//    }
//
//    private void fillVoxelsBasedOnTriangleIntersection(Collection<MTriangle> triangles) {
//
//        // additionally fill voxels it they intersect with any triangle...
//        // this is important for very thing structures which might not be found by raytracing
//        // but only filling voxels by this approach would lead to structures with holes inside... so we NEEED both,
//        // ratracing and voxel triangle intersection
//        for (MTriangle triangle : triangles) {
//            boolean[][][] visited = new boolean[stepsX][stepsY][stepsZ];
//
//            int x = (int)Math.floor((triangle.getA().getX() - minMaxX.min) / rasterSize);
//            int y = (int)Math.floor((triangle.getA().getY() - minMaxY.min) / rasterSize);
//            int z = (int)Math.floor((triangle.getA().getZ() - minMaxZ.min) / rasterSize);
//            //		System.out.println("NEXT");
//            updateVoxels(triangle, x, y, z, visited);
//        }
//    }
//
//    public VoxelGrid(
//            Sector sector, ITransformationProvider trafo, double sizeParallel, double sizeInFlight, double camAlt) {
//        // TODO raster elevation data
//    }
//
//    public void setupAveragingRadii(double sizeParallel, double sizeInFlight) {
//        normalAvgSize =
//            (int)
//                Math.round(
//                    Math.min(sizeParallel, sizeInFlight)
//                        / 2
//                        / rasterSize); // distance to the center point between tow neighbour images
//        normalAvgSize2 = normalAvgSize * normalAvgSize;
//        System.out.println("normalAvgSize=" + normalAvgSize);
//        normalRadiusY = new int[normalAvgSize + 1];
//        normalRadiusZ = new int[normalAvgSize + 1][normalAvgSize + 1];
//        for (int x = 0; x <= normalAvgSize; x++) {
//            int errStepsY = (int)Math.round(Math.sqrt(normalAvgSize2 - x * x));
//            normalRadiusY[x] = errStepsY;
//            for (int y = 0; y <= errStepsY; y++) {
//                int errStepsZ = (int)Math.round(Math.sqrt(normalAvgSize2 - x * x - y * y));
//                normalRadiusZ[x][y] = errStepsZ;
//            }
//        }
//    }
//
//    public void computeNormal(int x, int y, int z) {
//        PlanVoxel vi = voxels[x][y][z];
//        if (vi.normalComputed) return;
//        vi.normalComputed = true;
//
//        int minX = Math.max(0, x - normalAvgSize);
//        int maxX = Math.min(stepsX - 1, x + normalAvgSize);
//        int normalX = 0;
//        int normalY = 0;
//        int normalZ = 0;
//        int totalMass = 0;
//        for (int xi = minX; xi <= maxX; xi++) {
//            int dxi = Math.abs(xi - x);
//            int minY = Math.max(0, y - normalRadiusY[dxi]);
//            int maxY = Math.min(stepsY - 1, y + normalRadiusY[dxi]);
//
//            int dx = xi - x;
//
//            for (int yi = minY; yi <= maxY; yi++) {
//                int dyi = Math.abs(yi - y);
//                int dy = yi - y;
//
//                int minZ = Math.max(0, z - normalRadiusZ[dxi][dyi]);
//                int maxZ = Math.min(stepsZ - 1, z + normalRadiusZ[dxi][dyi]);
//                for (int zi = minZ; zi <= maxZ; zi++) {
//                    if (!voxels[xi][yi][zi].dilatedModel) continue;
//                    totalMass++;
//                    int dz = zi - z;
//
//                    normalX += dx;
//                    normalY += dy;
//                    normalZ += dz;
//                }
//            }
//        }
//
//        int lenMetro = Math.abs(normalX) + Math.abs(normalY) + Math.abs(normalZ);
//        if (lenMetro == 0) return;
//        vi.hasNormal = true;
//        vi.normal = new Vec4(normalX, normalY, normalZ);
//        double lenEuklid = vi.normal.getLength3();
//        vi.normal = vi.normal.divide3(lenEuklid);
//        vi.curving =
//            lenEuklid
//                / (3.
//                    / 8
//                    * normalAvgSize
//                    * totalMass); // of just a half sphere would be filled (as for a flat surface, we would expect a
//        // length of 3/8 * radius
//
//        //		System.out.println("volume:" + volume +  "  lenEuklid : " + lenEuklid + "  avgSize:"+normalAvgSize + " -->
//        // curved:"+vi.curving);
//        vi.fromSide = Math.abs(vi.normal.dot3(Vec4.UNIT_Z)) < sqrt12;
//    }
//
//    private void detectOuterCluster() {
//        // try to detect outer cluster
//        // all border non core voxels are outsited ;-)
//        for (int x = 0; x != stepsX; x++) {
//            for (int y = 0; y != stepsY; y++) {
//                if (!voxels[x][y][0].coreModel) voxels[x][y][0].isOutside = true;
//                if (!voxels[x][y][stepsZ - 1].coreModel) voxels[x][y][stepsZ - 1].isOutside = true;
//            }
//
//            for (int z = 0; z != stepsZ; z++) {
//                if (!voxels[x][0][z].coreModel) voxels[x][0][z].isOutside = true;
//                if (!voxels[x][stepsY - 1][z].coreModel) voxels[x][stepsY - 1][z].isOutside = true;
//            }
//        }
//
//        for (int y = 0; y != stepsY; y++) {
//            for (int z = 0; z != stepsZ; z++) {
//                if (!voxels[0][y][z].coreModel) voxels[0][y][z].isOutside = true;
//                if (!voxels[stepsX - 1][y][z].coreModel) voxels[stepsX - 1][y][z].isOutside = true;
//            }
//        }
//
//        boolean change = true;
//        int lm = 0;
//        while (change) {
//            // MM: Yes, I know their are way more efficient (single pass) algorithms, but I had only some minutes time
//            // for coding... ;-)
//            lm++;
//            System.out.println("outside detect cycle no:" + lm);
//            change = false;
//            for (int x = 1; x != stepsX - 1; x++) {
//                for (int y = 1; y != stepsY - 1; y++) {
//                    for (int z = 1; z != stepsZ - 1; z++) {
//                        if (voxels[x][y][z].isOutside || voxels[x][y][z].coreModel) continue;
//                        if (voxels[x - 1][y][z].isOutside
//                                || voxels[x][y - 1][z].isOutside
//                                || voxels[x][y][z - 1].isOutside) {
//                            change = true;
//                            voxels[x][y][z].isOutside = true;
//                        }
//                    }
//                }
//            }
//
//            for (int x = stepsX - 2; x > 0; x--) {
//                for (int y = stepsY - 2; y > 0; y--) {
//                    for (int z = stepsZ - 2; z > 0; z--) {
//                        if (voxels[x][y][z].isOutside || voxels[x][y][z].coreModel) continue;
//                        if (voxels[x + 1][y][z].isOutside
//                                || voxels[x][y + 1][z].isOutside
//                                || voxels[x][y][z + 1].isOutside) {
//                            change = true;
//                            voxels[x][y][z].isOutside = true;
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void updateVoxels(MTriangle t, int x, int y, int z, boolean visited[][][]) {
//        if (visited[x][y][z]) return;
//        visited[x][y][z] = true;
//
//        double[] boxStart =
//            new double[] {minMaxX.min + rasterSize * x, minMaxY.min + rasterSize * y, minMaxZ.min + rasterSize * z};
//        double[] boxEnd = new double[] {boxStart[0] + rasterSize, boxStart[1] + rasterSize, boxStart[2] + rasterSize};
//
//        boolean found = t.isTriangleIntersectingOrInsideBox(boxStart, boxEnd, rasterSize, rasterSize, rasterSize);
//
//        // System.out.print("x:"+x+"\ty:"+y+"\tz:" + z);
//        if (found) {
//            voxels[x][y][z].coreModel = true;
//            // System.out.println("\t X");
//        } else {
//            // System.out.println("\t ");
//            return;
//        }
//        // System.out.println("does intersect?\t" + lol);
//
//        if (x >= 1 && !visited[x - 1][y][z]) {
//            updateVoxels(t, x - 1, y, z, visited);
//        }
//
//        if (y >= 1 && !visited[x][y - 1][z]) {
//            updateVoxels(t, x, y - 1, z, visited);
//        }
//
//        if (z >= 1 && !visited[x][y][z - 1]) {
//            updateVoxels(t, x, y, z - 1, visited);
//        }
//
//        if (x < stepsX - 1 && !visited[x + 1][y][z]) {
//            updateVoxels(t, x + 1, y, z, visited);
//        }
//
//        if (y < stepsY - 1 && !visited[x][y + 1][z]) {
//            updateVoxels(t, x, y + 1, z, visited);
//        }
//
//        if (z < stepsZ - 1 && !visited[x][y][z + 1]) {
//            updateVoxels(t, x, y, z + 1, visited);
//        }
//    }
//
//    public void performDilation(int dilationSteps, boolean innerFalseOuterTrue) {
//        long t = System.currentTimeMillis();
//        int dilationSteps2 = dilationSteps * dilationSteps;
//        int[] dilationRadiusY = new int[dilationSteps + 1];
//        int[][] dilationRadiusZ = new int[dilationSteps + 1][dilationSteps + 1];
//        for (int x = 0; x <= dilationSteps; x++) {
//            int errStepsY = (int)Math.round(Math.sqrt(dilationSteps2 - x * x));
//            dilationRadiusY[x] = errStepsY;
//            for (int y = 0; y <= errStepsY; y++) {
//                int errStepsZ = (int)Math.round(Math.sqrt(dilationSteps2 - x * x - y * y));
//                dilationRadiusZ[x][y] = errStepsZ;
//            }
//        }
//
//        for (int x = 1; x != stepsX - 1; x++) {
//            for (int y = 1; y != stepsY - 1; y++) {
//                for (int z = 1; z != stepsZ - 1; z++) {
//                    if (!voxels[x][y][z].coreModel) continue;
//                    boolean cutXn = voxels[x - 1][y][z].coreModel;
//                    boolean cutXp = voxels[x + 1][y][z].coreModel;
//                    boolean cutYn = voxels[x][y - 1][z].coreModel;
//                    boolean cutYp = voxels[x][y + 1][z].coreModel;
//                    boolean cutZn = voxels[x][y][z - 1].coreModel;
//                    boolean cutZp = voxels[x][y][z + 1].coreModel;
//                    if (cutXn && cutXp && cutYn && cutYp && cutZn && cutZp) continue; // jht: i am in the middle, yay :)
//                    voxels[x][y][z].coreSurface = true;
//
//                    // okay, so i am on some corner... its worth to dilate
//                    int minX = cutXn ? x : Math.max(0, x - dilationSteps);
//                    int maxX = cutXp ? x : Math.min(stepsX - 1, x + dilationSteps);
//
//                    for (int xi = minX; xi <= maxX; xi++) {
//                        int dxi = Math.abs(xi - x);
//                        int minY = cutYn ? y : Math.max(0, y - dilationRadiusY[dxi]);
//                        int maxY = cutYp ? y : Math.min(stepsY - 1, y + dilationRadiusY[dxi]);
//                        for (int yi = minY; yi <= maxY; yi++) {
//                            int dyi = Math.abs(yi - y);
//
//                            int minZ = cutZn ? z : Math.max(0, z - dilationRadiusZ[dxi][dyi]);
//                            int maxZ = cutZp ? z : Math.min(stepsZ - 1, z + dilationRadiusZ[dxi][dyi]);
//                            for (int zi = minZ; zi <= maxZ; zi++) {
//                                if (innerFalseOuterTrue) {
//                                    voxels[xi][yi][zi].dilatedModel = true;
//                                } else {
//                                    voxels[xi][yi][zi].dilatedModelCollisionCheck = true;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        int filled = 0;
//        for (int x = 0; x != stepsX; x++) {
//            for (int y = 0; y != stepsY; y++) {
//                for (int z = 0; z != stepsZ; z++) {
//                    if (voxels[x][y][z].coreModel) {
//                        if (innerFalseOuterTrue) {
//                            voxels[x][y][z].dilatedModel = true;
//                        } else {
//                            voxels[x][y][z].dilatedModelCollisionCheck = true;
//                        }
//
//                        filled++;
//                        // System.out.print("X");
//                    } else {
//                        // System.out.print(" ");
//                    }
//                }
//                // System.out.print("\n");
//            }
//            // System.out.print("\n\n");
//        }
//
//        System.out.println("filled:" + filled);
//        System.out.println("tnew dilate:" + (System.currentTimeMillis() - t));
//    }
//
//    public void computeDilatedSurface() {
//        int filled = 0;
//        for (int x = 1; x != stepsX - 1; x++) {
//            for (int y = 1; y != stepsY - 1; y++) {
//                for (int z = 1; z != stepsZ - 1; z++) {
//                    if (voxels[x][y][z].dilatedModel) {
//                        boolean cutXn = voxels[x - 1][y][z].dilatedModel;
//                        boolean cutXp = voxels[x + 1][y][z].dilatedModel;
//                        boolean cutYn = voxels[x][y - 1][z].dilatedModel;
//                        boolean cutYp = voxels[x][y + 1][z].dilatedModel;
//                        boolean cutZn = voxels[x][y][z - 1].dilatedModel;
//                        boolean cutZp = voxels[x][y][z + 1].dilatedModel;
//                        voxels[x][y][z].dilatedSurface = !(cutXn && cutXp && cutYn && cutYp && cutZn && cutZp);
//                        if (voxels[x][y][z].dilatedSurface) {
//                            filled++;
//                        }
//                    }
//                }
//                // System.out.print("\n");
//            }
//            // System.out.print("\n\n");
//        }
//
//        System.out.println("computeDilatedSurface:" + filled);
//    }
//
//    public LinkedList<PlanLine> cutVoxelsToFlightLines(
//            double stepXYZ, final MinMaxPair minMaxCropZ, Vector<Vec4> cropPolygone) {
//        // int pointCnt=0;
//        // intersect in Z-direction
//        LinkedList<PlanLine> lines = new LinkedList<>();
//        MinMaxPair minMaxCropX = new MinMaxPair();
//        MinMaxPair minMaxCropY = new MinMaxPair();
//        if (cropPolygone != null) {
//            for (Vec4 v : cropPolygone) {
//                minMaxCropX.update(v.x);
//                minMaxCropY.update(v.y);
//            }
//        }
//
//        for (double pZ = Math.max(minMaxZ.min, minMaxCropZ.min);
//            pZ < Math.min(minMaxZ.max, minMaxCropZ.max);
//            pZ += stepXYZ) {
//            // this will be the small edge of the cam
//            PlanLine line = new PlanLine();
//            line.scanDir = Vec4.UNIT_Z;
//            line.scanLevel = pZ;
//            lines.add(line);
//            int z = (int)Math.round((pZ - minMaxZ.min) / rasterSize);
//            if (z < 1) {
//                z = 1; // make sure we are not at the boundary of the voxel set bevause of side effects
//            }
//
//            //			System.out.println("scan z=" + z);
//            for (int x = 1; x != stepsX - 1; x++) {
//                // jht position of point
//                double pX = minMaxX.min + x * rasterSize;
//                //				System.out.println();
//                if (cropPolygone != null && !minMaxCropX.contains(pX)) {
//                    continue;
//                }
//
//                for (int y = 1; y != stepsY - 1; y++) {
//                    if (voxels[x][y][z].dilatedModel
//                            && (!voxels[x - 1][y][z].dilatedModel
//                                || !voxels[x][y - 1][z].dilatedModel
//                                || !voxels[x + 1][y][z].dilatedModel
//                                || !voxels[x][y + 1][z].dilatedModel)) {
//                        computeNormal(x, y, z);
//                        if (voxels[x][y][z].fromSide == false) {
//                            continue;
//                        }
//
//                        LinePoint p = new LinePoint();
//                        double pY = minMaxY.min + y * rasterSize;
//                        if (cropPolygone != null && !minMaxCropY.contains(pY)) {
//                            continue;
//                        }
//
//                        p.p = new Vec4(pX, pY, pZ);
//                        if (cropPolygone != null && !AutoFPhelper.isInsidePolygone(cropPolygone, p.p)) {
//                            continue;
//                        }
//
//                        p.normal = voxels[x][y][z].normal;
//                        p.curving = voxels[x][y][z].curving;
//                        line.points.add(p);
//                        // pointCnt++;
//                    }
//                }
//            }
//
//            //			System.out.println("added Z-line:" + line.points.size() + " @ " + pZ + "m");
//        }
//
//        //	System.out.println("intersect in X");
//        // intersect in X-direction
//        int zMax =
//            minMaxCropZ.max == Double.POSITIVE_INFINITY
//                ? stepsZ - 1
//                : Math.min(stepsZ - 1, (int)Math.round((minMaxCropZ.max - minMaxZ.min) / rasterSize));
//        int zMin = Math.max(1, (int)Math.round((minMaxCropZ.min - minMaxZ.min) / rasterSize));
//
//        for (double pY = minMaxY.min; pY < minMaxY.max; pY += stepXYZ) {
//            if (cropPolygone != null && !minMaxCropY.contains(pY)) {
//                continue;
//            }
//
//            PlanLine line = new PlanLine();
//            line.scanDir = Vec4.UNIT_Y;
//            line.scanLevel = pY;
//            lines.add(line);
//
//            int y = (int)Math.round((pY - minMaxY.min) / rasterSize);
//            if (y == 0) y = 1;
//            for (int z = zMin; z < zMax; z++) {
//                double pZ = minMaxZ.min + z * rasterSize;
//                // this will be the small edge of the cam
//                for (int x = 1; x != stepsX - 1; x++) {
//                    if (voxels[x][y][z].dilatedModel
//                            && (!voxels[x][y - 1][z].dilatedModel
//                                || !voxels[x][y][z - 1].dilatedModel
//                                || !voxels[x][y + 1][z].dilatedModel
//                                || !voxels[x][y][z + 1].dilatedModel)) {
//                        computeNormal(x, y, z);
//                        if (voxels[x][y][z].fromSide == true) {
//                            continue;
//                        }
//
//                        // edge detected :-)
//                        LinePoint p = new LinePoint();
//                        double pX = minMaxX.min + x * rasterSize;
//                        if (cropPolygone != null && !minMaxCropX.contains(pX)) {
//                            continue;
//                        }
//
//                        p.p = new Vec4(pX, pY, pZ);
//                        if (cropPolygone != null && !AutoFPhelper.isInsidePolygone(cropPolygone, p.p)) {
//                            continue;
//                        }
//
//                        p.normal = voxels[x][y][z].normal;
//                        p.curving = voxels[x][y][z].curving;
//                        line.points.add(p);
//                        //			pointCnt++;
//                    }
//                }
//            }
//            //		System.out.println("added X-line:"+line.points.size() + " @ " + pX+"m");
//        }
//
//        return lines;
//    }
//
//    public void applyToAll(Consumer<PlanVoxel> consumer) {
//        for (int x = 0; x != stepsX; x++) {
//            for (int y = 0; y != stepsY; y++) {
//                for (int z = 0; z != stepsZ; z++) {
//                    consumer.accept(voxels[x][y][z]);
//                }
//                // System.out.print("\n");
//            }
//            // System.out.print("\n\n");
//        }
//    }
//
//    public void applyToAll(BiConsumer<Vec4, PlanVoxel> consumer) {
//        for (int x = 0; x != stepsX; x++) {
//            double dx = minMaxX.min + x * rasterSize;
//            for (int y = 0; y != stepsY; y++) {
//                double dy = minMaxY.min + y * rasterSize;
//                for (int z = 0; z != stepsZ; z++) {
//                    double dz = minMaxZ.min + z * rasterSize;
//                    consumer.accept(new Vec4(dx, dy, dz), voxels[x][y][z]);
//                }
//                // System.out.print("\n");
//            }
//            // System.out.print("\n\n");
//        }
//    }
//
//    Vec4 idxToVec(int x, int y, int z) {
//        double dx = minMaxX.min + x * rasterSize;
//        double dy = minMaxY.min + y * rasterSize;
//        double dz = minMaxZ.min + z * rasterSize;
//        return new Vec4(dy, dy, dz);
//    }
//
//    public int getMaxCoverage() {
//        int maxCoverage = 0;
//        for (int x = 0; x != stepsX; x++) {
//            for (int y = 0; y != stepsY; y++) {
//                for (int z = 0; z != stepsZ; z++) {
//                    PlanVoxel v = voxels[x][y][z];
//                    if (v.coverage > maxCoverage) maxCoverage = v.coverage;
//                }
//            }
//        }
//
//        return maxCoverage;
//    }
}
