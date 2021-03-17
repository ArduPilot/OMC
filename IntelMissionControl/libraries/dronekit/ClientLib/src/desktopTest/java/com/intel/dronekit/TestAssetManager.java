package com.intel.dronekit;

import android.content.res.AssetManager;
import androidCompat.content.DesktopAssetManager;
import com.o3dr.android.client.Drone;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestAssetManager {


    static void dump(URL url) {
        int i = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String res;
            while ((res = br.readLine()) != null && i++ < 5) {
                System.out.println(res);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testAssetManager() throws IOException {
        AssetManager assetManager = new DesktopAssetManager(Drone.class);

        URL resource = Drone.class.getResource("/CameraInfo");
        //dump(resource);
        assertNotNull(resource);


        String[] vehicleProfiles = assetManager.list("CameraInfo");
        assertThat(vehicleProfiles.length, greaterThan(3));

        URL resource1 = Drone.class.getResource("/Parameters/ParameterMetaData.xml");
        assertNotNull(resource1);

        try (InputStream is = assetManager.open("Parameters/ParameterMetaData.xml")) {
            assertNotNull(is);

            int available = is.available();
            // throws IO exception
        } catch (IOException e) {
            fail();
        }


    }
}
