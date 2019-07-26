package com.internhub.data.positions.scrapers.strategies.impl;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.pages.Page;
import com.internhub.data.positions.extractors.PositionExtractor;
import com.internhub.data.positions.scrapers.strategies.SearchPositionStrategy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ShallowSearchPositionStrategy implements SearchPositionStrategy  {
    private static final int MAX_DEPTH = 4;
    private static final int MAX_TOTAL_LINKS = 100;
    private static final int PAGE_LOAD_DELAY = 2000;

    private static final Logger logger = LoggerFactory.getLogger(ShallowSearchPositionStrategy.class);

    private WebDriver mDriver;
    private PositionExtractor mPositionExtractor;

    public ShallowSearchPositionStrategy(WebDriver driver) {
        mDriver = driver;
        mPositionExtractor = new PositionExtractor();
    }

    @Override
    public List<Position> fetchWithInitialLinks(Company company, List<String> initialLinks) {
        List<Position> results = Lists.newArrayList();
        PriorityQueue<Candidate> candidates = new PriorityQueue<>(new CandidateComparator());
        Set<String> visited = new HashSet<>(initialLinks);

        candidates.addAll(initialLinks.stream()
                .map((link) -> new Candidate(link, 1))
                .collect(Collectors.toList()));

        int totalLinks = 0;

        while (!candidates.isEmpty() && totalLinks < MAX_TOTAL_LINKS) {
            Candidate candidate = candidates.poll();

            logger.info(String.format(
                    "[%d/%d] Visiting %s (depth = %d) ...",
                    totalLinks + 1, MAX_TOTAL_LINKS, candidate.link, candidate.depth));

            Page page = getPage(candidate.link);
            PositionExtractor.ExtractionResult extraction = mPositionExtractor.scrapePage(page, company);

            totalLinks++;
            reportPosition(extraction.position, totalLinks);

            if (extraction.position != null) {
                results.add(extraction.position);
            }

            if (candidate.depth < MAX_DEPTH) {
                candidates.addAll(extraction.nextPositions.stream()
                        .filter((link) -> !visited.contains(link))
                        .map((link) -> new Candidate(link, candidate.depth + 1))
                        .collect(Collectors.toList()));
            }
        }

        return results;
    }

    /**
     * Returns a processed page with all necessary information describing a web page
     */
    private Page getPage(String link) {
        // Use Selenium to fetch the page and wait a bit for it to load
        try {
            mDriver.get(link);
            Thread.sleep(PAGE_LOAD_DELAY);
        } catch (TimeoutException ex) {
            logger.error("Skipping page due to timeout issues.", ex);
            return null;
        } catch (InterruptedException ex) {
            logger.error("Could not wait for page to load.", ex);
            return null;
        }

        Page page = new Page(mDriver.getPageSource(), link);
        page.process(mDriver);
        return page;
    }

    /**
     * Logs info about found position
     */
    private void reportPosition(Position position, int totalLinks) {
        if (position != null) {
            logger.info(String.format("[%d/%d] Identified valid position.", totalLinks, MAX_TOTAL_LINKS));
            logger.info(String.format("[%d/%d] Title is %s.", totalLinks, MAX_TOTAL_LINKS, position.getTitle()));
            logger.info(String.format("[%d/%d] Season & year is %s %d.", totalLinks, MAX_TOTAL_LINKS, position.getSeason(), position.getYear()));
            logger.info(String.format("[%d/%d] Location is %s.", totalLinks, MAX_TOTAL_LINKS, position.getLocation()));
            logger.info(String.format("[%d/%d] Minimum degree is %s.", totalLinks, MAX_TOTAL_LINKS, position.getDegree()));
        } else {
            logger.info(String.format("[%d/%d] Unable to find position.", totalLinks, MAX_TOTAL_LINKS));
        }
    }
}

class Candidate {
    final String link;
    final int depth;

    Candidate(String link, int depth) {
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
