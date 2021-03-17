/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by Intel Cooperation, Germany (C) 2018:
 * use FastMath instead of Math -> 6x faster
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.PublishSource;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.util.Logging;
import org.apache.commons.math3.util.FastMath;

@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class FastEarth extends Earth {

    protected Position ellipsoidalToGeodetic(Vec4 cart) {
        // This code is copy&paste from WWJ sources but replacing Math with FastMath

        // Contributed by Nathan Kronenfeld. Integrated 1/24/2011. Brings this calculation in line with Vermeille's
        // most recent update.
        if (null == cart) {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // According to
        // H. Vermeille,
        // "An analytical method to transform geocentric into geodetic coordinates"
        // http://www.springerlink.com/content/3t6837t27t351227/fulltext.pdf
        // Journal of Geodesy, accepted 10/2010, not yet published
        double X = cart.z;
        double Y = cart.x;
        double Z = cart.y;
        double XXpYY = X * X + Y * Y;
        double sqrtXXpYY = FastMath.sqrt(XXpYY);

        double a = this.equatorialRadius;
        double ra2 = 1 / (a * a);
        double e2 = this.es;
        double e4 = e2 * e2;

        // Step 1
        double p = XXpYY * ra2;
        double q = Z * Z * (1 - e2) * ra2;
        double r = (p + q - e4) / 6;

        double h;
        double phi;

        double evoluteBorderTest = 8 * r * r * r + e4 * p * q;
        if (evoluteBorderTest > 0 || q != 0) {
            double u;

            if (evoluteBorderTest > 0) {
                // Step 2: general case
                double rad1 = FastMath.sqrt(evoluteBorderTest);
                double rad2 = FastMath.sqrt(e4 * p * q);

                // 10*e2 is my arbitrary decision of what Vermeille means by "near... the cusps of the evolute".
                if (evoluteBorderTest > 10 * e2) {
                    double rad3 = FastMath.cbrt((rad1 + rad2) * (rad1 + rad2));
                    u = r + 0.5 * rad3 + 2 * r * r / rad3;
                } else {
                    u =
                        r
                            + 0.5 * FastMath.cbrt((rad1 + rad2) * (rad1 + rad2))
                            + 0.5 * FastMath.cbrt((rad1 - rad2) * (rad1 - rad2));
                }
            } else {
                // Step 3: near evolute
                double rad1 = FastMath.sqrt(-evoluteBorderTest);
                double rad2 = FastMath.sqrt(-8 * r * r * r);
                double rad3 = FastMath.sqrt(e4 * p * q);
                double atan = 2 * FastMath.atan2(rad3, rad1 + rad2) / 3;

                u = -4 * r * FastMath.sin(atan) * FastMath.cos(FastMath.PI / 6 + atan);
            }

            double v = FastMath.sqrt(u * u + e4 * q);
            double w = e2 * (u + v - q) / (2 * v);
            double k = (u + v) / (FastMath.sqrt(w * w + u + v) + w);
            double D = k * sqrtXXpYY / (k + e2);
            double sqrtDDpZZ = FastMath.sqrt(D * D + Z * Z);

            h = (k + e2 - 1) * sqrtDDpZZ / k;
            phi = 2 * FastMath.atan2(Z, sqrtDDpZZ + D);
        } else {
            // Step 4: singular disk
            double rad1 = FastMath.sqrt(1 - e2);
            double rad2 = FastMath.sqrt(e2 - p);
            double e = FastMath.sqrt(e2);

            h = -a * rad1 * rad2 / e;
            phi = rad2 / (e * rad2 + rad1 * FastMath.sqrt(p));
        }

        // Compute lambda
        double lambda;
        double s2 = FastMath.sqrt(2);
        if ((s2 - 1) * Y < sqrtXXpYY + X) {
            // case 1 - -135deg < lambda < 135deg
            lambda = 2 * FastMath.atan2(Y, sqrtXXpYY + X);
        } else if (sqrtXXpYY + Y < (s2 + 1) * X) {
            // case 2 - -225deg < lambda < 45deg
            lambda = -FastMath.PI * 0.5 + 2 * FastMath.atan2(X, sqrtXXpYY - Y);
        } else {
            // if (sqrtXXpYY-Y<(s2=1)*X) {  // is the test, if needed, but it's not
            // case 3: - -45deg < lambda < 225deg
            lambda = FastMath.PI * 0.5 - 2 * FastMath.atan2(X, sqrtXXpYY + Y);
        }

        return Position.fromRadians(phi, lambda, h);
    }
}
