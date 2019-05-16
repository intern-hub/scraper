package com.internhub.data.verifiers;

import com.internhub.data.models.Season;
import org.jsoup.select.Elements;

import java.util.*;

public class PositionVerifier {

    public boolean isPositionValid(String applicationLink, Elements applicationPage) {

        // TODO: Differentiate between general applications and internship applications - (eg - appearance of the word internship)
        // TODO: Try to find more effective heuristics to add

        int score = 0;
        int threshold = 1;
        
        List<String> keyWords= Arrays.asList("apply", "start", "application", "submit");

        Elements links = applicationPage.select("a[href]");
        List<String> linkText = links.eachText();
        
        for(String keyStr: keyWords) {
            for(String str: linkText) {
                if(str.trim().toLowerCase().contains(keyStr)) {
                    score += 1;
                }                    
            }
        }

        if (score >= threshold) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getPositionTitle(String applicationLink, Elements applicationPage) {

        // TODO add more heuristics

        if (this.isPositionValid(applicationLink, applicationPage)) {
            List<String> title_list = applicationPage.select("h1").eachText();
            if (title_list.size() > 0) {
                return title_list.get(0);
            }
            else {
                return null;
            }
            
        }
    }

    public Season getPositionSeason(String applicationLink, Elements applicationPage) { return Season.SUMMER; }

    public int getPositionYear(String applicationLink, Elements applicationPage) { return 2019; }

    public String getPositionDegree(String applicationLink, Elements applicationPage) { return null; }

    public String getPositionLocation(String applicationLink, Elements applicationPage) { return null; }
}
