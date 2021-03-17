package timber.log;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Mock Timber API with {@link java.util.logging}, configure via {@link #logger()} */
public class Timber {
    static DebugTree debugTree = new DebugTree(Logger.getAnonymousLogger());

    public static Logger logger() {
        return debugTree.LOG;
    }

    public static void e(String fmt, Object... objects) {
        debugTree.log(Level.SEVERE, fmt, objects);
    }

    public static void e(Throwable e, String message) {
        debugTree.log(Level.SEVERE, e, message);
    }

    public static void e(Throwable e, String fmt, Object... objects) {
        debugTree.log(Level.SEVERE, e,  fmt, objects);
    }

    public static void i(String message) {
        debugTree.log(Level.INFO, message);
    }

    public static void i(String fmt, Object... objects) {
        debugTree.log(Level.INFO, fmt, objects);
    }

    public static void w(String s) {
        debugTree.log(Level.WARNING, s);
    }

    public static void w(String fmt, Object... objects) {
        debugTree.log(Level.WARNING, fmt, objects);
    }

    public static void w(Throwable t, String fmt, Object... objects) {
        debugTree.log(Level.WARNING, t, fmt, objects);
    }

    public static void d(String fmt) {
        debugTree.log(Level.FINER, fmt);
    }

    public static void d(String fmt, Object... objects) {
        debugTree.log(Level.FINER, fmt, objects);
    }

    public static void v(String fmt) {
        debugTree.log(Level.FINE, fmt);
    }

    public static void v(String fmt, Object... objects) {
        debugTree.log(Level.FINE, fmt, objects);
    }

    static final class FormatStringSupplier implements Supplier<String> {
        final String fmt;
        final Object[] objs;

        FormatStringSupplier(String fmt, Object... objs) {
            this.fmt = fmt;
            this.objs = objs;
        }

        @Override
        public String get() {
            return String.format(fmt, objs);
        }
    }

    public static final class DebugTree {
        final Logger LOG;

        DebugTree(Logger logger) {
            LOG = logger;
            LOG.setLevel(Level.ALL);
        }

        StackTraceElement getThrower() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            int index = stackTrace.length >= 4 ? 4 : stackTrace.length;
            return stackTrace[index];
        }

        protected void log(Level level, Throwable t, String message) {
            StackTraceElement e = getThrower();
            LOG.logp(level, e.getClassName(), e.getMethodName(), message, t);
        }

        protected void log(Level level, String message) {
            StackTraceElement e = getThrower();
            LOG.logp(level, e.getClassName(), e.getMethodName(), message);
        }

        protected void log(Level level, String fmt, Object... objs) {
            StackTraceElement e = getThrower();
            LOG.logp(level, e.getClassName(), e.getMethodName(), new FormatStringSupplier(fmt, objs));
        }

        protected void log(Level level, Throwable t, String fmt, Object... objs) {
            StackTraceElement e = getThrower();
            LOG.logp(level, e.getClassName(), e.getMethodName(), new FormatStringSupplier(fmt, objs));
        }

    }
}
