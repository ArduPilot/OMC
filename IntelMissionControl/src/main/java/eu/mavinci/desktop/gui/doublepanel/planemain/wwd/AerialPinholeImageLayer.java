/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.settings.ExpertSettings;
import eu.mavinci.airspace.Point;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.flightplan.IMuteable;
import eu.mavinci.core.helper.Pair;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicArea;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPics;
import eu.mavinci.desktop.gui.wwext.IAerialPinholeImageLayerListener;
import eu.mavinci.desktop.gui.wwext.IUserObjectComposite;
import eu.mavinci.desktop.gui.wwext.SurfaceImageWithUserData;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import eu.mavinci.desktop.helper.ImageHelper;
import eu.mavinci.desktop.helper.ImageMask;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.WWMath;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import javax.media.jai.PerspectiveTransform;
import javax.media.jai.WarpPerspective;

public class AerialPinholeImageLayer extends RenderableLayer
        implements IUserObjectComposite, IMuteable { // implements ISectorReferenced {
    //
    //	private WorldWindow wwd = null;
    // private IAirplane plane = null;

    RenderableLayer surfaceImageLayer;

    private SurfaceImageWithUserData surfaceImage = null;
    MapLayerPics mapLayerPics = null;

    public static final Color IMG_LAYER_DEF_COL = new Color(0, 0, 255, 64);

    private static final Globe globe = StaticInjector.getInstance(IWWGlobes.class).getDefaultGlobe();

    Color color = IMG_LAYER_DEF_COL;

    public void setColor(Color color) {
        if (this.color.equals(color)) return;
        this.color = color;
        fireImageLayerChanged();
    }

    public Color getColor() {
        return color;
    }

    // public void setPlane(IAirplane plane){
    //    this.plane = plane;
    /*for(Renderable renderable : getRenderables()){
        if (renderable instanceof AerialPinholeImage) {
            AerialPinholeImage image = (AerialPinholeImage) renderable;
            image.setPlane(plane);
        }
    }*/
    //    fireImageLayerChanged();
    // }

    protected boolean mute;

    public void setMute(boolean mute) {
        this.mute = mute;
        if (mute == false) fireImageLayerChanged();
    }

    public boolean isMute() {
        return mute;
    }

    public AerialPinholeImageLayer() {
        setPickEnabled(true);
        setOpacity(1);
    }

    public AerialPinholeImageLayer(String name) { // }, IAirplane plane){
        this();
        setName(name);
        // setPlane(plane);
    }

    public AerialPinholeImageLayer(MapLayerPics mapLayerPics) {
        this();
        if (mapLayerPics != null) {
            this.mapLayerPics = mapLayerPics;
        }
    }

    protected boolean shouldShowImages() {
        return mapLayerPics != null
            && mapLayerPics.isVisible()
            && mapLayerPics.isProjectingToGround()
            && !mapLayerPics.isShowOnlyOutlines();
    }

    public void resetVisibility() {
        //		Debug.printStackTrace("resetVisibility", shouldShowImages());
        if (recompRun == null) return;
        if (mapLayerPics != null && mapLayerPics.isVisible()) {
            recomp.tryStartRecomp();
        }
    }

    RecomputeImageRunnable recompRun = new RecomputeImageRunnable();
    Recomputer recomp = new Recomputer(recompRun);

    static {
        // disable warning that native libs are not found
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    BufferedImage lastComposedImage = null;
    Sector lastSector = null;

    public synchronized Pair<BufferedImage, Sector> getLastComposed() {
        if (lastComposedImage == null) return null;
        return new Pair<BufferedImage, Sector>(lastComposedImage, lastSector);
    }

    Dimension lastThumpDim = null;

    public class RecomputeImageRunnable implements Runnable {

        public void run() {
            long start = System.currentTimeMillis();

            int tileSizeMax = 4 * 1024;
            tileSizeMax = StaticInjector.getInstance(ExpertSettings.class).getMaxTextureSize();
            tileSizeMax = WWMath.powerOfTwoFloor(Math.min(tileSizeMax, WWFactory.maxTextureSize));

            //			System.out.println("WWFactory.maxTextureSize="+WWFactory.maxTextureSize);
            //			System.out.println("tileSizeMax="+tileSizeMax);

            //			Graphics2D g = null;
            Point[] cTran = null;
            boolean elevationDataReady = true;
            try {
                Debug.getLog().fine("starting previewMake" + start);
                LinkedList<AerialPinholeKnownImage> imgs = new LinkedList<AerialPinholeKnownImage>();

                Sector sec = null;
                Dimension dThump = null;
                double minDLat = Double.POSITIVE_INFINITY;
                double minDLon = Double.POSITIVE_INFINITY;
                for (Renderable renderable : getRenderables()) {
                    if (renderable instanceof AerialPinholeKnownImage) {
                        AerialPinholeKnownImage img = (AerialPinholeKnownImage)renderable;
                        if (img.getPhotoFile() == null) continue;
                        if (!img.thumpFileExists()) continue;

                        if (dThump == null) {
                            try {
                                BufferedImage buf = ImageHelper.loadImage(img.getThumpFile());
                                dThump = new Dimension(buf.getWidth(), buf.getHeight());
                                lastThumpDim = dThump;
                            } catch (IOException e) {
                            }
                        }

                        ComputeCornerData computeCornerData = img.getComputeCornerData();
                        if (computeCornerData == null) {
                            continue;
                        }

                        ArrayList<LatLon> corners = computeCornerData.getGroundProjectedCorners();
                        if (!computeCornerData.isElevationDataReady()) elevationDataReady = false;
                        if (corners == null || !img.shouldRender()) continue;
                        imgs.add(img);
                        Sector secThis = computeCornerData.getSector();
                        if (secThis == null || secThis.getDeltaLatDegrees() == 0 || secThis.getDeltaLonDegrees() == 0) {
                            //							Debug.printStackTrace("null sector?!?!",secThis);
                            continue;
                        }

                        if (sec == null) {
                            sec = secThis;
                        } else {
                            sec = sec.union(secThis);
                        }

                        minDLat = Math.min(sec.getDeltaLatDegrees(), minDLat);
                        minDLon = Math.min(sec.getDeltaLonDegrees(), minDLon);
                    }
                }

                int tileHeight = 1;
                int tileWidth = 1;
                if (sec != null) {
                    int thumpSize = Math.max(dThump.height, dThump.width);
                    //					tileHeight = Math.max(1, Math.min(tileSizeMax,
                    // WWMath.powerOfTwoCeiling((int)(thumpSize/minDLat*sec.getDeltaLatDegrees()))));
                    //					tileWidth  = Math.max(1, Math.min(tileSizeMax,
                    // WWMath.powerOfTwoCeiling((int)(thumpSize/minDLon*sec.getDeltaLonDegrees()))));
                    // compute optimal resolution based on avaliable resolution in input images
                    tileHeight =
                        Math.max(
                            1, Math.min(tileSizeMax, (int)Math.round(thumpSize / minDLat * sec.getDeltaLatDegrees())));
                    tileWidth =
                        Math.max(
                            1, Math.min(tileSizeMax, (int)Math.round(thumpSize / minDLon * sec.getDeltaLonDegrees())));

                    // make it more GPU compatible
                    tileHeight = Math.min(WWMath.powerOfTwoCeiling(tileHeight), tileSizeMax);
                    tileWidth = Math.min(WWMath.powerOfTwoCeiling(tileWidth), tileSizeMax);
                    //					System.out.println("tileHeight=" + tileHeight +  "  tileWidth="+tileWidth);
                }

                BufferedImage composedImage = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
                final DataBuffer bufferD = composedImage.getRaster().getDataBuffer();

                final int bytePerPixel_D = 4;
                final int OFFSET_R_D = 3;
                final int OFFSET_G_D = 2;
                final int OFFSET_B_D = 1;
                final int OFFSET_A_D = 0;
                float[][] dist = new float[tileWidth][tileHeight];
                for (int x = 0; x != tileWidth; ++x) {
                    for (int y = 0; y != tileHeight; ++y) {
                        dist[x][y] = Float.POSITIVE_INFINITY;
                        int idxD = (y * tileWidth + x) * bytePerPixel_D;
                        bufferD.setElem(idxD + OFFSET_A_D, 0); // enable alpha
                    }
                }
                //				g = composedImage.createGraphics();
                //		        g.setBackground(SurfaceImageWithUserData.transparencyColor);
                //		        g.clearRect(0, 0, composedImage.getWidth(), composedImage.getHeight());
                //		        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                // RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                if (sec != null && !imgs.isEmpty()) {
                    double minX = sec.getMinLongitude().degrees;
                    //					double minY = sec.getMinLatitude().degrees;
                    double maxY = sec.getMaxLatitude().degrees;
                    double dX = sec.getDeltaLonDegrees();
                    double dY = sec.getDeltaLatDegrees();
                    //					System.out.println("sec="+sec);
                    //					System.out.println("minX="+minX + "maxY="+maxY + " dx="+dX+ " dy="+dY);

                    for (AerialPinholeKnownImage img : imgs) {
                        if (!img.shouldRender()) continue;
                        ComputeCornerData computeCornerData = img.getComputeCornerData();
                        if (computeCornerData == null) {
                            continue;
                        }

                        Sector secThis = computeCornerData.getSector();
                        if (secThis == null) continue;
                        double dXThis = secThis.getDeltaLonDegrees();
                        double dYThis = secThis.getDeltaLatDegrees();
                        if (dXThis == 0 || dYThis == 0) {
                            //							Debug.printStackTrace("null sector?!?!",secThis);
                            continue;
                        }

                        ArrayList<LatLon> corners = computeCornerData.getGroundProjectedCorners();
                        if (corners == null) continue;

                        // this stuff was allready tested above, but maybe the corners where recomputed meanwhile and
                        // now its somewhere 0
                        Sector s = img.getSector();
                        if (s == null) continue;
                        if (s.getDeltaLatDegrees() == 0 || s.getDeltaLonDegrees() == 0) continue;

                        Position center = computeCornerData.getCenterRayPosition();
                        if (center == null) continue;

                        MapLayerMatch match = img.getMatch();
                        ImageMask mask = null;
                        if (match != null) {
                            mask = match.getMaskWide();
                        }

                        BufferedImage imgS = BufferedImageCache.getImage(img.getThumpFile(), mask);
                        if (imgS == null) continue;

                        int sourceWidth = imgS.getWidth();
                        if (sourceWidth == 0) continue;
                        int sourceHeight = imgS.getHeight();
                        if (sourceHeight == 0) continue;

                        //					System.out.println("transforming:"+img.getThumpFile().getAbsolutePath());
                        //					System.out.println("secThis="+secThis);

                        //						System.out.print("img:" + img.getMatch().getResourceFile().getName()+"\t");

                        final int bytePerPixel;
                        final int OFFSET_R;
                        final int OFFSET_G;
                        final int OFFSET_B;
                        final int OFFSET_A;

                        final DataBuffer bufferS = imgS.getRaster().getDataBuffer();
                        switch (imgS.getType()) {
                        case BufferedImage.TYPE_INT_RGB:
                            bytePerPixel = 3;
                            OFFSET_R = 0;
                            OFFSET_G = 1;
                            OFFSET_B = 2;
                            OFFSET_A = -1;
                            break;
                        case BufferedImage.TYPE_INT_BGR:
                        case BufferedImage.TYPE_3BYTE_BGR:
                            bytePerPixel = 3;
                            OFFSET_B = 0;
                            OFFSET_G = 1;
                            OFFSET_R = 2;
                            OFFSET_A = -1;
                            break;
                        case BufferedImage.TYPE_INT_ARGB:
                        case BufferedImage.TYPE_INT_ARGB_PRE:
                            bytePerPixel = 4;
                            OFFSET_A = 0;
                            OFFSET_R = 1;
                            OFFSET_G = 2;
                            OFFSET_B = 3;
                            break;
                        case BufferedImage.TYPE_4BYTE_ABGR:
                        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                            bytePerPixel = 4;
                            OFFSET_A = 0;
                            OFFSET_B = 1;
                            OFFSET_G = 2;
                            OFFSET_R = 3;
                            break;
                        case BufferedImage.TYPE_BYTE_GRAY:
                            bytePerPixel = 1;
                            OFFSET_A = 0;
                            OFFSET_B = 0;
                            OFFSET_G = 0;
                            OFFSET_R = 0;
                            break;
                        default:
                            throw new IOException("could not load image, wrong colormodel: " + imgS.getType());
                        }

                        // LON = X , LAT = Y

                        //					System.out.println("this: minX="+minXThis+ " minY="+minYThis + "maxY="+maxYThis + "
                        // dx="+dXThis+ " dy="+dYThis);
                        //					System.out.println("d="+d);
                        //						int thumpOutSize = Math.max(d.width, d.height);

                        cTran = new Point[4]; // airspace points are in double!
                        for (int i = 0; i != 4; ++i) {
                            LatLon latLon = corners.get(i);
                            double x = tileWidth * (latLon.getLongitude().degrees - minX) / dX;
                            double y = tileHeight * (maxY - latLon.getLatitude().degrees) / dY;
                            Point p = new Point(x, y);
                            cTran[i] = p;
                            //							if (p.x <0 || p.y < 0 || p.x > d.width || p.y > d.height){
                            //								throw new RuntimeException("Projection problem in corner i="+i + " of
                            // img="+img.getThumpFile().getName()+ " p:"+p + "   not in d:"+d);
                            //							}
                            //							System.out.println("corner " + i + " @ "  +latLon +  "  ->  " + p);
                        }

                        double xCenter = tileWidth * (center.getLongitude().degrees - minX) / dX;
                        double yCenter = tileHeight * (maxY - center.getLatitude().degrees) / dY;

                        WarpPerspective wp =
                            new WarpPerspective(
                                PerspectiveTransform.getQuadToQuad(
                                    cTran[0].x,
                                    cTran[0].y,
                                    cTran[1].x,
                                    cTran[1].y,
                                    cTran[2].x,
                                    cTran[2].y,
                                    cTran[3].x,
                                    cTran[3].y,
                                    0,
                                    sourceHeight,
                                    sourceWidth,
                                    sourceHeight,
                                    sourceWidth,
                                    0,
                                    0,
                                    0));

                        // possible target sector, scan this and search for source colors
                        double minXThis = secThis.getMinLongitude().degrees;
                        //					double minYThis = secThis.getMinLatitude().degrees;
                        double maxYThis = secThis.getMaxLatitude().degrees;

                        int dx1 = (int)(tileWidth * (minXThis - minX) / dX);
                        int dy1 = (int)(tileHeight * (maxY - maxYThis) / dY);
                        int dx2 = (int)(tileWidth * (minXThis - minX + dXThis) / dX);
                        int dy2 = (int)(tileHeight * (maxY - maxYThis + dYThis) / dY);

                        //						if (dx2<dx1) {
                        //							int tmp = dx1;
                        //							dx1 = dx2;
                        //							dx2= tmp;
                        //							System.out.println("swapX");
                        //						}
                        //						if (dy2<dy1) {
                        //							int tmp = dy1;
                        //							dy1 = dy2;
                        //							dy2= tmp;
                        //							System.out.println("swapY");
                        //						}
                        if (dx1 < 0) dx1 = 0;
                        if (dy1 < 0) dy1 = 0;
                        if (dx2 >= tileWidth) dx2 = tileWidth - 1;
                        if (dy2 >= tileHeight) dy2 = tileHeight - 1;

                        //						System.out.print(xCenter +"\t"+yCenter+"\t");

                        //						System.out.println("---------");
                        //						System.out.println("dest: " +tileWidth + " " + tileHeight);
                        //						System.out.println("soruce: " +d.width + " " +d.height);
                        //						int pixC=0;
                        //						int pixC2=0;
                        //						int pixC3=0;
                        for (int x = dx1; x <= dx2; ++x) {
                            for (int y = dy1; y <= dy2; ++y) {
                                double distCenterX = (xCenter - x);
                                double distCenterY = (yCenter - y);
                                float distCenter = (float)(distCenterX * distCenterX + distCenterY * distCenterY);
                                if (dist[x][y] <= distCenter) continue;
                                //								pixC2++;
                                // check if inside source image
                                Point2D sourceP = wp.mapDestPoint(new java.awt.Point(x, y));
                                //								System.out.print("\t" + new java.awt.Point(x,y) );
                                int sourceX = (int)(sourceP.getX());
                                if (sourceX < 0 || sourceX >= sourceWidth) continue;
                                //								pixC3++;
                                int sourceY = (int)(sourceP.getY());
                                if (sourceY < 0 || sourceY >= sourceHeight) continue;

                                // overwrite color and distance
                                dist[x][y] = distCenter;
                                //								System.out.println("dest: " + x + " " + y+ "  soruce: "+ sourceX+ "
                                // "+sourceY);
                                int idxD = (y * tileWidth + x) * bytePerPixel_D;
                                int idxS = (sourceY * sourceWidth + sourceX) * bytePerPixel;

                                if (OFFSET_A >= 0 && bufferS.getElem(idxS + OFFSET_A) == 0) continue;

                                bufferD.setElem(idxD + OFFSET_A_D, 255); // disable alpha
                                bufferD.setElem(idxD + OFFSET_R_D, bufferS.getElem(idxS + OFFSET_R));
                                bufferD.setElem(idxD + OFFSET_G_D, bufferS.getElem(idxS + OFFSET_G));
                                bufferD.setElem(idxD + OFFSET_B_D, bufferS.getElem(idxS + OFFSET_B));
                                //								pixC++;
                            }
                        }
                        //						System.out.println(pixC+"\t"+pixC2+"\t"+pixC3);
                    }
                }

                //				System.out.println();
                //				synchronized (AerialPinholeImageLayer.this) {
                //				System.out.println("surface img sector:"+sec);
                if (sec != null) {

                    //					System.out.println("try store image");
                    //					File f = new File("/home/marco/test.png");
                    //					f.delete();
                    //
                    //					ImageIO.write(composedImage, "png", f);
                    //					FileHelper.openFile(f);
                    //					System.out.println("done storing");

                    if (surfaceImage == null) {
                        // experimental code for texture compression
                        //							try{
                        //							File tempFile = File.createTempFile("wwj-", ".png");
                        //		                    tempFile.deleteOnExit();
                        //		                    ImageIO.write(composedImage, "png", tempFile);
                        //		                    System.out.println("orgSize = " +
                        // StringHelper.numberToIngNameBytes(tileWidth*tileHeight*4,3) + "Byte -> Compressed = " +
                        // StringHelper.numberToIngNameBytes(tempFile.length(),3)+ "Byte");
                        //							surfaceImage = new SurfaceImageWithUserData(tempFile,sec);
                        //							} catch (Exception e){
                        //								Debug.getLog().log(Level.WARNING,"could not cache surface image to a file",e);
                        //								surfaceImage = new SurfaceImageWithUserData(composedImage,sec);
                        //							}
                        surfaceImage = new SurfaceImageWithUserData(composedImage, sec);
                        surfaceImageLayer = new RenderableLayer();
                        surfaceImageLayer.addRenderable(surfaceImage);
                        surfaceImage.setUserData(AerialPinholeImageLayer.this);
                    } else {
                        surfaceImage.setImageSource(composedImage, sec);
                    }
                } else {
                    if (surfaceImage != null) {
                        surfaceImage.setImageSource(composedImage, Sector.EMPTY_SECTOR);
                    }
                }

                synchronized (AerialPinholeImageLayer.this) {
                    lastComposedImage = composedImage;
                    lastSector = sec;
                }

            } catch (Exception e) {

                throw new RuntimeException(
                    "problems wrapping: corners:"
                        + (cTran == null ? null : Arrays.asList(cTran))
                        + "  file:"
                        + mapLayerPics.getMatching().getResourceFile(),
                    e);
            } finally {
                //				if (g!= null) g.dispose();
                Debug.getLog()
                    .info(
                        "Ground projected dataset preview recalc done. TextureSize="
                            + tileSizeMax
                            + ".  It took "
                            + (System.currentTimeMillis() - start) / 1000.
                            + " sec");
            }

            firePropertyChange(AVKey.LAYER, null, AerialPinholeImageLayer.this);
        }
    };

    @Override
    public void addRenderable(Renderable renderable) {
        if (renderable instanceof AerialPinholeImage) {
            AerialPinholeImage image = (AerialPinholeImage)renderable;
            //			System.out.println("adding image"+image.toString() );
            //			if (wwd != null) image.setWWd(wwd);
            // if (plane != null) image.setPlane(plane);
        }

        if (renderable instanceof AerialPinholeUnknownImage) {
            AerialPinholeUnknownImage image = (AerialPinholeUnknownImage)renderable;
            //			System.out.println(System.currentTimeMillis()+ "setting parent layer color" + getColor());
            //			(new Exception()).printStackTrace();
            image.setColor(getColor());
        }

        super.addRenderable(renderable);
        fireImageLayerChanged();
    }

    public LinkedList<AerialPinholeImage> getImages() {
        LinkedList<AerialPinholeImage> list = new LinkedList<AerialPinholeImage>();
        for (Renderable renderable : getRenderables()) {
            if (renderable instanceof AerialPinholeImage) {
                list.add((AerialPinholeImage)renderable);
            }
        }

        return list;
    }

    private WeakListenerList<IAerialPinholeImageLayerListener> listeners =
        new WeakListenerList<IAerialPinholeImageLayerListener>("AerialPinholeImageLayerListener");

    public void fireImageLayerChanged() {
        if (isMute()) {
            return;
        }

        for (IAerialPinholeImageLayerListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.imageLayerChanged();
        }

        //		System.out.println("AerialPinholeImageLayer fire changed");
        //		(new Exception()).printStackTrace();
        resetVisibility();
        firePropertyChange(AVKey.LAYER, null, this);
    }

    public void addListener(IAerialPinholeImageLayerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeAllRenderables() {
        super.removeAllRenderables();
        fireImageLayerChanged();
    }

    @Override
    public void removeRenderable(Renderable renderable) {
        super.removeRenderable(renderable);
        fireImageLayerChanged();
    }

    @Override
    public void dispose() {
        recompRun = null;
        super.dispose();
        if (surfaceImage != null) surfaceImage.dispose();
    }

    @Override
    public AerialPinholeImage getRealUserObject(Position position, java.awt.Point point) {
        if (position == null) {
            return null;
        }

        AerialPinholeImage topImg = null;
        double minDist = Double.POSITIVE_INFINITY;
        for (Renderable renderable : getRenderables()) {
            if (renderable instanceof AerialPinholeImage) {
                AerialPinholeImage img = (AerialPinholeImage)renderable;
                if (!img.shouldRender()) continue;

                ComputeCornerData computeCornerData = img.getComputeCornerData();
                ArrayList<LatLon> corners = computeCornerData.getGroundProjectedCorners();
                if (corners == null) {
                    continue;
                }

                Sector sector = computeCornerData.getSector();

                if (MapLayerPicArea.intersectsWith(sector, corners, position)) {
                    LatLon center = MapLayerPicArea.meanPoint(corners);
                    if (center == null) {
                        continue;
                    }

                    double dist =
                        LatLon.ellipsoidalDistance(
                            position, center, globe.getEquatorialRadius(), globe.getPolarRadius());
                    if (dist < minDist) {
                        minDist = dist;
                        topImg = img;
                    }
                }
            }
        }

        return topImg;
    }

    // ###############

    // make shure that the surface image is always rendered first (in background)

    SurfaceImageWithUserData high = null;
    File highFile = null;

    SurfaceImageWithUserData highNext = null;
    File highFileNext = null;

    @Override
    public void preRender(DrawContext dc) {
        try {
            if (surfaceImage != null && shouldShowImages()) surfaceImage.preRender(dc);

            // first, lets check if the current highFile could be replaced by next one
            if (high != null && high.isRendering() && highNext != null && highNext.isRendering()) {
                //			System.out.println("shift highNext to High");
                mute = true;
                removeRenderable(high);
                mute = false;
                high = highNext;
                highFile = highFileNext;
                highNext = null;
            }

            Vec4 center = dc.getView().getCenterPoint();
            boolean doRenderHigh = false;
            if (center != null && shouldShowImages()) {
                // compute high res. preview
                Position pos = dc.getGlobe().computePositionFromPoint(center);

                AerialPinholeImage img =
                    getRealUserObject(
                        pos,
                        new java.awt.Point(
                            (int)dc.getView().getViewport().getCenterX(),
                            (int)dc.getView().getViewport().getCenterY()));

                //			System.out.println("center pos: " + pos + " " + new
                // java.awt.Point((int)dc.getView().getViewport().getCenterX() ,
                // (int)dc.getView().getViewport().getCenterY() ) );
                if (img != null) {
                    double gsd = Double.POSITIVE_INFINITY;
                    ComputeCornerData computeCornerData = img.getComputeCornerData();
                    if (lastThumpDim != null && computeCornerData.getAltOverCenter() != null) {
                        gsd =
                            CameraHelper.estimateGsdAtDistance(
                                computeCornerData.getAltOverCenter(),
                                lastThumpDim.width,
                                lastThumpDim.height,
                                img.getHardwareConfiguration());
                    } else if (computeCornerData.getAltOverCenter() != null) {
                        gsd =
                            CameraHelper.estimateGsdAtDistance(
                                computeCornerData.getAltOverCenter(), img.getHardwareConfiguration());
                    }

                    File file = null;

                    double d = center.distanceTo3(dc.getView().getEyePoint());
                    double pixSize = dc.getView().computePixelSizeAtDistance(d);
                    //		        System.out.println("pixSize:" + pixSize);
                    ImageMask maskParams = null;

                    if (gsd > pixSize * 2 || !(img instanceof AerialPinholeKnownImage)) {
                        //		        	System.out.println("fullRes");
                        MapLayerMatch match = img.getMatch();
                        if (match != null) {
                            file = match.getResourceFile(); // full resolution image!
                            maskParams = match.getMaskWide();
                            //			        		if (match.needsMask() && maskParams==null) file=null;//delay loading
                        }
                    }

                    if ((file == null || !file.exists()) && img instanceof AerialPinholeKnownImage) {
                        AerialPinholeKnownImage img2 = (AerialPinholeKnownImage)img;
                        //		        	System.out.println("preview");
                        file = img2.getThumpFile();
                        MapLayerMatch match = img.getMatch();
                        if (match != null && match.needsMask()) {
                            maskParams = match.getMaskNarrow();
                            //			        		if (maskParams==null) file =null;//delay loading
                        }
                    }

                    //				System.out.println("next preview file: " + file.getName());
                    if (mapLayerPics.isProjectingToGround() && file != null && file.exists()) {
                        if (file.equals(highFile) || file.equals(highFileNext)) {
                            // stay with current highlight files
                            doRenderHigh = true;
                            //						System.out.println("equal to old files");
                        } else {
                            try {
                                mute = true;
                                if (high == null) {
                                    //								high = new SurfaceImageWithUserData(file, img.getCorners());
                                    high = new SurfaceImageWithUserData(); // does not work, I have no clue why!!
                                    high.setImageSource(
                                        file, computeCornerData.getGroundProjectedCorners(), maskParams);
                                    addRenderable(high);
                                    highFile = file;
                                    //								System.out.println("set as High");
                                    doRenderHigh = true;
                                } else if (highNext == null) {
                                    //								highNext = new SurfaceImageWithUserData(file, img.getCorners()); //does
                                    // not work, I have no clue why!!
                                    highNext = new SurfaceImageWithUserData();
                                    highNext.setImageSource(
                                        file, computeCornerData.getGroundProjectedCorners(), maskParams);
                                    addRenderable(highNext);
                                    highFileNext = file;
                                    //								System.out.println("set as HighNext");
                                    doRenderHigh = true;
                                } else {
                                    // render the old ones, until they are loaded, and load this afterwards
                                    doRenderHigh = true;
                                    //								System.out.println("no slot left to load");
                                }

                                mute = false;
                            } catch (Exception e) {
                                Debug.getLog().log(Level.WARNING, "could not load preview image", e);
                            }
                        }
                    }
                }
            }

            if (!doRenderHigh) {
                // remove high res. preview
                mute = true;
                if (high != null) {
                    removeRenderable(high);
                    high.dispose();
                    high = null;
                }

                if (highNext != null) {
                    removeRenderable(highNext);
                    highNext.dispose();
                    highNext = null;
                }

                mute = false;
                //			System.out.println("no highlight");
            }

            super.preRender(dc);
        } finally {
            mute = false;
        }
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (surfaceImage != null && shouldShowImages()) surfaceImage.render(dc);
        super.doRender(dc);
    }

    @Override
    public void pick(DrawContext dc, java.awt.Point point) {
        if (surfaceImage != null && shouldShowImages()) {
            surfaceImageLayer.pick(dc, point);
        } else {
            super.pick(dc, point); // dont use this, otherwise highlightes preview will flicker
        }
    }

    @Override
    public void setSilentUnmute() {
        this.mute = false;
    }

}
