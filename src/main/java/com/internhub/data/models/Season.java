package com.internhub.data.models;

import java.util.HashMap;
import java.util.Map;

public enum Season {
    SPRING,
    SUMMER,
    FALL,
    WINTER;

    private static final Map<String, Season> SEASON_LOWER_NAMES;

    static {
        SEASON_LOWER_NAMES = new HashMap<>();
        for (Season season : Season.values()) {
            SEASON_LOWER_NAMES.put(season.name().toLowerCase(), season);
        }
    }

    public static Season getSeasonFromLowercaseName(String query) {
        return SEASON_LOWER_NAMES.get(query);
    }
}
