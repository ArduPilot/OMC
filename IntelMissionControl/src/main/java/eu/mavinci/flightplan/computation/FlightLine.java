/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.map.elevation.ElevationList;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.flightplan.camera.AirplanePreferredTurn;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.ITransformationProvider;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

public class FlightLine implements Comparable<FlightLine>, Cloneable {

    public static final FlightLine dummyLine = new FlightLine();

    static final double IGNORE_DISTANCE = 1;
    static final double MIN_NEW_POINT_DISTANCE = 20;

    private static final IElevationModel elevationModel = StaticInjector.getInstance(IElevationModel.class);

    public static Vector<FlightLine> assureTurnRadius(
            ITransformationProvider transformator,
            boolean onlyInOneDirection,
            Vector<FlightLine> flightLines,
            AltitudeAdjustModes shiftAltitudes,
            IPlatformDescription platformDescription,
            CorridorHelper corrHelper,
            PlanType planType) {
        final int max = flightLines.size() - 1;
        if (max <= 0) {
            return flightLines;
        }

        boolean leftToRight = flightLines.get(0).isForward();

        switch (planType) {
        case POLYGON:
        case CITY:
            // shift camera uncentrency
            //			System.out.println("=====================");
            for (int i = 0; i <= max; i++) {
                //				boolean shouldBeForward = i%2==0;
                FlightLine fl = flightLines.get(i);
                fl.applyCamUncentrency();
            }

            if (platformDescription.planIndividualImagePositions()) {
                return flightLines;
            }

            double turnRadius = platformDescription.getTurnRadius().convertTo(Unit.METER).getValue().doubleValue();

            if (onlyInOneDirection) {
                // right corners
                for (int i = 0; i < max; i++) {
                    FlightLine cur = flightLines.get(i);
                    FlightLine next = flightLines.get(i + 1);
                    double dy = Math.abs(cur.parallelCoordinate - next.parallelCoordinate);
                    double offset = Math.max(0, turnRadius - dy); // intentionally without times 2
                    if (leftToRight) offset = -offset;

                    // this would be the target point of the way back to the beginning of the line
                    if (cur.isRot90 != next.isRot90) {
                        // dont return to the begin of this line
                    } else if (cur.isRot90) {
                        cur.corners.add(new Vec4(cur.parallelCoordinate, next.corners.get(0).y + offset));
                    } else {
                        cur.corners.add(new Vec4(next.corners.get(0).x + offset, cur.parallelCoordinate));
                    }
                }
            } else {
                for (int i = 0; i < max; i++) {
                    FlightLine cur = flightLines.get(i);
                    FlightLine next = flightLines.get(i + 1);

                    if (cur.isRot90 != next.isRot90) continue;

                    double dy = Math.abs(cur.parallelCoordinate - next.parallelCoordinate);
                    double offset = Math.max(0, turnRadius - dy); // intentionally without times 2

                    Vec4 curLast = cur.corners.lastElement();
                    Vec4 nextFirst = next.corners.firstElement();

                    if (cur.isRot90) {
                        double yNew;
                        if (cur.isForward()) {
                            yNew = Math.max(curLast.y, nextFirst.y + offset);
                        } else {
                            yNew = Math.min(curLast.y, nextFirst.y - offset);
                        }

                        curLast = new Vec4(cur.parallelCoordinate, yNew);
                        nextFirst = new Vec4(next.parallelCoordinate, yNew);
                    } else {
                        double xNew;
                        if (cur.isForward()) {
                            xNew = Math.max(curLast.x, nextFirst.x + offset);
                        } else {
                            xNew = Math.min(curLast.x, nextFirst.x - offset);
                        }

                        curLast = new Vec4(xNew, cur.parallelCoordinate);
                        nextFirst = new Vec4(xNew, next.parallelCoordinate);
                    }

                    cur.corners.set(cur.corners.size() - 1, curLast);
                    next.corners.set(0, nextFirst);
                }
            }

            break;
        case CORRIDOR:
            if (platformDescription.planIndividualImagePositions()) {
                return flightLines;
            }

            if (onlyInOneDirection) {
                // backtracking on the same path
                for (int i = 0; i < max; i++) {
                    FlightLine cur = flightLines.get(i);
                    for (int k = cur.corners.size() - 2; k >= 0; k--) {
                        cur.corners.add(cur.corners.get(k));
                    }
                }
            }

            break;
        case COPTER3D:
        case SPIRAL:
        case SEARCH:
        case STAR:
        case TOWER:
        case WINDMILL:
        case BUILDING:
        case FACADE:
        case POINT_OF_INTEREST:
        case PANORAMA:
            break;
        }

        for (int i = 0; i <= max; i++) {
            flightLines.get(i).resetDerrivates();
        }

        return flightLines;
    }

    public static int computeJumpStep(IPlatformDescription platformDescription, double sizeYeff) {
        double turnRadius = platformDescription.getTurnRadius().convertTo(Unit.METER).getValue().doubleValue();
        if (platformDescription.getPreferredTurn() == AirplanePreferredTurn.NONE) {
            return Math.max(1, (int)Math.round(0.5 + 2 * turnRadius / sizeYeff));
        } else {
            return Math.max(1, (int)Math.round(2 * turnRadius / sizeYeff));
        }
    }

    public static Vector<FlightLine> deepCopy(Vector<FlightLine> flightLines) {
        Vector<FlightLine> copy = new Vector<FlightLine>(flightLines.size());
        for (FlightLine fl : flightLines) {
            copy.add(fl.clone());
        }

        return copy;
    }

    public static void mirrowAll(Iterable<FlightLine> lines) {
        for (FlightLine line : lines) {
            line.mirrow();
        }
    }

    public static Vector<FlightLine> switchBlocks(Iterable<FlightLine> lines) {
        Boolean rotFirst = null;
        Vector<FlightLine> blockA = new Vector<>();
        Vector<FlightLine> blockB = new Vector<>();
        for (FlightLine line : lines) {
            if (rotFirst == null) {
                rotFirst = line.isRot90();
            }

            if (rotFirst == line.isRot90()) {
                blockA.add(line);
            } else {
                blockB.add(line);
            }
        }

        blockB.addAll(blockA);
        return blockB;
    }

    public static void reverseAllInsideLines(Iterable<FlightLine> lines) {
        for (FlightLine line : lines) {
            line.reverseAllCollections();
        }
    }

