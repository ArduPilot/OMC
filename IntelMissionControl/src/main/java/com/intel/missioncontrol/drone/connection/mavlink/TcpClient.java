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
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.time.Duration;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

/** TCP client. Used to connect to a specified ip/port, and initializes a tcp channel for sending / receiving. */
public class TcpClient {

    private EventLoopGroup workerGroup;
    private MavlinkHandler mavlinkHandler;

    public MavlinkHandler getHandler() {
        return mavlinkHandler;
    }

    public Future<Void> connectAsync(
            InetSocketAddress address, Duration connectTimeout, CancellationSource cancellationSource) {
        // TODO re-use pool
        workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
            .channel(NioSocketChannel.class)
            .handler(
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.config().setConnectTimeoutMillis((int)connectTimeout.toMillis());
                        mavlinkHandler = new MavlinkHandler(socketChannel);
                        socketChannel.pipeline().addLast(mavlinkHandler);
                    }
                });

        ChannelFuture channelFuture = b.connect(address);

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
