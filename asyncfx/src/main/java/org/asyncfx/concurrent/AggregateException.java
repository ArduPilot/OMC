/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Aggregates exceptions from multiple sequential async stack traces. */
public class AggregateException extends RuntimeException {

    private final Throwable[] throwables;

    AggregateException(Throwable throwable) {
        super(throwable);
        this.throwables = new Throwable[] {throwable};
    }

    AggregateException(Throwable throwable, Throwable[] throwables) {
        super(throwable);

        Throwable[] array = new Throwable[throwables.length + 1];
        array[0] = throwable;
        System.arraycopy(throwables, 0, array, 1, throwables.length);

        if (array.length > 1) {
            List<Throwable> list = new ArrayList<>(array.length);

            for (Throwable throwableInArray : array) {
                boolean found = false;
                for (Throwable throwableInList : list) {
                    if (throwableInList == throwableInArray) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    list.add(throwableInArray);
                }
            }

            this.throwables = list.toArray(new Throwable[0]);
        } else {
            this.throwables = array;
        }
    }

    public Throwable getFirstCause() {
        return throwables[throwables.length - 1];
    }

    public Throwable[] getThrowables() {
        return throwables;
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream s) {
        printStackTrace(new WrappedPrintStream(s));
    }

    public void printStackTrace(PrintWriter s) {
        printStackTrace(new WrappedPrintWriter(s));
    }

    private void printStackTrace(PrintStreamOrWriter s) {
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>());
        dejaVu.add(this);

        synchronized (s.lock()) {
            s.println(this);

            boolean first = true;
            for (Throwable throwable : throwables) {
                if (!first) {
                    s.println("Preceded in async execution by: " + throwable);
                }

                first = false;

                StackTraceElement[] trace = throwable.getStackTrace();
                for (StackTraceElement traceElement : trace) {
                    s.println("\tat " + traceElement);
                }

                for (Throwable se : throwable.getSuppressed()) {
                    printEnclosedStackTrace(se, s, trace, "Suppressed: ", "\t", dejaVu);
                }

                Throwable ourCause = throwable.getCause();
                if (ourCause != null) {
                    printEnclosedStackTrace(ourCause, s, trace, "Caused by: ", "", dejaVu);
                }
            }
        }
    }

    private static void printEnclosedStackTrace(
            Throwable throwable,
            PrintStreamOrWriter s,
            StackTraceElement[] enclosingTrace,
            String caption,
            String prefix,
            Set<Throwable> dejaVu) {
        if (dejaVu.contains(throwable)) {
            s.println("\t[CIRCULAR REFERENCE:" + throwable + "]");
        } else {
            dejaVu.add(throwable);
            StackTraceElement[] trace = throwable.getStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n])) {
                m--;
                n--;
            }

            int framesInCommon = trace.length - 1 - m;

            s.println(prefix + caption + throwable);
            for (int i = 0; i <= m; i++) s.println(prefix + "\tat " + trace[i]);
            if (framesInCommon != 0) {
                s.println(prefix + "\t... " + framesInCommon + " more");
            }

            for (Throwable se : throwable.getSuppressed()) {
                printEnclosedStackTrace(se, s, trace, "Suppressed: ", prefix + "\t", dejaVu);
            }

            Throwable ourCause = throwable.getCause();
            if (ourCause != null) {
                printEnclosedStackTrace(ourCause, s, trace, "Caused by: ", prefix, dejaVu);
            }
        }
    }

    private abstract static class PrintStreamOrWriter {
        abstract Object lock();

        abstract void println(Object o);
    }

    private static class WrappedPrintStream extends PrintStreamOrWriter {
        private final PrintStream printStream;

        WrappedPrintStream(PrintStream printStream) {
            this.printStream = printStream;
        }

        Object lock() {
            return printStream;
        }

        void println(Object o) {
            printStream.println(o);
        }
    }

    private static class WrappedPrintWriter extends PrintStreamOrWriter {
        private final PrintWriter printWriter;

        WrappedPrintWriter(PrintWriter printWriter) {
            this.printWriter = printWriter;
        }

        Object lock() {
            return printWriter;
        }

        void println(Object o) {
            printWriter.println(o);
        }
    }

}