    public static Vector<FlightLine> permuteLines(
            IPlatformDescription platformDescription,
            double sizeYeff,
            Vector<FlightLine> flightLines,
            AltitudeAdjustModes shiftAltitudes,
            boolean onlyInOneDirection) {

        //		System.out.println("\n\npermuteLines!!");

        // for terrain flightplans dont use any jumppattern

        //		System.out.println("cam:"+camera + " rad="+radius);
        if (flightLines.size() < 2) {
            return flightLines;
        }

        int jumpStep = computeJumpStep(platformDescription, sizeYeff);

        // I am totally not sure about this line!
        // if this should be applied depends on the vertical step between the lines
        // on relatively flat terrain, it´s better to jump, on steep terrain, non jumping
        // but higher overshoot is better!
        // unfortunately this property isn'T constant along flight lines
        //		if (shiftAltitudes.usesAbsoluteHeights()) jumpStep = 1;

        //		System.out.println("shiftAltInPermute:" + shiftAltitudes + " jumpStep:"+jumpStep );

        AirplanePreferredTurn prefTurn = platformDescription.getPreferredTurn();

        //		System.out.println("------- jumpStep="+jumpStep + "  size=" + flightLines.size());
        // this magic searches for holes in the line pattern. they are usually caused by multiflightplan splitting, and
        // so it could be the case
        // that some lines are missing inbetween. in this case stop the pattern before and continue it behind the gap
        // again. this is detected by one interline
        // spacing which is at least slightly bigger than it should be
        // additionally stop the pattern after change of orientation of the lines e.g. in city mapping mode
        int lastBegin = 0;
        FlightLine lastLine = flightLines.get(0);
        for (int i = 1; i < flightLines.size(); i++) {
            FlightLine curLine = flightLines.get(i);
            //			System.out.println("line:"+i + " isForward:"+ curLine.isForward());
            boolean rotatedHere = lastLine.isRot90 != curLine.isRot90;
            if (jumpStep > 1) {
                //				System.out.println("alternative jumpStep>0");
                boolean gabDetected =
                    Math.abs(lastLine.parallelCoordinate - curLine.parallelCoordinate) > sizeYeff * 1.1;
                if (!rotatedHere && !gabDetected) {
                    // continue current pattern as long we have not detected any gab or rotation
                    lastLine = curLine;
                    continue;
                }
                //				System.out.println("->gap");
                // gap detected!
                //				System.out.println("detect jump at "+i);
                flightLines = permuteLines(jumpStep, flightLines, lastBegin, i, prefTurn, onlyInOneDirection);
                lastBegin = i;
            } else if (!rotatedHere && !onlyInOneDirection) {
                // since alternating directions is not applied without jumping, do it here!
                if (curLine.isForward() == lastLine.isForward()) {
                    curLine.mirrow();
                    //					System.out.println("mirrow"+i + " -> " + curLine.isForward() +  " l==c:"+ (curLine==lastLine)
                    // + " "+curLine.isRot90);
                }
            }

            if (rotatedHere) {
                //				System.out.println("ROTATE");
                // rotate / permute remaining lines, to get a smooth turnover between the orientations
                Vec4 curPos = lastLine.corners.lastElement();

                Vec4 nextBeginVec = curLine.corners.firstElement();
                Vec4 nextEndVec = curLine.corners.lastElement();

                Vec4 lastBeginVec = flightLines.lastElement().corners.firstElement();
                Vec4 lastEndVec = flightLines.lastElement().corners.lastElement();

                double nextBeginDist = curPos.distanceTo2(nextBeginVec);
                double nextEndDist = curPos.distanceTo2(nextEndVec);

                double lastBeginDist = curPos.distanceTo2(lastBeginVec);
                double lastEndDist = curPos.distanceTo2(lastEndVec);

                double min = Math.min(Math.min(Math.min(nextBeginDist, nextEndDist), lastBeginDist), lastEndDist);
                if (min == nextBeginDist) {
                    //					System.out.println("A1");
                    // perfekt!
                } else if (min == nextEndDist) {
                    //					System.out.println("A2");
                    // mirrow each remaining line
                    for (int k = i; k != flightLines.size(); k++) {
                        flightLines.get(k).mirrow();
                        //						FlightLine fl = flightLines.get(k);
                        //						System.out.println("mirr5:"+k + fl.isForward + " "+fl.isRot90);
                    }
                } else if (min == lastBeginDist) {
                    //					System.out.println("A3");
                    Vector<FlightLine> linesNew = new Vector<FlightLine>(flightLines.size());
                    // reverse remaining lines order
                    for (int k = 0; k != i; k++) {
                        linesNew.add(flightLines.get(k));
                    }

                    for (int k = flightLines.size() - 1; k >= i; k--) {
                        linesNew.add(flightLines.get(k));
                    }

                    flightLines = linesNew;
                } else {
                    //					System.out.println("A4");
                    Vector<FlightLine> linesNew = new Vector<FlightLine>(flightLines.size());
                    // reverse remaining lines order AND mirrow them
                    for (int k = 0; k != i; k++) {
                        linesNew.add(flightLines.get(k));
                    }

                    for (int k = flightLines.size() - 1; k >= i; k--) {
                        FlightLine fl = flightLines.get(k);
                        fl.mirrow();
                        //						System.out.println("mirr6:"+k + fl.isForward + " "+fl.isRot90);
                        linesNew.add(fl);
                    }

                    flightLines = linesNew;
                }
            }

            lastLine = curLine;
        }
        //		for (int k = 0; k!=flightLines.size();k++){
        //			FlightLine fl = flightLines.get(k);
        //			System.out.println("k:"+k+" "+fl.lineID + " " + fl.isForward + " "+fl.isRot90);
        //		}

        if (jumpStep == 1) {
            return flightLines;
        }

        return permuteLines(jumpStep, flightLines, lastBegin, flightLines.size(), prefTurn, onlyInOneDirection);
    }

