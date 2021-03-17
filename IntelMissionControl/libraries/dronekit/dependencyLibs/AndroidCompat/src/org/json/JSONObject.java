package org.json;

import java.util.HashMap;

public class JSONObject {
    HashMap<String, String> map;
    public JSONObject(String string) throws JSONException {
        this();
    }

    public JSONObject() throws JSONException {
        map = new HashMap<>();
    }

    public void put(String uidLabel, String uid) {
        map.put(uidLabel, uid);
    }
}
