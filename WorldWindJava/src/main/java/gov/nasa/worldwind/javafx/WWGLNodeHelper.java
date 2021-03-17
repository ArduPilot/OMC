/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwind.javafx;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.util.Utils;
import javafx.scene.Node;

class WWGLNodeHelper extends NodeHelper {

    private static final WWGLNodeHelper instance = new WWGLNodeHelper();
    private static WWGLNodeAccessor accessor;

    static {
        Utils.forceInit(WWGLNode.class);
    }

    private WWGLNodeHelper() {
    }

    private static WWGLNodeHelper getInstance() {
        return instance;
    }

    static void initHelper(WWGLNode var0) {
        setHelper(var0, getInstance());
    }

    public static void setAccessor(WWGLNodeAccessor accessor) {
        if (WWGLNodeHelper.accessor != null) {
            throw new IllegalStateException();
        } else {
            WWGLNodeHelper.accessor = accessor;
        }
    }

    protected NGNode createPeerImpl(Node var1) {
        return accessor.doCreatePeer(var1);
    }

    protected void updatePeerImpl(Node var1) {
        super.updatePeerImpl(var1);
        accessor.doUpdatePeer(var1);
    }

    protected void markDirtyImpl(Node var1, DirtyBits var2) {
        accessor.doMarkDirty(var1, var2);
        super.markDirtyImpl(var1, var2);
    }

    protected BaseBounds computeGeomBoundsImpl(Node var1, BaseBounds var2, BaseTransform var3) {
        return accessor.doComputeGeomBounds(var1, var2, var3);
    }

    protected boolean computeContainsImpl(Node var1, double var2, double var4) {
        return accessor.doComputeContains(var1, var2, var4);
    }

    public interface WWGLNodeAccessor {
        NGNode doCreatePeer(Node var1);

        void doUpdatePeer(Node var1);

        BaseBounds doComputeGeomBounds(Node var1, BaseBounds var2, BaseTransform var3);

        boolean doComputeContains(Node var1, double var2, double var4);

        void doMarkDirty(Node var1, DirtyBits var2);
    }

}
