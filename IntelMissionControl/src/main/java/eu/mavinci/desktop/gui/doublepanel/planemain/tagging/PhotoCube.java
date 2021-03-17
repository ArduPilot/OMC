/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.desktop.helper.sync.ArrayIterator;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

public class PhotoCube implements Comparable<PhotoCube>, Iterable<PhotoFile> {

    public static final PhotoCube EMPTY_CUBE = new PhotoCube(0);

    public final PhotoFile[] photoFiles;

    public PhotoCube(PhotoFile[] photoFiles) {
        this.photoFiles = photoFiles;
    }

    public PhotoCube(int noBands) {
        this.photoFiles = new PhotoFile[noBands];
    }

    public PhotoCube(File f) throws Exception {
        this(new PhotoFile[] {new PhotoFile(f)});
    }

    public PhotoCube(File f, ExifInfos exif) throws Exception {
        this(new PhotoFile[] {new PhotoFile(f, exif)});
    }

    public PhotoCube(PhotoFile photoFile) {
        this(new PhotoFile[] {photoFile});
    }

    public int getNumberFiles() {
        return photoFiles.length;
    }

    public int noInFolderTmp;
    public CPhotoLogLine logTmp;

    @Override
    public int hashCode() {
        return Arrays.hashCode(photoFiles);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        PhotoCube other = (PhotoCube)obj;
        if (!Arrays.equals(photoFiles, other.photoFiles)) {
            return false;
        }

        return true;
    }

    public double getTimestamp() {
        return photoFiles[0].getExif().timestamp;
    }

    public boolean isTimestampValid() {
        return getTimestamp() > 0;
    }

    @Override
    public int compareTo(PhotoCube o) {
        // workaround for a broken clock, fallback to filename order!
        if (!isTimestampValid() || !o.isTimestampValid()) {
            // System.out.println("compare broken PhotFiles");
            return photoFiles[0].getFile().getName().compareTo(o.photoFiles[0].getFile().getName());
        }

        int t = (int)(getTimestamp() - o.getTimestamp());
        if (t != 0) {
            return t;
        }

        t = Double.compare(photoFiles[0].wavelength, o.photoFiles[0].wavelength);
        if (t != 0) {
            return t;
        }

        return photoFiles[0].getFile().getName().compareTo(o.photoFiles[0].getFile().getName());
        // if (t==0 && o !=this) System.out.println("files equal? " + o + " " + this);
        // return date.compareTo(o.date); //wont work if timestamps are equal!!
    }

    @Override
    public Iterator<PhotoFile> iterator() {
        return new ArrayIterator<>(photoFiles);
    }

    public void setMatch(MapLayerMatch match) {
        for (PhotoFile pf : this) {
            pf.setMatch(match);
        }
    }
}
