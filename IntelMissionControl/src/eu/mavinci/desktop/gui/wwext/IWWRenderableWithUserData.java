/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

public interface IWWRenderableWithUserData {
    public Object getUserData();

    public void setUserData(Object o);

    public boolean isDraggable();

    public void setDraggable(boolean isDraggable);

    public boolean isSelectable();

    public void setSelectable(boolean isSelectable);

    public boolean isPopupTriggering();

    public void setPopupTriggering(boolean isPopupTriggering);

    public boolean hasTooltip();

    public void setHasTooltip(boolean hasTooltip);

    public boolean isSelectableWhileAddNewPoints();

    public void setSelectableWhileAddNewPoints(boolean isSelectableWhileAddNewPoints);

    public boolean isHighlightableEvenWithoutSelectability();

    public void setHighlightableEvenWithoutSelectability(boolean isHighlightable);
}
