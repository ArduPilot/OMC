package org.droidplanner.services.android.impl.utils.video;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Surface;
import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.android.client.utils.connection.IpConnectionListener;
import com.o3dr.services.android.lib.model.ICommandListener;
import org.droidplanner.services.android.impl.communication.model.DataLink;

import java.nio.ByteBuffer;

/**
 * Handles the video stream from artoo.
 */
public class VideoManager implements IpConnectionListener {

    public VideoManager(Context context, Handler handler, DataLink.DataLinkProvider<MAVLinkMessage> mavClient) {
    }

    @Override
    public void onIpConnected() {

    }

    @Override
    public void onIpDisconnected() {

    }

    @Override
    public void onPacketReceived(ByteBuffer packetBuffer) {

    }

    public void startVideoStream(Bundle videoProps, String appId, String newVideoTag, Surface videoSurface, ICommandListener listener) {
    }

    public void stopVideoStream(String appId, String currentVideoTag, ICommandListener listener) {
    }

    public void startVideoStreamForObserver(String appId, String newVideoTag, ICommandListener listener) {
    }

    public void stopVideoStreamForObserver(String appId, String currentVideoTag, ICommandListener listener) {
    }

    public void tryStoppingVideoStream(String appId) {
    }
}
