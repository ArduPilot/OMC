package com.intel.dronekit;

import android.util.Xml;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;

import static org.junit.Assert.assertNotNull;

public class XmlPullParserTest {

    @Test
    public void testNewPullParser() {
        XmlPullParser parser = Xml.newPullParser();

        assertNotNull(parser);
    }
}
