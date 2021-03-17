/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.MVector;

import java.util.ArrayList;
import java.util.List;

public class AirplaneListenerFileTransferDelegator implements IAirplaneListenerFileTransfer {
    private List<IAirplaneListenerFileTransfer> transferListeners = new ArrayList<>();

    public void addTransferListener(IAirplaneListenerFileTransfer transferListener) {
        transferListeners.add(transferListener);
    }

    @Override
    public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {
        transferListeners.forEach(l -> l.recv_dirListing(parentPath, files, sizes));
    }

    @Override
    public void recv_fileSendingProgress(String path, Integer progress) {
        transferListeners.forEach(l -> l.recv_fileSendingProgress(path, progress));
    }

    @Override
    public void recv_fileReceivingProgress(String path, Integer progress) {
        transferListeners.forEach(l -> l.recv_fileReceivingProgress(path, progress));
    }

    @Override
    public void recv_fileSendingSucceeded(String path) {
        transferListeners.forEach(l -> l.recv_fileSendingSucceeded(path));
    }

    @Override
    public void recv_fileReceivingSucceeded(String path) {
        transferListeners.forEach(l -> l.recv_fileReceivingSucceeded(path));
    }

    @Override
    public void recv_fileSendingCancelled(String path) {
        transferListeners.forEach(l -> l.recv_fileSendingCancelled(path));
    }

    @Override
    public void recv_fileReceivingCancelled(String path) {
        transferListeners.forEach(l -> l.recv_fileReceivingCancelled(path));
    }
}
