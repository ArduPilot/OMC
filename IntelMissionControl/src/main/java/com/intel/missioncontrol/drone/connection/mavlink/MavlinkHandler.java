/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import com.intel.missioncontrol.drone.connection.TcpIpTransportType;
import io.dronefleet.mavlink.MavlinkDialect;
import io.dronefleet.mavlink.annotations.MavlinkMessageInfo;
import io.dronefleet.mavlink.ardupilotmega.ArdupilotmegaDialect;
import io.dronefleet.mavlink.common.CommonDialect;
import io.dronefleet.mavlink.common.MavAutopilot;
import io.dronefleet.mavlink.grayhawk.GrayhawkDialect;
import io.dronefleet.mavlink.protocol.MavlinkPacket;
import io.dronefleet.mavlink.serialization.payload.MavlinkPayloadDeserializer;
import io.dronefleet.mavlink.serialization.payload.MavlinkPayloadSerializer;
import io.dronefleet.mavlink.serialization.payload.reflection.ReflectionPayloadDeserializer;
import io.dronefleet.mavlink.serialization.payload.reflection.ReflectionPayloadSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCounted;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavlinkHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkHandler.class);

    /*
    TODO:
    http://ardupilot.org/dev/docs/mavlink-routing-in-ardupilot.html

    - only send to system_id that was seen before
    - check SYSTEM_TIME if time_boot_ms decreased
     */

    private static MavlinkDialect COMMON_DIALECT = new CommonDialect();
    private Map<Integer, MavlinkDialect> systemDialects = new HashMap<>();
    private final Map<MavAutopilot, MavlinkDialect> dialects = new HashMap<>();
    private MavlinkPayloadDeserializer deserializer = new ReflectionPayloadDeserializer();
    private MavlinkPayloadSerializer serializer = new ReflectionPayloadSerializer();
    private int sequence = 0;
    private List<IPayloadReceivedDelegate> payloadReceivedDelegates;

    private final Channel channel;

    MavlinkHandler(Channel channel) {
        this.channel = channel;
        payloadReceivedDelegates = new CopyOnWriteArrayList<>();

        dialects.put(MavAutopilot.MAV_AUTOPILOT_PX4, new GrayhawkDialect());
        dialects.put(MavAutopilot.MAV_AUTOPILOT_ARDUPILOTMEGA, new ArdupilotmegaDialect());
    }

    public Channel getChannel() {
        return channel;
    }

    /**
     * Add a delegate that gets called whenever a payload packet is received. When the delegate indicates that it
     * applied to the payload by returning true, the returned future succeeds and the delegate stops being called. In
     * case the delegate did not apply within the given duration, the future fails with a TimeoutException.
     */
    void addPayloadReceivedDelegate(IPayloadReceivedDelegate payloadReceivedDelegate) {
        payloadReceivedDelegates.add(payloadReceivedDelegate);
    }

    private static MavlinkPacket extractMavlinkPacket(ByteBuf buf) {
        final int MavlinkV2MinPacketSize = 12;
        final int MavlinkV1MinPacketSize = 8;
        final int MavlinkV2SignatureLength = 13;
        final int MavlinkV2SignatureFlag = 0x1;

        int bytesLeft = buf.readableBytes();
        if (bytesLeft < MavlinkV1MinPacketSize) {
            buf.skipBytes(bytesLeft);
            return null;
        }

        buf.markReaderIndex();
        int magic = buf.readUnsignedByte();
        int payloadLen = buf.readUnsignedByte();
        int incompatibleFlags = buf.readUnsignedByte();
        buf.resetReaderIndex();

        switch (magic) {
        case MavlinkPacket.MAGIC_V2:
            int signatureLen = 0;
            if ((incompatibleFlags & MavlinkV2SignatureFlag) != 0) {
                signatureLen = MavlinkV2SignatureLength;
            }

            if (bytesLeft >= payloadLen + signatureLen + MavlinkV2MinPacketSize) {
                byte[] pktData = new byte[payloadLen + signatureLen + MavlinkV2MinPacketSize];
                buf.readBytes(pktData);
                return MavlinkPacket.fromV2Bytes(pktData);
            }

            break;
        case MavlinkPacket.MAGIC_V1:
            if (bytesLeft >= payloadLen + MavlinkV1MinPacketSize) {
                byte[] pktData = new byte[payloadLen + MavlinkV1MinPacketSize];
                buf.readBytes(pktData);
                return MavlinkPacket.fromV1Bytes(pktData);
            }

            break;
        }

        buf.skipBytes(bytesLeft);
        return null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ReferenceCounted msgHandle;
        ByteBuf content;
        TcpIpTransportType tcpIpTransportType;
        InetSocketAddress sender;
        if (msg instanceof DatagramPacket) {
            DatagramPacket pkt = (DatagramPacket)msg;
            content = pkt.content();
            sender = pkt.sender();
            msgHandle = pkt;
            tcpIpTransportType = TcpIpTransportType.UDP;
        } else if (msg instanceof ByteBuf && channel.remoteAddress() instanceof InetSocketAddress) {
            content = (ByteBuf)msg;
            msgHandle = content;
            sender = (InetSocketAddress)channel.remoteAddress();
            tcpIpTransportType = TcpIpTransportType.TCP;
        } else {
            throw new IllegalArgumentException("channelRead: msg has unknown data type");
        }

        MavlinkPacket mavPkt;
        while ((mavPkt = extractMavlinkPacket(content)) != null) {
            MavlinkDialect dialect = this.systemDialects.getOrDefault(mavPkt.getSystemId(), COMMON_DIALECT);

            if (!dialect.supports(mavPkt.getMessageId())) {
                continue;
            }

            Class<?> messageType = dialect.resolve(mavPkt.getMessageId());
            MavlinkMessageInfo messageInfo = messageType.getAnnotation(MavlinkMessageInfo.class);
            if (!mavPkt.validateCrc(messageInfo.crc())) {
                continue;
            }

            Object payload = deserializer.deserialize(mavPkt.getPayload(), messageType);

            ReceivedPayload receivedPayload =
                new ReceivedPayload<>(
                    payload,
                    new MavlinkEndpoint(tcpIpTransportType, sender, mavPkt.getSystemId(), mavPkt.getComponentId()));

            // LOGGER.debug("mavlink packet received: " + payload);

            for (var payloadReceivedDelegate : payloadReceivedDelegates) {
                if (payloadReceivedDelegate.getResultFuture().isDone()) {
                    payloadReceivedDelegates.remove(payloadReceivedDelegate);
                } else {
                    boolean remove = payloadReceivedDelegate.invoke(receivedPayload);
                    if (remove) {
                        payloadReceivedDelegates.remove(payloadReceivedDelegate);
                    }
                }
            }
        }

        msgHandle.release();
    }

    public void unRegisterSystemDialect(int systemId) {
        systemDialects.remove(systemId);
    }

    public void registerSystemDialect(int systemId, MavlinkDialect mavlinkDialect) {
        systemDialects.put(systemId, mavlinkDialect);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause.getMessage());
        // TODO error event
        ctx.close();
    }

    Future<Void> writePacketAsync(MavlinkPacket sendPkt, MavlinkEndpoint targetEndpoint) {
        ChannelFuture f;

        try {
            ByteBuf buf = Unpooled.wrappedBuffer(sendPkt.getRawBytes());

            if (targetEndpoint.getTcpIpTransportType().equals(TcpIpTransportType.UDP)) {
                f = getChannel().writeAndFlush(new DatagramPacket(buf, targetEndpoint.getAddress()));
            } else {
                if (!targetEndpoint.getAddress().equals(channel.remoteAddress())) {
                    throw new IllegalArgumentException("Target endpoint mismatch");
                }

                f = getChannel().writeAndFlush(buf);
            }

            return FutureHelper.getWrappedChannelFuture(f);
        } catch (Exception e) {
            return Futures.failed(e);
        }
    }
}
