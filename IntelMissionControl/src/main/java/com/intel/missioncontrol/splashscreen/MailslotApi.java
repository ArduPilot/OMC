/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.splashscreen;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

interface MailslotApi extends StdCallLibrary, WinUser, WinNT {
    MailslotApi INSTANCE =
        (MailslotApi)Native.loadLibrary("kernel32", MailslotApi.class, W32APIOptions.DEFAULT_OPTIONS);

    HANDLE CreateMailslot(
            String lpName, DWORD nMaxMessageSize, DWORD lReadTimeout, SECURITY_ATTRIBUTES lpSecurityAttributes);

    boolean GetMailslotInfo(
            HANDLE hMailslot,
            DWORDByReference lpMaxMessageSize,
            DWORDByReference lpNextSize,
            DWORDByReference lpMessageCount,
            DWORDByReference lpReadTimeout);
}
