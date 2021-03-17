package org.droidplanner.services.android.impl.communication.connection;

import android.content.Context;
import android.os.Bundle;

import java.io.IOException;

public class BluetoothConnection extends AndroidMavLinkConnection {


    public BluetoothConnection(Context parentContext, String btAddress) {
        super(parentContext);
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