    protected static Vector<FlightLine> permuteLines(
            int jumpStep,
            Vector<FlightLine> flightLines,
            int beginRange,
            int endRange,
            AirplanePreferredTurn prefTurn,
            boolean onlyInOneDirection) {
        int sizeRange = endRange - beginRange;
        //		System.out.println("permuteLines(jumpStep"+jumpStep+"  begin"+beginRange + "  end"+endRange);

        if (sizeRange <= 1) return flightLines;

        //		Vector<Vec4> pointsNew = new Vector<Vec4>();
        Vector<FlightLine> linesNew = new Vector<FlightLine>(flightLines.size());
        //		Vector<Integer> idsNew = new Vector<Integer>();
        //		int k=0;
        //		int lineCount = flightLines.size();

        for (int i = 0; i != beginRange; i++) {
            //			System.out.println("mirrow " + i);
            linesNew.add(flightLines.get(i));
        }

        int curIdx = beginRange;

        //		System.out.println("sizeRange " + sizeRange);
        //		System.out.println("jumpStep " + jumpStep);

        if (prefTurn == AirplanePreferredTurn.BOTH) {
            boolean finalCirc = false;
            int jumpStepOpt = (int)Math.ceil((sizeRange) / 2.);
            if (jumpStepOpt < jumpStep) {
                finalCirc = true;
                jumpStep = jumpStepOpt;
                //			System.out.println("less than one regular step size pattern" + jumpStep);
            }

            boolean forwardJumping = true;

            //		System.out.println("jumpStep"+ jumpStep);
            //		System.out.println("points.size()"+points.size());
            //		System.out.println("lineCount"+lineCount);
            for (int i = sizeRange - 1; i >= 0; i--) { // i is number of remaining lines
                //				System.out.println("curIdx"+curIdx + "\ti"+i);
                linesNew.add(flightLines.get(curIdx));
                if (!finalCirc
                        && (sizeRange - i) % (2 * jumpStep - 1)
                            == 0) { // one back/forward pattern is done, so make a double forward step
                    curIdx += jumpStep;
                    //				System.out.println("circleDone");
                    forwardJumping = true;
                    if (i < (2 * jumpStep - 1)) {
                        finalCirc = true;
                        jumpStep = (int)Math.ceil(i / 2.);
                        //					System.out.println("lastCircle" + jumpStep);
                    }

                    continue;
                }

                if (forwardJumping || jumpStep == 1) {
                    curIdx += jumpStep;
                } else {
                    curIdx -= (jumpStep - 1);
                }

                forwardJumping = !forwardJumping;
            }
        } else {
            // care about direction
            if (prefTurn != AirplanePreferredTurn.NONE && !onlyInOneDirection) {
                AirplanePreferredTurn currentTurn = AirplanePreferredTurn.LEFT;
                if (flightLines.get(beginRange).isForward()) currentTurn = currentTurn.getReverse();
                if (flightLines.get(beginRange).parallelCoordinate > flightLines.get(beginRange + 1).parallelCoordinate)
                    currentTurn = currentTurn.getReverse();
                if (currentTurn != prefTurn) {
                    for (int i = beginRange; i != endRange; i++) {
                        flightLines.get(i).mirrow();
                        //						System.out.println("other Mir:"+i);
                    }
                }
            }

            // jump step auf die nächste gerade zahl runden
            //			System.out.println("jumpStep:"+jumpStep);
            int halfStep = (int)Math.ceil(jumpStep / 2.);
            //			System.out.println("halfStep1:"+halfStep);
            // das ramp up und ramp down des patterns dauert jeweils 2*(halfStep-1) lange, also insgesamt 4*(halfStep-1)
            halfStep = Math.min(halfStep, sizeRange / 4 + 1);
            //			System.out.println("halfStep2:"+halfStep);
            int evenMax = 2 * (sizeRange / 2);
            //			System.out.println("evenMax:"+evenMax);
            // ramp up the pattern
            curIdx += halfStep;
            int nextStep = -curIdx + beginRange;
            //			System.out.println("######### rampUp ########");
            for (int i = 0; i != halfStep - 1; i++) {
                //				System.out.println("curIdx:"+curIdx);
                //				System.out.println("nextStep:"+nextStep);
                linesNew.add(flightLines.get(curIdx));
                curIdx += nextStep;
                nextStep = -1 * nextStep + 2;
                //				System.out.println("curIdx:"+curIdx);
                //				System.out.println("nextStep:"+nextStep);
                linesNew.add(flightLines.get(curIdx));
                curIdx += nextStep;
                nextStep = -1 * nextStep + 1;
                //				System.out.println("--");
            }

            // main pattern
            //			System.out.println("######### main pattern ########");
            for (int i = 0; i < evenMax - 4 * (halfStep - 1); i += 2) {
                //				System.out.println("curIdx:"+curIdx);
                linesNew.add(flightLines.get(curIdx));
                curIdx -= 2 * halfStep - 1;
                //				System.out.println("curIdx:"+curIdx);
                linesNew.add(flightLines.get(curIdx));
                curIdx += 2 * halfStep + 1;
                //				System.out.println("--");
            }

            // ramp down
            curIdx--;
            nextStep = -2 * (halfStep - 1);
            //			System.out.println("######### ramp down ########");
            for (int i = 0; i != halfStep - 1; i++) {
                //				System.out.println("curIdx:"+curIdx);
                //				System.out.println("nextStep:"+nextStep);
                linesNew.add(flightLines.get(curIdx));
                curIdx += nextStep;
                nextStep = -1 * nextStep + 1;
                //				System.out.println("curIdx:"+curIdx);
                //				System.out.println("nextStep:"+nextStep);
                linesNew.add(flightLines.get(curIdx));
                curIdx += nextStep;
                nextStep = -1 * nextStep + 2;
                //				System.out.println("--");
            }
            //			System.out.println("######### ########");
            // bei ungerader anzahl fluglinien die letzte linie als letztes hinten dranhängen
            if (evenMax != sizeRange) {
                //				System.out.println("add Odd Ending:"+(endRange-1));
                linesNew.add(flightLines.get(endRange - 1));
            }
        }

        if (!onlyInOneDirection) {
            // make sure this interval of the pattern aternates in forward/backward directions
            boolean isForward = false;
            if (beginRange > 0) {
                isForward = linesNew.get(beginRange - 1).isForward();
            }

            for (int i = beginRange; i != endRange; i++) {
                isForward = !isForward;
                FlightLine fl = linesNew.get(i);
                if (fl.isForward() != isForward) fl.mirrow();
            }
        }

        for (int i = endRange; i != flightLines.size(); i++) {
            //			System.out.println("mirrow " + i);
            linesNew.add(flightLines.get(i));
        }

        return linesNew;
    }

    Vector<Vec4> corners;

    public Vector<Vec4> getCorners() {
        return corners;
    }

    Double dist;

    Vector<ElevationList> elevs;

    boolean isForward = true;

    boolean isLinear;

    boolean isRot90;

    public boolean isLinear() {
        return isLinear;
    }

    public boolean isRot90() {
        return isRot90;
    }

    public double getParallelCoordinate() {
        return parallelCoordinate;
    }

    private int lineID;

    MinMaxPair minMaxElevation;

    double parallelCoordinate;

    private ITransformationProvider trafo;

