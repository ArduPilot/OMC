package org.droidplanner.services.android.impl.communication.connection.usb;

import android.content.Context;
import android.os.Bundle;
import com.intel.dronekit.NotImplementedDesktop;
import org.droidplanner.services.android.impl.communication.connection.AndroidMavLinkConnection;

import java.io.IOException;

public class UsbConnection extends AndroidMavLinkConnection {

    public UsbConnection(Context parentContext, int baudRate) {
        super(parentContext);
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
