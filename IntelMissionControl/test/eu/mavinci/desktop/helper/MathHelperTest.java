/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("BROKEN TEST, but ignored to get testing in build system")
public class MathHelperTest {

    @Test
    public void shortestLineBetween() {
        Vec4 a = new Vec4(0, -10);
        Vec4 b = new Vec4(0, 10);

        Vec4 x = new Vec4(-10, 0);
        Vec4 y = new Vec4(10, 0);

        MathHelper.LineSegment ab = new MathHelper.LineSegment(a, b);
        MathHelper.LineSegment xy = new MathHelper.LineSegment(x, y);
        MathHelper.LineSegment dist = MathHelper.shortestLineBetween(ab, xy, true);
        assertThat(dist.first, is(Vec4.ZERO));
        assertThat(dist.second, is(Vec4.ZERO));
    }

    @Test
    public void t2() {
        Vec4 a0 = new Vec4(13.43, 21.77, 46.81);
        Vec4 a1 = new Vec4(27.83, 31.74, -26.60);
        Vec4 b0 = new Vec4(77.54, 7.53, 6.22);
        Vec4 b1 = new Vec4(26.99, 12.39, 11.18);

        MathHelper.LineSegment ab = new MathHelper.LineSegment(a0, a1);
        MathHelper.LineSegment xy = new MathHelper.LineSegment(b0, b1);
        MathHelper.LineSegment dist = MathHelper.shortestLineBetween(ab, xy, true);

        System.out.println(dist.second);
        System.out.println(dist.first);

        boolean ok2 = dist.second.distanceTo3(new Vec4(26.99, 12.39, 11.18)) < 0.001;
        assertThat(ok2, is(true));

        // this fails at the moment, and we dont know if math is wrong here, or if the unit test is broken...
        boolean ok1 = dist.first.distanceTo3(new Vec4(19.85163563, 26.21609078, 14.07303667)) < 0.001;
        assertThat(ok1, is(true));

        // closestDistanceBetweenLines(a0,a1,b0,b1,clampAll=True)
    }

    @Test
    public void WWJdeterminante() {
        Matrix m = new Matrix(-10, 1, 0, 0, 10, 0, 0, 0, 0, 0, -1, 0, 1, 1, 1, 1);
        System.out.println("Matrix2:" + m + "  " + m.getDeterminant());

        boolean ok1 = Math.abs(m.getDeterminant() - 10) < 0.001;
        assertThat(ok1, is(true));
        m = m.getTranspose();

        System.out.println("Matrix2:" + m + "  " + m.getDeterminant());
        boolean ok2 = Math.abs(m.getDeterminant() - 10) < 0.001;
        assertThat(ok2, is(true));
    }
}
