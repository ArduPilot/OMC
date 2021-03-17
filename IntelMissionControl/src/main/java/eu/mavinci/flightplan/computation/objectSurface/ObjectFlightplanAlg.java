/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.flightplan.ITransformationProvider;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.computation.FlightplanVertex;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import net.java.joglutils.model.ModelLoadException;
import org.asyncfx.concurrent.Dispatcher;

public class ObjectFlightplanAlg {

    private static final boolean optimizePath = true;

    public static final int maxProgress = 18;

    public static AllPointsResult computeObjectCoverageFlight(
            Vector<Vec4> polygon,
            double sizeParallel,
            double sizeInFlight,
            double camAlt,
            Vec4 vecBeforeGlobal,
            Vec4 vecAfterGlobal,
            PicArea picArea,
            ITransformationProvider trafo,
            IBackgroundTaskManager.BackgroundTask task)
            throws ModelLoadException, IOException {
        final double stepXYZ = Math.min(sizeInFlight, sizeParallel);
        final MinMaxPair minMaxAlt;
        final double safetyDist = picArea.getMinObjectDistance();
        final double scalingDistanceOnALine = 0.5;
        final double directionChangeRadToMeterPanelty = 3; // TODO find a good scale translation to meters
        final double cameraChangeRadToMeterPanelty =
            2; // TODO we need a relative cost factor to bring everything into one system
        final double maxPointDistance =
            2
                * Math.sqrt(
                    camAlt * camAlt
                        - safetyDist
                            * safetyDist); // make sure by diagonalizing a turn we dont come too close to an object

        final AllPointsResult result = new AllPointsResult();
        final MMesh mesh;
        final VoxelGrid grid;

        IHardwareConfiguration hwConf = picArea.getFlightplan().getHardwareConfiguration();
        if (picArea.getFlightplan().getRecalculateOnEveryChange()) {
            // cant dispatch the change immediately, otherwise UI wont notice it!
            Dispatcher.platform().runLater(() -> picArea.getFlightplan().setRecalculateOnEveryChange(false));
            return result;
        }

        if (picArea.getModelSource() == PicArea.ModelSourceTypes.MODEL_FILE) {
            task.updateProgress(1, maxProgress);
            task.updateMessage("load model");
            if (task.isCancelled()) {
                return null;
            }

            minMaxAlt = new MinMaxPair(picArea.getCropHeightMin(), picArea.getCropHeightMax());
            mesh = MeshLoader.loadMesh(new File(picArea.getModelFilePath()));
            mesh.transform(
                picArea.getModelAxisTransformationMatrix(),
                picArea.getModelAxisAlignmentX(),
                picArea.getModelAxisOffsetX(),
                picArea.getModelAxisAlignmentY(),
                picArea.getModelAxisOffsetY(),
                picArea.getModelAxisAlignmentZ(),
                picArea.getModelAxisOffsetZ(),
                picArea.getModelReferencePoint().getYaw() - (90 + picArea.getYaw()));
            grid = new VoxelGrid(mesh, sizeParallel, sizeInFlight, camAlt, trafo);
        } else {
            task.updateProgress(1, maxProgress);
            task.updateMessage("rasterize elevations");
            if (task.isCancelled()) {
                return null;
            }

            minMaxAlt = new MinMaxPair(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            mesh = null;
            grid = new VoxelGrid(polygon, sizeParallel, sizeInFlight, camAlt, trafo);
        }

        picArea.voxelGridTmp = grid;

        // dilation with cam alt
        int dilationSteps = (int)Math.round(camAlt / grid.rasterSize);

        int safetySteps = (int)Math.round(safetyDist / grid.rasterSize);
        System.out.println("dilationSteps:" + dilationSteps);

        task.updateProgress(2, maxProgress);
        task.updateMessage("dilation part 1");
        if (task.isCancelled()) {
            return null;
        }

        grid.performDilation(dilationSteps, true);

        task.updateProgress(3, maxProgress);
        task.updateMessage("dilation part 2");
        if (task.isCancelled()) {
            return null;
        }

        grid.performDilation(safetySteps, false);

        task.updateProgress(4, maxProgress);
        task.updateMessage("dilation part 3");
        if (task.isCancelled()) {
            return null;
        }

        grid.computeDilatedSurface();

        grid.setupAveragingRadii(sizeParallel, sizeInFlight);

        task.updateProgress(5, maxProgress);
        task.updateMessage("cut into flight lines");
        if (task.isCancelled()) {
            return null;
        }
        // cut covels map into slices and get the basic flight lines
        LinkedList<PlanLine> lines = grid.cutVoxelsToFlightLines(stepXYZ, minMaxAlt, polygon);

        task.updateProgress(6, maxProgress);
        task.updateMessage("sort points into lines");
        if (task.isCancelled()) {
            return null;
        }
        // sort points within lines
        lines = sortPointsWithinLines(lines, grid.rasterSize);

        task.updateProgress(7, maxProgress);
        task.updateMessage("sparse lies");
        if (task.isCancelled()) {
            return null;
        }
        // apply forward overlap distance on flight lines
        sparseLines(stepXYZ, lines, maxPointDistance);

        Vec4 before = vecBeforeGlobal;
        Vec4 beforeHeading = null;

        task.updateProgress(8, maxProgress);
        task.updateMessage("processing lines");
        if (task.isCancelled()) {
            return null;
        }

        lines = processFlightlines(mesh, lines, safetyDist, before, beforeHeading);

        Vector<Vector<FlightplanVertex>> allSubClouds = result.allSubClouds;
        Vector<Boolean> allSubCloudsClosed = new Vector<>();

        // sort lines into vertex list.... so only data transform
        task.updateProgress(9, maxProgress);
        task.updateMessage("create tours");
        if (task.isCancelled()) {
            return null;
        }

        createTour(lines, allSubClouds, allSubCloudsClosed, camAlt);

        // merge sub flights into big flight
        Vector<FlightplanVertex> flight = result.flightTour;
        for (Vector<FlightplanVertex> subFlight : allSubClouds) {
            flight.addAll(subFlight);
        }

        task.updateProgress(10, maxProgress);
        task.updateMessage("solve TSP");
        if (task.isCancelled()) {
            return null;
        }

        if (flight.isEmpty()) {
            return result;
        }

        double[][] costs =
            solveTsp(
                vecBeforeGlobal,
                stepXYZ,
                scalingDistanceOnALine,
                directionChangeRadToMeterPanelty,
                cameraChangeRadToMeterPanelty,
                result,
                grid,
                allSubClouds,
                allSubCloudsClosed,
                flight,
                task);

        task.updateProgress(14, maxProgress);
        task.updateMessage("compute distance");
        if (task.isCancelled()) {
            return null;
        }

        computeDistance(flight, costs);

        task.updateProgress(15, maxProgress);
        task.updateMessage("computeOrientations");
        if (task.isCancelled()) {
            return null;
        }

        computeOrientations(trafo, result);

        task.updateProgress(16, maxProgress);
        task.updateMessage("compute coverage");
        if (task.isCancelled()) {
            return null;
        }

        computeCoverage(camAlt, hwConf, result, grid);

        task.updateProgress(17, maxProgress);
        task.updateMessage("split tours");
        if (task.isCancelled()) {
            return null;
        }

        splitIntoSubtours(result);

        if (mesh != null) {
            task.updateProgress(18, maxProgress);
            task.updateMessage("collision sanety check");
            if (task.isCancelled()) {
                return null;
            }

            checkForCollisions(mesh, safetyDist, flight, costs);
        }

        return result;
    }

    private static double[][] solveTsp(
            Vec4 vecBeforeGlobal,
            double stepXYZ,
            double scalingDistanceOnALine,
            double directionChangeRadToMeterPanelty,
            double cameraChangeRadToMeterPanelty,
            AllPointsResult result,
            VoxelGrid grid,
            Vector<Vector<FlightplanVertex>> allSubClouds,
            Vector<Boolean> allSubCloudsClosed,
            Vector<FlightplanVertex> flight,
            IBackgroundTaskManager.BackgroundTask task) {
        // TODO jan
        // jht proably join here and re-order flight according to some energy distance
        //

        // final double maxRelatveDiveBelwoCamAlt = 0.5;

        task.updateProgress(11, maxProgress);
        task.updateMessage("compute cost matrix");
        if (task.isCancelled()) {
            return null;
        }

        TspPath<FlightplanVertex> tpath = new TspPath<FlightplanVertex>(flight);
        final int flightSize = flight.size();
        double costs[][] = new double[flight.size() + 1][flight.size() + 1];
        Vec4 directions[][] = new Vec4[flight.size() + 1][flight.size() + 1];
        for (int i = 0; i != flight.size(); i++) {
            flight.get(i).idx = i;
        }

        for (int i = 0; i < allSubClouds.size(); i++) {
            Vector<FlightplanVertex> tour = allSubClouds.get(i);

            // maximal cost between non neighbors inside one tour
            for (FlightplanVertex vertA : tour) {
                for (FlightplanVertex vertB : tour) {
                    if (vertA == vertB) break;
                    costs[vertA.idx][vertB.idx] = 10000;
                    costs[vertB.idx][vertA.idx] = 10000;
                    //					System.out.println("same tour NON neighbors: " + vertA.idx + " "+vertB.idx);
                }
            }

            FlightplanVertex vertLast = allSubCloudsClosed.get(i) ? tour.lastElement() : null;
            System.out.println("");
            // minimal cost on neigbors
            for (FlightplanVertex vert : tour) {
                if (vertLast != null) {
                    costs[vertLast.idx][vert.idx] =
                        costs[vert.idx][vertLast.idx] = vert.distanceAtWaypoint(vertLast) * scalingDistanceOnALine;

                    //					System.out.println("same tour neighbors: " + vert.idx + " "+vertLast.idx);
                }

                vertLast = vert;
            }
        }

        final double collisionPaneltyCost = 1000;

        for (int i = 0; i != flight.size() + 1; i++) {
            task.updateProgress(12 + i / (double)flightSize, maxProgress);
            task.updateMessage("collision matrix point i:" + i);
            if (task.isCancelled()) {
                return null;
            }
            //			System.out.println("coll from "+i);
            if (i < flight.size()) flight.get(i).idx = i;

            Vec4 a = i == flight.size() ? vecBeforeGlobal : flight.get(i).getWayPoint();
            Vec4 ca = i == flight.size() ? Vec4.UNIT_X : flight.get(i).getNormal();

            for (int j = 0; j < i; j++) {
                Vec4 b = flight.get(j).getWayPoint();
                Vec4 diff = a.subtract3(b).normalize3();
                directions[i][j] = diff;
                directions[j][i] = diff.getNegative3();

                if (costs[i][j] == 0) {
                    // this is NOT inside one tour... was set to a almost zero value allready

                    double cost = 0;
                    Vec4 pos = a;
                    Vec4 step = diff.normalize3().multiply3(stepXYZ);
                    int steps = (int)Math.ceil(diff.getLength3() / stepXYZ);
                    for (int s = 0; s < steps; s++) {
                        pos = pos.add3(step);
                        int x = (int)Math.floor((pos.x - grid.minMaxX.min) / grid.rasterSize);
                        int y = (int)Math.floor((pos.y - grid.minMaxY.min) / grid.rasterSize);
                        int z = (int)Math.floor((pos.z - grid.minMaxZ.min) / grid.rasterSize);
                        //						System.out.println(""+x +" " +y+" "+z);
                        if (x < 0 || x >= grid.stepsX) break;
                        if (y < 0 || y >= grid.stepsY) break;
                        if (z < 0 || z >= grid.stepsZ) break;
                        if (grid.voxels[x][y][z].dilatedModelCollisionCheck) {
                            cost = collisionPaneltyCost;
                            //							System.out.println("collision found "+i +" <-> " +j);
                            break;
                        }
                    }

                    /*for (MTriangle triangle : triangles) {
                    	DistanceResult res = triangle.getDistanceToLinesegment(a,b, camAlt);
                    	if (res == null || res.distance>= camAlt )continue;
                    	if (res.distance<= safetyDist){
                    		cost = collisionPaneltyCost;
                    		break;
                    	} else if (res.distance<= safetyDist + maxRelatveDiveBelwoCamAlt*(camAlt-safetyDist)){
                    		cost = Math.max(cost,Math.min(collisionPaneltyCost, collisionPaneltyCost*0.1*(camAlt-safetyDist)/(res.distance-safetyDist)));
                    	}
                    }*/

                    cost += a.distanceTo3(b);

                    Vec4 cb = flight.get(j).getNormal();
                    double angle = ca.angleBetween3(cb).getRadians();
                    cost += angle * cameraChangeRadToMeterPanelty;

                    costs[i][j] = cost;
                    costs[j][i] = cost;
                }
                //				System.out.println("coll to "+j +" --> "+costs[i][j]);
            }
        }

        System.out.println("DONE.....");

        tpath.distanceFunction =
            new EuclDistFP(costs, directions, flightSize, directionChangeRadToMeterPanelty, collisionPaneltyCost);

        // TspSolver testSolver = (TspSolver) new SimulatedAnnealing(1000,10000000,0.999999,tpath);
        // TspSolver testSolver = (TspSolver) new SimulatedAnnealing(1000,10000000,0.999999,tpath);
        TspSolver testSolver = (TspSolver)new SimulatedAnnealing(1000, 50000000, 0.99999, tpath);

        if (optimizePath) {
            testSolver.solve(task); // TODO FIXME reinclude me

            flight = new Vector<FlightplanVertex>();
            for (FlightplanVertex node : (List<FlightplanVertex>)testSolver.getPath().nodes) {
                flight.add(node);
            }

            result.flightTour = flight;
        } else {
            flight = result.flightTour;
        }

        return costs;
    }

    private static void checkForCollisions(
            MMesh mesh, double safetyDist, Vector<FlightplanVertex> flight, double[][] costs) {
        System.out.println("==================\n===================\nlist collisions");
        //		collisionCheck(triangles, safetyDist, flight);
        // COLLISION CHECK
        FlightplanVertex last = null;
        for (FlightplanVertex p : flight) {
            if (last == null) {
                last = p;
                continue;
            }

            boolean found = false;
            for (MTriangle triangle : mesh.triangles) {
                MTriangle.DistanceResult res =
                    triangle.getDistanceToLinesegment(last.getWayPoint(), p.getWayPoint(), safetyDist);
                if (res != null && res.distance < safetyDist) {
                    if (!found) {
                        System.out.println("--------\nLINE:" + last.getWayPoint() + " to " + p.getWayPoint());
                        System.out.println("cost on this edge:" + costs[last.idx][p.idx]);
                    }

                    System.out.println("HITTT:" + res.distance + "m on " + res.closestOnSegment);

                    found = true;
                }
            }

            last = p;
        }

        System.out.println("compute done");
    }

    private static void splitIntoSubtours(AllPointsResult result) {
        // regroup subtours to prevent hitting max points limit
        result.allSubClouds.clear();
        Vector<FlightplanVertex> lastLine = new Vector<>();
        result.allSubClouds.add(lastLine);
        for (FlightplanVertex vert : result.flightTour) {
            if (lastLine.size() >= ReentryPointID.maxNoRefinements - 10) {
                lastLine = new Vector<>();
                result.allSubClouds.add(lastLine);
            }

            lastLine.add(vert);
        }
    }

    private static void computeCoverage(
            double camAlt, IHardwareConfiguration hwConf, AllPointsResult result, VoxelGrid grid) {
        // compute coverage
        System.out.println("compute coverage");
        int n = 0;
        for (FlightplanVertex p : result.flightTour) {
            p.flag = false;
        }

        for (FlightplanVertex p : result.flightTour) {
            n++;
            //			if (n<4) continue;
            //			if (n==6) break;
            p.flag = true;
            System.out.println("\nVERTEX[" + n + "]:" + p);
            CPhotoLogLine line = new CPhotoLogLine(0, 0, 0, p.getOrientation());
            System.out.println("or:" + p.getOrientation());
            Matrix cameraTransform = CameraHelper.getCorrectedStateTransform(line, 0, 0, hwConf);
            //			System.out.println("pos:"+pos + " orientation:"+orientation );

            Vec4[] cornerDirections = CameraHelper.getCornerDirections(hwConf);
            Vec4 origin = p.getWayPoint(); // new Vec4(0, 0, 0, 0);
            for (int k = 0; k < 4; k++) {
                cornerDirections[k] = cornerDirections[k].transformBy3(cameraTransform);
                //	            cornerDirections[k] = cornerDirections[k].normalize3();
                //				System.out.println("orginal corner:" +k + "  -> " + cornerDirections[k]);
            }

            Vector<Vec4> rays = new Vector<Vec4>();
            int sampleSize = 100;
            int xSteps =
                hwConf.getPrimaryPayload(IGenericCameraConfiguration.class).getDescription().getCcdResX() / sampleSize;
            int ySteps =
                hwConf.getPrimaryPayload(IGenericCameraConfiguration.class).getDescription().getCcdResY() / sampleSize;
            double tolerance = 0.20;
            //			System.out.println();
            for (int x = (int)(xSteps * tolerance); x <= (int)(xSteps * (1 - tolerance)); x++) {
                double xRel = ((double)x) / xSteps;
                for (int y = (int)(ySteps * tolerance); y <= (int)(ySteps * (1 - tolerance)); y++) {
                    double yRel = ((double)y) / ySteps;
                    // linear interpolation of the 4 corners
                    rays.add(
                        cornerDirections[0]
                            .multiply3((1 - yRel) * (1 - xRel))
                            .add3(cornerDirections[1].multiply3(yRel * (1 - xRel)))
                            .add3(cornerDirections[3].multiply3((1 - yRel) * (xRel)))
                            .add3(cornerDirections[2].multiply3((yRel) * (xRel))));
                    //					System.out.println("x:"+x+" y:"+y+" -> "+rays.lastElement());
                }
            }
            //			System.out.println("num of rays:"+rays.size());
            for (Vec4 ray : rays) {
                double max = Math.max(Math.max(Math.abs(ray.x), Math.abs(ray.y)), Math.abs(ray.z));
                //				System.out.println(ray+"max:"+max + " step:"+rasterSize);
                ray =
                    ray.multiply3(
                        grid.rasterSize / max * 0.5); // making it stepping half a voxel at max speed direction
                //				Vec4 rayNormal = ray.normalize3().multiply3(-1);
                Vec4 pos = origin;
                //				System.out.println("ray:"+ray);
                // TODO... maybe fast forward some pixels?

                while (grid.minMaxX.contains(pos.x) && grid.minMaxY.contains(pos.y) && grid.minMaxZ.contains(pos.z)) {
                    int x = (int)Math.floor((pos.x - grid.minMaxX.min) / grid.rasterSize);
                    int y = (int)Math.floor((pos.y - grid.minMaxY.min) / grid.rasterSize);
                    int z = (int)Math.floor((pos.z - grid.minMaxZ.min) / grid.rasterSize);
                    //					System.out.println("pos:"+pos + " "+x+" "+y +" "+z);
                    //					voxels[x][y][z].coverage++;//tracking rays

                    if (grid.voxels[x][y][z].coreSurface) {
                        if (grid.voxels[x][y][z].lastCoveringImg != p) {
                            // only count not too steep mappings
                            //							System.out.println("hit:");
                            //							computeNormalEreased(x,y,z);
                            //							System.out.println("normal:"+voxels[x][y][z].normalEreased);
                            //							if (voxels[x][y][z].hasNormalEreased){
                            //								double angleDifference = voxels[x][y][z].normalEreased.dot3(p.getNormal());
                            //								System.out.println("hit with angle:"+angleDifference);
                            ////								if (angleDifference>90){
                            ////									angleDifference= 180-angleDifference;
                            ////								} else if (angleDifference<-90){
                            ////									angleDifference=-180-angleDifference;
                            ////								}
                            //								if (angleDifference>-Math.sqrt(0.5)) { //TODO, this wont work on thin surfaces
                            // which have basically no normal vector
                            ////									System.out.println("skipping point:"+angleDifference);
                            //									break;
                            //								}
                            //							}
                            //							System.out.println("cover:" +x +" " +y+" "+ z + " pos:"+pos);
                            grid.voxels[x][y][z].coverage++;
                            grid.voxels[x][y][z].lastCoveringImg = p;
                        }

                        break;
                    }

                    if (grid.voxels[x][y][z].coreModel)
                        break; // if we just somehow shot inside the plane, stop propagating!!

                    pos = pos.add3(ray);
                    double dist = Math.abs(pos.subtract3(origin).dot3(p.getNormal()));
                    //					System.out.println("dist:"+dist);
                    if (dist > camAlt * 2)
                        break; // stop raytracing if projected camera distance is more then X times the desired one
                }
            }
        }
    }

    private static void computeOrientations(ITransformationProvider trafo, AllPointsResult result) {
        // compute orientations BASED ON ORIGINAL FLIGHT LINES... to get them straight!!!
        for (Vector<FlightplanVertex> line : result.allSubClouds) {
            Orientation lastOrientation = null;
            FlightplanVertex lastVert =
                line.size() > 1
                    ? line.get(1)
                    : null; // just to get yaw for first waypoint correct, handle second point as its precessor and swap
            // yaw by 180°
            boolean first = true;

            for (FlightplanVertex vert : line) {
                Angle yaw;
                Vec4 dCenter;
                if (lastVert != null) {
                    dCenter =
                        first
                            ? lastVert.getCenterPoint().subtract3(vert.getCenterPoint())
                            : vert.getCenterPoint().subtract3(lastVert.getCenterPoint());
                    yaw =
                        trafo.transformYawToGlobal(
                            Angle.fromXY(
                                dCenter.y,
                                dCenter.x)); // x and y swapped by intention to get coorect geographic northing angles
                    first = false;
                    lastVert = vert;
                } else {
                    yaw = Angle.ZERO;
                    dCenter = Vec4.UNIT_X;
                }

                lastOrientation = vert.getRollPitchYaw(dCenter, yaw, lastOrientation, trafo);
                vert.setOrientation(lastOrientation);
            }
        }
    }

    private static void computeDistance(Vector<FlightplanVertex> flight, double[][] costs) {
        double flightDistance = 0.0;
        double cost = 0;
        if (!flight.isEmpty()) {
            FlightplanVertex v = flight.get(0);
            int i = 0;
            for (FlightplanVertex vertex : flight) {
                if (i == 0) {
                    i++;
                    continue;
                }

                flightDistance += vertex.getWayPoint().distanceTo3(v.getWayPoint());
                cost += costs[vertex.idx][v.idx];
                v = vertex;

                // System.out.println("i:" + flightDistance);
            }
        }

        System.out.println("lolTotal flight distance in meters:" + flightDistance + "  at cost of:" + cost);
    }

    private static void collisionCheck(
            Vector<MTriangle> triangles, double safetyDist, Vector<FlightplanVertex> flight) {
        // COLLISION CHECK
        FlightplanVertex last = null;
        for (FlightplanVertex p : flight) {
            if (last == null) {
                last = p;
                continue;
            }

            boolean found = false;
            for (MTriangle triangle : triangles) {
                MTriangle.DistanceResult res =
                    triangle.getDistanceToLinesegment(last.getWayPoint(), p.getWayPoint(), safetyDist);
                if (res != null && res.distance < safetyDist) {
                    if (!found) {
                        System.out.println("--------\nLINE:" + last.getWayPoint() + " to " + p.getWayPoint());
                    }

                    System.out.println("HITTT:" + res.distance + "m on " + res.closestOnSegment);

                    found = true;
                }
            }

            last = p;
        }
    }

    private static void createTour(
            LinkedList<PlanLine> lines,
            Vector<Vector<FlightplanVertex>> allSubClouds,
            Vector<Boolean> allSubCloudsClosed,
            double camAlt) {
        int i = 0;
        for (PlanLine line : lines) {
            Vector<FlightplanVertex> tour = new Vector<>();
            //			System.out.println(cntK+" closed:"+line.closedLoop);
            allSubCloudsClosed.add(line.closedLoop);
            allSubClouds.addElement(tour);

            for (LinePoint p : line.points) {
                // if (p.normal==null) continue;//p.normal=Vec4.ZERO;//TODO FIXME

                FlightplanVertex v = new FlightplanVertex(p.p.add3(p.normal.multiply3(camAlt)), p.p);
                // FlightplanVertex v = new FlightplanVertex(p.p, p.p);
                tour.add(v);
                System.out.println(i + "\t" + line.scanLevel + "\t" + line.scanDir + p.p);
                i++;
            }
            //			cntK++;
        }
    }

    // jht what is this whole thing doing, what is this flipping stuff?
    private static LinkedList<PlanLine> processFlightlines(
            MMesh mesh, LinkedList<PlanLine> lines, double safetyDist, Vec4 before, Vec4 beforeHeading) {
        LinkedList<PlanLine> linesNew = new LinkedList<>();
        boolean needBackflip = false;
        for (int i = 0; i != lines.size(); i++) {
            PlanLine line = lines.get(i);
            //			System.out.println("line " + i + " scanDir:"+line.scanDir + " level:"+line.scanLevel + "
            // looped:"+line.closedLoop);
            if (needBackflip) {
                System.out.println("do old flip");
                needBackflip = false;
                line = lines.get(i - 1);
            } else {
                if (i < lines.size() - 1 && i > 0) {
                    // sneak the next line... todo, improve this idea!!
                    PlanLine next = lines.get(i + 1);
                    //					System.out.println("next " + (i+1) + " scanDir:"+next.scanDir + " level:"+next.scanLevel + "
                    // looped:"+next.closedLoop);

                    if (line.scanDir == next.scanDir
                            && line.scanLevel == next.scanLevel
                            && !line.closedLoop
                            && !next.closedLoop) {
                        System.out.println("sneek line!");
                        double distThisFirst =
                            before == null || mesh == null
                                ? 0
                                : before.distanceTo3(line.points.getFirst().p)
                                    + (mesh.checkCollision(safetyDist, before, line.points.getFirst().p) < safetyDist
                                        ? Double.POSITIVE_INFINITY
                                        : 0);
                        double distThisLast =
                            before == null || mesh == null
                                ? 0
                                : before.distanceTo3(line.points.getLast().p)
                                    + (mesh.checkCollision(safetyDist, before, line.points.getLast().p) < safetyDist
                                        ? Double.POSITIVE_INFINITY
                                        : 0);
                        double distThis = Math.min(distThisLast, distThisFirst);

                        double distNextFirst =
                            before == null || mesh == null
                                ? 0
                                : before.distanceTo3(next.points.getFirst().p)
                                    + (mesh.checkCollision(safetyDist, before, next.points.getFirst().p) < safetyDist
                                        ? Double.POSITIVE_INFINITY
                                        : 0);
                        double distNextLast =
                            before == null || mesh == null
                                ? 0
                                : before.distanceTo3(next.points.getLast().p)
                                    + (mesh.checkCollision(safetyDist, before, next.points.getLast().p) < safetyDist
                                        ? Double.POSITIVE_INFINITY
                                        : 0);
                        double distNext = Math.min(distNextLast, distNextFirst);

                        if (distThis == Double.POSITIVE_INFINITY && distNext == Double.POSITIVE_INFINITY) {
                            System.out.println("will be close");
                            distThisFirst =
                                before == null || mesh == null
                                    ? 0
                                    : (mesh.checkCollision(safetyDist, before, line.points.getFirst().p));
                            distThisLast =
                                before == null || mesh == null
                                    ? 0
                                    : (mesh.checkCollision(safetyDist, before, line.points.getLast().p));
                            distThis = Math.max(distThisLast, distThisFirst);

                            distNextFirst =
                                before == null || mesh == null
                                    ? 0
                                    : (mesh.checkCollision(safetyDist, before, next.points.getFirst().p));
                            distNextLast =
                                before == null || mesh == null
                                    ? 0
                                    : (mesh.checkCollision(safetyDist, before, next.points.getLast().p));
                            distNext = Math.max(distNextLast, distNextFirst);

                            if (distThis < distNext) {
                                needBackflip = true;
                                //								System.out.println("flip!");
                                line = next;
                            }
                        } else if (distThis > distNext) {
                            //							System.out.println("select shortest AND FLIP");
                            needBackflip = true;
                            line = next;
                        }
                    }
                }
            }

            if (line.points.size() == 1) {
                before = line.points.getFirst().p;
                beforeHeading = linesNew.isEmpty() ? null : linesNew.getLast().points.getLast().p.subtract3(before);
                linesNew.add(line);
            }

            if (line.points.size() <= 1) {
                continue;
            }
            //			System.out.println("next line: "+i+"  looped:"+line.closedLoop);
            if (line.closedLoop) {
                int bestStartIdx = 0;
                double bestStartDist = Double.POSITIVE_INFINITY;

                int leastWorseStartIdx = 0;
                double leastWorseStartDist = 0;

                //				System.out.println("search closed: before: " + before );
                for (int idx = 0; idx < line.points.size(); idx++) {
                    int idxNext = (idx + 1) % line.points.size();
                    LinePoint pA = line.points.get(idx);
                    LinePoint pB = line.points.get(idxNext);
                    double distSafetyA = mesh == null ? 0 : mesh.checkCollision(safetyDist, before, pA.p);
                    double distBeforeA =
                        before == null
                            ? 0
                            : before.distanceTo3(pA.p) + (distSafetyA < safetyDist ? Double.POSITIVE_INFINITY : 0);

                    double distAB = pA.p.distanceTo3(pB.p);

                    double distAFirst = distBeforeA - distAB;

                    if (distAFirst < bestStartDist) {
                        bestStartDist = distAFirst;
                        bestStartIdx = idx;
                    }

                    if (distSafetyA > leastWorseStartDist) {
                        leastWorseStartDist = distSafetyA;
                        leastWorseStartIdx = idx;
                    }
                }
                //				System.out.println("next: idx:"+ bestStartIdx+" p:"+line.points.get(bestStartIdx).p + "
                // dist:"+bestStartDist);
                if (bestStartDist == Double.POSITIVE_INFINITY) {
                    bestStartIdx = leastWorseStartIdx;
                }

                LinkedList<LinePoint> pointsNew = new LinkedList<>();
                for (int idx = 0; idx < line.points.size(); idx++) {
                    int idxShifted = (idx + bestStartIdx) % line.points.size();
                    pointsNew.add(line.points.get(idxShifted));
                }

                if (pointsNew.size() > 1 && beforeHeading != null) {
                    Vec4 headingNew = pointsNew.getLast().p.subtract3(pointsNew.get(pointsNew.size() - 2).p);
                    if (headingNew.dot3(beforeHeading) < 0) Collections.reverse(pointsNew);
                }

                line.points = pointsNew;
            } else {
                double distBeforeFirst =
                    before == null || mesh == null
                        ? 0
                        : before.distanceTo3(line.points.getFirst().p)
                            + (mesh.checkCollision(safetyDist, before, line.points.getFirst().p) < safetyDist
                                ? Double.POSITIVE_INFINITY
                                : 0);
                double distBeforeLast =
                    before == null || mesh == null
                        ? 0
                        : before.distanceTo3(line.points.getLast().p)
                            + (mesh.checkCollision(safetyDist, before, line.points.getLast().p) < safetyDist
                                ? Double.POSITIVE_INFINITY
                                : 0);

                double min = Math.min(distBeforeFirst, distBeforeLast);
                //				System.out.println("min:"+min);
                if (min == Double.POSITIVE_INFINITY && mesh != null) {
                    // backtracking
                    // search for some spot in the line without collision
                    // find the point which is least worse
                    int bestN = 0;
                    double bestVal = Double.POSITIVE_INFINITY;
                    double bestSafety = 0;
                    int safetyN = 0;
                    for (int n = 0; n != line.points.size(); n++) {
                        double safety = mesh.checkCollision(safetyDist, before, line.points.get(n).p);
                        if (safety > bestSafety) {
                            bestSafety = safety;
                            safetyN = n;
                        }

                        if (safety < safetyDist) {
                            double dist = before.distanceTo3(line.points.get(n).p);
                            if (dist < bestVal) {
                                bestVal = dist;
                                bestN = n;
                            }
                        }
                    }

                    if (bestVal == Double.POSITIVE_INFINITY) {
                        bestN = safetyN;
                        System.err.println("still unfixed non cloed: least wr");
                    }

                    LinkedList<LinePoint> pointsNew = new LinkedList<>();
                    if (bestN < line.points.size() - 1) {
                        for (int m = bestN; m > 0; m--) {
                            pointsNew.add(line.points.get(m));
                        }

                        pointsNew.addAll(line.points);
                    } else {
                        for (int m = bestN; m < line.points.size() - 1; m++) {
                            pointsNew.add(line.points.get(m));
                        }

                        Collections.reverse(line.points);
                        pointsNew.addAll(line.points);
                    }

                    line.points = pointsNew;
                } else {
                    if (distBeforeFirst > distBeforeLast) {
                        Collections.reverse(line.points);
                    }
                }
                //
            }

            linesNew.add(line);
            before = line.points.getLast().p;
            beforeHeading = before.subtract3(line.points.get(line.points.size() - 2).p);
        }

        return linesNew;
    }

    private static void sparseLines(double stepXYZ, LinkedList<PlanLine> lines, double maxPointDistance) {
        // sample on the lines / sparse them
        for (PlanLine line : lines) {
            LinePoint last = line.points.getFirst();
            Iterator<LinePoint> it = line.points.iterator();
            it.next(); // first is allready sampled above..without this he would be deleted!
            while (it.hasNext()) {
                LinePoint point = it.next();

                // sparsing on outer curves points, and densifying on innter curves.
                // the exponents have been experimental
                double curving = Math.min(point.curving, last.curving);
                double exponent = curving <= 1 ? 1.5 : 3; // 1.4;//densify more inside then removing outside
                double dist = Math.min(maxPointDistance, stepXYZ * Math.pow(curving, exponent));
                if (point.p.distanceTo3(last.p) < dist && (it.hasNext() || line.closedLoop)) {
                    it.remove();
                } else {
                    last = point;
                }
            }
        }
    }

    // jht what is the idea behind this code?
    private static LinkedList<PlanLine> sortPointsWithinLines(LinkedList<PlanLine> lines, double rasterSize) {

        //		int filled = 0;
        // this is the square of the distance in which another dot in a plane in the voxel grid could be considered as
        // neighbor
        double searchRad2 = rasterSize * rasterSize * 2 * 1.05; // each line is
        // a 2d object,
        // so times two
        // is sufficent
        LinkedList<PlanLine> linesNew = new LinkedList<>();

        for (PlanLine line : lines) {
            LinkedList<LinePoint> recentPoints = new LinkedList<>();
            while (!line.points.isEmpty()) {

                // System.out.println("new line no:"+filled);
                //				filled++;

                LinePoint last = line.points.pop();
                PlanLine lineNew = new PlanLine();
                lineNew.scanDir = line.scanDir;
                lineNew.scanLevel = line.scanLevel;
                lineNew.points.add(last);

                // pick a random point and start searching.
                // select THE CLOSEST neighbor, to make sure we are not jumping
                // over someone
                // System.out.println("------------------:"+lineNew.scanLevel+"
                // dir:"+lineNew.scanDir);
                while (!line.points.isEmpty()) {
                    LinePoint best = null;
                    double bestDist2 = Double.POSITIVE_INFINITY;
                    int idxBest = -1;

                    ListIterator<LinePoint> it = line.points.listIterator();
                    // System.out.println("last:"+last);
                    while (it.hasNext()) {
                        int idx = it.nextIndex();
                        LinePoint point = it.next();

                        // double dist2LastLastLast =
                        // lastLastLast==null?Double.POSITIVE_INFINITY:
                        // point.p.distanceToSquared3(lastLastLast.p);
                        // if (dist2LastLastLast<searchRad2){
                        // it.remove();
                        // continue;
                        // }
                        double dist2 = point.p.distanceToSquared3(last.p);
                        if (dist2 < searchRad2 && dist2 < bestDist2) {
                            bestDist2 = dist2;
                            best = point;
                            idxBest = idx;
                        }
                    }
                    // System.out.println("best:" + best + " distb:"+bestDist2);
                    if (best == null) break;
                    lineNew.points.add(best);
                    line.points.remove(idxBest);
                    // lastLastLast=lastLast;
                    // lastLast=last;
                    last = best;
                }
                // System.out.println(" ++++++++ second ");
                // maybe on the other end of the line we could add some points
                // as well, the starting point was choosen very random!
                last = lineNew.points.getFirst();
                // lastLast =
                // lineNew.points.size()>1?lineNew.points.get(1):null;
                // lastLastLast =lineNew.points.size()>2?
                // lineNew.points.get(2):null;
                while (!line.points.isEmpty()) {
                    LinePoint best = null;
                    double bestDist2 = Double.POSITIVE_INFINITY;
                    int idxBest = -1;
                    ListIterator<LinePoint> it = line.points.listIterator();
                    // System.out.println("last:"+last);
                    while (it.hasNext()) {
                        int idx = it.nextIndex();
                        LinePoint point = it.next();
                        // double dist2LastLastLast =
                        // lastLastLast==null?Double.POSITIVE_INFINITY:
                        // point.p.distanceToSquared3(lastLastLast.p);
                        // if (dist2LastLastLast<searchRad2){
                        // it.remove();
                        // continue;
                        // }
                        double dist2 = point.p.distanceToSquared3(last.p);
                        if (dist2 < searchRad2 && dist2 < bestDist2) {
                            bestDist2 = dist2;
                            best = point;
                            idxBest = idx;
                        }
                    }
                    // System.out.println("best:" + best + " distb:"+bestDist2);

                    if (best == null) break;
                    lineNew.points.addFirst(best);
                    line.points.remove(idxBest);
                    // lastLastLast=lastLast;
                    // lastLast=last;
                    last = best;
                }

                // sparse new line to see if maybe parts of it is close to
                // recent
                Iterator<LinePoint> it = lineNew.points.iterator();
                // System.out.println("last:"+last + "
                // recentSize:"+recentPoints.size() + "
                // thisS:"+lineNew.points.size());
                while (it.hasNext()) {
                    LinePoint point = it.next();
                    for (LinePoint recent : recentPoints) {
                        double dist2 = point.p.distanceToSquared3(recent.p);
                        if (dist2 < searchRad2) {
                            it.remove();
                            break;
                        }
                    }
                }

                // remove artefacts -> lines with only one point are typicall
                // (TODO FIXME not always) artefacts from neighbor search
                if (!lineNew.points.isEmpty() && lineNew.points.getLast() != lineNew.points.getFirst()) {
                    recentPoints.addAll(lineNew.points);
                    linesNew.add(lineNew);
                }
            }
        }

        lines = linesNew;

        int pointCnt = 0;
        // detect loops
        for (PlanLine line : lines) {
            pointCnt += line.points.size();
            if (line.points.getFirst().p.distanceToSquared3(line.points.getLast().p) < searchRad2 * 2.5) {
                line.closedLoop = true;
                // System.out.println("closed " + line.points.size());
            } else {
                line.closedLoop = false;
                // } else {
                // System.out.println("open " + line.points.size());
            }
        }

        System.out.println("number new sorted and splitted lines: " + lines.size() + " with point#:" + pointCnt);
        return lines;
    }
}
