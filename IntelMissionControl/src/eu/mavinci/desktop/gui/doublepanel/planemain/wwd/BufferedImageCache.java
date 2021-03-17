/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoFile;
import eu.mavinci.desktop.helper.ImageHelper;
import eu.mavinci.desktop.helper.ImageMask;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.logging.Level;

public class BufferedImageCache {

    static HashMap<String, SoftReference<BufferedImage>> hashMap =
        new HashMap<String, SoftReference<BufferedImage>>(2000);
    static ReferenceQueue<BufferedImage> queue = new ReferenceQueue<BufferedImage>();

    public static synchronized BufferedImage getImage(File f, ImageMask imageMask) {
        String key = f.getAbsolutePath();
        if (imageMask != null) {
            key += "_masked";
        }

        // cleanup cache
        Reference<? extends BufferedImage> oldRef = queue.poll();
        while (oldRef != null) {
            hashMap.remove(oldRef);
            oldRef = queue.poll();
        }

        SoftReference<BufferedImage> ref = hashMap.get(key);

        BufferedImage bi = null;
        if (ref != null) {
            bi = ref.get();
        }
        // System.out.println("get img for from cache:" + f);
        if (bi == null) {
            // System.out.println(" cache miss!");

            // System.out.println("loading:"+f.getAbsolutePath());
            try {
                if (!f.exists()) {
                    throw new FileNotFoundException(f.getAbsolutePath());
                }

                bi = PhotoFile.convertFloatBuffImgIfNeeded(ImageHelper.loadImage(f));
                if (imageMask != null) {
                    // System.out.println("create image with mask:" + f);
                    bi = imageMask.applyMask(bi);
                    // ImageIO.write(bi, "png", new File("/home/marco/tmp/"+f.getName()+".png"));
                    // } else {
                    // Debug.printStackTrace(f,imageMask);
                }
            } catch (Exception e1) {
                Debug.getLog().log(Level.SEVERE, "could not load image: " + f, e1);
                return null;
            }
            //
            hashMap.put(key, new SoftReference<BufferedImage>(bi, queue));
        }

        return bi;
    }

    public static synchronized void dropFromCache(File f) {
        String key = f.getAbsolutePath();
        hashMap.remove(key);
        key += "_masked";
        hashMap.remove(key);
    }

}
