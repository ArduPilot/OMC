/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.cir;

import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.helper.Pair;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.helper.Pair;

public class Histogram {

    final double min;
    final double max;
    final double step;
    final boolean isLog;
    final int numBins;
    long[] data;
    double[] dataBounds;
    long countTotal;
    long countValid;
    long countSmaller;
    long countLarger;

    public Histogram(double min, double max, double step, boolean isLog) {
        this.min = min;
        this.isLog = isLog;
        if (isLog) {
            numBins = (int)Math.ceil(Math.log(max / min) / Math.log(step));
            this.max = min * Math.pow(step, numBins);
        } else {
            numBins = (int)Math.ceil((max - min) / step);
            this.max = min + step * numBins;
        }

        this.step = step;
        data = new long[numBins];
        dataBounds = new double[numBins + 1];
        if (isLog) {
            int i = 0;
            double x;
            for (x = min; x <= max; x *= step) {
                dataBounds[i] = x;
                i++;
            }

            if (i == numBins) {
                dataBounds[i] = x;
            }
        } else {
            int i = 0;
            double x;
            for (x = min; x <= max; x += step) {
                dataBounds[i] = x;
                i++;
            }

            if (i == numBins) {
                dataBounds[i] = x;
            }
        }
    }

    public void reset() {
        for (int i = 0; i != numBins; i++) {
            data[i] = 0;
        }

        countTotal = 0;
        countLarger = 0;
        countSmaller = 0;
        countValid = 0;
    }

    private int getBinNoDirty(double val) {
        if (isLog) {
            return (int)(Math.log(val / min) / Math.log(step));
        } else {
            return (int)((val - min) / step);
        }
    }

    public void count(double val) {
        count(val, 1);
    }

    public void count(double val, long num) {
        int bin = getBinNoDirty(val);
        if (bin < 0) {
            countSmaller += num;
        } else if (bin >= numBins) {
            countLarger += num;
        } else {
            data[bin] += num;
            countValid += num;
        }

        countTotal += num;
    }

    public int getBinNo(double val) {
        int bin = getBinNoDirty(val);
        if (bin < 0) {
            return Integer.MIN_VALUE;
        } else if (bin >= numBins) {
            return Integer.MAX_VALUE;
        } else {
            return bin;
        }
    }

    public int getBinNoHitBounds(double val) {
        int bin = getBinNoDirty(val);
        if (bin < 0) {
            return 0;
        } else if (bin >= numBins) {
            return numBins - 1;
        } else {
            return bin;
        }
    }

    public void integrate(Histogram other) {
        for (int i = 0; i != other.getNumBins(); i++) {
            count(other.getCenter(i), other.getData()[i]);
        }

        count(Double.NEGATIVE_INFINITY, other.getCountSmaller());
        count(Double.POSITIVE_INFINITY, other.getCountLarger());
    }

    public void transformInto(Histogram other, ITransformerDouble trans) {
        other.reset();
        for (int i = 0; i != numBins; i++) {
            double x = getCenter(i);
            other.count(trans.tranform(x), data[i]);
        }
    }

    public double getMean() {
        double d = 0;
        for (int i = 0; i != numBins; i++) {
            d += data[i] * (dataBounds[i + 1] + dataBounds[i]); // /2; will be done in the result line
        }

        return d / (countValid * 2);
    }

    /**
     * Returns the average and the unkorrigierte standardabweichung (faster to compute, equal for big count numbers) of
     * the distribution in this histogram.
     */
    public Pair<Double, Double> getMeanVariance() {
        Pair<Double, Double> res = new Pair<Double, Double>();
        double d = 0;
        double dd = 0;
        for (int i = 0; i != numBins; i++) {
            double x = (dataBounds[i + 1] + dataBounds[i]); // /2; will be done in the result line
            d += data[i] * x;
            dd += data[i] * x * x;
        }
        // System.out.println(d+"\t"+dd);
        res.first = d / (countValid * 2);
        res.second = dd / (countValid * 4);
        res.second = Math.sqrt(res.second - res.first * res.first); // unkorrigierte stichprobenvarianz
        return res;
    }

    /** Returns the center position of the bin and its counts which has the maximal number of counts. */
    public Pair<Double, Long> getMaxBin() {
        int maxI = -1;
        long maxC = -1;
        for (int i = 0; i != numBins; i++) {
            if (data[i] > maxC) {
                maxC = data[i];
                maxI = i;
            }
        }

        return new Pair<Double, Long>(getCenter(maxI), maxC);
    }

