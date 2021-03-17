/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.cssconv;

import javafx.css.Stylesheet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Main {

    public static void main(String[] args_) throws IOException {
        if (args_.length == 0) {
            showUsage();
            return;
        }

        String infile = null, outfile = null, indir = null;
        boolean recursive = false, delete = false, clear = false;

        Queue<String> args = new LinkedList<>(Arrays.asList(args_));
        while (!args.isEmpty()) {
            switch (args.poll()) {
            case "-f":
                infile = args.poll();
                outfile = args.poll();
                break;

            case "-d":
                indir = args.poll();
                break;

            case "-r":
                recursive = true;
                break;

            case "-rm":
                delete = true;
                break;

            case "-c":
                clear = true;
                break;
            }
        }

        if (infile != null && outfile == null || infile == null && outfile != null) {
            throw new IllegalArgumentException("Need to specify infile and outfile.");
        }

        if (infile != null) {
            convertFile(infile, outfile, delete, clear);
        } else if (indir != null) {
            convertDir(indir, recursive, delete, clear);
        } else {
            showUsage();
        }
    }

    private static void showUsage() {
        System.out.println("Usage: cssconv [-f infile outfile] [-d indir] [-r] [-rm] [-c]");
        System.out.println("  -f    process single file");
        System.out.println("  -d    process all files in directory");
        System.out.println("  -r    process subdirectories recursively");
        System.out.println("  -rm   remove CSS file after conversion to BSS file");
        System.out.println("  -c    don't remove CSS files, but clear the content");
    }

    private static void convertFile(String infile, String outfile, boolean delete, boolean clear) throws IOException {
        File file = new File(infile);
        System.out.print("Converting " + file.getAbsolutePath());

        try {
            Stylesheet.convertToBinary(file, new File(outfile));

            if (delete) {
                file.delete();
            } else if (clear) {
                new PrintWriter(file).close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private static void convertDir(String indir, boolean recursive, boolean delete, boolean clear) throws IOException {
        File dir = new File(indir);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory.");
        }

        if (recursive) {
            convertDirRecursive(dir, delete, clear);
        } else {
            convertFilesInDir(dir, delete, clear);
        }
    }

    private static void convertFilesInDir(File dir, boolean delete, boolean clear) throws IOException {
        File[] files = dir.listFiles(File::isFile);
        if (files == null) {
            return;
        }

        for (File file : files) {
            String name = file.getAbsolutePath();
            if (!name.toLowerCase().endsWith(".css")) {
                continue;
            }

            String newName = name.substring(0, name.length() - 4) + ".bss";
            System.out.println("Converting " + name);

            try {
                Stylesheet.convertToBinary(file, new File(newName));

                if (delete) {
                    file.delete();
                } else if (clear) {
                    new PrintWriter(file).close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
    }

    private static void convertDirRecursive(File dir, boolean delete, boolean clear) throws IOException {
        File[] subdirs = dir.listFiles(File::isDirectory);
        if (subdirs == null) {
            return;
        }

        convertFilesInDir(dir, delete, clear);

        for (File subdir : subdirs) {
            convertDirRecursive(subdir, delete, clear);
        }
    }

}
