package com.internhub.data.verifiers;

import com.internhub.data.models.Season;
import org.jsoup.select.Elements;

import java.util.*;

public class PositionVerifier {

    public boolean isPositionValid(String applicationLink, Elements applicationPage)  {

        // TODO: Try to find more effective heuristics to add

        int score = 0;
        int threshold = 2;
        
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

        List<String> internWords= Arrays.asList("intern", "internship");        
        if (score >= 1) {
            String pageText = applicationPage.text();
            for(String keyStr: internWords) {
                if (pageText.trim().toLowerCase().contains(keyStr)) {
                    score += 1;
                }
            }
        }

        if (score >= threshold) {
            return true;
        }
        return false;
    }

    public String getPositionTitle(String applicationLink, Elements applicationPage) {

        // TODO add more heuristics

        if (this.isPositionValid(applicationLink, applicationPage)) {
            List<String> titleList = applicationPage.select("h1").eachText();
            if (titleList.size() > 0) {
                return titleList.get(0);
            }
        }
        
        return null;
    }

    public Season getPositionSeason(String applicationLink, Elements applicationPage) {
        
        if (this.isPositionValid(applicationLink, applicationPage)) {
            String pageText = applicationPage.text().trim().toLowerCase();

            if (pageText.contains("summer")) {
                return Season.SUMMER;
            }
            else if (pageText.contains("fall")) {
                return Season.FALL;
            }
            else if (pageText.contains("winter")) {
                return Season.WINTER;
            }
            else if (pageText.contains("spring")) {
                return Season.SPRING;
            }
        }
        
        return null;
    }

    public int getPositionYear(String applicationLink, Elements applicationPage) { 
    
        if (this.isPositionValid(applicationLink, applicationPage)) {
            String pageText = applicationPage.text().trim().toLowerCase();
            List<String> yearList = Arrays.asList("2020","2019"); 
            for(String currYear: yearList) {
                if (pageText.contains(currYear)) {
                    return Integer.parseInt(currYear);
                }
            } 
        }    
        return -1; 
    }

    public String getPositionDegree(String applicationLink, Elements applicationPage) {

        if (this.isPositionValid(applicationLink, applicationPage)) {
            String pageText = applicationPage.text().trim().toLowerCase();

            if (pageText.contains("bachelor’s") || pageText.contains("bs")) {
                return "bachelor's";
            }
            else if (pageText.contains("master's") || pageText.contains("ms")) {
                return "master's";
            }
            else if (pageText.contains("phd")) {
                return "phd"; 
            }
        } 
        
        return null;
    }

    public String getPositionLocation(String applicationLink, Elements applicationPage) { 

        if (this.isPositionValid(applicationLink, applicationPage)) {
            String pageText = applicationPage.text().trim().toLowerCase();
            String[] words = pageText.split(" ");
            for (int i = 0; i < words.length; i++) {
                if (words[i].contains("location")) {
                    if ((i+1) < words.length) {
                        return words[i+1].replaceAll("[^a-zA-Z ]", "");
                    } 
                }
            }
        }

        return null; 
    }
}
