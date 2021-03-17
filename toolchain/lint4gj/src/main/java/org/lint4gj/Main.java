/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class Main {

    private static final String DEFAULT_CONFIG_FILENAME = "lint4gj.cfg";
    private static final String GIT_DIFF = "git --no-pager diff --name-only ..";
    private static final String GIT_BRANCH = "git show -s --pretty=%D HEAD";

    private static int errors, infos;
    private static Stack<Set<Rule>> currentGlobalRules = new Stack<>();
    private static Set<Rule> allGlobalRules = new HashSet<>();
    private static String[] changedFiles;
    private static String trackBranchName, currentBranchName;
    private static boolean verbose;

    public static void main(String[] args_) throws IOException {
        if (args_.length == 0) {
            showUsage();
            return;
        }

        String infile = null, indir = null, configFile = null;
        boolean recursive = false, banner = false;

        Queue<String> args = new LinkedList<>(Arrays.asList(args_));
        while (!args.isEmpty()) {
            switch (args.poll()) {
            case "-f":
                infile = args.poll();
                break;

            case "-d":
                indir = args.poll();
                break;

            case "-c":
                configFile = args.poll();
                break;

            case "-r":
                recursive = true;
                break;

            case "-b":
                banner = true;
                break;

            case "-v":
                verbose = true;
                break;

            case "-t":
                trackBranchName = args.poll();
                break;
            }
        }

        if (banner) {
            showBanner();
        }

        if (configFile != null) {
            Set<Rule> rules = Rule.readRules(new File(configFile), Main::printError);
            if (!rules.isEmpty()) {
                allGlobalRules.addAll(rules);
                currentGlobalRules.push(rules);
            }
        }

        if (infile != null) {
            if (!tryInitTrackBranch(new File(infile).getParentFile())) {
                System.exit(-1);
            }

            scanFile(new File(infile), null);
        } else if (indir != null) {
            if (!tryInitTrackBranch(new File(indir))) {
                System.exit(-1);
            }

            scanDir(indir, recursive);
        } else {
            showUsage();
        }

        boolean success = showEpilog();

        if (success && trackBranchName != null && changedFiles != null) {
            Map<Maintainer, List<String>> changedFilesMap = getChangedFilesPerMaintainer(changedFiles);
            showChangedFiles(changedFilesMap);

            if (isServerBuild()) {
                EmailSender emailSender = new EmailSender();

                for (Map.Entry<Maintainer, List<String>> entry : changedFilesMap.entrySet()) {
                    emailSender.sendMail(currentBranchName, trackBranchName, entry.getKey(), entry.getValue());
                }
            }
        }

        if (!success) {
            System.exit(-1);
        }
    }

    static void debugShowChangedFiles() {
        Map<Maintainer, List<String>> changedFilesMap = getChangedFilesPerMaintainer(changedFiles);
        showChangedFiles(changedFilesMap);
    }

    static boolean showEpilog() {
        for (Rule rule : allGlobalRules) {
            if (!rule.encountered) {
                ++errors;
                System.err.println("[lint4gj] Rule did not match any files: " + rule.getOriginalPattern());
            }
        }

        if (errors == 0) {
            if (infos == 0 || verbose) {
                System.out.println("[lint4gj] Code scan passed with 0 errors.");
            } else {
                System.out.println(
                    "[lint4gj] Code scan passed with 0 errors, "
                        + infos
                        + (infos == 1 ? " message. " : " messages. ")
                        + "Enable verbose output with -v.");
            }
        } else if (errors == 1) {
            if (infos == 0 || verbose) {
                System.out.println("[lint4gj] Code scan failed with 1 error.");
            } else {
                System.out.println(
                    "[lint4gj] Code scan failed with 1 error, "
                        + infos
                        + (infos == 1 ? " message. " : " messages. ")
                        + "Enable verbose output with -v.");
            }
        } else {
            if (infos == 0 || verbose) {
                System.out.println("[lint4gj] Code scan failed with " + errors + " errors.");
            } else {
                System.out.println(
                    "[lint4gj] Code scan failed with "
                        + errors
                        + " errors, "
                        + infos
                        + (infos == 1 ? " message. " : " messages. ")
                        + "Enable verbose output with -v.");
            }
        }

        return errors == 0;
    }

    private static void showChangedFiles(Map<Maintainer, List<String>> changedFilesMap) {
        for (Map.Entry<Maintainer, List<String>> entry : changedFilesMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                System.out.println(
                    "[lint4gj] The following files have changed with respect to branch '"
                        + trackBranchName
                        + "', please inform maintainer "
                        + entry.getKey()
                        + ":");

                for (String file : entry.getValue()) {
                    System.out.println("    " + file);
                }
            }
        }
    }

    private static void showBanner() {
        System.out.println(
            ".- .-.. .-..    -.-- --- ..- .-.    -... .- ... .    .- .-. .    -... . .-.. --- -. --.    - ---    ..- ...");
    }

    private static void showUsage() {
        System.out.println("Usage: lint4gj [-f infile] [-d indir] [-c file] [-t <branch>] [-r] [-v] [-b]");
        System.out.println("  -f    process single file");
        System.out.println("  -d    process all files in directory");
        System.out.println("  -r    process subdirectories recursively");
        System.out.println("  -c    configuration file");
        System.out.println("  -v    verbose output");
        System.out.println("  -t    track changes with respect to this branch");
        System.out.println("  -e    inform maintainers of file changes by email");
        System.out.println("  -b    show banner");
    }

    private static void printError(Linter linter, String message) {
        ++errors;
        System.err.println("[lint4gj::" + linter.getClass().getSimpleName() + "] " + message);
    }

    private static void printInfo(Linter linter, String message) {
        ++infos;

        if (verbose) {
            System.out.println("[lint4gj::" + linter.getClass().getSimpleName() + "] " + message);
        }
    }

    private static void scanFile(File file, Rule rule) throws IOException {
        try {
            if (rule != null) {
                new CodeScanner(
                        file,
                        rule.getMaintainers().orElse(new Maintainer[0]),
                        rule.getSuppressions().orElse(Collections.emptySet()))
                    .scan(Main::printError, Main::printInfo);
            } else {
                new CodeScanner(file).scan(Main::printError, Main::printInfo);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    static void scanDir(String indir, boolean recursive) throws IOException {
        File dir = new File(indir);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory.");
        }

        if (recursive) {
            scanDirRecursive(dir);
        } else {
            Set<Rule> rules = Rule.readRules(new File(dir, DEFAULT_CONFIG_FILENAME), Main::printError);
            if (!rules.isEmpty()) {
                allGlobalRules.addAll(rules);
                currentGlobalRules.push(rules);
            }

            scanFilesInDir(dir);
        }
    }

    private static void scanFilesInDir(File dir) throws IOException {
        File[] files = dir.listFiles(File::isFile);
        if (files == null) {
            return;
        }

        Set<Rule> rules = new TreeSet<>();
        for (Set<Rule> sup : currentGlobalRules) {
            rules.addAll(sup);
        }

        for (File file : files) {
            String name = file.getAbsolutePath();
            if (!name.toLowerCase().endsWith(".java")) {
                continue;
            }

            scanFile(file, selectRule(file, rules));
        }
    }

    private static Rule selectRule(File file, Set<Rule> rules) {
        String fileName = file.getAbsolutePath().toLowerCase().replace('\\', '/');
        Rule selectedRule = null;
        Maintainer[] maintainers = null;
        Set<Class<? extends Linter>> suppressions = null;

        for (Rule rule : rules) {
            String n = fileName.substring(rule.getBasePath().length()).toLowerCase();

            if (rule.getPattern().equals(n)) {
                rule.encountered = true;
                maintainers = rule.getMaintainers().orElse(maintainers);
                suppressions = rule.getSuppressions().orElse(suppressions);
                selectedRule = rule;
                // don't break here, because we want to match all applicable entries
            } else {
                String pattern = rule.getPattern();
                int idx = Utils.indexOfDifference(n, pattern);
                if (idx >= 0 && pattern.substring(idx).equals("*") && pattern.length() == (idx + 1)) {
                    rule.encountered = true;
                    maintainers = rule.getMaintainers().orElse(maintainers);
                    suppressions = rule.getSuppressions().orElse(suppressions);
                    selectedRule = rule;
                    // don't break here, because we want to match all applicable entries
                }
            }
        }

        return Rule.amend(selectedRule, maintainers, suppressions);
    }

    private static void scanDirRecursive(File dir) throws IOException {
        File[] subdirs = dir.listFiles(File::isDirectory);
        if (subdirs == null) {
            // 'dir' wasn't a directory, or an I/O error occurred
            return;
        }

        Set<Rule> rules = Rule.readRules(new File(dir, DEFAULT_CONFIG_FILENAME), Main::printError);
        if (!rules.isEmpty()) {
            allGlobalRules.addAll(rules);
            currentGlobalRules.push(rules);
        }

        scanFilesInDir(dir);

        for (File subdir : subdirs) {
            scanDirRecursive(subdir);
        }

        if (!rules.isEmpty()) {
            currentGlobalRules.pop();
        }
    }

    private static Map<Maintainer, List<String>> getChangedFilesPerMaintainer(String[] changedFiles) {
        Map<Maintainer, List<String>> maintainersChangedFiles = new HashMap<>();
        for (Rule rule : allGlobalRules) {
            List<Maintainer> maintainers = Arrays.asList(rule.getMaintainers().orElse(new Maintainer[0]));
            maintainers.forEach(m -> maintainersChangedFiles.putIfAbsent(m, new ArrayList<String>()));

            List<String> files = rule.getMatchingFiles(changedFiles);
            if (!files.isEmpty()) {
                maintainers.forEach(m -> maintainersChangedFiles.get(m).addAll(files));
            }

            maintainersChangedFiles.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }

        return maintainersChangedFiles;
    }

    private static boolean isServerBuild() {
        // For the moment, let's assume that we're running a CI server build if we're not on Windows.
        return !Utils.isWindows();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean tryInitTrackBranch(File workingDir) {
        try {
            if (trackBranchName != null) {
                changedFiles = Utils.exec(GIT_DIFF + trackBranchName, workingDir);
                currentBranchName = getBranchName(workingDir);
            }

            return true;
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    private static String getBranchName(File workingDir) {
        String[] lines = Utils.exec(GIT_BRANCH, workingDir);

        if (lines.length > 0) {
            if (lines[0].contains(",")) {
                String[] tokens = lines[0].split(",");
                if (tokens.length == 2) {
                    return tokens[1].trim();
                }
            } else if (lines[0].startsWith("HEAD ->")) {
                return lines[0].substring(7).trim();
            }
        }

        throw new RuntimeException("Unable to determine the current branch name.");
    }

}
