package com.teknorota.tm_native_media;

import java.util.HashMap;

public class MediaInformation {
    public int width = 0;
    public int height = 0;

    HashMap<String, Object> toHashMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("width", width);
        map.put("height", height);

        return map;
    }
}
