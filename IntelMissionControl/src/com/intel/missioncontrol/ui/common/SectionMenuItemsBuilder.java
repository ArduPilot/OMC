/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common;

import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class which helps to create menu items split into sections. Each section is header (disabled menu item) and
 * its content placed below it. Sections are split with @{link SeparatorMenuItem}
 */
@Deprecated
public class SectionMenuItemsBuilder {

    private Map<Object, Section> sections = new LinkedHashMap<>();

    public Section addSection(Object identifier, MenuItem titleItem) {
        Section section = new Section(titleItem);
        sections.put(identifier, section);
        return section;
    }

    public Section getSection(Object identifier) {
        return sections.get(identifier);
    }

    public List<MenuItem> buildFlatItems() {
        List<MenuItem> result = new ArrayList<>();
        Iterator<Section> sections = this.sections.values().iterator();
        result.addAll(sections.next().toList());
        while (sections.hasNext()) {
            if (sections.hasNext()) {
                result.add(new SeparatorMenuItem());
                result.addAll(sections.next().toList());
            }
        }

        return result;
    }

    public static class Section {
        private List<MenuItem> subItems = new ArrayList<>();
        private MenuItem headItem;

        public Section(MenuItem headItem) {
            this.headItem = headItem;
        }

        public void addSubItems(List<MenuItem> subItems) {
            this.subItems.addAll(subItems);
        }

        public List<MenuItem> toList() {
            List<MenuItem> unitedList = new ArrayList<>();
            unitedList.add(headItem);
            unitedList.addAll(subItems);
            return unitedList;
        }
    }
}
