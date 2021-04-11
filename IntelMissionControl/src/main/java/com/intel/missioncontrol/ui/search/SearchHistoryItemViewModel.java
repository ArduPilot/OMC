package com.intel.missioncontrol.ui.search;

import com.intel.missioncontrol.measure.Location;
import gov.nasa.worldwind.geom.Sector;

/*
For storing the last search results like the user's text plus used index from the list
 */
public class SearchHistoryItemViewModel implements IResultViewModel {

    private final String text;
    private final int selectedOption;

    SearchHistoryItemViewModel(String text, int selectedOption) {
        this.text = text;
        this.selectedOption = selectedOption;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public Sector getSector() {
        return null;
    }

    public String getName() {
        return text;
    }

    public String getDetail() {
        return null;
    }

    public Object getSearchResult() {
        return null;
    }

    @Override
    public boolean isLazyLoaded() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SearchHistoryItemViewModel) {
            return text.equals(((SearchHistoryItemViewModel)obj).text);
        }
        return false;
    }

    public int getSelectedOption() {
        return selectedOption;
    }
}
