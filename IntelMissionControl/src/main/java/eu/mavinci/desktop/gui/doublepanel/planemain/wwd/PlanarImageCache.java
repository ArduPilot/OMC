/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.desktop.helper.ImageHelper;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.logging.Level;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.CompositeDescriptor;

public class PlanarImageCache {

    static HashMap<File, SoftReference<PlanarImage>> hashMap = new HashMap<File, SoftReference<PlanarImage>>(2000);
    static ReferenceQueue<PlanarImage> queue = new ReferenceQueue<PlanarImage>();

    static final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    static final ColorModel cm =
        RasterFactory.createComponentColorModel(DataBuffer.TYPE_BYTE, cs, true, false, Transparency.BITMASK);

    static final int numBands = 3;

    static {
        // disable warning that native libs are not found
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    static boolean haveOpenJDKwarnedOnce =
        StaticInjector.getInstance(IVersionProvider.class)
            .isOpenJDK(); // openJDK will fail anywai, so not even try it ;-)

    public static synchronized PlanarImage getImage(File f) {

        // cleanup cache
        Reference<? extends PlanarImage> oldRef = queue.poll();
        while (oldRef != null) {
            hashMap.remove(oldRef);
            oldRef = queue.poll();
        }

        SoftReference<PlanarImage> ref = hashMap.get(f);

        PlanarImage pi = null;
        if (ref != null) {
            pi = ref.get();
        }
        // System.out.println("get img for from cache:" + f);
        if (pi == null) {
            // System.out.println(" cache miss!");

            // System.out.println("loading:"+img.getThumpFile().getAbsolutePath());
            SampleModel sm = null;
            if (!haveOpenJDKwarnedOnce) {
                try {
                    pi = JAI.create("fileload", f.getAbsolutePath());
                    sm =
                        RasterFactory.createComponentSampleModel(
                            pi.getSampleModel(),
                            DataBuffer.TYPE_BYTE,
                            pi.getTileWidth(),
                            pi.getTileHeight(),
                            numBands + 1);
                } catch (Throwable e) {
                    if (!haveOpenJDKwarnedOnce) {
                        haveOpenJDKwarnedOnce = true;
                        Debug.getLog()
                            .log(
                                Level.WARNING,
                                "problems using JAI to load image, fallback to pure Java for loading",
                                e);
                    }
                }
            }

            if (pi == null) {
                // fallback, e.g. for openJDK
                try {
                    BufferedImage buf = ImageHelper.loadImage(f);
                    pi = PlanarImage.wrapRenderedImage(buf);
                    sm =
                        RasterFactory.createComponentSampleModel(
                            pi.getSampleModel(),
                            DataBuffer.TYPE_BYTE,
                            pi.getTileWidth(),
                            pi.getTileHeight(),
                            numBands + 1);
                } catch (Exception e1) {
                    Debug.getLog().log(Level.SEVERE, "could not load thumbnail: " + f, e1);
                    return null;
                }
            }
            // System.out.println("imgwidth = " + pi.getTileWidth() + " height="+pi.getTileHeight());

            ImageLayout il = new ImageLayout();
            il.setSampleModel(sm).setColorModel(cm);
            RenderingHints rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);

            ParameterBlock pb = new ParameterBlock();
            pb.add(new Float(pi.getWidth())).add(new Float(pi.getHeight()));
            pb.add(new Byte[] {new Byte((byte)0xFF)});
            RenderedOp alpha = JAI.create("constant", pb);

            pb = new ParameterBlock();
            pb.addSource(pi).addSource(pi);
            pb.add(alpha).add(alpha).add(Boolean.FALSE);
            pb.add(CompositeDescriptor.DESTINATION_ALPHA_LAST);
            pi = JAI.create("composite", pb, rh);
            hashMap.put(f, new SoftReference<PlanarImage>(pi, queue));
        }

        return pi;
    }

}
