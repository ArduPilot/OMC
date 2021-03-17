/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by MAVinci GmbH, Germany (C) 2009-2016:
 * adapted from nasa soruces to support multiple images in an annotation
 */
package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.PublishSource;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenAnnotation;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Vector;

@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class MScreenAnnotation extends ScreenAnnotation {

    private static final int PADDING = 15;
    // public int spacing = 4;

    Vector<BufferedImage> images = new Vector<BufferedImage>();

    public MScreenAnnotation(String text, Point position) {
        this(text, position, false);
    }

    boolean trueUpperLeftFalseUpperRight;

    public MScreenAnnotation(String text, Point position, boolean trueUpperLeftFalseUpperRight) {
        super(text, position);
        this.trueUpperLeftFalseUpperRight = trueUpperLeftFalseUpperRight;
    }

    /**
     * the icon to be added to this annotation it is inserted directly below the previous image if it exists
     *
     * @param image
     */
    public void addImage(BufferedImage image) {

        // our current images and the new one
        images.add(image);

        int height = 0;
        int width = 0;
        // int maxWidth = 0;

        for (int i = 0; i < images.size(); i++) {
            width = Math.max(width, images.get(i).getWidth());
            height += images.get(i).getHeight();
            // if (width < images.get(i).getWidth()) {
            // maxWidth = images.get(i).getWidth();
            // }
        }

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int currentHeight = 0;
        // update the new image with the old images and the new one.
        for (int i = 0; i < images.size(); i++) {
            BufferedImage currentImage = images.get(i);

            currentHeight += currentImage.getHeight();
            if (currentHeight == 0) {
                currentHeight = image.getHeight();
            }

            // write the images into the larger one
            for (int k = currentHeight - currentImage.getWidth(); k < currentHeight; k++) {
                for (int j = 0; j < currentImage.getWidth(); j++) {
                    // k - currentHeight + image.getHeight()
                    // this get the correct height position on the new buffered image which was created big enough for
                    // all
                    // images to fit into it
                    int val = currentImage.getRGB(j, k - currentHeight + currentImage.getWidth());
                    bi.setRGB(j, k, val);
                }
            }
        }

        this.getAttributes().setImageSource(bi);
        this.getAttributes().setImageRepeat("IMAGE_REPEAT_NONE");
    }

    /** add some space between the icons may be useful when trying to line up the text. */
    public void addPadding(int size) {
        if (size == 0) {
            return;
        }

        BufferedImage padding = new BufferedImage(1, size, BufferedImage.TYPE_INT_ARGB);
        images.add(padding);
    }

    public void removeImage(int i) {
        images.remove(i);
        addImage(new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB));
    }

    @Override
    protected void doRenderNow(DrawContext dc) {
        java.awt.Rectangle viewport = dc.getView().getViewport();
        Point offset = this.computeLocation(viewport);
        // setScreenPoint(new Point((int)(offset.x - attributes.getSize().getWidth()), (int)(offset.y -
        // attributes.getSize().getHeight())));
        this.getAttributes()
            .setDrawOffset(
                new Point(
                    (int)(offset.x - attributes.getSize().getWidth()),
                    (int)(offset.y - attributes.getSize().getHeight() - PADDING)));

        super.doRenderNow(dc);
    }

    public Point computeLocation(java.awt.Rectangle viewport) {
        double x;
        double y;
        x = viewport.getWidth();
        y = viewport.getHeight();

        Point point = new Point(trueUpperLeftFalseUpperRight ? 0 : (int)x, (int)y);
        return point;
    }

}
