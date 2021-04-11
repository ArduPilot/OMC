package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonGeotags;

import eu.mavinci.core.obfuscation.IKeepAll;

public class Vec3 implements IKeepAll {
    public double x;
    public double y;
    public double z;

    public double getLength() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
