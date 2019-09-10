package com.internhub.data.positions.scrapers.strategies;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public interface IPositionBFSScraperStrategy {
    int MAX_DEPTH = 4;
    int MAX_TOTAL_LINKS = 100;
    int JAVASCRIPT_LOAD_MILLISECONDS = 2000;

    class Candidate {
        public final String link;
        public final int depth;

        public Candidate(String link, int depth) {
            this.link = link;
            this.depth = depth;
        }
    }

    class CandidateComparator implements Comparator<Candidate> {
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
        public int compare(Candidate c1, Candidate c2) {
            return heuristic(c2) - heuristic(c1);
        }

        private int heuristic(Candidate candidate) {
            int score = 0;
            String llink = candidate.link.toLowerCase();
            for (Map.Entry<String, Integer> entry : TAGS.entrySet()) {
                if (llink.contains(entry.getKey())) {
                    score += entry.getValue();
                }
            }
            return score;
        }
    }
}
