/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.cir.Histogram;
import eu.mavinci.desktop.gui.widgets.MProgressMonitor;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import org.apache.commons.io.FilenameUtils;

public class ImageHelper {

    public static final String KEY = "eu.mavinci.desktop.helper.ImageHelper";

    public static synchronized BufferedImage loadImage(File file) throws IOException {
        // since WWJ installs some own tiff reader, we have to avoid this one,
        // to be able to read rikola tiffs without problems
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }

        if (!file.canRead()) {
            throw new IOException("could not read: " + file.toString());
        }

        if (file.length() == 0) {
            throw new IOException("file empty: " + file.toString());
        }

        // rikola tiff images are broken due to gdal tiff reader placed by factory pattern into java imageIO
        if (MFileFilter.tiffFilter.accept(file)) {
            FileSeekableStream stream = new FileSeekableStream(file);
            TIFFDecodeParam decodeParam = new TIFFDecodeParam();
            decodeParam.setDecodePaletteAsShorts(true);
            ParameterBlock params = new ParameterBlock();
            params.add(stream);
            RenderedOp image1 = JAI.create("tiff", params);
            BufferedImage img = image1.getAsBufferedImage();
            if (img == null) {
                throw new IOException("cant read image:" + file);
            }

            return img;
        } else {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                throw new IOException("cant read image:" + file);
            }

