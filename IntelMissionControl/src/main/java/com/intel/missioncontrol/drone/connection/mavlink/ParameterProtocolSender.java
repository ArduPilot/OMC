/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.ParamAck;
import io.dronefleet.mavlink.common.ParamExtAck;
import io.dronefleet.mavlink.common.ParamExtRequestRead;
import io.dronefleet.mavlink.common.ParamExtSet;
import io.dronefleet.mavlink.common.ParamExtValue;
import io.dronefleet.mavlink.common.ParamRequestRead;
import io.dronefleet.mavlink.common.ParamSet;
import io.dronefleet.mavlink.common.ParamValue;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

public class ParameterProtocolSender extends PayloadSender {

    public ParameterProtocolSender(
            MavlinkEndpoint recipient, MavlinkHandler handler, CancellationSource cancellationSource) {
        super(recipient, handler, cancellationSource);
    }

    public Future<Parameter> requestParamByIdAsync(String paramId) {
        return requestParamImplAsync(paramId, -1);
    }

    public Future<Parameter> requestParamByIndexAsync(int paramIndex) {
        return requestParamImplAsync(null, paramIndex);
    }

    /** Request multiple params by id. Fails if any of the requests fails. */
    public Future<List<Parameter>> requestMultipleParamsByIdAsync(List<String> paramIds) {
        return requestMultipleItemsSimultaneouslyAsync(paramIds.size(), i -> requestParamByIdAsync(paramIds.get(i)));
    }

    /** Request all params by index. */
    public Future<List<Parameter>> requestParamsListAsync() {
        // Using mavlink PARAM_REQUEST_LIST would be more efficient.

        FutureCompletionSource<List<Parameter>> fcs = new FutureCompletionSource<>(cancellationSource);

        requestParamByIndexAsync(0)
            .whenSucceeded(
                param -> {
                    int paramCount = param.getParamCount();

                    requestMultipleItemsSimultaneouslyAsync(paramCount, this::requestParamByIndexAsync)
                        .whenSucceeded(fcs::setResult)
                        .whenFailed(e -> fcs.setException(e.getCause()));
                })
            .whenFailed(fcs::setException);

        return fcs.getFuture();
    }

    private Future<Parameter> requestParamImplAsync(String paramId, int paramIndex) {
        ParamRequestRead payloadToSend =
            ParamRequestRead.builder()
                .paramIndex(paramIndex)
                .paramId(paramId)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(targetEndpoint.getComponentId())
                .build();

        Function<ReceivedPayload<?>, Parameter> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                ParamValue.class,
                receivedValue ->
                    ((paramIndex > -1) && receivedValue.paramIndex() == paramIndex)
                        || (paramIndex == -1 && paramId.equals(receivedValue.paramId())),
                Parameter::new,
                targetEndpoint.getSystemId(),
                targetEndpoint.getComponentId());