    private FlightLine() {}
    /**
     * usage only for dummy testcases!
     *
     * @param lineID
     */
    public FlightLine(double parallelCoordinate, MinMaxPair minMaxForward, int lineID, boolean isRot90) {
        corners = new Vector<Vec4>();
        if (isRot90) {
            corners.add(new Vec4(parallelCoordinate, minMaxForward.min));
            corners.add(new Vec4(parallelCoordinate, minMaxForward.max));
            this.parallelCoordinate = parallelCoordinate;
        } else {
            corners.add(new Vec4(minMaxForward.min, parallelCoordinate));
            corners.add(new Vec4(minMaxForward.max, parallelCoordinate));
            this.parallelCoordinate = parallelCoordinate;
        }

        this.lineID = lineID;
        this.isRot90 = isRot90;
        isLinear = true;
    }

    public FlightLine(
            double parallelCoordinate,
            MinMaxPair minMaxForward,
            int lineID,
            ITransformationProvider trafo,
            boolean isRot90) {
        this(parallelCoordinate, minMaxForward, lineID, isRot90);
        this.trafo = trafo;
    }

    public FlightLine(
            Vector<Vec4> corners,
            int lineID,
            double parallelCoordinate,
            ITransformationProvider trafo,
            boolean isRot90) {
        this.trafo = trafo;
        this.lineID = lineID;
        this.corners = corners;
        this.parallelCoordinate = parallelCoordinate;
        this.isRot90 = isRot90;
        isLinear = false;
    }

    RefineResult fixRefinement;

    public FlightLine(Vector<FlightplanVertex> sub, int lineID, ITransformationProvider trafo, double startingElev) {
        this.trafo = trafo;
        this.lineID = lineID;
        corners = new Vector<>(sub.size());

        fixRefinement = new RefineResult();
        fixRefinement.positions = new ArrayList<Position>();
        fixRefinement.orientations = new ArrayList<>();
        fixRefinement.surfaceNormals = new ArrayList<>();
        fixRefinement.groundDistance = new MinMaxPair();
        fixRefinement.sollDistance = new MinMaxPair();
        fixRefinement.realToleranceGSD = 0;
        Orientation lastOrientation = null;
        FlightplanVertex last =
            sub.size() > 1
                ? sub.get(1)
                : null; // just to get yaw for first waypoint correct, handle second point as its precessor and swap yaw
        // by 180°
        boolean first = true;

        for (FlightplanVertex vert : sub) {
            Vec4 vecWP = vert.getWayPoint();
            corners.add(vecWP);

            Position p = trafo.transformToGlobe(vecWP);
            p = new Position(p, p.elevation - startingElev);
            fixRefinement.positions.add(p);

            double ground = elevationModel.getElevationAsGoodAsPossible(p);
            double distToGround = vecWP.z - ground;
            fixRefinement.groundDistance.update(distToGround);

            fixRefinement.surfaceNormals.add(trafo.transformToGlobalNorthing(vert.getNormal()));
            Angle yaw;
            Vec4 dCenter;
            if (last != null) {
                dCenter =
                    first
                        ? last.getCenterPoint().subtract3(vert.getCenterPoint())
                        : vert.getCenterPoint().subtract3(last.getCenterPoint());
                yaw =
                    trafo.transformYawToGlobal(
                        Angle.fromXY(
                            dCenter.y,
                            dCenter.x)); // x and y swapped by intention to get coorect geographic northing angles
                first = false;
                last = vert;
            } else {
                yaw = Angle.ZERO;
                dCenter = Vec4.UNIT_X;
            }

            lastOrientation = vert.getRollPitchYaw(dCenter, yaw, lastOrientation, trafo);
            fixRefinement.orientations.add(lastOrientation);

            fixRefinement.sollDistance.enlarge(vert.getImageDistance());
        }
    }

    public FlightLine(
            Vector<Vec4> positions,
            Vector<Orientation> orientations,
            double dist,
            int lineID,
            ITransformationProvider trafo) {
        this.trafo = trafo;
        this.lineID = lineID;
        corners = new Vector<>(positions.size());

        fixRefinement = new RefineResult();
        fixRefinement.positions = new ArrayList<Position>();
        fixRefinement.orientations = new ArrayList<>();
        fixRefinement.surfaceNormals = new ArrayList<>();
        fixRefinement.groundDistance = new MinMaxPair();
        fixRefinement.sollDistance = new MinMaxPair();
        fixRefinement.realToleranceGSD = 0;
        for (int i = 0; i != positions.size(); i++) {
            Vec4 vecWP = positions.get(i);
            Orientation orientation = orientations.elementAt(i);
            corners.add(vecWP);

            Position p = trafo.transformToGlobe(vecWP);
            fixRefinement.positions.add(p);

            double ground = elevationModel.getElevationAsGoodAsPossible(p);
            double distToGround = vecWP.z - ground;
            fixRefinement.groundDistance.update(distToGround);

            Matrix m =
                MathHelper.getRollPitchYawTransformation(
                    orientation.getRoll(), orientation.getPitch(), orientation.getYaw());

            fixRefinement.surfaceNormals.add(trafo.transformToGlobalNorthing(Vec4.UNIT_Z.transformBy3(m)));

            fixRefinement.orientations.add(orientation);

            fixRefinement.sollDistance.enlarge(dist);
        }
    }

