/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class InterProcessHandler implements IInterProcessHandler {

    private static final short PORT = 10576;

    private final AsynchronousServerSocketChannel serverSocketChannel;
    private Consumer<String> messageHandler;

    public InterProcessHandler() {
        CompletionHandler<AsynchronousSocketChannel, Object> completionHandler =
            new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
                    if (serverSocketChannel.isOpen()) {
                        serverSocketChannel.accept(null, this);
                    }

                    if (clientChannel.isOpen()) {
                        try {
                            ByteBuffer buffer = ByteBuffer.allocate(256);
                            int readBytes = clientChannel.read(buffer).get();

                            if (messageHandler != null) {
                                if (readBytes > 0) {
                                    byte[] buf = new byte[readBytes];
                                    buffer.rewind();
                                    buffer.get(buf, 0, readBytes);
                                    String message = new String(buf);
                                    messageHandler.accept(message);
                                } else {
                                    messageHandler.accept("");
                                }
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            // expected
                        }
                    }
                }

                @Override
                public void failed(Throwable e, Object attachment) {}
            };

        AsynchronousServerSocketChannel channel;

        try {
            channel = AsynchronousServerSocketChannel.open();
            channel.bind(new InetSocketAddress("127.0.0.1", PORT));
            channel.accept(null, completionHandler);
        } catch (IOException e) {
            channel = null;
        }

        this.serverSocketChannel = channel;
    }

    @Override
    public void setMessageHandler(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void sendMessage(String message) {
        try (Socket socket = new Socket("127.0.0.1", PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.print(message);
        } catch (IOException e) {
            // expected
        }
    }

    @Override
    public void close() {
        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                // expected
            }
        }
    }

    @Override
    public boolean isAlreadyRunning() {
        return serverSocketChannel == null;
    }

}
