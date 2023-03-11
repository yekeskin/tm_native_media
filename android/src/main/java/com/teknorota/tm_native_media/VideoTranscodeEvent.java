package com.teknorota.tm_native_media;

import java.util.HashMap;

public class VideoTranscodeEvent {
    String id;
    String inputPath;
    String outputPath;
    int percentage;
    boolean success = false;
    boolean error = false;
    String errorMessage = "";

    HashMap<String, Object> toHashMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("inputPath", inputPath);
        map.put("outputPath", outputPath);
        map.put("percentage", percentage);
        map.put("success", success);
        map.put("error", error);
        map.put("errorMessage", errorMessage);

        return map;
    }
}