        return sendAndExpectResponseWithRetriesAsync(
            payloadToSend,
            receiverFnc,
            PayloadSender.defaultRepetitions,
            PayloadSender.defaultResponseTimeoutPerRepetition);
    }

    // Set mavlink param with known type
    public Future<Void> setParamAsync(Parameter parameter) {
        ParamSet payloadToSend =
            ParamSet.builder()
                .paramId(parameter.getId())
                .paramType(parameter.getType())
                .paramValue(parameter.getRawFloatValue())
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(targetEndpoint.getComponentId())
                .build();

        Function<ReceivedPayload<?>, ParamValue> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                ParamValue.class,
                receivedValue -> {
                    if (parameter.getId().equals(receivedValue.paramId())
                            && parameter.getRawFloatValue() == receivedValue.paramValue()) {
                        if (parameter.getType() == receivedValue.paramType().entry()) {
                            return true;
                        } else {
                            throw new InvalidParamTypeException(parameter);
                        }
                    }

                    return false;
                },
                targetEndpoint.getSystemId(),
                targetEndpoint.getComponentId());

        return runAndFailOnStatusTextAsync(
                () ->
                    sendAndExpectResponseWithRetriesAsync(
                        payloadToSend,
                        receiverFnc,
                        PayloadSender.defaultRepetitions,
                        PayloadSender.defaultResponseTimeoutPerRepetition),

                // TODO: This filter might be PX4-specific:
                new StatusTextFilter("(\\[(.*)\\] )?unknown param: " + Pattern.quote(parameter.getId())))
            .thenGet(() -> null);
    }

    // Set mavlink param with unknown type. Use setParamAsync if mavlink param type is known.
    public Future<Void> setParamByIdAsync(String paramId, double value) {
        return requestParamByIdAsync(paramId)
            .thenApplyAsync(
                previousParam -> {
                    Parameter newParam = Parameter.create(paramId, value, previousParam.getType());
                    return setParamAsync(newParam);
                });
    }

    // Mavlink 2 extended parameters

    public Future<ExtendedParameter> requestExtParamByIdAsync(String paramId) {
        return requestExtParamImplAsync(paramId, -1);
    }

    public Future<ExtendedParameter> requestExtParamByIndexAsync(int paramIndex) {
        return requestExtParamImplAsync(null, paramIndex);
    }

    private Future<ExtendedParameter> requestExtParamImplAsync(String paramId, int paramIndex) {
        ParamExtRequestRead payloadToSend =
            ParamExtRequestRead.builder()
                .paramIndex(paramIndex)
                .paramId(paramId)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(targetEndpoint.getComponentId())
                .build();

        Function<ReceivedPayload<?>, ExtendedParameter> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                ParamExtValue.class,
                receivedValue ->
                    ((paramIndex > -1) && receivedValue.paramIndex() == paramIndex)
                        || (paramIndex == -1 && paramId.equals(receivedValue.paramId())),
                ExtendedParameter::new,
                targetEndpoint.getSystemId(),
                targetEndpoint.getComponentId());

        return sendAndExpectResponseWithRetriesAsync(
            payloadToSend,
            receiverFnc,
            PayloadSender.defaultRepetitions,
            PayloadSender.defaultResponseTimeoutPerRepetition);
    }

    public Future<Void> setExtParamAsync(ExtendedParameter parameter) {
        ParamExtSet payloadToSend =
            ParamExtSet.builder()
                .paramId(parameter.getId())
                .paramType(parameter.getType())
                .paramValue(parameter.getRawStringValue())
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(targetEndpoint.getComponentId())
                .build();

        Function<ReceivedPayload<?>, ParamExtAck> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                ParamExtAck.class,
                receivedAck -> {
                    if (parameter.getId().equals(receivedAck.paramId())) {
                        if (parameter.getType() == receivedAck.paramType().entry()) {
                            return true;
                        } else {
                            throw new InvalidParamTypeException(parameter);
                        }
                    }

                    return false;
                },
                targetEndpoint.getSystemId(),
                targetEndpoint.getComponentId());

        return sendAndExpectResponseWithRetriesAsync(
                payloadToSend,
                receiverFnc,
                PayloadSender.defaultRepetitions,
                PayloadSender.defaultResponseTimeoutPerRepetition)
            .thenAccept(
                paramExtAck -> {
                    ParamAck paramResult = paramExtAck.paramResult().entry();
                    if (paramResult != ParamAck.PARAM_ACK_ACCEPTED && paramResult != ParamAck.PARAM_ACK_IN_PROGRESS) {
                        throw new ParamAckException(parameter, paramResult);
                    }
                });
    }

    /** Request all extended params by index. */
    public Future<List<ExtendedParameter>> requestExtParamsListAsync() {
        // Using mavlink PARAM_EXT_REQUEST_LIST would be more efficient.

        return requestExtParamByIndexAsync(0)
            .thenApplyAsync(
                param -> {
                    int paramCount = param.getParamCount();

                    return requestMultipleItemsSimultaneouslyAsync(paramCount, this::requestExtParamByIndexAsync);
                });
    }

    public Future<Void> setParamAsync(IMavlinkParameter parameter) {
        if (parameter instanceof Parameter) {
            return setParamAsync((Parameter)parameter);
        } else if (parameter instanceof ExtendedParameter) {
            return setExtParamAsync((ExtendedParameter)parameter);
        } else {
            throw new IllegalArgumentException("Unknown IMavlinkParameter instance");
        }
    }

    // Set multiple mavlink parameters simultaneously (normal or extended).
    public Future<Void> setParamsAsync(List<IMavlinkParameter> parameters) {
        CancellationSource cts = new CancellationSource();
        this.cancellationSource.addListener(cts::cancel);

        // Create sub-sender that can be cancelled independently
        ParameterProtocolSender subSender = new ParameterProtocolSender(targetEndpoint, handler, cts);

        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();

        futureCompletionSource.getFuture().whenDone(f -> cts.cancel());

        AtomicInteger doneCount = new AtomicInteger(0);

        for (IMavlinkParameter p : parameters) {
            subSender
                .setParamAsync(p)
                .whenSucceeded(
                    v -> {
                        int newDoneCount = doneCount.incrementAndGet();
                        if (newDoneCount == parameters.size()) {
                            futureCompletionSource.setResult(null);
                        }
                    })
                .whenFailed(e -> futureCompletionSource.setException(e.getCause()));
        }

        return futureCompletionSource.getFuture();
    }

}
