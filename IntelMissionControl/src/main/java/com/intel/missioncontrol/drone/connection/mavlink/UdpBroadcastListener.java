/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

/** UDP broadcast listener. Used to bind / listen on a specified port, and initializes a udp channel for sending. */
public class UdpBroadcastListener {
    private EventLoopGroup workerGroup;
    private MavlinkHandler mavlinkHandler;

    public MavlinkHandler getHandler() {
        return mavlinkHandler;
    }

    public Future<Void> bindAsync(int port, CancellationSource cancellationSource) {
        workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
            .channel(NioDatagramChannel.class)
            .handler(
                new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel dc) {
                        dc.config().setBroadcast(true);
                        mavlinkHandler = new MavlinkHandler(dc);
                        dc.pipeline().addLast(mavlinkHandler);
                    }
                });

        ChannelFuture channelFuture = b.bind(port);

        cancellationSource.addListener(
            mayInterruptIfRunning -> {
                if (cancellationSource.isCancellationRequested()) {
                    workerGroup.shutdownGracefully();
                    channelFuture.cancel(false);
                }
            });

        return FutureHelper.getWrappedChannelFuture(channelFuture);
    }
}
