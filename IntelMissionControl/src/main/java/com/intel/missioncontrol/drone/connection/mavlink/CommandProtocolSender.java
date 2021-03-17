/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import com.intel.missioncontrol.drone.SpecialDuration;
import io.dronefleet.mavlink.annotations.MavlinkMessageInfo;
import io.dronefleet.mavlink.common.AutopilotVersion;
import io.dronefleet.mavlink.common.CommandAck;
import io.dronefleet.mavlink.common.CommandLong;
import io.dronefleet.mavlink.common.FlightInformation;
import io.dronefleet.mavlink.common.MavCmd;
import io.dronefleet.mavlink.common.MavResult;
import io.dronefleet.mavlink.common.MessageInterval;
import io.dronefleet.mavlink.common.StorageInformation;
import java.time.Duration;
import java.util.function.Function;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

public class CommandProtocolSender extends PayloadSender {

    private static final int retriesWhenTemporarilyRejected = 40;
    private static final Duration delayWhenTemporarilyRejected = Duration.ofMillis(500);

    public CommandProtocolSender(
            MavlinkEndpoint recipient, MavlinkHandler handler, CancellationSource cancellationSource) {
        super(recipient, handler, cancellationSource);
    }

    public Future<Void> sendTakeOffAsync(float altitude) {
        return sendCommandAsync(
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_NAV_TAKEOFF)
                .param1(-1)
                .param2(0)
                .param3(0)
                .param4(Float.NaN)
                .param5(Float.NaN)
                .param6(Float.NaN)
                .param7(altitude)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build());
    }

    public Future<Void> sendArmDisarmAsync(boolean arm) {
        return sendCommandAsync(
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_COMPONENT_ARM_DISARM)
                .param1(arm ? 1 : 0)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build());
    }

    /** Request FlightInformation and asynchronously return it. No retries in case of errors / timeout. */
    public Future<FlightInformation> requestFlightInformationAsync() {
        CommandLong commandLong =
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_REQUEST_FLIGHT_INFORMATION)
                .param1(1)
                .param2(Float.NaN)
                .param3(Float.NaN)
                .param4(Float.NaN)
                .param5(Float.NaN)
                .param6(Float.NaN)
                .param7(Float.NaN)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build();

        Function<ReceivedPayload<?>, FlightInformation> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                FlightInformation.class,
                receivedFlightInformation -> true,
                commandLong.targetSystem(),
                commandLong.targetComponent());

        int repetitions = 1;

        return sendCommandAndExpectResponseAsync(
            commandLong, receiverFnc, repetitions, PayloadSender.defaultResponseTimeoutPerRepetition);
    }

    /** Request StorageStatus Information and asynchronously return it. No retries in case of errors / timeout. */
    public Future<StorageInformation> requestStorageInformationAsync() {
        CommandLong commandLong =
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_REQUEST_STORAGE_INFORMATION)
                .param1(0) // all
                .param2(1) // Request storage information
                .param3(Float.NaN)
                .param4(Float.NaN)
                .param5(Float.NaN)
                .param6(Float.NaN)
                .param7(Float.NaN)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build();

        Function<ReceivedPayload<?>, StorageInformation> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                StorageInformation.class,
                receivedStorageInformation -> true,
                commandLong.targetSystem(),
                commandLong.targetComponent());

        int repetitions = 1;

        return sendCommandAndExpectResponseAsync(
            commandLong, receiverFnc, repetitions, PayloadSender.defaultResponseTimeoutPerRepetition);
    }

    public Future<Void> startStreamingAsync(Integer streamId) {
        return sendCommandAsync(
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_VIDEO_START_STREAMING)
                .param1(streamId)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build());
    }

    public Future<Void> stopStreamingAsync(Integer streamId) {
        return sendCommandAsync(
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_VIDEO_STOP_STREAMING)
                .param1(streamId)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build());
    }

    Future<Void> sendVideoStreamInformationRequestAsync() {
        return sendCommandAsync(
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_REQUEST_VIDEO_STREAM_INFORMATION)
                .param1(0) // 0: all streams
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build());
    }

    public <TPayload> Future<Duration> getMessageIntervalAsync(Class<TPayload> payloadType) {
        int messageId = payloadType.getAnnotation(MavlinkMessageInfo.class).id();

        CommandLong commandLong =
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_GET_MESSAGE_INTERVAL)
                .param1(messageId)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build();

        Function<ReceivedPayload<?>, MessageInterval> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                MessageInterval.class,
                receivedMsgInterval -> receivedMsgInterval.messageId() == messageId,
                x -> x,
                commandLong.targetSystem(),
                commandLong.targetComponent());

        return sendCommandAndExpectResponseAsync(commandLong, receiverFnc)
            .thenApply(
                messageInterval -> {
                    int intervalUs = messageInterval.intervalUs();
                    if (intervalUs > 0.0f) {
                        return Duration.ofMillis((long)(1e-3 * (double)intervalUs));
                    }

                    return SpecialDuration.UNKNOWN;
                });
    }

    public <TPayload> Future<Void> setMessageIntervalAsync(Class<TPayload> payloadType, Duration duration) {
        int messageId = payloadType.getAnnotation(MavlinkMessageInfo.class).id();

        float interval;
        if (SpecialDuration.isUnknown(duration)) {
            interval = 0; // default rate
        } else if (SpecialDuration.isIndefinite(duration)) {
            interval = -1; // disable
        } else {
            interval = (float)(duration.toSeconds() * 1e6); // microseconds
        }

        return sendCommandAsync(
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL)
                .param1(messageId)
                .param2(interval)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build());
    }

    public Future<Void> sendSetModeAsync(int baseMode, int customMainMode, int customSubMode) {
        return sendCommandAsync(
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_DO_SET_MODE)
                .param1((float)baseMode)
                .param2((float)customMainMode)
                .param3((float)customSubMode)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build());
    }

    /** Set mode waiting for ack, or fail if a status text according to the given filter is received. */
    public Future<Void> sendSetModeAsync(
            int baseMode, int customMainMode, int customSubMode, StatusTextFilter failOnStatusText) {
        return runAndFailOnStatusTextAsync(
            () -> sendSetModeAsync(baseMode, customMainMode, customSubMode), failOnStatusText);
    }

    public Future<Void> sendChangeSpeedAsync(MavlinkSpeedType mavlinkSpeedType, float absoluteSpeedMetersPerSecond) {
        return sendCommandAsync(
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_DO_CHANGE_SPEED)
                .param1(mavlinkSpeedType.getValue())
                .param2(absoluteSpeedMetersPerSecond)
                .param3(-1.0f) // no throttle change
                .param4(0) // absolute
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build());
    }

    public Future<Void> sendMissionStartAsync() {
        return sendCommandAsync(
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_MISSION_START)
                .param1(-1.0f)
                .param2(-1.0f)
                .param3(-1.0f)
                .param4(-1.0f)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build());
    }

    public Future<AutopilotVersion> requestAutopilotCapabilitiesAsync() {
        CommandLong commandLong =
            new CommandLong.Builder()
                .command(MavCmd.MAV_CMD_REQUEST_AUTOPILOT_CAPABILITIES)
                .param1(1) // request autopilot version
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(1)
                .build();

        Function<ReceivedPayload<?>, AutopilotVersion> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                AutopilotVersion.class, apVersion -> true, commandLong.targetSystem(), commandLong.targetComponent());

        return sendCommandAndExpectResponseAsync(commandLong, receiverFnc);
    }

    /**
     * Send a mavlink command and wait for a matching CMD_ACK response, with automatic retries on timeouts or temporary
     * rejection. Only succeeds if the response indicates success.
     */
    private Future<Void> sendCommandAsync(CommandLong commandLong) {
        // Send command, and retry if temporarily rejected.
        RetryScheduler<MavResult> retryScheduler =
            new RetryScheduler<>(retriesWhenTemporarilyRejected, cancellationSource, delayWhenTemporarilyRejected);
        return retryScheduler
            .runWithRetriesAsync(
                repetition -> sendCommandAndExpectAckAsync(commandLong),
                mavResult -> (mavResult == MavResult.MAV_RESULT_TEMPORARILY_REJECTED))
            .thenGet(() -> null);
    }

    /**
     * Send a mavlink command and wait for a matching CMD_ACK response, with automatic retries on timeouts. Returns the
     * response.
     */
    private Future<MavResult> sendCommandAndExpectAckAsync(CommandLong commandLong) {
        Function<CommandAck, Boolean> isApplicableFnc =
            (CommandAck receivedAck) ->
                receivedAck.command().entry() == commandLong.command().entry()
                    && (receivedAck.targetSystem() == systemId || receivedAck.targetSystem() == 0)
                    && (receivedAck.targetComponent() == componentId
                        || receivedAck.targetComponent() == MavlinkEndpoint.AllComponentIds);

        Function<ReceivedPayload<?>, CommandAck> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                CommandAck.class, isApplicableFnc, commandLong.targetSystem(), commandLong.targetComponent());

        return sendCommandAndExpectResponseAsync(commandLong, receiverFnc)
            .thenApply(commandAck -> commandAck.result().entry());
    }

    private <TRes> Future<TRes> sendCommandAndExpectResponseAsync(
            CommandLong commandLong, Function<ReceivedPayload<?>, TRes> receiverFnc) {
        return sendCommandAndExpectResponseAsync(
            commandLong,
            receiverFnc,
            PayloadSender.defaultRepetitions,
            PayloadSender.defaultResponseTimeoutPerRepetition);
    }

    private <TRes> Future<TRes> sendCommandAndExpectResponseAsync(
            CommandLong commandLong,
            Function<ReceivedPayload<?>, TRes> receiverFnc,
            int repetitions,
            Duration responseTimeoutPerRepetition) {
        Function<Integer, CommandLong> payloadForRepetitionFnc =
            repetition ->
                CommandLong.builder()
                    .targetSystem(commandLong.targetSystem())
                    .targetComponent(commandLong.targetComponent())
                    .command(commandLong.command())
                    .confirmation(repetition)
                    .param1(commandLong.param1())
                    .param2(commandLong.param2())
                    .param3(commandLong.param3())
                    .param4(commandLong.param4())
                    .param5(commandLong.param5())
                    .param6(commandLong.param6())
                    .param7(commandLong.param7())
                    .build();

        return sendAndExpectResponseWithRetriesAsync(
            payloadForRepetitionFnc, receiverFnc, repetitions, responseTimeoutPerRepetition);
    }

}