    /** Returns the center values of the minimal and maximal bin, which has at least threshold counts in it. */
    public MinMaxPair getMinMaxInUse(int threshold) {
        MinMaxPair minMax = new MinMaxPair();
        int i = 0;
        while (i != numBins && data[i] < threshold) {
            i++;
        }

        if (i == numBins) {
            return minMax;
        }

        minMax.update(getCenter(i));
        i = numBins - 1;
        while (i != -1 && data[i] < threshold) {
            i--;
        }

        minMax.update(getCenter(i));
        return minMax;
    }

    public MinMaxPair getMinMaxInUseRelative(double relativeThreshold) {
        int i = 1;
        if (relativeThreshold > 0) {
            i = (int)Math.round(getMaxBin().second * relativeThreshold);
            if (i < 1) {
                i = 1;
            }
        }

        return getMinMaxInUse(i);
    }

    /** Returns the entropy of the underlying distribution in bits. */
    public double getEntropy() {
        double entropy = 0;
        double p;
        for (int i = 0; i != numBins; i++) {
            p = ((double)(data[i])) / countTotal;
            if (p > 0) {
                entropy += Math.log(p) * p;
            }
        }

        return -entropy / Math.log(2.);
    }

    /** Returns the entropy of the underlying distribution in bits but including entropy from unvalid containers. */
    public double getEntropyTotal() {
        double entropy = 0;
        double p;
        for (int i = 0; i != numBins; i++) {
            p = ((double)(data[i])) / countTotal;
            if (p > 0) {
                entropy += Math.log(p) * p;
            }
        }

        p = ((double)(countSmaller)) / countTotal;
        if (p > 0) {
            entropy += Math.log(p) * p;
        }

        p = ((double)(countLarger)) / countTotal;
        if (p > 0) {
            entropy += Math.log(p) * p;
        }

        return -entropy / Math.log(2.);
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public long getCountLarger() {
        return countLarger;
    }

    public long getCountSmaller() {
        return countSmaller;
    }

    public long getCountTotal() {
        return countTotal;
    }

    public long getCountValid() {
        return countValid;
    }

    public double getStep() {
        return step;
    }

    public int getNumBins() {
        return numBins;
    }

    public long[] getData() {
        return data;
    }

    public double[] getDataBounds() {
        return dataBounds;
    }

    public double getCenter(int binNo) {
        if (binNo < 0 || binNo >= numBins) {
            throw new IllegalArgumentException("bin number " + binNo + " out of Range [0:" + (numBins - 1) + "]");
        }

        return (dataBounds[binNo] + dataBounds[binNo + 1]) / 2;
    }

    public int getPercentileBin(final double ratio) {
        long thisPix = countSmaller;
        for (int i = 0; i != numBins; i++) {
            thisPix += data[i];
            // System.out.println(thisPix +"\t"+countValid+"\t"+(((double)thisPix) / countValid));
            if (((double)thisPix) / countTotal > ratio) {
                return i;
            }
        }

        return numBins - 1;
    }

    protected javax.media.jai.Histogram toJaiHistogram() {
        javax.media.jai.Histogram h = new javax.media.jai.Histogram(numBins, getMin(), getMax(), 1);
        int[] bins = h.getBins(0);
        for (int t = 0; t < numBins; t++) {
            bins[t] = (int)data[t];
        }

        return h;
    }

    /**
     * Calculates the threshold which minimizes the probability of error.
     *
     * <p>For each band the histogram is modeled as the sum of two Gaussian distributions and the threshold which
     * minimizes the misclassification error is computed. If the underlying histogram is unimodal the mean value of each
     * band will be returned as the threshold. The bimodality of the histogram for that band will be identically zero.
     */
    public double getMinErrorThreshold() {
        return toJaiHistogram().getMinErrorThreshold()[0];
    }

    public void dump() {
        System.out.println("countTotal:\t" + countTotal);
        System.out.println("countValid:\t" + countValid);
        System.out.println("countSmaller:\t" + countSmaller);
        System.out.println("countLarger:\t" + countLarger);
        System.out.println("i\tmin\tmax\tcenter\tcount");
        // long t=0;
        for (int i = 0; i != numBins; i++) {
            System.out.println(
                i
                    + "\t"
                    + getDataBounds()[i]
                    + "\t"
                    + getDataBounds()[i + 1]
                    + "\t"
                    + getCenter(i)
                    + "\t"
                    + getData()[i]);
            // t+=getData()[i];
        }
        // System.out.println("new Total:" + t + " org:"+countValid);
    }
}
