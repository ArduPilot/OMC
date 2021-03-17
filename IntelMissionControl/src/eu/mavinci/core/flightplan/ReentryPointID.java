/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public class ReentryPointID {

    public static final ReentryPointID INVALID_REENTRYPOINT = new ReentryPointID(-99);

    public static final int MAXIMAL_ALLOWED_NEGATIVE_ID = -50; // everything below this value, and >= 0 is allowed as ID
    public static final int maxValidID = 32767; // (2^16)-1, since this is the maximal negative short value
    // has to be with %4 ==0, to not disturbe line type measurements!

    public static final int maskBitOrthogonalLines = 32768; // this bit seems to be unsused right now

    public static final int maskBitBackwardLines = 2;
    public static final int maskBitMainLines = 1;

    // IDs are currently limited by the tiny position packages, their they are cropped to "short" -> +-/32768 max/min
    // 2^16 since reentrypoints are sometimes castet to short, so only the cellnumber is los by doing so.
    // 09.2015: MM: based to my knowledge all current builds from AP/Conn revision 2.4 or higher dont cast anything to
    // short anywhere

    // background idea of the following to lines is to keep this compatible, by handling refinement IDs virtually as
    // cells, and put real new
    // cells on every 512-th ID
    public static final int multiplierRefinementIDs = 65536;
    public static final int multiplierCellIDs = multiplierRefinementIDs * 512; // this keeps

    public static final int multiplierLineIDs = 4;

    public static final int maxNoLines = (maxValidID + 1) / multiplierLineIDs - 1; // for ALL rows in sum
    public static final int maxNoCells = (Integer.MAX_VALUE / multiplierCellIDs); // in each row individually
    public static final int maxNoRefinements =
        multiplierCellIDs / multiplierRefinementIDs - 1; // max. no points in each individual cell in
    // each individual line

    final int id;

    public ReentryPointID(int id) {
        this.id = id;
    }

    public ReentryPointID(
            boolean isManual,
            boolean isMainLine,
            boolean isForwardLine,
            boolean isOrthogonalLine,
            int lineNumber,
            int cellNumber,
            int refinementNo)
            throws FlightplanContainerFullException {
        this.id = createID(isManual, isMainLine, isForwardLine, isOrthogonalLine, lineNumber, cellNumber, refinementNo);
    }

    public static int createID(
            boolean isManual,
            boolean isMainLine,
            boolean isForwardLine,
            boolean isOrthogonalLine,
            int lineNumber,
            int cellNumber,
            int refinementNo)
            throws FlightplanContainerFullException {
        if (true){//TODO, we have to refactor this entire concept since the number of points are too limited!
            return refinementNo;
        }

        // System.out.print("isManual:"+isManual +" isMainLine:"+isMainLine + " isForwardLine:"+isForwardLine + "
        // isOrthogonalLine:"+isOrthogonalLine+
        // " lineNumber:"+lineNumber + " cellNumber:"+cellNumber + " refinementNo:"+refinementNo);
        // known bug:
        // calling this with false, true,true,false,-1,-1,-1
        // will give as ID ==0
        // which would be resolve as normal manual ID !!!!

        if (isManual) {
            if (lineNumber < 0) {
                throw new RuntimeException("no negative IDs allowed in manual mode:" + lineNumber);
            }
            // System.out.println(" => "+lineNumber);
            return lineNumber;
        } else {
            if (cellNumber < -1 || cellNumber >= maxNoCells) {
                throw new FlightplanContainerFullException("invalid cellNumber:" + cellNumber);
            }

            if (refinementNo < -1 || refinementNo >= maxNoRefinements) {
                throw new FlightplanContainerFullException("invalid refinementNumber:" + refinementNo);
            }

            if (lineNumber < -1 || lineNumber >= maxNoLines) {
                throw new FlightplanContainerFullException("invalid lineID:" + lineNumber);
            }

            int id = 0;
            id += (cellNumber + 1) * multiplierCellIDs;
            id += (refinementNo + 1) * multiplierRefinementIDs;
            id += (lineNumber + 1) * multiplierLineIDs;
            if (isMainLine) {
                id += maskBitMainLines;
            }

            if (!isForwardLine) {
                id += maskBitBackwardLines;
            }

            if (isOrthogonalLine) {
                id += maskBitOrthogonalLines;
            }
            // System.out.println(" => "+(-id));
            return -id; // negative marks automatically generated IDs
        }
    }

    public int getID() {
        return id;
    }

    public boolean isAutoFPid() {
        return id < 0;
    }

    public boolean isAutoPlanned() {
        return id < 0;
    }

    public boolean isForwardLine() {
        return isForwardLine(id);
    }

    public boolean isMultiFPid() {
        return isMultiFPid(id);
    }

    public boolean isOnMainLine() {
        return isOnMainLine(id);
    }

    public String toString() {
        return toString(id);
    }

    public int getCellNumber() {
        return getCellNumber(id);
    }

    public int getIDPureWithoutCell() {
        return getIDPureWithoutCell(id);
    }

    public int getLineNumberPure() {
        return getLineNumberPure(id);
    }

    public boolean equalIDexceptCell(ReentryPointID other) {
        return equalIDexceptCell(id, other.id);
    }

    public String toStringWithoutCell() {
        return toStringWithoutCell(id);
    }

    public ReentryPointID createNextMainLineID() {
        return new ReentryPointID(createNextMainLineID(id));
    }

    public ReentryPointID createNextPureID() {
        return new ReentryPointID(createNextPureID(id));
    }

    public ReentryPointID createNextSideLineID() {
        return new ReentryPointID(createNextSideLineID(id));
    }

    public ReentryPointID getNextSideLineID() {
        return new ReentryPointID(getNextSideLineID(id));
    }

    public static int correctLineID(int orgId, int cellNo) {
        if (cellNo >= 0) {
            orgId += (cellNo + 1) * multiplierCellIDs;
        }

        return -orgId;
    }

    public static int createNextMainLineID(int lastUsedID) {
        lastUsedID++;
        lastUsedID += 1 - (lastUsedID % maskBitBackwardLines);
        return lastUsedID;
    }

    public static int createNextPureID(int lastUsedID) {
        lastUsedID +=
            multiplierLineIDs
                - (lastUsedID % multiplierLineIDs); // immer mindestens 1 drauf addieren, im zweifel auf den
        // nächsten block gehen
        return lastUsedID;
    }

    public static int createNextSideLineID(int lastUsedID) {
        lastUsedID +=
            2 - (lastUsedID % 2); // immer mindestens 1 drauf addieren, im zweifel auf den nächsten block gehen
        if (lastUsedID > maxValidID) {
            throw new FlightplanFullException();
        }

        return lastUsedID;
    }

    public static boolean equalIDexceptCell(int id1, int id2) {
        return id1 == id2;
        // the approach below is depricated, since it will break with fully3d terrain Flightplanning
        // it was needed in history, due to reentrypoint IDs casted to short somewhere inbetween
        // return getIDPureWithoutCell(id1) == getIDPureWithoutCell(id2);
    }

    public static int getCellNumber(int lineNumber) {
        if (!isMultiFPid(lineNumber)) {
            return -1;
        }

        return (Math.abs(lineNumber) / multiplierCellIDs) - 1;
    }

    public int getRefinementID() {
        return getRefinementID(id);
    }

    public static int getRefinementID(int lineNumber) {
        if (!isMultiFPid(lineNumber)) {
            return -1;
        }

        return ((Math.abs(lineNumber) % multiplierCellIDs) / multiplierRefinementIDs) - 1;
    }

    public static int getIDPureWithoutCell(int lineNumber) {
        return Math.abs(lineNumber) % multiplierCellIDs;
    }

    public static int getLineNumberPure(int lineNumber) {
        return (Math.abs(lineNumber) % maskBitOrthogonalLines) / multiplierLineIDs - 1;
    }

    public static boolean isAutoPlanned(int lineNumber) {
        return lineNumber < 0;
    }

    public static boolean isForwardLine(int lineNumber) {
        return Math.abs(lineNumber % (maskBitBackwardLines * 2)) < maskBitBackwardLines;
    }

    public static boolean isOrthogonalLine(int lineNumber) {
        return Math.abs(lineNumber % (maskBitOrthogonalLines * 2)) >= maskBitOrthogonalLines;
    }

    public boolean isOrthogonalLine() {
        return isOrthogonalLine(id);
    }

    public static boolean isMultiFPid(int lineNumber) {
        return -lineNumber >= multiplierRefinementIDs;
    }

    public static boolean isOnMainLine(int lineNumber) {
        return Math.abs(lineNumber % maskBitBackwardLines) == 1;
    }

    public static int getNextSideLineID(int lineNumber) {
        lineNumber++;
        return lineNumber + Math.abs(lineNumber % maskBitBackwardLines);
    }

    public static String toString(int lineNumber) {
        int id = lineNumber;
        String t;
        if (isOnMainLine(id)) {
            if (isForwardLine(id)) {
                t = "\u2192";
            } else {
                t = "\u2190";
            }
        } else {
            t = "|";
        }

        return id
            + "(pure="
            + getIDPureWithoutCell(id)
            + " cell="
            + getCellNumber(id)
            + " mainLineNo="
            + getLineNumberPure(id)
            + " refinement:"
            + getRefinementID(id)
            + " isOnMainLine:"
            + isOnMainLine(id)
            + ")"
            + " "
            + t;
    }

    public static String toStringWithoutCell(int lineNumber) {
        int id = lineNumber;
        String t;
        if (isOnMainLine(id)) {
            if (isForwardLine(id)) {
                t = "\u2192";
            } else {
                t = "\u2190";
            }
        } else {
            t = "|";
        }

        return "line=" + getIDPureWithoutCell(id) + " main=" + getLineNumberPure(id) + " " + t;
    }

    public static void main(String[] args) {
        System.out.println(new ReentryPointID(-3342337));
        System.out.println(new ReentryPointID(-3407885));
        System.out.println(new ReentryPointID(-65549));
        System.out.println(new ReentryPointID(-13));
        System.out.println(new ReentryPointID(-131084));
        System.out.println(new ReentryPointID(-196620));
        System.out.println(new ReentryPointID(-262156));
        System.out.println(new ReentryPointID(-327692));
        System.out.println(new ReentryPointID(-393228));
        System.out.println(new ReentryPointID(-458764));
        System.out.println(new ReentryPointID(-65543));
    }

}
