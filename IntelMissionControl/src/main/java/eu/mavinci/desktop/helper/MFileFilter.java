/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoFile;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

public class MFileFilter extends FileFilterUniversal {
    public static MFileFilter txtFilter = new MFileFilter("txt");
    public static MFileFilter logFilter = new MFileFilter("log");
    public static MFileFilter tpoFilter = new MFileFilter("tpo");
    public static MFileFilter connectorUpdateFilter = new MFileFilter("cbin");
    public static MFileFilter mlfUpdateFilter = new MFileFilter("mlf");
    public static MFileFilter rpmUpdateFilter = new MFileFilter("rpm");
    public static MFileFilter appLogFilter = new MFileFilter("applog", "log");
    public static MFileFilter exeFilter = new MFileFilter("exe");
    public static MFileFilter ensoCameraCalFilter = new MFileFilter("ensoCamCal", "cal");
    public static MFileFilter icarosCameraCalFilter = new MFileFilter("icarosCamCal", "cam");
    public static MFileFilter menciCameraCalFilter = new MFileFilter("menciCamCal", "cvcc");
    public static MFileFilter agisoftCameraCalFilter = new MFileFilter("agisoftCamCal", "xml");
    public static MFileFilter macUpdateFilter = new MFileFilter("macUpdate", "dmg");
    public static MFileFilter csvFpExportFilter = new MFileFilter("csvFP", "csv");
    public static MFileFilter rteFpExportFilter = new MFileFilter("rte");
    public static MFileFilter fplFpExportFilter = new MFileFilter("fpl");
    public static MFileFilter gpxFpExportFilter = new MFileFilter("gpx");
    public static MFileFilter csvMatchingExportFilter = new MFileFilter("csvMatching", "csv");
    public static MFileFilter pszMatchingExportFilter = new MFileFilter("photoscanProject", "psz");
    public static MFileFilter psxMatchingExportFilter = new MFileFilter("photoscan/metashapeProject", "psx");
    public static MFileFilter bentleyXMLMatchingExportFilter = new MFileFilter("Bentley xml", "xml");
    public static MFileFilter pix4dMatchingExportFilter = new MFileFilter("pix4d", "p4d");
    public static MFileFilter acpFPExportFilter = new MFileFilter("acp");
    public static MFileFilter anpFPExportFilter = new MFileFilter("anp");
    public static MFileFilter csvAscTecFpExportFilter = new MFileFilter("csvAscTec", "csv");
    public static MFileFilter apUpdateFilter = new MFileFilter("apUpdate", "tgz");
    public static MFileFilter tgzFilter = new MFileFilter("tgz", "tgz", "tar.gz");
    public static MFileFilter kmlFilter = new MFileFilter("kml");
    public static MFileFilter kmzFilter = new MFileFilter("kmz");
    public static MFileFilter kmlKmzFilter = new MFileFilter("kmlkmz", "kml", "kmz");
    public static MFileFilter shpFilter = new MFileFilter("ESRIshape", "shp");
    public static MFileFilter geoTiffFilter = new MFileFilter("geoTiff", "gtif", "tif", "tiff");
    public static MFileFilter tiffFilter = new MFileFilter("tif", "tif", "tiff");
    public static MFileFilter tpsFilter = new MFileFilter("tps");
    public static MFileFilter fmlFilter = new MFileFilter("fml");
    public static MFileFilter flgFilter = new MFileFilter("flg");
    public static MFileFilter vlgFilter = new MFileFilter("vlg");
    public static MFileFilter pngFilter = new MFileFilter("png");
    public static MFileFilter gifFilter = new MFileFilter("gif");
    public static MFileFilter flgZipFilter = new MFileFilter("flg.zip");
    public static MFileFilter vlgZipFilter = new MFileFilter("vlg.zip");
    public static MFileFilter configFilter = new MFileFilter("config");
    public static MFileFilter cameraFilter = new MFileFilter("camera");
    public static MFileFilter airspaceFilter = new MFileFilter("airspace", "txt");
    public static MFileFilter photoLogFilter = new MFileFilter("photoLog", "plg");
    public static MFileFilter photoJsonFilter =
        new MFileFilter("geotags", "json") {

            @Override
            public boolean accept(String name) {
                if (!super.accept(name)) {
                    return false;
                }

                return name.endsWith("_geotags.json");
            }
        };
    public static MFileFilter photoLogZipFilter = new MFileFilter("photoLogZip", "plg.zip");
    public static MFileFilter bbxFilter = new MFileFilter("bbx");
    public static MFileFilter bbxZipFilter = new MFileFilter("bbx.zip");
    public static MFileFilter pmtFilter = new MFileFilter("picMatch", "pmt");
    public static MFileFilter ptgFilter = new MFileFilter("pictureTaGging", "ptg");
    public static MFileFilter archivFilter = new MFileFilter("archiv", "zip", "tar.gz", "tgz", "rar");

