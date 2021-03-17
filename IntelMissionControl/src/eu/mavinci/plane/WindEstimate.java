/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane;

import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPowerOn;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PositionData;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WindEstimate implements IAirplaneListenerOrientation, IAirplaneListenerPosition, IAirplaneListenerPowerOn {

    IAirplane plane;

    private ConcurrentLinkedDeque<YawTimeTuple> history_yaw;
    private static final int MAX_HISTORY_TIME = 20;

    public static final int BUCKET_COUNT = 36 * 2; // has to be a multiply of 4

    public double[] vBuckN = new double[BUCKET_COUNT];
    public double[] vBuckE = new double[BUCKET_COUNT];

    public double[] vWheight = new double[BUCKET_COUNT];

    public double vW = 0; // in m/s
    public double vA = 0; // in m/s
    public double phi = 0; // in deg
    public double cutWeight = 0;

    public double vWN = 0; // in m/s
    public double vWE = 0; // in m/s

    public static final double beta = 0.1;
    public static final double alpha = 0.03;
    public static final double gamma = 1;

    public static final double MAX_WHEIGHT_PER_BUCKET = 1. / 6;

    public WindEstimate(IAirplane plane) {
        this.plane = plane;
        history_yaw = new ConcurrentLinkedDeque<YawTimeTuple>();
        plane.addListener(this);
        reset();
    }

    public void reset() {
        lastMute = -1;
        lastOrientationDat = null;
        // Debug.printStackTrace("reset");
        vA =
            plane.getHardwareConfiguration()
                .getPlatformDescription()
                .getMaxPlaneSpeed()
                .convertTo(Unit.METER_PER_SECOND)
                .getValue()
                .doubleValue();
        for (int i = 0; i != BUCKET_COUNT; i++) {
            double yawR = Math.toRadians(bucketToYaw(i));
            vBuckN[i] = vA * Math.cos(yawR);
            vBuckE[i] = vA * Math.sin(yawR);
            vWheight[i] = 1. / BUCKET_COUNT;
        }
    }

    private class YawTimeTuple {
        public double yaw;
        public double time;
    }

    OrientationData lastOrientationDat;

    @Override
    public void recv_orientation(OrientationData o) {
        lastOrientationDat = o;
        YawTimeTuple t = new YawTimeTuple();
        t.yaw = o.yaw;
        try {
            t.time = plane.getAirplaneCache().getCurTime();
        } catch (AirplaneCacheEmptyException e) {
            return;
        }

        history_yaw.addLast(t);
        if (history_yaw.size() > MAX_HISTORY_TIME) {
            history_yaw.removeFirst();
        }
    }

    public static final double MAX_ROLL = 13;
    public static final double MAX_PITCH = 13;
    public static final double MUTE_AFTER_TURN = 10; // sec

    double lastMute = -1;

    @Override
    public void recv_position(PositionData p) {
        // System.out.println("windEstimage new Pos:"+ p);
        if (lastOrientationDat == null) {
            return;
        }

        if (p.flightphase != AirplaneFlightphase.airborne.ordinal()) {
            return;
        }

        if (p.gpsLossFallback) {
            return;
        }

        if (Math.abs(lastOrientationDat.roll) > MAX_ROLL || Math.abs(lastOrientationDat.pitch) > MAX_PITCH) {
            lastMute = p.getTimestamp();
            return;
        }
        // System.out.println("after turn:"+(p.getTimestamp()-lastMute));
        if (p.getTimestamp() - lastMute < MUTE_AFTER_TURN) {
            return;
        }

        // System.out.println("got data!");

        // get a approximation of a good yaw value delayed by gps delay
        double delay =
            plane.getHardwareConfiguration()
                .getPlatformDescription()
                .getGpsDelay()
                .convertTo(Unit.MILLISECOND)
                .getValue()
                .doubleValue();
        Iterator<YawTimeTuple> it = history_yaw.descendingIterator();
        YawTimeTuple last = null;
        YawTimeTuple cur = null;
        double time = p.getTimestamp();
        double timeD = time - delay;
        while (it.hasNext()) {
            last = cur;
            cur = it.next();
            // System.out.println("dt:"+(time-cur.time));
            if (cur.time < timeD && last != null) {
                break;
            }
        }

        // interpolate/extrapolate yaw
        // System.out.println("cur:" + cur + " last:" + last);
        if (cur == null || last == null) {
            return;
        }
        // System.out.println("got yaw:" + cur + " "+last);
        double yawCur = cur.yaw;
        double yawLast = last.yaw;
        if (Math.abs(yawLast - yawCur) > 180) {
            if (yawLast > yawCur) {
                yawCur += 360;
            } else {
                yawLast += 360;
            }
        }

        double yaw =
            cur.time == last.time
                ? yawCur
                : ((time - last.time) * (yawCur - yawLast) / (cur.time - last.time) + yawLast);
        double speed = p.groundspeed / 100.; // to m/h
        // System.out.println("last:" + last.time + "@" +yawLast + " cur:"+cur.yaw + "@"+cur.time +" -> now=" +time+"@"
        // +yaw);
        // fill to buckets
        int i = yawToBucket(yaw);
        double headingR;
        // System.out.println("before heading");
        try {
            headingR = Math.toRadians(plane.getAirplaneCache().getHeadingEstimate());
        } catch (AirplaneCacheEmptyException e) {
            return;
        }
        // System.out.println("after heading");
        double speedN = speed * Math.cos(headingR);
        double speedE = speed * Math.sin(headingR);

        vBuckN[i] = beta * speedN + (1 - beta) * vBuckN[i];
        vBuckE[i] = beta * speedE + (1 - beta) * vBuckE[i];

        // try {
        // System.out.println("yaw:\t"+yaw+"
        // "+bucketToYaw(i)+"\theading:\t"+plane.getAirplaneCache().getHeadingEstimate()+"\tspeed N:\t" +
        // speedN + "\tE:\t"+speedE+"\t bN:"+vBuckN[i]+"\t bE"+vBuckE[i]);
        // } catch (AirplaneCacheEmptyException e) {
        // }

        // change wheight of buckets
        double dG = alpha * (MAX_WHEIGHT_PER_BUCKET - vWheight[i]);
        double vWalt = vWheight[i];
        vWheight[i] += dG;
        double scale = (1. - vWheight[i]) / (1. - vWalt);
        double sum = 0;
        for (int k = 0; k != BUCKET_COUNT; k++) {
            if (i != k) {
                vWheight[k] *= scale;
            }

            sum += vWheight[k];
        }

        // maybe an additional rescale of vWheight to 1
        scale = 1. / sum;
        sum = 0;
        for (int k = 0; k != BUCKET_COUNT; k++) {
            vWheight[k] *= scale;
            sum += vWheight[k];
        }

        // wait++;
        // if (wait % 10==0){
        // System.out.println();
        // System.out.println("newPos gps-speed:" + speed +" oldYaw" + yaw+ "("+ i +") sum=" +sum);
        // for (int k = 0; k != BUCKET_COUNT ; k++){
        // System.out.println(k+"\t" + vBuckN[k] + "\t" + vBuckE[k]+ "\t"+vWheight[k]);
        // }
        // }
        // System.out.println("before fit");
        makeFit();
    }

    public int yawToBucket(double degreeYaw) {
        while (degreeYaw >= 360.) {
            degreeYaw -= 360;
        }

        while (degreeYaw < 0.) {
            degreeYaw += 360;
        }

        return (int)(degreeYaw / 360. * BUCKET_COUNT);
    }

    public double bucketToYaw(int bucketID) {
        return (.5 + (double)bucketID) / BUCKET_COUNT * 360.;
    }

    public static final int MIN_BUCKETS_TO_USE = 6;
    public static final double SKIPPABLE_WHEIGHTS_RANGE_PERCENT = .2;

    public void makeFit() {
        double W = 0;
        double eN = 0;
        double eE = 0;
        double gN = 0;
        double gE = 0;
        double eN2 = 0;
        double eE2 = 0;
        double gNeN = 0;
        double gEeE = 0;

        Vector<Double> sortedWeights = new Vector<Double>();
        for (int i = 0; i != BUCKET_COUNT; i++) {
            sortedWeights.add(vWheight[i]);
        }

        Collections.sort(sortedWeights);

        // System.out.println("sortedWeights:"+sortedWeights);

        double cutRange =
            sortedWeights.firstElement()
                + (sortedWeights.lastElement() - sortedWeights.firstElement()) * SKIPPABLE_WHEIGHTS_RANGE_PERCENT;

        double cutNum = sortedWeights.get(BUCKET_COUNT - MIN_BUCKETS_TO_USE);
        // System.out.println("cutRange:"+cutRange);
        // System.out.println("cutNum:"+cutNum);

        cutWeight = Math.min(cutRange, cutNum);

        for (int i = 0; i != BUCKET_COUNT; i++) {
            double w = vWheight[i];
            if (w < cutWeight) {
                continue;
            }

            double yawR = Math.toRadians(bucketToYaw(i));
            double eNi = Math.cos(yawR);
            double eEi = Math.sin(yawR);
            double gNi = vBuckN[i];
            double gEi = vBuckE[i];
            W += w;
            eN += w * eNi;
            eE += w * eEi;
            gN += w * gNi;
            gE += w * gEi;
            eN2 += w * eNi * eNi;
            eE2 += w * eEi * eEi;
            gNeN += w * gNi * eNi;
            gEeE += w * gEi * eEi;
        }

        vA = (W * (gNeN + gEeE) - gN * eN - gE * eE) / (W * (eN2 + eE2) - eN * eN - eE * eE);
        vWN = (gN - vA * eN) / W;
        vWE = (gE - vA * eE) / W;
        phi = Math.toDegrees(Math.atan2(-vWE, -vWN));
        if (phi < 0) {
            phi += 360;
        }

        vW = Math.sqrt(vWN * vWN + vWE * vWE);

        // System.out.println("vW="+vW);
    }

    @Override
    public void recv_powerOn() {
        reset();
    }

}
