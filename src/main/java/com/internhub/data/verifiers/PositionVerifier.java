package com.internhub.data.verifiers;

import com.internhub.data.models.Season;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.stream.Collectors;

public class PositionVerifier {
    private static final int VALIDITY_THRESHOLD = 2;
    private static List<String> VALID_SUBMISSION_BUTTONS;

    static {
        VALID_SUBMISSION_BUTTONS = Arrays.asList("apply", "start", "application", "submit");
    }

    private Map<String, String> m_savedTitles;

    public PositionVerifier() {
        this.m_savedTitles = new HashMap<>();
    }

    public boolean isPositionValid(String applicationLink, Elements applicationPage)  {
        int score = 0;

        // Find all buttons in the page that have no more than 3 words in their text field
        Elements links = applicationPage.select("a,button,a > span");
        List<String> linkTexts = links.eachText()
                .stream()
                .filter(text -> text.chars().filter(Character::isWhitespace).count() <= 3)
                .collect(Collectors.toList());

        // Page must have an apply button for it to be valid
        foundApplyButton:
        for (String linkText : linkTexts) {
            String shortenedText = linkText.trim().toLowerCase();
            for (String buttonKeyword : VALID_SUBMISSION_BUTTONS) {
                if (shortenedText.contains(buttonKeyword)) {
                    score += 1;
                    break foundApplyButton;
                }
            }
        }

        // Page must have a position title for it to be valid
        if (getPositionTitle(applicationLink, applicationPage) != null) {
            score += 1;
        }

        return score >= VALIDITY_THRESHOLD;
    }

    public String getPositionTitle(String applicationLink, Elements applicationPage) {
        if (m_savedTitles.containsKey(applicationLink)) {
            return m_savedTitles.get(applicationLink);
        }

        // Iterate over each header in the page, starting with the largest header
        for (int hid = 1; hid <= 6; hid++) {
            List<String> titleList = applicationPage.select("h" + hid).eachText();
            for (String title : titleList) {
                int idx = title.toLowerCase().indexOf("intern");
                if (idx >= 0) {
                    m_savedTitles.put(applicationLink, title);
                    return title;
                }
            }
        }

        // Return null, indicating that no appropriate title was found
        m_savedTitles.put(applicationLink, null);
        return null;
    }

    public Season getPositionSeason(String applicationLink, Elements applicationPage) {
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
        return Season.SUMMER;
    }

    public int getPositionYear(String applicationLink, Elements applicationPage) { 
        String pageText = applicationPage.text().trim().toLowerCase();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        int nextYear = currentYear + 1;

        // TODO: Only search large bodies of text, the title, and unordered lists

        List<String> yearList = Arrays.asList(String.valueOf(nextYear), String.valueOf(currentYear));
        for(String currYear: yearList) {
            if (pageText.contains(currYear)) {
                return Integer.parseInt(currYear);
            }
        }

        return nextYear;
    }

    public String getPositionDegree(String applicationLink, Elements applicationPage) {
        String pageText = applicationPage.text().trim().toLowerCase();
        if (pageText.contains("bachelor’s") || pageText.contains("bs")) {
            return "BS";
        }
        else if (pageText.contains("master's") || pageText.contains("ms")) {
            return "MS";
        }
        else if (pageText.contains("phd")) {
            return "PhD";
        }
        return "BS";
    }

    public String getPositionLocation(String applicationLink, Elements applicationPage) { 
        String pageText = applicationPage.text().trim().toLowerCase();
        String[] words = pageText.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (words[i].contains("location")) {
                if ((i+1) < words.length) {
                    return words[i+1].replaceAll("[^a-zA-Z ]", "");
                }
            }
        }
        return "";
    }
}