    public void applyCamUncentrency() {
        if (!isLinear) throw new RuntimeException("applyCamUncentrency is only applicable to linear lines");
        parallelCoordinate = trafo.compensateCamCentrency(parallelCoordinate, isForward);
        //		System.out.println("----------------------");
        for (int k = 0; k != corners.size(); k++) {
            //			System.out.print("k:"+k+" " + corners.get(k) + " forw:="+isForward+ " rod="+isRot90 + " -> ");
            corners.set(k, trafo.compensateCamCentrency(corners.get(k), isForward, isRot90));
            //			System.out.println(corners.get(k) );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public FlightLine clone() {
        FlightLine other = new FlightLine();
        other.corners = (Vector<Vec4>)corners.clone(); // flat copy is ok, since Vec4 is a final object
        other.lineID = lineID;
        other.minMaxElevation = minMaxElevation;
        other.trafo = trafo;
        other.isForward = isForward;
        other.parallelCoordinate = parallelCoordinate;
        other.dist = dist;
        other.elevs = elevs;
        other.isLinear = isLinear;
        other.isRot90 = isRot90;
        other.fixRefinement = fixRefinement;
        //		System.out.println("cloning");
        return other;
    }

    @Override
    public int compareTo(FlightLine o) {
        return lineID - o.lineID;
    }

    public static boolean enlargePolygone(Vector<Vec4> corners, double enlargeStartM, double enlargeEndM) {
        if (corners == null || corners.size() < 2) {
            Debug.getLog().log(Level.WARNING, "corners is null or its size is less than 2");
            return false;
        }

        int idxEnd = corners.size() - 1;
        Vec4 s0 = corners.get(0);
        Vec4 s1 = corners.get(1);

        Vec4 dStart = s0.subtract3(s1).normalize3();
        corners.set(0, s0.add3(dStart.multiply3(enlargeStartM)));

        Vec4 e0 = corners.get(idxEnd);
        Vec4 e1 = corners.get(idxEnd - 1);

        Vec4 dEnd = e0.subtract3(e1).normalize3();
        corners.set(idxEnd, e0.add3(dEnd.multiply3(enlargeEndM)));
        return true;
    }

    public void enlarge(double enlargeStartM, double enlargeEndM) {
        if (!enlargePolygone(corners, enlargeStartM, enlargeEndM)) {
            return;
        }

        resetDerrivates();
    }

    double lastSampleDistance;

    public Vector<ElevationList> getElevations(double sampleDistance) {
        if (elevs != null) return elevs;
        elevs = new Vector<ElevationList>();
        minMaxElevation = new MinMaxPair();
        if (corners.size() < 2) return elevs;

        Vec4 last = corners.get(0);
        for (int i = 1; i < corners.size(); i++) {
            Vec4 c = corners.get(i);
            ElevationList elev =
                elevationModel.computeElevationList(
                    trafo.transformToGlobe(last), trafo.transformToGlobe(c), sampleDistance);
            elevs.add(elev);
            minMaxElevation.enlarge(elev.minMaxElev);
            last = c;
        }

        lastSampleDistance = sampleDistance;
        return elevs;
    }

    public double getLength() {
        if (dist != null) return dist;
        if (corners.size() < 2) return 0;
        double dist = 0;
        Vec4 last = corners.get(0);
        for (int i = 1; i < corners.size(); i++) {
            Vec4 c = corners.get(i);
            dist += c.distanceTo3(last);
            last = c;
        }

        this.dist = dist;
        return dist;
    }

    public int getLineID() {
        return lineID;
    }

    public MinMaxPair getMinMaxElevation() {
        if (minMaxElevation == null) getElevations(IElevationModel.ELEVATION_SAMPLE_DISTANCE);
        return minMaxElevation;
    }

    public ArrayList<Position> getPositions(double altitude) {
        ArrayList<Position> positions = new ArrayList<Position>();
        for (Vec4 c : corners) {
            c = new Vec4(c.x, c.y, altitude);
            positions.add(trafo.transformToGlobe(c));
        }

        return positions;
    }

    public ITransformationProvider getTrafo() {
        return trafo;
    }

    public boolean isForward() {
        return isForward;
    }

    public boolean isValid() {
        double dist = getLength();
        return !(Double.isInfinite(dist) || Double.isNaN(dist));
    }

    /** switch start and end of line, this changes the flying direction */
    public void mirrow() {
        Collections.reverse(corners);

        isForward = !isForward;
    }

    public void reverseAllCollections() {
        Collections.reverse(corners);
        if (elevs != null) {
            Collections.reverse(elevs);
        }

        if (fixRefinement != null) {
            Collections.reverse(fixRefinement.positions);
            Collections.reverse(fixRefinement.orientations);
            Collections.reverse(fixRefinement.surfaceNormals);
        }

        isForward = !isForward;
    }

    /**
     * returns a maybe refined list of positions vertical component is relative to startlevel
     *
     * <p>pedending on shiftAltitudes a different refinement pattern is used
     *
     * @return RefineResult {@link RefineResult}
     */
    public RefineResult refine(
            AltitudeAdjustModes shiftAltitudes,
            IPlatformDescription platformDescription,
            double startElev,
            float lineAlt,
            LatLon startNextLine,
            LatLon endLastLine,
            double sizeInFlightEff,
            double sizeInFlightEffMax,
            double sizeParallelFlightEff,
            double minTriggerDistance,
            double gsdTolerance) {
        if (fixRefinement != null) {
            return fixRefinement;
        }

        double newSampleDistance = sizeInFlightEff;

        if (lastSampleDistance != newSampleDistance) {
            elevs = null;
        }

        getElevations(newSampleDistance); // assure they are computed

        RefineResult result = new RefineResult();
        // System.out.println("====== REFINE ========= : " +shiftAltitudes);

        double minResolution = Math.min(sizeInFlightEff, sizeParallelFlightEff);

        switch (shiftAltitudes) {
        case FOLLOW_TERRAIN:
            // refine stuff
            double toleranceMeter = gsdTolerance * lineAlt;

            double MAX_ACCEPTED_CTE = toleranceMeter / 2;

            // System.out.println("MAX_ACCEPTED_CTE:"+MAX_ACCEPTED_CTE);

            ArrayList<Position> positions = result.positions;
            ArrayList<Vec4> surfaceNormals = result.surfaceNormals;
            ArrayList<Vec4> droppedVec = new ArrayList<Vec4>();

            Position previousPosition = null;
            Vec4 previousVec = null;
            Vec4 lastFixedVec = null;

            boolean useLinearMode = shiftAltitudes.useLinearMode();
            // System.out.println("segments:" + elevs.size());
            // int segNo=-1;
            for (ElevationList elev : elevs) {
                // segNo++;
                // System.out.println(segNo+ " noSamples" + elev.p.size());
                // int posInSeg = -1;

                if (platformDescription.planIndividualImagePositions()) {
                    // refine once again to have distance between points constant even with slope:
                    ElevationList elevNew = new ElevationList();
                    elevNew.minMaxElev.enlarge(elev.minMaxElev);
                    Vec4 last = trafo.transformToLocalInclAlt(elev.p.getFirst());

                    double sumDistance = 0;
                    elevNew.p.add(elev.p.getFirst());

                    for (Position p : elev.p) {
                        Vec4 next = trafo.transformToLocalInclAlt(p);
                        Vec4 dVec = next.subtract3(last);
                        double segmentLength = dVec.getLength3();

                        double distThisSegFromStart = -sumDistance;
                        sumDistance += segmentLength;
                        while (sumDistance >= sizeInFlightEff) {
                            distThisSegFromStart += sizeInFlightEff;
                            sumDistance -= sizeInFlightEff;
                            Vec4 newVec = dVec.multiply3(distThisSegFromStart / segmentLength).add3(last);
                            Position newP = trafo.transformToGlobe(newVec);
                            elevNew.p.add(newP);
                            // elevNew.p.add(new Position(newP, newP.elevation + lineAlt));
                        }

                        last = next;
                    }

                    if (sumDistance / sizeInFlightEff < 0.1) {
                        // just "shift" the last point, instead of adding two very close to each other
                        elevNew.p.removeLast();
                    }

                    if (elev == elevs.lastElement()) {
                        // if last segment
                        // otherwise we would get a too eratic flight behavior since this point will be for flar terrain
                        // very close to the fist point on the next line
                        elevNew.p.add(elev.p.getLast());
                    }

                    elev = elevNew;
                }

                if (elev.p.size() == 0) {
                    continue;
                }

                Vec4 lineDirect =
                    trafo.transformToLocal(elev.p.getLast()).subtract3(trafo.transformToLocal(elev.p.getFirst()));

                ArrayList<Position> positionsSegment = new ArrayList<>();
                ArrayList<Vec4> normalsSegment = new ArrayList<>();
                for (Position pos : elev.p) {
                    // posInSeg++;
                    boolean isLast = pos == elev.p.getLast();
                    boolean isFirst = pos == elev.p.getFirst();

                    Vec4 vec = trafo.transformToLocal(pos);

                    // z should contain ground altitude relative to StartElev
                    // since line altitude is in the same vertical reference frame
                    vec = new Vec4(vec.x, vec.y, pos.elevation - startElev);

                    // update position in a way to be able to use this as WP coordinates
                    // set its altitude above start AND add line Alt
                    pos = new Position(pos, lineAlt + pos.elevation - startElev);

                    if (platformDescription.isInCopterMode()) {
                        lastFixedVec = vec;

                        // sample elevation model close to the
                        List<Position> elevSamples = elevationModel.sampleGroundAtPos(pos, minResolution);
                        Vector<Vec4> elevSamplesV = new Vector<>();
                        for (Position p : elevSamples) {
                            Vec4 v = trafo.transformToLocalInclAlt(p);
                            v = new Vec4(v.x, v.y, p.elevation);
                            elevSamplesV.add(v);
                        }

                        FlightplanVertex centerAndCamVec =
                            PointShiftingHelper.getCenterAndShiftedAlongNormal(
                                elevSamplesV.get(0), elevSamplesV, Vec4.UNIT_Z, lineAlt, lineDirect);

                        Position waypoint = trafo.transformToGlobe(centerAndCamVec.getWayPoint());
                        waypoint = new Position(waypoint, waypoint.elevation - startElev);

                        // attention, this normal vector is in a rotated local reference frame
                        Vec4 normal = centerAndCamVec.getCameraPointingLine();
                        // rotate it back in a way that north is pointing in +X direction
                        normal = trafo.transformToGlobalNorthing(normal);
                        positionsSegment.add(waypoint);
                        normalsSegment.add(normal);
                    } else if (platformDescription.planIndividualImagePositions()) {
                        positionsSegment.add(pos);
                    } else if (previousPosition == null) {
                        // always add the VERY FIRST pos to the positions list

                        // start empty line!
                        lastFixedVec = vec;
                        positionsSegment.add(pos);
                        // System.out.println("initial pos Adding:" + positions);
                    } else if (isFirst) {
                        // first element of a non fist segment -> skip it, since its a duplicate of the last point of
                        // the previous segment
                    } else {
                        Ensure.notNull(previousVec, "previousVec");
                        // int i = positions.size()-1;
                        double dist = lastFixedVec.distanceTo2(vec);
                        // System.out.println("dist:"+dist);
                        if (dist < MIN_NEW_POINT_DISTANCE && !isLast) {
                            // only tiny shift from last accepted position,
                            // ignore that for performance reason as long it's no line end
                            droppedVec.add(previousVec);

                            // positions.set(i, pos);//update current pos
                            // System.out.println("update pos no:"+ i);
                        } else {

                            // System.out.println("lastVec="+lastVec );
                            // check if chaning the last segment of accepted positions to end in this
                            // new position would lead to a too hight cross track error (CTE)
                            // if yes, fix the last segment as it was, and start a new one with the
                            // previous position
                            boolean droppingPos =
                                estimateMaxAltError(droppedVec, lastFixedVec, vec, useLinearMode) <= MAX_ACCEPTED_CTE;

                            // System.out.println(posInSeg + "\t dropping:" + droppingPos + " \tisLast:"+ isLast);

                            // mask the current position as potentially changeable
                            if (!droppingPos) {
                                // last pos of allready dropped points
                                // has to become a real corner!
                                positionsSegment.add(previousPosition);
                                estimateAltError(lineAlt, result, droppedVec, lastFixedVec, previousVec, useLinearMode);
                                lastFixedVec = previousVec;
                            } else {
                                droppedVec.add(previousVec);
                            }

                            if (isLast) {
                                // current position has to become
                                // a real corner
                                positionsSegment.add(pos);
                                estimateAltError(lineAlt, result, droppedVec, lastFixedVec, vec, useLinearMode);
                                lastFixedVec = vec;
                            }
                        }
                    }

                    previousPosition = pos;
                    previousVec = vec;
                }

                // sort inside segment lineary to avoid hysteric back and forth flying due to terrain adjustment
                if (platformDescription.isInCopterMode()) {
                    ArrayList<CamPosAndNormal> positionsToSort = new ArrayList<>(positionsSegment.size());

                    Vec4 segmentDirection =
                        trafo.transformToLocalInclAlt(positionsSegment.get(positionsSegment.size() - 1))
                            .subtract3(trafo.transformToLocalInclAlt(positionsSegment.get(0)));
                    for (int i = 0; i != positionsSegment.size(); i++) {
                        CamPosAndNormal tmp = new CamPosAndNormal();
                        tmp.camPos = positionsSegment.get(i);
                        tmp.rankOnFlightline = segmentDirection.dot3(trafo.transformToLocalInclAlt(tmp.camPos));
                        if (normalsSegment != null && normalsSegment.size() > i) {
                            tmp.normal = normalsSegment.get(i);
                        }

                        positionsToSort.add(tmp);
                    }

                    Collections.sort(positionsToSort, Comparator.comparing(p -> p.rankOnFlightline));
                    for (CamPosAndNormal tmp : positionsToSort) {
                        positions.add(tmp.camPos);
                        if (tmp.normal != null) {
                            surfaceNormals.add(tmp.normal);
                        }
                    }
                } else {
                    positions.addAll(positionsSegment);
                    surfaceNormals.addAll(normalsSegment);
                }

                positionsSegment.clear();
                normalsSegment.clear();
            }

            result.realToleranceGSD = result.sollDistance.absMax() / lineAlt;

            if (platformDescription.isInCopterMode()) {
                // compute orientations
                int max = positions.size() - 1;
                Orientation lastOrientation = null;
                for (int k = 0; k <= max; k++) {
                    Position posLast = positions.get(k > 0 ? k - 1 : 0);
                    Position posNext = positions.get(k < max ? k + 1 : max);
                    Vec4 vL = trafo.transformToLocalInclAlt(posLast);
                    Vec4 vN = trafo.transformToLocalInclAlt(posNext);
                    Vec4 dVec = vN.subtract3(vL);
                    Angle lineYaw =
                        LatLon.ellipsoidalForwardAzimuth(
                            posLast, posNext, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
                    lastOrientation =
                        PointShiftingHelper.yawPitchRollFromYawAndNormal(
                            dVec, lineYaw, lastOrientation, surfaceNormals.get(k));
                    result.orientations.add(lastOrientation);
                }
            } else if (platformDescription.planIndividualImagePositions()) {
                int max = positions.size() - 1;
                for (int k = 0; k <= max; k++) {
                    Position posLast = positions.get(k > 0 ? k - 1 : 0);
                    Position posNext = positions.get(k < max ? k + 1 : max);
                    Angle lineYaw =
                        LatLon.ellipsoidalForwardAzimuth(
                            posLast, posNext, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
                    Vec4 vL = trafo.transformToLocalInclAlt(posLast);
                    Vec4 vN = trafo.transformToLocalInclAlt(posNext);
                    double dx = vN.x - vL.x;
                    double dy = vN.y - vL.y;
                    double dz = vN.z - vL.z;
                    double pitch = Math.asin(dz / Math.sqrt(dx * dx + dy * dy + dz * dz));
                    Orientation orientation = new Orientation();
                    orientation.setYaw(lineYaw.degrees);
                    orientation.setPitch(pitch);
                    result.orientations.add(orientation);
                }
            }

            break;

        case CONSTANT_OVER_R:
            // compute ideal line alt
            double sollAlt = lineAlt;
            result.groundDistance = getMinMaxElevation().clone();
            result.groundDistance.shift(-startElev); // minMax is nun geländehöhe relativ zu startpos

            /*            if (shiftAltitudes == AltitudeAdjustModes.contstantOnLine) {
                lineAlt += result.groundDistance.mean();
                //				System.out.println("afterMean "+lineAlt+ "  relativeGround:"+minMax);

                MinMaxPair minMaxSubLineToNext = new MinMaxPair();
                if (startNextLine != null)
                    minMaxSubLineToNext.enlarge(
                        EarthElevationModel.computeMinMaxElevation(
                            trafo.transformToGlobe(corners.lastElement()), startNextLine));
                if (endLastLine != null)
                    minMaxSubLineToNext.enlarge(
                        EarthElevationModel.computeMinMaxElevation(
                            trafo.transformToGlobe(corners.firstElement()), endLastLine));
                if (minMaxSubLineToNext.isValid()) {
                    minMaxSubLineToNext.shift(-startElev);
                    result.groundDistance.enlarge(minMaxSubLineToNext);
                    //					System.out.println("relativeToGroundInclPreAndSubLine"+minMax);
                }

                lineAlt =
                    (float)
                        Math.max(
                            lineAlt,
                            result.groundDistance.max
                                + platformDescription
                                    .getMinGroundDistance()
                                    .convertTo(Unit.METER)
                                    .getValue()
                                    .doubleValue()
                                + EarthElevationModel.TINY_GROUND_ELEVATION);
                //				System.out.println("afterMinAlt"+lineAlt);
            }*/

            result.groundDistance.distanceTo(lineAlt); // now this contains the distance between airplane and ground
            //			System.out.println("final grund distance to flyingAlt:"+minMax);
            result.sollDistance.overwrite(result.groundDistance);

            result.sollDistance.shift(-sollAlt);
            result.realToleranceGSD = result.sollDistance.absMax() / sollAlt;

            if (platformDescription.planIndividualImagePositions()) {
                // refine positions in 2d only
                if (shiftAltitudes == AltitudeAdjustModes.CONSTANT_OVER_R) {
                    for (int i = 0; i < corners.size() - 1; i++) {
                        Vec4 vLast = corners.get(i);
                        Vec4 vNext = corners.get(i + 1);
                        Vec4 diff = vNext.subtract3(vLast);
                        double dist = diff.getLength3();
                        diff = diff.multiply3(1 / dist);
                        int numImg = (int)Math.ceil(dist / sizeInFlightEff);
                        double step = dist / numImg;
                        for (int img = (i == 0 ? 0 : 1); img <= numImg; img++) {
                            Vec4 v = vLast.add3(diff.multiply3(step * img));
                            result.positions.add(new Position(trafo.transformToGlobe(v), lineAlt));
                        }
                    }
                } else { // constant line wise... adjust forward distance to flight altitude
                    double distStartSegment = 0;
                    double lastImgDist;

                    //					System.out.println("-----------------------------------");
                    //					System.out.println("minDistance:"+minTriggerDistance);
                    //					System.out.println("sizeInFlightEff:"+sizeInFlightEff);

                    for (int i = 0; i < corners.size() - 1; i++) {
                        Position p = new Position(trafo.transformToGlobe(corners.elementAt(i)), lineAlt);
                        result.positions.add(p);
                        lastImgDist = distStartSegment;

                        double elev = elevationModel.getElevationAsGoodAsPossible(p, sizeInFlightEff);
                        double lastScale = (startElev + lineAlt - elev) / lineAlt;

                        // System.out.println();
                        //						System.out.println("###\nentering segment: " + i);
                        Vec4 vLast = corners.get(i);
                        Vec4 vNext = corners.get(i + 1);
                        Vec4 vDiff = vNext.subtract3(vLast);
                        double lengthThisSegment = vDiff.getLength3();
                        vDiff = vDiff.multiply3(1. / lengthThisSegment);
                        //						System.out.println("length this segemnt:" + lengthThisSegment);
                        //						System.out.println("distStartSegment:"+distStartSegment);
                        double distEndSegment = distStartSegment + lengthThisSegment;

                        while (lastImgDist + sizeInFlightEff * lastScale < distEndSegment) {
                            //							System.out.println();
                            //							System.out.println("");
                            // pres estimating positing, to get point to sample elevation
                            double stepTmp = sizeInFlightEff * lastScale;
                            if (stepTmp < minTriggerDistance) stepTmp = minTriggerDistance;
                            //							System.out.println("distThisSegment"+stepTmp);
                            if (lastImgDist + stepTmp > distEndSegment)
                                stepTmp = distEndSegment - lastImgDist; // at end of a segment we HAVE to place an image

                            Vec4 v = vLast.add3(vDiff.multiply3(stepTmp + lastImgDist - distStartSegment));
                            p = new Position(trafo.transformToGlobe(v), lineAlt);
                            elev = elevationModel.getElevationAsGoodAsPossible(p, sizeInFlightEff);
                            double scale = (startElev + lineAlt - elev) / lineAlt;
                            //							System.out.println("new scale:" +scale);

                            // compute the true next position based on the elevation on sampling spot
                            double step = sizeInFlightEff * scale;
                            if (step < minTriggerDistance) step = minTriggerDistance;
                            lastImgDist += step;
                            //							System.out.println("lastImgDist:"+lastImgDist);
                            if (lastImgDist
                                    >= distEndSegment) { // at end of a segment we HAVE to place an image, this will be
                                // done at the beginning of the next segment or at the end of
                                // all segments externally
                                break;
                            }

                            v = vLast.add3(vDiff.multiply3(lastImgDist - distStartSegment));
                            p = new Position(trafo.transformToGlobe(v), lineAlt);
                            lastScale = scale;
                            result.positions.add(p);
                        }

                        distStartSegment = distEndSegment;
                    }

                    result.positions.add(new Position(trafo.transformToGlobe(corners.lastElement()), lineAlt));
                }

                int max = result.positions.size() - 1;
                for (int k = 0; k <= max; k++) {
                    Position posLast = result.positions.get(k > 0 ? k - 1 : 0);
                    Position posNext = result.positions.get(k < max ? k + 1 : max);
                    Angle lineYaw =
                        LatLon.ellipsoidalForwardAzimuth(
                            posLast, posNext, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
                    Orientation orientation = new Orientation();
                    orientation.setYaw(lineYaw.degrees);
                    result.orientations.add(orientation);
                }
            } else {
                for (Vec4 v : corners) {
                    result.positions.add(new Position(trafo.transformToGlobe(v), lineAlt));
                }
            }

            break;
        }

        //		System.out.println("finally . size:"+result.positions.size());

        return result;
    }

    private static class CamPosAndNormal {
        Position camPos;
        Vec4 normal;
        double rankOnFlightline;
    }

    /**
     * estimate maximal z error above dropped positions
     *
     * @param droppedVec
     * @param beginLine
     * @param endLine
     * @param useLinearMode
     * @return
     */
    private double estimateMaxAltError(
            ArrayList<Vec4> droppedVec, Vec4 beginLine, Vec4 endLine, boolean useLinearMode) {
        if (droppedVec.isEmpty()) return 0;

        MinMaxPair maxAltError = new MinMaxPair();
        if (useLinearMode) {
            double dz = endLine.z - beginLine.z;
            double len2d = endLine.distanceTo2(beginLine);

            for (Vec4 v : droppedVec) {
                double len2dV = v.distanceTo2(beginLine);

                double altExpected = dz * len2dV / len2d + beginLine.z;
                double expectedAboveGround = altExpected - v.z;

                maxAltError.update(expectedAboveGround);
            }
        } else {
            double alt1 = beginLine.z;
            double alt2 = endLine.z;
            for (Vec4 v : droppedVec) {
                double dist1 = alt1 - v.z;
                double dist2 = alt2 - v.z;
                maxAltError.update(dist1);
                maxAltError.update(dist2);
            }
        }
        //		System.out.println("checkingAltErr:" + maxAltError);
        return maxAltError.absMax();
    }

    private void estimateAltError(
            float lineAlt,
            RefineResult result,
            ArrayList<Vec4> droppedVec,
            Vec4 beginLine,
            Vec4 endLine,
            boolean useLinearMode) {
        // estimate z error
        if (droppedVec.size() > 0) {
            if (useLinearMode) {
                double dz = endLine.z - beginLine.z;
                double len2d = endLine.distanceTo2(beginLine);

                for (Vec4 v : droppedVec) {
                    double len2dV = v.distanceTo2(beginLine);

                    double altExpected = lineAlt + dz * len2dV / len2d + beginLine.z;
                    double expectedAboveGround = altExpected - v.z;

                    result.groundDistance.update(expectedAboveGround);
                    result.sollDistance.update(expectedAboveGround - lineAlt);
                }
            } else {
                double alt1 = beginLine.z + lineAlt;
                double alt2 = endLine.z + lineAlt;
                for (Vec4 v : droppedVec) {
                    double dist1 = alt1 - v.z;
                    double dist2 = alt2 - v.z;
                    result.groundDistance.update(dist1);
                    result.groundDistance.update(dist2);
                    result.sollDistance.update(dist1 - lineAlt);
                    result.sollDistance.update(dist2 - lineAlt);
                }
            }

            droppedVec.clear(); // could not be done while iterating -> concurrentMod.Exception
        }
    }

    public void removeAfter(double dist, FlightLine refLine) {
        if (dist < 0) return;
        if (refLine.corners.size() < 2) return;
        Vec4 last = refLine.corners.get(refLine.corners.size() - 1);
        for (int i = corners.size() - 2; i >= 0; i--) {
            Vec4 c = refLine.corners.get(i);
            double runLength = c.distanceTo3(last);
            if (runLength >= dist) {
                last = last.add3(c.subtract3(last).multiply3(dist / runLength));
                corners.set(i + 1, last);
                while (corners.size() > i + 2) {
                    corners.remove(i + 2);
                }

                resetDerrivates();
                return;
            }

            dist -= runLength;
            last = c;
        }

        // larger as refernce line!
        corners.clear();
        resetDerrivates();
    }

    /**
     * removes all line segments and parts of lines before the first intersection with this cut line
     *
     * @param dist in meters from the begin of the reference line. if no one is given, from start on this line
     */
    public void removeBefore(double dist, FlightLine refLine) {
        if (dist < 0) return;
        if (refLine.corners.size() < 2) return;
        Vec4 last = refLine.corners.get(0);
        for (int i = 1; i < corners.size(); i++) {
            Vec4 c = refLine.corners.get(i);
            double runLength = c.distanceTo3(last);
            if (runLength >= dist) {
                last = last.add3(c.subtract3(last).multiply3(dist / runLength));
                corners.set(i - 1, last);
                for (int k = 0; k < i - 1; k++) {
                    corners.remove(k);
                }

                resetDerrivates();
                return;
            }

            dist -= runLength;
            last = c;
        }

        // larger as refernce line!
        corners.clear();
        resetDerrivates();
    }

    public void removeBeforeAfter(double distStart, double distEnd, FlightLine refLine) {
        removeAfter(distEnd, refLine);
        removeBefore(distStart, refLine);
    }

    public void resetDerrivates() {
        dist = null;
        elevs = null;
        minMaxElevation = null;
        lastSampleDistance = 0;
    }

    @Override
    public String toString() {
        return "corners:" + corners + "(id=" + lineID + ")" + "\tMinMaxElevation:" + minMaxElevation;
    }

    public static void main(String[] args) {
        FlightLine fl = new FlightLine(0, new MinMaxPair(Double.NaN, Double.POSITIVE_INFINITY), 0, null, false);
        System.out.println(fl.isValid());
    }
}