    public static MFileFilter wktDcFilter = new MFileFilter("dc", "dc");
    public static MFileFilter wktPrjFilter = new MFileFilter("prj", "prj");

    public static MFileFilter jpegFilterAscTec = new MFileFilter("jpgAscTec", "jpg");

    public static FileFilterUniversal folderFilter =
        new FileFilterUniversal() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return null;
            }

        };

    public static class AscTecFileFilter extends FileFilterUniversal {

        boolean isTrinityLog(File folder) {
            HashSet<String> files;
            if (folder.getParentFile() == null) {
                // forbid logs directly on a drive without a folder in between, otherwise we have no
                // chance to get a name for this log
                return false;
            }
            // TODO: a falcon log should not contain any subfolders!

            if (folder.list() != null) {
                files = new HashSet<>(Arrays.asList(folder.list()));
            } else {
                return false;
            }

            return files.contains("ASCTEC.IFO") && files.contains("ASCTEC.LOG") && files.contains("ASCHP.LOG");
        }

        @Override
        public String getDescription() {
            return "AscTec log folder";
        } // TODO i18n

        @Override
        public boolean accept(File f) {
            return f.exists() && f.isDirectory();
        }

        public boolean acceptTrinityLog(File f) {
            return f.exists() && f.isDirectory() && isTrinityLog(f);
        }
    };

    public static AscTecFileFilter ascTecLogFolder = new AscTecFileFilter();

    public static FileFilterUniversal rikolaTiffs =
        new FileFilterUniversal() {

            @Override
            public String getDescription() {
                return "rikola Tiffs"; // TODO i18n
            }

            @Override
            public boolean accept(File f) {
                return tiffFilter.getWithoutFolders().accept(f)
                    && f.exists()
                    && f.getParentFile().getName().matches("(wavelength)?[0-9_]+(nm)?");
            }
        };

    public static final String[] rawNonTiffExtensions = {"rw2", "arw", "raf"};

    public static final String[] rawExtensions = {"rw2", "arw", "raf", "tif", "tiff", "xmp"};

    public static MFileFilter jpegFilter = new JpegFilterNonPrev(false);
    public static MFileFilter jpegFilterNoRaw = new JpegFilterNonPrev(true, false, false);
    public static MFileFilter jpegFilterInclThumps = new JpegFilterNonPrev(true);
    public static MFileFilter rawFilter = new RawFilterNonPref();
    public static MFileFilter rawFilterNonTiff = new MFileFilter("rawNonTiff", rawNonTiffExtensions);
    public static MFileFilter imagesWithoutPreview = new MFileFilter("imgNonPreview", "xmp");

    public static class JpegFilterNonPrev extends MFileFilter {
        public boolean includeThumps;

        public JpegFilterNonPrev(boolean includeThumps) {
            this(true, includeThumps, true);
        }

        private JpegFilterNonPrev(boolean isAcceptingFoldersAnyway, boolean includeThumps, boolean includeRAW) {
            super("tmp"); // is overwritten anyway
            this.includeThumps = includeThumps;
            key = "jpeg";
            this.isAcceptingFoldersAnyway = isAcceptingFoldersAnyway;
            this.extensions = new Vector<String>();
            this.extensions.add("jpeg");
            this.extensions.add("jpg");
            if (includeRAW) {
                for (String s : rawExtensions) {
                    this.extensions.add(s);
                }
            }

            if (isAcceptingFoldersAnyway) {
                withoutFolders = new JpegFilterNonPrev(false, includeThumps, includeRAW);
            } else {
                withoutFolders = this;
            }
        }

        public boolean accept(String name) {
            // System.out.println("name:" + name + " superAccept:"+super.accept(name) + " incl Thumps:"+includeThumps);
            if (!super.accept(name)) {
                return false;
            }

            boolean includeThumps =
                StaticInjector.getInstance(ISettingsManager.class).getSection(GeneralSettings.class).getOperationLevel()
                        == OperationLevel.DEBUG
                    ? true
                    : this.includeThumps;

            return includeThumps || !(name.startsWith(PhotoFile.PREFIX_PREVIEW_IMG));
        };
    }

    private static class RawFilterNonPref extends MFileFilter {
        public RawFilterNonPref() {
            this(true);
        }

        private RawFilterNonPref(boolean isAcceptingFoldersAnyway) {
            super("tmp"); // is overwritten anyway
            key = "jpeg";
            this.isAcceptingFoldersAnyway = isAcceptingFoldersAnyway;
            this.extensions = new Vector<String>();
            for (String s : rawExtensions) {
                this.extensions.add(s);
            }

            if (isAcceptingFoldersAnyway) {
                withoutFolders = new RawFilterNonPref(false);
            } else {
                withoutFolders = this;
            }
        }

        public boolean accept(String name) {
            if (!super.accept(name)) {
                return false;
            }

            return !(name.startsWith(PhotoFile.PREFIX_PREVIEW_IMG));
        };
    }

    public static FileFilterUniversal allFilter =
        new FileFilterUniversal() {
            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return "";
            }
        };

    public static FileFilterUniversal allFilterNonSVN =
        new FileFilterUniversal() {
            @Override
            public boolean accept(File f) {
                return (!f.getAbsolutePath().contains(File.separator + ".svn"));
            }

            @Override
            public String getDescription() {
                return "";
            }
        };

    public static FileFilterUniversal getAllFilterNonSVNnonTmp(ILicenceManager licenceManager) {
        return new FileFilterUniversal() {
            @Override
            public boolean accept(File f) {
                Licence activeLicence = licenceManager.activeLicenceProperty().get();
                if (activeLicence != null
                        && (activeLicence.getMaxOperationLevel() == OperationLevel.DEBUG
                            || activeLicence.isGrayHawkEdition())) {
                    return (!f.getAbsolutePath().contains(File.separator + ".svn")) && !f.getName().endsWith("~");
                } else {
                    return (!f.getAbsolutePath().contains(File.separator + ".svn"))
                        && !f.getName().endsWith("~")
                        && !f.getName().startsWith("_");
                }
            }

            @Override
            public String getDescription() {
                return "";
            }
        };
    }

    public static FileFilterUniversal notHiddenFilter =
        new FileFilterUniversal() {
            @Override
            public boolean accept(File f) {
                return !f.isHidden();
            }

            @Override
            public String getDescription() {
                return "";
            }
        };

    public static FileFilterUniversal notHiddenFilesFilter =
        new FileFilterUniversal() {
            @Override
            public boolean accept(File f) {
                return !f.isHidden() && !f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "";
            }
        };

    public static FileFilterUniversal notHiddenDirFilter =
        new FileFilterUniversal() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() && !f.isHidden();
            }

            @Override
            public String getDescription() {
                return "";
            }
        };

    public static FileFilterUniversal notHiddenEmptyDirFilter =
        new FileFilterUniversal() {
            @Override
            public boolean accept(File f) {
                String[] listf = f.list();
                Ensure.notNull(listf, "listf");
                return f.isDirectory() && !f.isHidden() && (listf.length == 0);
            }

            @Override
            public String getDescription() {
                return "";
            }
        };

    public static FileFilterUniversal cameraPictures =
        new FileFilterUniversal() {
            @Override
            public boolean accept(File f) {
                if (f.isHidden()) {
                    return false;
                }

                if (!f.isDirectory()) {
                    return false;
                }

                Vector<File> fl = FileHelper.getSDcardJPEGs(f, MFileFilter.jpegFilter.getWithoutFolders());
                return fl.size() != 0;
            }

            @Override
            public String getDescription() {
                return "";
            }
        };

    public static FileFilterUniversal rikolaTifPictures =
        new FileFilterUniversal() {
            @Override
            public boolean accept(File f) {
                if (f.isHidden()) {
                    return false;
                }

                if (!f.isDirectory()) {
                    return false;
                }

                Vector<File> fl = FileHelper.getRikolaTiffs(f);
                return fl.size() != 0;
            }

            @Override
            public String getDescription() {
                return "";
            }
        };

    /** a embedded filter, which is equivalent to the current one, except that he is NOT accepting folders */
    protected MFileFilter withoutFolders;

    public MFileFilter getWithoutFolders() {
        return withoutFolders;
    }

    protected Vector<String> extensions;
    protected boolean isAcceptingFoldersAnyway = true;
    protected String key;

    public MFileFilter(boolean isAcceptingFoldersAnyway, String extension) {
        this.key = extension;
        this.isAcceptingFoldersAnyway = isAcceptingFoldersAnyway;
        extensions = new Vector<String>();
        this.extensions.add(extension);
        if (isAcceptingFoldersAnyway) {
            withoutFolders = new MFileFilter(false, extension);
        } else {
            withoutFolders = this;
        }
    }

    public MFileFilter(String extension) {
        this(true, extension);
    }

    public MFileFilter(boolean isAcceptingFoldersAnyway, String name, String... extensions) {
        key = name;
        this.isAcceptingFoldersAnyway = isAcceptingFoldersAnyway;
        this.extensions = new Vector<String>();
        for (String s : extensions) {
            this.extensions.add(s);
        }

        if (isAcceptingFoldersAnyway) {
            withoutFolders = new MFileFilter(false, name, extensions);
        } else {
            withoutFolders = this;
        }
    }

    public MFileFilter(String name, String... extensions) {
        this(true, name, extensions);
    }

    @Override
    public boolean accept(File f) {
        if (f == null) {
            return false;
        }

        if (f.isHidden()) {
            return false;
        }

        if (f.isDirectory()) {
            return isAcceptingFoldersAnyway;
        }

        return accept(f.getName());
    }

    public boolean acceptWithoutFolders(File f) {
        if (f == null) {
            return false;
        }

        if (f.isHidden()) {
            return false;
        }

        return accept(f.getName());
    }

    public boolean accept(String name) {
        String lowCaseName = name.toLowerCase();
        for (String s : extensions) {
            s = "." + s;
            if (lowCaseName.endsWith(s)) {
                return true;
            }
        }

        return false;
    }

    public String removeExtension(File f) {
        return removeExtension(f.getAbsolutePath());
    }

    public String removeExtension(String f) {
        String low = f.toLowerCase();
        for (String s : extensions) {
            if (low.endsWith(s.toLowerCase())) {
                return f.substring(0, f.length() - s.length() - 1);
            }
        }

        return f;
    }

    @Override
    public String getDescription() {
        return "";
    }

    public String getExtension() {
        return extensions.get(0);
    }

}
