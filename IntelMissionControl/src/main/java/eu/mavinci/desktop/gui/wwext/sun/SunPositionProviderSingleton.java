    package eu.mavinci.desktop.gui.wwext.sun;

import eu.mavinci.desktop.gui.wwext.sunlight.SunPositionProvider;
import gov.nasa.worldwind.geom.LatLon;
import eu.mavinci.desktop.gui.wwext.sunlight.SunPositionProvider;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * TODO do some caching in this class to avoid to much load
 *
 * @author peter
 */
public class SunPositionProviderSingleton implements SunPositionProvider {

    protected static SunPositionProviderSingleton spp;

    // protected LatLon position;
    protected Calendar calendar;

    public SunPositionProviderSingleton() {
        calendar = new GregorianCalendar();
    }

    public synchronized LatLon getPosition(Calendar calendar) {
        return SunCalculatorNOAA.subsolarPoint(calendar);
    }

    public synchronized LatLon getPosition() {
        // SunCalculator.subsolarPoint(calendar);
        // System.out.println("subsolar: "+SunCalculatorNOAA.subsolarPoint(calendar));
        return SunCalculatorNOAA.subsolarPoint(calendar);
    }

    public static SunPositionProviderSingleton getInstance() {
        if (spp == null) {
            spp = new SunPositionProviderSingleton();
        }

        return spp;
    }

}
