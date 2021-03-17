package android.util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class Xml {

    public static org.xmlpull.v1.XmlPullParser newPullParser() {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, true);
            factory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            return factory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