            return img;
        }
    }

    public static BufferedImage loadTiffNonWWJ(InputStream is) throws IOException {
        try {
            // since WWJ installs some own tiff reader, we have to avoid this one,
            // to be able to read rikola tiffs without problems
            // rikola tiff images are broken due to gdal tiff reader placed by factory pattern into java imageIO
            // FileSeekableStream stream = new FileSeekableStream(file);
            SeekableStream stream = new MemoryCacheSeekableStream(is);
            TIFFDecodeParam decodeParam = new TIFFDecodeParam();
            decodeParam.setDecodePaletteAsShorts(true);
            ParameterBlock params = new ParameterBlock();
            params.add(stream);
            RenderedOp image1 = JAI.create("tiff", params);
            return image1.getAsBufferedImage();
        } finally {
            is.close();
        }
    }

    /**
     * store an BufferedImage without compression to a file strait forward java ImageIO approach would use gdal storing
     * method, and is somehow broken!
     *
     * @param img
     * @param out
     * @throws IOException
     */
    public static void writeTiffWithoutGdal(BufferedImage img, File out) throws IOException {
        // ImageIO.write(img,"tiff", out); //is not working with tiff due to GDAL!?!?!
        com.sun.media.jai.codec.TIFFEncodeParam paramsOut = new com.sun.media.jai.codec.TIFFEncodeParam();
        paramsOut.setCompression(com.sun.media.jai.codec.TIFFEncodeParam.COMPRESSION_NONE);
        try (FileOutputStream os = new FileOutputStream(out)) {
            javax.media.jai.JAI.create("encode", img, os, "TIFF", paramsOut);
        }
    }

    /**
     * creates two mask files for images from fisheye compatibleLenseIds.
     *
     * <p>maskNarrowTarget contains only a very narrow mask hiding the most outer region while maskWideTarget masks out
     * much more, so no shadows should be visible in the masked images
     *
     * @param sampleImgs
     * @param maskNarrowTarget
     * @param maskWideTarget
     * @throws IOException
     */
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    public static boolean createMask(Vector<File> sampleImgs, File maskNarrowTarget, File maskWideTarget, String name)
            throws IOException {
        int prog = 0;
        int x;
        int y;

        String format;
        MProgressMonitor mon =
            new MProgressMonitor(
                null,
                languageHelper.getString(KEY + ".msg", name),
                languageHelper.getString(KEY + ".note.init"),
                0,
                sampleImgs.size() + 5);
        try {
            BufferedImage imgMask = loadImage(sampleImgs.firstElement());

            ImgParams params = new ImgParams(imgMask);

            // reset the mask to have only grayscale
            imgMask = new BufferedImage(params.width, params.height, BufferedImage.TYPE_BYTE_GRAY);
            DataBuffer bufferMask = imgMask.getRaster().getDataBuffer();
            int[] avgCol = new int[params.pixCount];

            // prepare white mask
            for (int i = 0; i != params.max / params.bytePerPixel; i++) {
                bufferMask.setElem(i, 0);
            }

            for (File inImg : sampleImgs) {
                prog++;
                mon.setProgressNote(languageHelper.getString(KEY + ".note.load", inImg.getName()), prog);
                if (mon.isCanceled()) {
                    return false;
                }

                BufferedImage img = loadImage(inImg);
                // System.out.println("process img: " + inImg);

                DataBuffer buffer = img.getRaster().getDataBuffer();
                for (int i = 0; i != params.max; i += params.bytePerPixel) {
                    double r = buffer.getElemDouble(i + params.OFFSET_R);
                    double b = buffer.getElemDouble(i + params.OFFSET_B);
                    double g = buffer.getElemDouble(i + params.OFFSET_G);

                    // double gray = (r+g+b)/3;
                    // hist.count(gray);
                    avgCol[i / params.bytePerPixel] += r + g + b;
                }

                // double threshold = hist.getPercentileBin(.3);

                // for (int i = 0; i != max; i += bytePerPixel) {
                //// if (bytePerPixel==4){
                //// double a = buffer.getElemDouble(i + OFFSET_A);
                //// if (a!=255) continue;
                //// }
                // double r = buffer.getElemDouble(i + OFFSET_R);
                // double b = buffer.getElemDouble(i + OFFSET_B);
                // double g = buffer.getElemDouble(i + OFFSET_G);
                //
                // double gray = (r+g+b)/3;
                // int newMask= (gray < threshold) ? 0:255;
                //// System.out.println(gray);
                //
                // int iMask =i/bytePerPixel;
                // int oldMask =bufferMask.getElem(iMask);
                // newMask = Math.max(oldMask, newMask);//mask value has to be black in EVERY input image
                //
                // bufferMask.setElem(iMask,newMask);
                // }

            }

            prog++;
            mon.setProgressNote(languageHelper.getString(KEY + ".note.polarize"), prog);
            if (mon.isCanceled()) {
                return false;
            }

            Histogram hist = new Histogram(0, 255, 1, false);

            double norm = sampleImgs.size() * 3; // 3 because of band count

            if (DependencyInjector.getInstance()
                        .getInstanceOf(ISettingsManager.class)
                        .getSection(GeneralSettings.class)
                        .getOperationLevel()
                    == OperationLevel.DEBUG) {
                x = 0;
                y = 0;
                // writing avg image sample
                int[][] avg = new int[params.width][params.height];
                for (int i = 0; i != params.pixCount; i++) {
                    int val = (int)(avgCol[i] / norm);
                    avg[x][y] = val;

                    x++;
                    if (x >= params.width) {
                        x = 0;
                        y++;
                    }
                }

                x = 0;
                y = 0;
                for (int i = 0; i != params.pixCount; i++) {
                    bufferMask.setElem(i, avg[x][y]);

                    x++;
                    if (x >= params.width) {
                        x = 0;
                        y++;
                    }
                }

                format = maskNarrowTarget.getName();
                format = format.substring(format.length() - 3);
                File fileSample = new File(maskNarrowTarget.getAbsolutePath() + "_avg." + format);
                ImageIO.write(imgMask, format, fileSample);
                System.out.println("write avg result: " + fileSample);
                // try {
                // FileHelper.openFile(fileSample);
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
            }

            // System.out.println("norm:"+norm);

            for (int i : avgCol) {
                hist.count(i / norm);
            }

            double threshold =
                MathHelper.intoRange(
                    hist.getMinErrorThreshold(), hist.getPercentileBin(.10), hist.getPercentileBin(.40));
            // threshold*=2;
            // System.out.println("thresholdNEW:"+threshold + " minErr:" +hist.getMinErrorThreshold()+" min:"+
            // hist.getPercentileBin(.10)+"
            // max:"+hist.getPercentileBin(.40));

            threshold *= norm;

            // apply threshold and convert to 2d array
            int[][] mask = new int[params.width][params.height];
            x = 0;
            y = 0;
            for (int i = 0; i != params.pixCount; i++) {
                // int val=(int) (avgCol[i] / norm);
                int val = (avgCol[i] < threshold) ? 0 : 255;
                mask[x][y] = val;

                x++;
                if (x >= params.width) {
                    x = 0;
                    y++;
                }
            }

            if (DependencyInjector.getInstance()
                        .getInstanceOf(ISettingsManager.class)
                        .getSection(GeneralSettings.class)
                        .getOperationLevel()
                    == OperationLevel.DEBUG) {
                // 2d array to buffImg
                x = 0;
                y = 0;
                for (int i = 0; i != params.pixCount; i++) {
                    // bufferMask.setElem(i,mask[x][y]);
                    bufferMask.setElem(i, mask[x][y] == 0 ? 0 : 255);

                    x++;
                    if (x >= params.width) {
                        x = 0;
                        y++;
                    }
                }

                format = maskNarrowTarget.getName();
                format = format.substring(format.length() - 3);
                File filePol = new File(maskNarrowTarget.getAbsolutePath() + "_avg_polarized." + format);
                ImageIO.write(imgMask, format, filePol);
            }

            // find sourounding black cluster
            // errode this cluster by 10 pixels
            // this cluster is tagged black, everthing else white
            // https://en.wikipedia.org/wiki/Erosion_(morphology)

            prog++;
            mon.setProgressNote(languageHelper.getString(KEY + ".note.clusterize"), prog);
            if (mon.isCanceled()) {
                return false;
            }

            // figure out whats outer area by clustering all empty nodes
            // with the hoshen-kopelman algorithm, additionally all empty nodes hitting a border should belong to
            // cluster 0

            int[] clusterRename = new int[params.pixCount];
            int[][] clusterNo = new int[params.width][params.height];

            int curCol;
            int curCluster;

            // make sure all corner are black
            mask[0][0] = 0;
            mask[params.width - 1][0] = 0;
            mask[0][params.height - 1] = 0;
            mask[params.width - 1][params.height - 1] = 0;

            // upper left corner
            // make sure cluster 0 is the black cluster / the outer cluster
            curCol = mask[0][0];
            curCluster = 0;

            // cluster =0, outer cluster without points
            clusterRename[0] = 0;
            int nextFreeClusterNo = 1;

            // first row
            for (x = 1; x != params.width - 1; x++) {
                int lastCol = curCol;
                curCol = mask[x][0];
                if (curCol != lastCol) {
                    curCluster = nextFreeClusterNo;
                    clusterRename[nextFreeClusterNo] = nextFreeClusterNo;
                    nextFreeClusterNo++;
                }

                clusterNo[x][0] = curCluster;
            }

            // upper right corner -> also cluster 0 !!!
            mask[params.width - 1][0] = 0;
            clusterNo[params.width - 1][0] = 0;
            // rename current cluster to 0 if nessesary
            clusterRename[curCluster] = 0;
            curCluster = 0;

            // System.out.println("nextFreeClusterNo:"+nextFreeClusterNo);
            //
            // for (x= 1; x != width; x++){
            // System.out.print(clusterNo[x][0]+",");
            // }
            // System.out.println();

            // all except the first rot
            for (y = 1; y != params.height; y++) {
                // first cell in this row
                curCol = mask[0][y];
                int aboveCol = mask[0][y - 1];
                if (curCol != aboveCol) {
                    curCluster = nextFreeClusterNo;
                    clusterRename[nextFreeClusterNo] = nextFreeClusterNo;
                    nextFreeClusterNo++;
                } else {
                    int aboveCluster = clusterNo[0][y - 1];
                    curCluster = aboveCluster;
                }
                // System.out.println("curCluster:"+curCluster);

                clusterNo[0][y] = curCluster;

                // all the rest..
                for (x = 1; x != params.width; x++) {
                    int lastCol = curCol;
                    // lastCluster = curCluster;
                    curCol = mask[x][y];
                    aboveCol = mask[x][y - 1];

                    int aboveCluster = clusterNo[x][y - 1];
                    if (curCol == lastCol) {
                        if (curCol == aboveCol) {
                            // collision
                            if (curCluster == aboveCluster) {
                                // allready in the same cluster
                                // curCluster = aboveCluster;
                            } else {
                                // if (curCol!=0){
                                // System.out.println("found white sample" + curCluster+" " + aboveCluster);
                                // }

                                // merge clusters
                                int a = find(curCluster, clusterRename);
                                int b = find(aboveCluster, clusterRename);

                                // System.exit(-1);
                                curCluster = Math.min(a, b);
                                int i = Math.max(a, b);
                                clusterRename[i] = curCluster;
                                // System.out.println("new Rename:" + i+"->" + curCluster);
                                // if (curCol!=0){
                                // System.exit(-2);
                                // }
                            }
                            // } else {
                            // curCluster = lastCluster;
                        }
                    } else {
                        if (curCol == aboveCol) {
                            curCluster = aboveCluster;
                        } else {
                            // new cluster
                            curCluster = nextFreeClusterNo;
                            clusterRename[nextFreeClusterNo] = nextFreeClusterNo;
                            nextFreeClusterNo++;
                        }
                    }

                    clusterNo[x][y] = curCluster;
                    // System.out.print(clusterNo[x][y]+",");
                }

                // for (x= 1; x != width; x++){
                // System.out.print(clusterNo[x][0]+",");
                // }
                // System.out.println();
                // System.exit(-1);

            }

            for (int i = 1; i != nextFreeClusterNo; i++) {
                clusterRename[i] = find(i, clusterRename); // [clusterRename[i]];
                // System.out.println(i+"->"+clusterRename[i]);
            }

            // make sure both lower corners are also considered to belong to the black cluster
            // rename cluster to 0
            clusterRename[find(clusterNo[0][params.height - 1], clusterRename)] = 0;
            clusterRename[find(clusterNo[params.width - 1][params.height - 1], clusterRename)] = 0;

            for (int i = 1; i != nextFreeClusterNo; i++) {
                clusterRename[i] = find(i, clusterRename);
            }

            // for (x=0; x!=params.width; x++){
            // System.out.println("clusterNo["+x+"][params.height-1]="+clusterNo[x][params.height-1] + " renamed:
            // "+clusterRename[clusterNo[x][params.height-1]]);
            // }

            // System.out.println("nextFreeClusterNo:"+nextFreeClusterNo);
            // System.out.println("final rename:");
            // cleanup rename mapping, so that every mapping is pointing to a identity mapping

            // apply renaming
            for (y = 0; y != params.height; y++) {
                for (x = 0; x != params.width; x++) {
                    clusterNo[x][y] = clusterRename[clusterNo[x][y]];
                }
            }

            // creating mask
            for (y = 0; y != params.height; y++) {
                for (x = 0; x != params.width; x++) {
                    mask[x][y] = clusterNo[x][y] == 0 ? 0 : 255;
                }
            }

            // 2d array to buffImg -cluster map
            if (DependencyInjector.getInstance()
                        .getInstanceOf(ISettingsManager.class)
                        .getSection(GeneralSettings.class)
                        .getOperationLevel()
                    == OperationLevel.DEBUG) {
                int max = 0;
                for (y = 0; y != params.height; y++) {
                    for (x = 0; x != params.width; x++) {
                        max = Math.max(clusterNo[x][y], max);
                    }
                }

                System.out.println("max numbers of avaliable clusters:" + max);
                x = 0;
                y = 0;
                for (int i = 0; i != params.pixCount; i++) {
                    // bufferMask.setElem(i,mask[x][y]);
                    bufferMask.setElem(i, (int)(clusterNo[x][y] * 255. / max));

                    x++;
                    if (x >= params.width) {
                        x = 0;
                        y++;
                    }
                }

                format = maskNarrowTarget.getName();
                format = format.substring(format.length() - 3);
                File fileClust = new File(maskNarrowTarget.getAbsolutePath() + "_avg_polarized_clust." + format);
                ImageIO.write(imgMask, format, fileClust);
            }

            // 2d array to buffImg
            x = 0;
            y = 0;
            for (int i = 0; i != params.pixCount; i++) {
                // bufferMask.setElem(i,mask[x][y]);
                bufferMask.setElem(i, mask[x][y] == 0 ? 0 : 255);

                x++;
                if (x >= params.width) {
                    x = 0;
                    y++;
                }
            }

            format = maskNarrowTarget.getName();
            format = format.substring(format.length() - 3);
            ImageIO.write(imgMask, format, maskNarrowTarget);
            // System.out.println("write narrowMask: " + maskNarrowTarget);

            ////////////////////////
            //////// ERROSION///////
            ////////////////////////

            prog++;
            mon.setProgressNote(languageHelper.getString(KEY + ".note.errode"), prog);
            if (mon.isCanceled()) {
                return false;
            }

            // errode
            int[][] maskNew = new int[params.width][params.height];
            // init with white
            for (x = 1; x != params.width - 1; x++) {
                for (y = 1; y != params.height - 1; y++) {
                    // maskNew[x][y]=255;
                    maskNew[x][y] = mask[x][y];
                }
            }

            // long t =System.currentTimeMillis();

            // create errosionMask
            // int[][] errosionMask = new int[NUMPIXELERRODE*2+1][NUMPIXELERRODE*2+1];
            // for (x=-NUMPIXELERRODE; x<=NUMPIXELERRODE; x++){
            // for (y=-NUMPIXELERRODE; y<=NUMPIXELERRODE; y++){
            //// double dist = Math.sqrt(x*x+y*y);
            // errosionMask[x+NUMPIXELERRODE][y+NUMPIXELERRODE] = 1;//dist<=NUMPIXELERRODE?1:0;
            // }
            // }

            for (x = 0; x != params.width; x++) {
                for (y = 0; y != params.height; y++) {
                    if (mask[x][y] == 0) {
                        // if sourungind 4 pixels are black, they can do the errosion as well,its not nessesary to do it
                        // here
                        int num = 0;
                        if (x > 0) {
                            num += mask[x - 1][y];
                        }

                        if (x < params.width - 1) {
                            num += mask[x + 1][y];
                        }

                        if (y > 0) {
                            num += mask[x][y - 1];
                        }

                        if (y < params.height - 1) {
                            num += mask[x][y + 1];
                        }

                        if (num == 0) {
                            continue;
                        }

                        for (int xi = -NUMPIXELERRODE; xi <= NUMPIXELERRODE; xi++) {
                            int xn = x + xi;
                            if (xn < 0 || xn >= params.width) {
                                continue;
                            }

                            for (int yi = -NUMPIXELERRODE; yi <= NUMPIXELERRODE; yi++) {
                                int yn = y + yi;
                                if (yn < 0 || yn >= params.height) {
                                    continue;
                                }
                                // if (errosionMask[xi+NUMPIXELERRODE][yi+NUMPIXELERRODE]!=0){
                                maskNew[xn][yn] = 0;
                                // }
                            }
                        }
                        // x+=NUMPIXELERRODE-1;
                    }
                }
            }

            mask = maskNew;

            // System.out.println("time for errosion: "+(System.currentTimeMillis() -t));

            // 2d array to buffImg
            x = 0;
            y = 0;
            for (int i = 0; i != params.pixCount; i++) {
                // bufferMask.setElem(i,mask[x][y]);
                bufferMask.setElem(i, mask[x][y] == 0 ? 0 : 255);

                x++;
                if (x >= params.width) {
                    x = 0;
                    y++;
                }
            }

            format = maskWideTarget.getName();
            format = format.substring(format.length() - 3);
            ImageIO.write(imgMask, format, maskWideTarget);
        } finally {
            mon.close();
        }
        // System.out.println("write maskWideTarget: " + maskWideTarget);
        return true;
    }

    public static final int NUMPIXELERRODE = 35;

    static int find(int idx, int[] clusterRename) {
        int x = idx;
        int _x = clusterRename[x];
        // System.out.println(x+"->"+_x);
        while (_x != x) {
            x = _x;
            _x = clusterRename[x];
            // System.out.println(x+"->"+_x);
        }

        if (idx != x) {
            clusterRename[idx] = x;
        }

        return x;
    }

    public static Dimension getImageDimension(File image) {
        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(FilenameUtils.getExtension(image.getName()));
        if (iter.hasNext()) {
            ImageReader reader = iter.next();
            ImageInputStream stream = null;
            try {
                stream = new FileImageInputStream(image);
                reader.setInput(stream);
                return new Dimension(reader.getWidth(reader.getMinIndex()), reader.getHeight(reader.getMinIndex()));
            } catch (IOException e) {
            } finally {
                reader.dispose();
                try {
                    stream.close();
                } catch (Exception e) {
                }
            }
        }

        return null;
    }
}
