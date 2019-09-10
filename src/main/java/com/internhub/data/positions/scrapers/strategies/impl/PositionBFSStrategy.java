package com.internhub.data.positions.scrapers.strategies.impl;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.extractors.PositionExtractor;
import com.internhub.data.positions.pages.Page;
import com.internhub.data.positions.scrapers.strategies.IPositionBFSScraperStrategy;
import com.internhub.data.positions.scrapers.strategies.IPositionScraperStrategy;
import com.internhub.data.selenium.InternWebDriverPool;
import com.internhub.data.selenium.InternWebDriverPoolConnection;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PositionBFSStrategy implements IPositionScraperStrategy, IPositionBFSScraperStrategy {
    private InternWebDriverPool myWebDriverPool;
    private PositionExtractor mPositionExtractor;

    public PositionBFSStrategy(InternWebDriverPool pool) {
        myWebDriverPool = pool;
        mPositionExtractor = new PositionExtractor();
    }

    @Override
    public void producePositions(Company company, List<String> initialLinks, Consumer<Position> consumer) {
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
                    "[%d/%d] Candidate is %s. (depth = %d)",
                    totalLinks + 1, MAX_TOTAL_LINKS, candidate.link, candidate.depth));
            try {
                Page page = getPage(candidate.link);
                PositionExtractor.ExtractionResult extraction = mPositionExtractor.extract(page, company);

                if (extraction.position != null) {
                    consumer.accept(extraction.position);
                }

                if (candidate.depth < MAX_DEPTH) {
                    candidates.addAll(extraction.nextPositions.stream()
                            .filter((link) -> !visited.contains(link))
                            .map((link) -> new Candidate(link, candidate.depth + 1))
                            .collect(Collectors.toList()));
                }
            } catch (TimeoutException e) {
                logger.error(String.format("[%d/%d] Timed out on %s. (depth = %d)",
                        totalLinks, MAX_TOTAL_LINKS, candidate.link, candidate.depth));
            } catch (Exception e) {
                logger.error(String.format(
                        "[%d/%d] Swallowed error on %s. (depth = %d)",
                        totalLinks, MAX_TOTAL_LINKS, candidate.link, candidate.depth), e);
            }

            ++totalLinks;
        }

        consumer.accept(null);
    }

    private Page getPage(String link) {
        try (InternWebDriverPoolConnection driverWrapper = myWebDriverPool.acquire()) {
            WebDriver driver = driverWrapper.getDriver();
            // Use Selenium to fetch the page
            driver.get(link);
            // Wait for the page to load any JavaScript (e.g Amazon's internships)
            try {
                Thread.sleep(JAVASCRIPT_LOAD_MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Could not wait for JavaScript to load.", e);
            }
            Page page = new Page(driver.getPageSource(), link);
            page.process(driver);
            return page;
        } catch (InterruptedException e) {
            logger.error("Could not acquire web driver.", e);
            return null;
        }
    }
}
