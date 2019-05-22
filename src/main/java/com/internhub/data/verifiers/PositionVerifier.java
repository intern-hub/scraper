package com.internhub.data.verifiers;

import com.internhub.data.models.Season;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.stream.Collectors;

public class PositionVerifier {
    private static final int VALIDITY_THRESHOLD = 2;
    // After May of a year, it is assumed all summer postings are for next year by default
    private static final int CUTOFF_MONTH = 3;

    private static List<String> VALID_SUBMISSION_BUTTONS;

    static {
        VALID_SUBMISSION_BUTTONS = Arrays.asList("apply", "start", "application", "submit");
    }

    private Map<String, String> m_savedTitles;
    private Map<String, Season> m_savedSeasons;

    public PositionVerifier() {
        this.m_savedTitles = new HashMap<>();
        this.m_savedSeasons = new HashMap<>();
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
                String[] words = title.toLowerCase().split(" ");
                for (String word : words) {
                    if (word.equals("intern")) {
                        m_savedTitles.put(applicationLink, title);
                        return title;
                    }
                }
            }
        }

        // Return null, indicating that no appropriate title was found
        m_savedTitles.put(applicationLink, null);
        return null;
    }



    public int getPositionYear(String applicationLink, Elements applicationPage) { 
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int nextYear = currentYear + 1;

        // Only search the title and large bodies of text
        // Avoid unordered lists containing dates of job postings, which can misleading
        List<String> informationAreas = new ArrayList<>();
        informationAreas.add(getPositionTitle(applicationLink, applicationPage));
        informationAreas.addAll(applicationPage.select("p").eachText()
                .stream()
                .filter(paragraph -> paragraph.chars().filter(Character::isWhitespace).count() > 10)
                .collect(Collectors.toList())
        );

        // Check each area for a word corresponding to a potential start date year
        List<String> years = Arrays.asList(String.valueOf(nextYear), String.valueOf(currentYear));
        for (String area : informationAreas) {
            List<String> words = Arrays.asList(area.split(" "));
            for(String year : years) {
                for (int i = 0; i < words.size(); i++) {
                    if (words.get(i).equals(year)) {
                        // Check immediate next word for mention of a season
                        if (i <= words.size() - 2) {
                            String next = words.get(i + 1).toLowerCase();
                            Season nextSeason = Season.getSeasonFromLowercaseName(next);
                            if (nextSeason != null) {
                                m_savedSeasons.put(applicationLink, nextSeason);
                            }
                        }
                        // Check immediate previous word for mention of a season
                        if (i >= 1) {
                           String previous = words.get(i - 1).toLowerCase();
                           Season previousSeason = Season.getSeasonFromLowercaseName(previous);
                            if (previousSeason != null) {
                                m_savedSeasons.put(applicationLink, previousSeason);
                            }
                        }
                        return Integer.parseInt(year);
                    }
                }
            }
        }

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        return currentMonth < CUTOFF_MONTH ? currentYear : nextYear;
    }

    public Season getPositionSeason(String applicationLink, Elements applicationPage) {
        if (m_savedSeasons.containsKey(applicationLink)) {
            return m_savedSeasons.get(applicationLink);
        }

        // The default season is SUMMER, since that's when most internships occur.
        Season season = Season.SUMMER;

        // Find the season the "dumb" way: just search through the entire page and try to
        // find instances of the season word. We are unlikely to get a false positive because the
        // word is most likely tied to the contents of the job posting, rather than some other source.
        String pageText = applicationPage.text().trim().toLowerCase();
        if (pageText.contains("summer")) {
            season = Season.SUMMER;
        }
        else if (pageText.contains("fall")) {
            season = Season.FALL;
        }
        else if (pageText.contains("winter")) {
            season = Season.WINTER;
        }
        else if (pageText.contains("spring")) {
            season = Season.SPRING;
        }

        m_savedSeasons.put(applicationLink, season);
        return season;
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
