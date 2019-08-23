package com.internhub.data.positions.scrapers.strategies.impl;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.pages.Page;
import com.internhub.data.positions.extractors.PositionExtractor;
import com.internhub.data.positions.scrapers.PositionCallback;
import com.internhub.data.positions.scrapers.strategies.IPositionBFSScraperStrategy;
import com.internhub.data.positions.scrapers.strategies.IPositionScraperStrategy;
import com.internhub.data.selenium.InternWebDriverPool;
import com.internhub.data.selenium.InternWebDriverPoolConnection;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.util.*;
import java.util.stream.Collectors;

public class PositionBFSStrategy implements IPositionScraperStrategy, IPositionBFSScraperStrategy {
    private InternWebDriverPool myWebDriverPool;
    private PositionExtractor mPositionExtractor;

    public PositionBFSStrategy(InternWebDriverPool pool) {
        myWebDriverPool = pool;
        mPositionExtractor = new PositionExtractor();
    }

    @Override
    public void fetch(Company company, List<String> initialLinks, PositionCallback callback) {
        List<Position> results = Lists.newArrayList();
        PriorityQueue<Candidate> candidates = new PriorityQueue<>(new CandidateComparator());
        Set<String> visited = new HashSet<>(initialLinks);

        candidates.addAll(initialLinks.stream()
                .map((link) -> new Candidate(link, 1))
                .collect(Collectors.toList()));

        int totalLinks = 0;

        while (!candidates.isEmpty() && totalLinks < MAX_TOTAL_LINKS) {
            Candidate candidate = candidates.poll();
            visited.add(candidate.link);

            logger.info(String.format(
                    "[%d/%d] Visiting %s (depth = %d) ...",
                    totalLinks + 1, MAX_TOTAL_LINKS, candidate.link, candidate.depth));
            try {
                Page page = getPage(candidate.link);
                PositionExtractor.ExtractionResult extraction = mPositionExtractor.extract(page, company);

                logPosition(extraction.position, candidate.link);

                if (extraction.position != null) {
                    results.add(extraction.position);
                }

                if (candidate.depth < MAX_DEPTH) {
                    candidates.addAll(extraction.nextPositions.stream()
                            .filter((link) -> !visited.contains(link))
                            .map((link) -> new Candidate(link, candidate.depth + 1))
                            .collect(Collectors.toList()));
                }
            } catch (TimeoutException e) {
                logger.error(String.format("[%d/%d] Encountered timeout exception on %s. (depth = %d)",
                        totalLinks, MAX_TOTAL_LINKS, candidate.link, candidate.depth));
            } catch (Exception e) {
                logger.error(String.format(
                        "[%d/%d] Unknown error encountered on %s, swallowed exception. (depth = %d)",
                        totalLinks, MAX_TOTAL_LINKS, candidate.link, candidate.depth), e);
            }

            ++totalLinks;
        }

        callback.run(results);
    }

    private Page getPage(String link) {
        try (InternWebDriverPoolConnection driverWrapper = myWebDriverPool.acquire()) {
            WebDriver driver = driverWrapper.getDriver();
            // Use Selenium to fetch the page
            driver.get(link);
            // Wait for the page to load any JavaScript (e.g Amazon's internships)
            try {
                Thread.sleep(PAGE_LOAD_DELAY_MS);
            } catch (InterruptedException e) {
                logger.error("Could not wait for page to load.", e);
            }
            Page page = new Page(driver.getPageSource(), link);
            page.process(driver);
            return page;
        } catch (InterruptedException e) {
            logger.error("Could not acquire web driver.", e);
            return null;
        }
    }

    private void logPosition(Position position, String link) {
        if (position != null) {
            logger.info(String.format("Identified valid position at %s.", link));
            logger.info(String.format("Title is %s.", position.getTitle()));
            logger.info(String.format("Season & year is %s %d.", position.getSeason(), position.getYear()));
            logger.info(String.format("Location is %s.", position.getLocation()));
            logger.info(String.format("Minimum degree is %s.", position.getDegree()));
        } else {
            logger.info(String.format("Unable to find position at %s.", link));
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
