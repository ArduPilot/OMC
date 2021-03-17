/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public interface IMuteable {

    class MuteScope implements AutoCloseable {
        private final IMuteable muteable;

        MuteScope(IMuteable muteable) {
            this.muteable = muteable;
            muteable.setMute(true);
        }

        @Override
        public void close() {
            muteable.setMute(false);
        }
    }

    default MuteScope openMuteScope() {
        return new MuteScope(this);
    }

    // on setting mute=false a kind of allChanged event should be fired
    void setMute(boolean mute);

    void setSilentUnmute();

    boolean isMute();
}
