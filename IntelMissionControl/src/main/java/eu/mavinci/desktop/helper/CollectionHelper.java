/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Comparator;
import java.util.List;

public class CollectionHelper {
    public static <T> void checkContract(List<@NonNull T> list, Comparator<? super T> c, boolean uniqe)
            throws Exception {
        Object[] a = list.toArray();
        for (int i = 0; i != a.length; i++) {
            for (int k = i; k != a.length; k++) {
                // for (int i = 54; i != a.length; i++){
                // for (int k = 545; k != a.length; k++){

                System.out.println("i:" + i + " k:" + k);
                checkPair(i, k, uniqe, a, c);

                for (int n = k; n < a.length; n++) {
                    // for (int n = 606; n < a.length; n++){
                    // dreiecksungleichungen
                    checkTriangle(i, k, n, a, c);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void checkPair(int i, int k, boolean uniqe, Object[] a, Comparator<? super T> c)
            throws Exception {
        if (i == k) {
            myAssert(c.compare((T)a[i], (T)a[k]) == 0, i, k, -1, a);
            myAssert(c.compare((T)a[k], (T)a[i]) == 0, i, k, -1, a);
        } else if (uniqe) {
            int first = c.compare((T)a[i], (T)a[k]);
            int second = c.compare((T)a[k], (T)a[i]);
            myAssert(first != 0, i, k, -1, a);
            myAssert(second != 0, i, k, -1, a);
            if (first > 0) {
                myAssert(second < 0, i, k, -1, a);
            } else {
                myAssert(second > 0, i, k, -1, a);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void checkTriangle(int i, int k, int n, Object[] a, Comparator<? super T> c) throws Exception {
        if (c.compare((T)a[i], (T)a[k]) < 0 && c.compare((T)a[k], (T)a[n]) < 0) {
            myAssert(c.compare((T)a[i], (T)a[n]) < 0, i, k, n, a);
        }

        if (c.compare((T)a[i], (T)a[k]) > 0 && c.compare((T)a[k], (T)a[n]) > 0) {
            myAssert(c.compare((T)a[i], (T)a[n]) > 0, i, k, n, a);
        }

        if (c.compare((T)a[i], (T)a[k]) == 0 && c.compare((T)a[k], (T)a[n]) == 0) {
            myAssert(c.compare((T)a[i], (T)a[n]) == 0, i, k, n, a);
        }
    }

    public static <T> void checkAll(int i, int k, int n, boolean uniqe, Object[] a, Comparator<? super T> c)
            throws Exception {
        checkPair(i, k, uniqe, a, c);
        checkPair(i, n, uniqe, a, c);
        checkPair(k, n, uniqe, a, c);
        checkTriangle(i, k, n, a, c);
    }

    public static void myAssert(boolean t, int i, int k, int n, Object[] arr) throws Exception {
        if (!t) {
            System.out.println("int i=" + i + ", k=" + k + ", n=" + n + ";");
            System.out.println("i: " + arr[i]);
            System.out.println("k: " + arr[k]);
            if (n >= 0) {
                System.out.println("n: " + arr[n]);
            }

            throw new Exception("assertionFailed");
        }
    }
}
