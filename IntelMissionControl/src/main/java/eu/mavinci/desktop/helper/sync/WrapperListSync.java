/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.sync;

import eu.mavinci.desktop.helper.IVectorAccess;

public class WrapperListSync {

    /**
     * This function syncs a given interval of the conteiner "wrappedContainer" with those object given by the iterator
     * in childHandler. If a new node is nessesary, it generates a wrapper object with the child handler, and inserts it
     *
     * <p>start and oldCount are giving the intervall of the childs which should be synchronized
     *
     * @param wrappedContainer
     * @param childHandler
     * @param start start of intervall to be sync
     * @param oldCount lenght of intervall to be sync (before sync)
     * @return lenght of synced intervall after the sync or -1 if nothing has changed
     */
    public static int sync(
            IVectorAccess wrappedContainer, AWrappedListSyncSourceHandler childHandler, int start, int oldCount) {
        // System.out.println();
        // System.out.println(">>start reprocess " + start + " initOldC" + oldCount);
        if (oldCount < 0) {
            throw new IllegalArgumentException("old count was < 0");
        }

        int i = start;
        boolean hasChanged = false;
        while (childHandler.hasNext()) {
            Object nextUserObj = childHandler.next();
            // System.out.println("nextChild:" + nextUserObj);
            // take a look ahead to find nextUserObject, if maybe something was deleted meanwhile
            // System.out.println("searching in intervall:" + i + " to " + (start+oldCount));
            for (int aheadI = i; aheadI < start + oldCount; aheadI++) {
                if (wrappedContainer.getUserObject(aheadI) == nextUserObj) {
                    // System.out.println("found at index:" + aheadI);
                    for (int k = i; k < aheadI; k++) {
                        // delete everything what is too much in between
                        // System.out.println("remove old IDX:" + i);
                        wrappedContainer.removeWrapped(i);
                        hasChanged = true;
                        oldCount--;
                    }
                    // ok, it is found..
                    break;
                }
            }

            if (i < start + oldCount && wrappedContainer.getUserObject(i) == nextUserObj) {
                // perfect, object is where it should be, lets continue
                // System.out.println("match at .. continue" + i);
                i++;
                continue;
            }

            // otherwise insert it as "new" node
            // System.out.println("insert as new at "+ i);
            hasChanged = true;
            wrappedContainer.insertWrapped(childHandler.getWrapperObjectForLastObject(), i);
            oldCount++;
            i++;
        }
        // System.out.println();
        // System.out.println("<<intermediate old count"+oldCount);

        // remove nodes that are left from old stuff..
        for (int k = i; k < start + oldCount; k++) {
            // System.out.println("<<remove remaining old stuff at " + i);
            hasChanged = true;
            wrappedContainer.removeWrapped(i);
        }

        oldCount = i - start;
        // System.out.println("current old count"+oldCount);
        if (hasChanged) {
            return oldCount; // return current size
        } else {
            return -1;
        }
    }
}
