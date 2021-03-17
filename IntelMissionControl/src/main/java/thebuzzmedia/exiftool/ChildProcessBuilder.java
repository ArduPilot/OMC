/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package thebuzzmedia.exiftool;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.win32.BaseTSD.SIZE_T;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.IO_COUNTERS;
import com.sun.jna.platform.win32.WinNT.LARGE_INTEGER;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.io.IOException;

/** Starts a child process that is automatically terminated if the parent process terminates. */
class ChildProcessBuilder {

    private static final int JobObjectExtendedLimitInformation = 9;
    private static final int JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE = 0x00002000;
    private static final int PROCESS_SET_QUOTA = 0x0100;
    private static final int PROCESS_TERMINATE = 0x0001;

    @SuppressWarnings({"WeakerAccess", "unused"})
    @FieldOrder({
        "PerProcessUserTimeLimit",
        "PerJobUserTimeLimit",
        "LimitFlags",
        "MinimumWorkingSetSize",
        "MaximumWorkingSetSize",
        "ActiveProcessLimit",
        "Affinity",
        "PriorityClass",
        "SchedulingClass"
    })
    public static class JOBOBJECT_BASIC_LIMIT_INFORMATION extends Structure implements Structure.ByValue, IKeepAll {
        public LARGE_INTEGER PerProcessUserTimeLimit;
        public LARGE_INTEGER PerJobUserTimeLimit;
        public DWORD LimitFlags;
        public SIZE_T MinimumWorkingSetSize;
        public SIZE_T MaximumWorkingSetSize;
        public DWORD ActiveProcessLimit;
        public ULONG_PTR Affinity;
        public DWORD PriorityClass;
        public DWORD SchedulingClass;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @FieldOrder({
        "BasicLimitInformation",
        "IoInfo",
        "ProcessMemoryLimit",
        "JobMemoryLimit",
        "PeakProcessMemoryUsed",
        "PeakJobMemoryUsed"
    })
    public static class JOBOBJECT_EXTENDED_LIMIT_INFORMATION extends Structure
            implements Structure.ByReference, IKeepAll {
        public JOBOBJECT_BASIC_LIMIT_INFORMATION BasicLimitInformation;
        public IO_COUNTERS IoInfo;
        public SIZE_T ProcessMemoryLimit;
        public SIZE_T JobMemoryLimit;
        public SIZE_T PeakProcessMemoryUsed;
        public SIZE_T PeakJobMemoryUsed;
    }

    private interface Kernel32Ex extends StdCallLibrary, WinUser, WinNT {
        Kernel32Ex INSTANCE = Native.loadLibrary("kernel32", Kernel32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        HANDLE CreateJobObjectA(SECURITY_ATTRIBUTES lpJobAttributes, String lpName);

        boolean SetInformationJobObject(
                HANDLE hJob,
                int JobObjectInformationClass,
                Structure.ByReference lpJobObjectInformation,
                int cbJobObjectInformationLength);

        boolean AssignProcessToJobObject(HANDLE hJob, HANDLE hProcess);
    }

    private static final WinNT.HANDLE job;

    static {
        job = Kernel32Ex.INSTANCE.CreateJobObjectA(null, null);
        if (job == null) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        JOBOBJECT_EXTENDED_LIMIT_INFORMATION info = new JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
        info.BasicLimitInformation.LimitFlags = new DWORD(JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE);
        if (!Kernel32Ex.INSTANCE.SetInformationJobObject(job, JobObjectExtendedLimitInformation, info, info.size())) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
    }

    private final String[] args;

    ChildProcessBuilder(String[] args) {
        this.args = args;
    }

    public Process start() throws IOException {
        Process process = new ProcessBuilder(args).start();
        WinNT.HANDLE hProcess =
            Kernel32.INSTANCE.OpenProcess(PROCESS_SET_QUOTA | PROCESS_TERMINATE, true, (int)process.pid());
        if (hProcess == null) {
            process.destroyForcibly();
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        if (!Kernel32Ex.INSTANCE.AssignProcessToJobObject(job, hProcess)) {
            Kernel32.INSTANCE.CloseHandle(hProcess);
            process.destroyForcibly();
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        Kernel32.INSTANCE.CloseHandle(hProcess);
        return process;
    }

}
