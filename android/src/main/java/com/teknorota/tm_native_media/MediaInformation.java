package com.teknorota.tm_native_media;

import java.util.HashMap;

public class MediaInformation {
    public int width = 0;
    public int height = 0;
    public int durationMs = 0;
    public int orientation = 0;
    public String mimeType = "";

    HashMap<String, Object> toHashMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("width", width);
        map.put("height", height);
        map.put("durationMs", durationMs);
        map.put("orientation", orientation);
        map.put("mimeType", mimeType);

        return map;
    }
}
