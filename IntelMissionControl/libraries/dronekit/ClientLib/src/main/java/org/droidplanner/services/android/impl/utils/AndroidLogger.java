package org.droidplanner.services.android.impl.utils;

import org.droidplanner.services.android.impl.core.model.Logger;
import timber.log.Timber;

/**
 * Android specific implementation for the {org.droidplanner.services.android.core.model.Logger}
 * interface.
 */
public class AndroidLogger implements Logger {
	private static Logger sLogger = new AndroidLogger();

	public static Logger getLogger() {
		return sLogger;
	}

	// Only one instance is allowed.
	private AndroidLogger() {
	}

	@Override
	public void logVerbose(String logTag, String verbose) {
		if (verbose != null) {
			Timber.v(logTag + ":" + verbose);
		}
	}

	@Override
	public void logDebug(String logTag, String debug) {
		if (debug != null) {
			Timber.d(logTag + " : " + debug);
		}
	}

	@Override
	public void logInfo(String logTag, String info) {
		if (info != null) {
			Timber.i(logTag + ":" + info);
		}
	}

	@Override
	public void logWarning(String logTag, String warning) {
		if (warning != null) {
			Timber.w(logTag + " : " + warning);
		}
	}

	@Override
	public void logWarning(String logTag, Exception exception) {
		if (exception != null) {
			Timber.w(exception, logTag);
		}
	}

	@Override
	public void logWarning(String logTag, String warning, Exception exception) {
		if (warning != null && exception != null) {
			Timber.w(logTag + " : " + warning, logTag);
		}
	}

	@Override
	public void logErr(String logTag, String err) {
		if (err != null) {
			Timber.e(logTag + " : " + err);
		}
	}

	@Override
	public void logErr(String logTag, Exception exception) {
		if (exception != null) {
			Timber.e(exception, logTag);
		}
	}

	@Override
	public void logErr(String logTag, String err, Exception exception) {
		if (err != null && exception != null) {
			Timber.w(exception, logTag + " : " + err);
		}
	}
}
