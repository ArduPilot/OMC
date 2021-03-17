/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import com.intel.missioncontrol.utils.IVersionProvider;
import eu.mavinci.core.main.OsTypes;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;

public class WinRegistryQuery {

    private static final String REG_QUERY = "query";
    private static final String REG = "reg";
    private static final String REGSTR_TOKEN = "REG_SZ";

    private static final String[] CMD_Query_OpenVPN = {
        REG, REG_QUERY, "HKLM\\SOFTWARE\\OpenVPN", "/v", "\"\""
    }; // ? so oder /ve
    private static final String[] CMD_Query_LocalAppData = {
        REG,
        REG_QUERY,
        "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
        "/v",
        "Local AppData"
    };

    private static final String[] CMD_Query_TeamViewerID_32 = {
        REG, REG_QUERY, "HKEY_CURRENT_USER\\Software\\TeamViewer", "/v", "ClientID"
    };
    private static final String[] CMD_Query_TeamViewerID_64 = {
        REG, REG_QUERY, "HKEY_CURRENT_USER\\Software\\Wow6432Node\\TeamViewer", "/v", "ClientID"
    };

    public static File getLocalAppData() {
        String localappdataproperty = System.getProperty("LOCALAPPDATA");
        if (System.getenv("LOCALAPPDATA") != null) {
            return new File(System.getenv("LOCALAPPDATA"));
        } else if (localappdataproperty != null) {
            return new File(localappdataproperty);
        } else { // fallback for XP and more
            String ret = getValueUniv(CMD_Query_LocalAppData);
            if (ret == null) {
                return null;
            }

            return new File(ret);
        }
    }

    public static String getProgramFolderOpenVPN() throws Exception {
        return getValueUniv(CMD_Query_OpenVPN);
    }

    /**
     * look up teamviewer ID of installed version of teamviewer if avaliable
     *
     * @return
     */
    public static Integer getTeamViewerID() {
        try {
            OsTypes system = DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getSystem();
            if (system.isWindows()) {
                String ret = getValueUniv(CMD_Query_TeamViewerID_32);
                if (ret == null) {
                    ret = getValueUniv(CMD_Query_TeamViewerID_64);
                }

                if (ret != null) {
                    return Integer.parseInt(ret);
                } else {
                    return null;
                }
            } else if (system.isLinux()) {
                try (BufferedReader br = new BufferedReader(new FileReader("/etc/teamviewer/global.conf"))) {
                    for (String line; (line = br.readLine()) != null; ) {
                        if (line.startsWith("[int32] ClientID =")) {
                            return Integer.parseInt(line.substring(18).trim());
                        }
                    }

                    return null;
                }
            } else if (system.isMac()) {
                // TODO FIXME IMPLEMENT ME
                return null;
            } else {
                return null;
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.FINE, "Could not lookup teamviewer ID", e);
            return null;
        }
    }

    private static String getValueUniv(String[] cmd) {
        String ret = getValue(cmd);

        // for making sure that in a 64 bit windows we are using a 64 bit reg query tool, and dont end up with a
        // 32bit-view to the regestry
        // valu
        if (ret == null) {
            cmd[0] = System.getenv("windir") + "\\sysnative\\reg.exe";
            ret = getValue(cmd);
        }
        // for old XP and vista 32bit support
        if (ret == null) {
            cmd[0] = System.getenv("windir") + "\\reg.exe";
            ret = getValue(cmd);
        }

        if (ret == null) {
            cmd[0] = System.getenv("windir") + "\\system32\\reg.exe";
            ret = getValue(cmd);
        }

        return ret;
    }

    private static String getValue(String[] cmd) {
        try {
            Process process = ProcessHelper.exec(cmd);
            StreamReader reader = new StreamReader(process.getInputStream());

            reader.start();
            process.waitFor();
            reader.join();

            String result = reader.getResult();
            int p = result.indexOf(REGSTR_TOKEN);

            if (p == -1) {
                throw new Exception(REGSTR_TOKEN + " not found in " + result);
            }

            return result.substring(p + REGSTR_TOKEN.length()).trim();
        } catch (Exception e) {
            Debug.getLog().log(Level.CONFIG, "could not readout registry cmd: " + cmd, e);
            return null;
        }
    }

    static class StreamReader extends Thread {
        private InputStream is;
        private ArrayList<Byte> list = new ArrayList<Byte>();

        StreamReader(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1) {
                    list.add((byte)c);
                }
            } catch (IOException e) {
                Debug.getLog().log(Level.SEVERE, "could not readout registry reply", e);
            }
        }

        String getResult() throws UnsupportedEncodingException {
            byte[] listB = new byte[list.size()];
            for (int i = 0; i != listB.length; i++) {
                listB[i] = list.get(i);
            }

            // I have given up to make this full utf8 compatible!, and I haven't fount where this encoding
            // type is stored
            return new String(listB, "CP850");
        }
    }

    public static void main(String[] args) throws Exception {
        // System.out.println("prefs:"+System.getProperties().toString());
        System.out.println("TeamViewerClientID: " + getTeamViewerID());
        System.out.println("openVPN: " + getProgramFolderOpenVPN());
    }
}
