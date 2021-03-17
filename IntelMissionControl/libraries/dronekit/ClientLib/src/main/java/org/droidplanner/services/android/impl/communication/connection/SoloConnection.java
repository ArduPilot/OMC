package org.droidplanner.services.android.impl.communication.connection;

import android.content.Context;
import android.os.Bundle;
import com.intel.dronekit.NotImplementedDesktop;

import java.io.IOException;

/**
 * Abstract the connection to a Solo vehicle.
 * Created by Fredia Huya-Kouadio on 12/17/15.
 */
public class SoloConnection extends AndroidMavLinkConnection  {

    public SoloConnection(Context applicationContext, String soloLinkId, String password) {
        super(applicationContext);

        throw new NotImplementedDesktop();
    }

    @Override
    protected void openConnection(Bundle connectionExtras) throws IOException {

    }

    @Override
    protected int readDataBlock(byte[] buffer) throws IOException {
        return 0;
    }

    @Override
    protected void sendBuffer(byte[] buffer) throws IOException {

    }

    @Override
    protected void closeConnection() throws IOException {

    }

    @Override
    protected void loadPreferences() {

    }

    @Override
    public int getConnectionType() {
        return 0;
    }
}
