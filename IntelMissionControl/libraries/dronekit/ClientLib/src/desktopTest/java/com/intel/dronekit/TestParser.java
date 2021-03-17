package com.intel.dronekit;

import android.content.Context;
import android.net.Uri;
import androidCompat.DesktopHelper;
import androidCompat.content.ResourceHelper;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import org.droidplanner.services.android.impl.utils.MissionUtils;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestParser {

    @Test
    public void testLoadMission() throws URISyntaxException {
        URL resource = getClass().getResource("CMAC-circuit.txt");
        assertNotNull(resource);

        Uri uri = new Uri(resource.toURI());
        Context context = DesktopHelper.setup(this.getClass(), new File("."));

        Mission mission = MissionUtils.loadMission(context, uri);

        assertNotNull(mission);

        List<MissionItem> missionItems = mission.getMissionItems();
        assertThat(missionItems.size(), greaterThan(7));
    }
}
