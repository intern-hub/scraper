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
        PriorityQueue<ShallowCandidate> candidates = new PriorityQueue<>(new ShallowCandidateComparator<>());
        Set<String> visited = new HashSet<>(initialLinks);

        candidates.addAll(initialLinks.stream()
                .map((link) -> new ShallowCandidate(link, 1))
                .collect(Collectors.toList()));

        int totalLinks = 0;

        while (!candidates.isEmpty() && totalLinks < MAX_TOTAL_LINKS) {
            ShallowCandidate candidate = candidates.poll();

            logger.info(String.format(
                    "[%d/%d] Visiting %s (depth = %d) ...",
                    totalLinks + 1, MAX_TOTAL_LINKS, candidate.getLink(), candidate.getDepth()));

            Page page = getPage(candidate.getLink());
            PositionExtractor.ExtractionResult extraction = mPositionExtractor.scrapePage(page, company);

            totalLinks++;
            reportPosition(extraction.position, totalLinks);

            if (extraction.position != null) {
                results.add(extraction.position);
            }

            if (candidate.getDepth() < MAX_DEPTH) {
                candidates.addAll(extraction.nextPositions.stream()
                        .filter((link) -> !visited.contains(link))
                        .map((link) -> new ShallowCandidate(link, candidate.getDepth() + 1))
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
