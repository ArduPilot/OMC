package org.droidplanner.services.android.impl.core.gcs.location;

import android.content.Context;
import android.os.Handler;
import org.droidplanner.services.android.impl.core.gcs.location.Location.LocationFinder;

/**
 * Feeds Location Data from Android's FusedLocation LocationProvider
 */
public class FusedLocation implements LocationFinder {
    public FusedLocation(Context context, Handler handler, int priorityHighAccuracy, long l, long l1, int updateMinimalDisplacement) {
    }

    public FusedLocation(Context context, Handler handler) {
    }

    @Override
    public void enableLocationUpdates(String tag, Location.LocationReceiver receiver) {

    }

    @Override
    public void disableLocationUpdates(String tag) {

    }
}
