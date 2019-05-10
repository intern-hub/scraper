package com.internhub.data.scrapers.positions;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CandidateHeuristicComparator implements Comparator<CandidatePosition> {
    private static final Map<String, Integer> TAGS;

    static {
        TAGS = new HashMap<>();
        TAGS.put("intern", 40);
        TAGS.put("career", 12);
        TAGS.put("job", 8);
        TAGS.put("student", 2);
        TAGS.put("university", 2);
        TAGS.put("software", 1);
        TAGS.put("hardware", 1);
        TAGS.put("engineer", 1);
        TAGS.put("greenhouse", 1);
        TAGS.put("workday", 1);
        TAGS.put("taleo", 1);
        TAGS.put("jobvite", 1);
        TAGS.put("icims", 1);
    }

    @Override
    public int compare(CandidatePosition c1, CandidatePosition c2) {
        return heuristic(c2) - heuristic(c1);
    }

    private int heuristic(CandidatePosition candidate) {
        int score = 0;
        String llink = candidate.getLink().toLowerCase();
        for (Map.Entry<String, Integer> entry : TAGS.entrySet()) {
            if (llink.contains(entry.getKey())) {
                score += entry.getValue();
            }
        }
        return score;
    }
}
