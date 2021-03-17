/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import com.google.common.collect.Lists;
import io.netty.channel.ChannelFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;
import org.asyncfx.concurrent.Futures;

public class FutureHelper {
    static Future<Void> getWrappedChannelFuture(ChannelFuture channelFuture) {
        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();
        channelFuture.addListener(
            f -> {
                if (f.isSuccess()) {
                    futureCompletionSource.setResult(null);
                } else if (f.isCancelled()) {
                    futureCompletionSource.setCancelled();
                } else {
                    futureCompletionSource.setException(f.cause());
                }
            });

        return futureCompletionSource.getFuture();
    }

    static <TRes> Future<List<TRes>> repeatSequentiallyAsync(Function<Integer, Future<TRes>> asyncFnc, int count) {
        return repeatSequentiallyAsync(asyncFnc, count, new CancellationSource());
    }

    static <TRes> Future<List<TRes>> repeatSequentiallyAsync(
            Function<Integer, Future<TRes>> asyncFnc, int count, CancellationSource cs) {
        Future<List<TRes>> f;

        if (count == 0) {
            f = Futures.successful(new ArrayList<>());
        } else {
            f = asyncFnc.apply(0).thenApply((Function<TRes, List<TRes>>)Lists::newArrayList);

            for (int i = 1; i < count; i++) {
                f =
                    f.thenApplyAsync(
                        list ->
                            asyncFnc.apply(list.size())
                                .thenApply(
                                    tRes -> {
                                        list.add(tRes);
                                        return list;
                                    }));
            }
        }

        cs.addListener(f::cancel);
        return f;
    }
}
