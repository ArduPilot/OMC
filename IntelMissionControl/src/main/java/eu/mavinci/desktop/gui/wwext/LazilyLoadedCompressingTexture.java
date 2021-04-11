/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by MAVinci GmbH, Germany (C) 2009-2016: speeded up rendering or resources by pyping them in background through compression if needed
 *
 */
package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.PublishSource;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoFile;
import eu.mavinci.desktop.helper.ByteBufferBackedInputStream;
import eu.mavinci.desktop.helper.DCRaw;
import eu.mavinci.desktop.helper.ImageHelper;
import eu.mavinci.desktop.helper.ImageMask;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.LazilyLoadedTexture;
import gov.nasa.worldwind.util.OGLUtil;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWMath;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.OverlappingFileLockException;
import java.util.logging.Level;

@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class LazilyLoadedCompressingTexture extends LazilyLoadedTexture {

    public LazilyLoadedCompressingTexture(Object imageSource, boolean useMipMaps) {
        super(imageSource, useMipMaps);
    }

    public LazilyLoadedCompressingTexture(Object imageSource) {
        super(imageSource);
    }

    Iterable<? extends LatLon> corners;

    protected void initialize(Object imageSource, boolean useMipMaps, PropertyChangeListener listener) {
        super.initialize(imageSource, useMipMaps, listener);
    }

    @Override
    protected Runnable createRequestTask() {
        return new RequestTaskCompressing(this);
    }

    protected synchronized void setTextureDataCorners(TextureData textureData, Iterable<? extends LatLon> corners) {
        super.setTextureData(textureData);
        this.corners = corners;
    }

    public synchronized Iterable<? extends LatLon> getCorners() {
        return corners;
    }

    @Override
    protected void notifyTextureLoaded() {
        super.notifyTextureLoaded();
    }

    @Override
    protected boolean isBufferedImageSource() {
        // this maybe wrong false will cause that EVERY texture is loaded in another thread, even buffered images!
        // otherwise buffered images are also not comressed!
        return false;
    }

    protected Texture requestTexture(DrawContext dc) {
        if (this.isBufferedImageSource()) {
            return this.makeBufferedImageTexture(dc);
        }

        if (this.getTextureData() != null && this.getTexture(dc) == null) {
            return this.makeTextureFromTextureData(dc);
        }

        synchronized (WorldWind.getTaskService()) {
            if (WorldWind.getTaskService().isFull()) {
                return null;
            }

            Runnable task = this.createRequestTask();
            if (WorldWind.getTaskService().contains(task)) {
                return null;
            }

            // Use either the current layer or the layer list as the listener to notify when the request completes. The
            // latter is used when the image source is requested during ordered rendering and the current layer is null.
            this.listener = dc.getCurrentLayer() != null ? dc.getCurrentLayer() : dc.getLayers();

            // System.out.println("adding task:"+imageSource+" "+task.hashCode()+" "+ super.hashCode());
            WorldWind.getTaskService().addTask(task);
        }

        return null;
    }

    public static final class SourceWithCorners {
        public final Object imageSource;
        public final Iterable<? extends LatLon> corners;
        public final ImageMask mask;

        public SourceWithCorners(Object imageSource, Iterable<? extends LatLon> corners) {
            this(imageSource, corners, null);
        }

        public SourceWithCorners(Object imageSource, Iterable<? extends LatLon> corners, ImageMask mask) {
            this.imageSource = imageSource;
            this.corners = corners;
            this.mask = mask;
        }

        @Override
        public String toString() {
            return imageSource + " @ " + corners + " (mask:" + mask + ")";
        }
    }

    protected static class RequestTaskCompressing implements Runnable {

        LazilyLoadedCompressingTexture wwTexture;
        Object imageSourceRaw;
        Object imageSource;
        ImageMask mask;
        Iterable<? extends LatLon> corners = null;

        protected RequestTaskCompressing(LazilyLoadedCompressingTexture wwTexture) {
            this.wwTexture = wwTexture;
            imageSourceRaw = wwTexture.imageSource;
            if (imageSourceRaw instanceof SourceWithCorners) {
                SourceWithCorners combined = (SourceWithCorners)imageSourceRaw;
                imageSource = combined.imageSource;
                corners = combined.corners;
                mask = combined.mask;
            } else {
                imageSource = imageSourceRaw;
            }
        }

        @Override
        public void run() {
            if (Thread.currentThread().isInterrupted()) {
                return; // the task was cancelled because it's a duplicate or for some other reason
            }

            try {
                BufferedImage img = null;
                ByteBuffer ddsBuffer = null;
                File ddsFile = null;

                if (imageSource instanceof File) {
                    File file = (File)imageSource;

                    ddsFile = new File(WWIO.replaceSuffix(file.getPath(), mask != null ? "_masked.dds" : ".dds"));
                    if (!ddsFile.exists() || ddsFile.lastModified() < file.lastModified()) {
                        // generate dds file
                        if (!file.exists() || MFileFilter.imagesWithoutPreview.accept(file)) {
                            return;
                        } else if (MFileFilter.rawFilterNonTiff.accept(file)) {
                            img = DCRaw.callingDCRawToBufferedImage(file);
                        } else {
                            img = ImageHelper.loadImage(file);
                        }

                        if (img == null) {
                            doLoad(imageSource);
                            return;
                        }
                    } else {
                        // System.out.println("cache hint!:"+ddsFile);
                        doLoad(ddsFile.toURI());
                        ddsFile.deleteOnExit();
                        return;
                    }
                } else if (imageSource instanceof BufferedImage) {
                    img = (BufferedImage)imageSource;
                    // System.out.println("buffered image compression" +img+RequestTaskCompressing.this.hashCode());
                    // Debug.printStackTrace("buffered image compression" , img,RequestTaskCompressing.this.hashCode());
                } else {
                    // System.out.println("uncompressed fallback for soruceType" + imageSource.getClass());
                    doLoad(imageSource);
                    return;
                }

                if (mask != null) {
                    img = mask.applyMask(img);
                }

                img = PhotoFile.convertFloatBuffImgIfNeeded(img); // rikola preview

                // scale it to pow2
                if (!WWMath.isPowerOfTwo(img.getWidth()) || !WWMath.isPowerOfTwo(img.getHeight())) {
                    int w1 = img.getWidth();
                    int h1 = img.getHeight();

                    // System.out.print("rescale"+img + " "+w1+"x"+h1);

                    int w2, h2;
                    if (w1 * h1 > 512 * 512) {
                        w2 = WWMath.powerOfTwoFloor(w1);
                        h2 = WWMath.powerOfTwoFloor(h1);
                    } else {
                        w2 = WWMath.powerOfTwoCeiling(w1);
                        h2 = WWMath.powerOfTwoCeiling(h1);
                    }
                    // System.out.println(" -> "+w2+"x"+h2);

                    BufferedImage after = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB);
                    AffineTransform at = new AffineTransform();
                    at.scale(((double)(w2)) / w1, ((double)(h2)) / h1);
                    AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                    after = scaleOp.filter(img, after);
                    img = after;
                }

                DDSCompressor compressor = new DDSCompressor();
                ddsBuffer = compressor.compressImage(img, DDSCompressor.getDefaultCompressionAttributes());
                InputStream is = new ByteBufferBackedInputStream(ddsBuffer);

                TextureData textureData =
                    OGLUtil.newTextureData(Configuration.getMaxCompatibleGLProfile(), is, wwTexture.isUseMipMaps());
                if (textureData != null) {
                    wwTexture.setTextureDataCorners(textureData, corners);
                    this.wwTexture.notifyTextureLoaded();
                    // System.out.println("ready loaded " + imageSource);
                }

                if (ddsFile != null) {
                    ddsBuffer.rewind();
                    File tmpFile =
                        new File(
                            ddsFile.getAbsolutePath()
                                + "~"
                                + (int)(Math.random() * 1000000)); // dds generation on another
                    // thread can cause file
                    // access errors, so make the
                    // file name random!
                    WWIO.saveBuffer(ddsBuffer, tmpFile);
                    tmpFile.renameTo(
                        ddsFile); // this makes ddsFile creation atomar in perspective of another loading thread
                    // System.out.println("save dds: " +ddsFile);
                    ddsFile.deleteOnExit();
                }

            } catch (OverlappingFileLockException e) {
                Debug.getLog()
                    .log(
                        Level.FINE,
                        "maybe this is transformed simultaniously for multi matching and the matching itself"
                            + imageSourceRaw,
                        e);
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "problem loading / comressing texture for:" + imageSourceRaw, e);
            }
        }

        protected boolean loadTextureData(URL fileUrl) {
            TextureData td;

            synchronized (wwTexture.fileLock) {
                td = wwTexture.readImage(fileUrl);

                if (td != null) {
                    wwTexture.setTextureDataCorners(td, corners);
                }
            }

            return wwTexture.getTextureData() != null;
        }

        public void doLoad(Object source) {
            // its a bad idea to just set the image source of the parent, and use its run.
            // this would lead to two concurrent loading threads, since they could not be identifyed anymore as the same
            // job!

            if (Thread.currentThread().isInterrupted()) {
                return; // the task was cancelled because it's a duplicate or for some other reason
            }

            URL fileUrl = this.wwTexture.fileStore.requestFile(source.toString());

            if (fileUrl != null) {
                if (loadTextureData(fileUrl)) {
                    this.wwTexture.notifyTextureLoaded();
                }
            }
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final RequestTaskCompressing that = (RequestTaskCompressing)o;

            return !(this.imageSourceRaw != null
                ? !this.imageSourceRaw.equals(that.imageSourceRaw)
                : that.imageSourceRaw != null);
        }

        public int hashCode() {
            return (this.imageSourceRaw != null ? this.imageSourceRaw.hashCode() : 0);
        }

        public String toString() {
            return this.imageSourceRaw.toString();
        }
    }
}
