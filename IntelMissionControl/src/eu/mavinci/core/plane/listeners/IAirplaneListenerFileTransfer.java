/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.MVector;

public interface IAirplaneListenerFileTransfer extends IAirplaneListener {

    /** Receive directory listing. Filesizes are in byte. a size of -1 means this is directory */
    public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes);

    /** progress in promille */
    public void recv_fileSendingProgress(String path, Integer progress);

    public void recv_fileReceivingProgress(String path, Integer progress);

    public void recv_fileSendingSucceeded(String path);

    public void recv_fileReceivingSucceeded(String path);

    public void recv_fileSendingCancelled(String path);

    public void recv_fileReceivingCancelled(String path);
}
