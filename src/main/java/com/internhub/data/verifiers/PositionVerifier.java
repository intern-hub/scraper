package com.internhub.data.verifiers;

import com.internhub.data.models.Season;
import org.jsoup.select.Elements;

public class PositionVerifier {

    public boolean isPositionValid(String applicationLink, Elements applicationPage) {
        return false;
    }

    public String getPositionTitle(String applicationLink, Elements applicationPage) {
        return null;
    }

    public Season getPositionSeason(String applicationLink, Elements applicationPage) { return Season.SUMMER; }

    public int getPositionYear(String applicationLink, Elements applicationPage) { return 2019; }

    public String getPositionDegree(String applicationLink, Elements applicationPage) { return null; }

    public String getPositionLocation(String applicationLink, Elements applicationPage) { return null; }
}
