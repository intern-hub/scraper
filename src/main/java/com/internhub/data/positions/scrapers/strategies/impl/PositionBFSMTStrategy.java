package com.internhub.data.positions.scrapers.strategies.impl;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.pages.Page;
import com.internhub.data.positions.extractors.PositionExtractor;
import com.internhub.data.positions.scrapers.strategies.IPositionBFSScraperStrategy;
import com.internhub.data.positions.scrapers.strategies.IPositionScraperStrategy;
import com.internhub.data.selenium.MyWebDriverPool;
import com.internhub.data.selenium.MyWebDriverPoolWrapper;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PositionBFSMTStrategy implements IPositionScraperStrategy, IPositionBFSScraperStrategy {
    private final MyWebDriverPool mWebDriverPool;
    private final PositionExtractor mPositionExtractor;
    private final ScheduledExecutorService scheduler;
    private final Object syncObj = new Object();

    public PositionBFSMTStrategy(MyWebDriverPool pool) {
        mWebDriverPool = pool;
        mPositionExtractor = new PositionExtractor();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public List<Position> fetch(Company company, List<String> initialLinks) {
        List<Position> results = Lists.newArrayList();
        PriorityQueue<Candidate> candidates = new PriorityQueue<>(new CandidateComparator());
        Set<String> visited = new HashSet<>();

        final AtomicInteger totalLinks = new AtomicInteger(0);
        candidates.addAll(initialLinks.stream()
                .map((link) -> new Candidate(link, 1))
                .collect(Collectors.toList()));

        scheduler.schedule(
                () -> process(company, candidates, totalLinks, visited, results), PAGE_LOAD_DELAY_MS, TimeUnit.MILLISECONDS
        );

        synchronized (syncObj) {
            try {
                syncObj.wait();
            } catch (InterruptedException ignored) {
            }
        }

        return results;
    }

    private void process(Company company, PriorityQueue<Candidate> candidates,
                         final AtomicInteger totalLinks, Set<String> visited, List<Position> results) {
        if (!candidates.isEmpty() && totalLinks.get() < MAX_TOTAL_LINKS) {
            Candidate candidate = candidates.poll();
            Runnable processFunc = () -> process(company, candidates, totalLinks, visited, results);

            try {
                if (visited.contains(candidate.link)) {
                    return;
                }

                logger.info(String.format(
                        "[%d/%d] Visiting %s (depth = %d) ...",
                        totalLinks.getAndIncrement(), MAX_TOTAL_LINKS, candidate.link, candidate.depth));

                processCandidate(company, candidate, candidates, visited, results);
            } catch (Exception e) {
                String message = String.format(
                        "[%d/%d] Unknown error encountered on %s, swallowed exception. (depth = %d)",
                        totalLinks.get(), MAX_TOTAL_LINKS, candidate.link, candidate.depth);
                if (e instanceof TimeoutException) {
                    logger.error(message);
                }
                else {
                    logger.error(message, e);
                }
            } finally {
                scheduler.schedule(
                        processFunc,
                        PAGE_LOAD_DELAY_MS, TimeUnit.MILLISECONDS
                );
            }
        } else {
            logger.info(String.format(
                    "[%d/%d] Finished scraping %s",
                    totalLinks.get(), MAX_TOTAL_LINKS, company));
            synchronized (syncObj) {
                syncObj.notify();
                scheduler.shutdownNow();
            }
        }

    }

    private void processCandidate(Company company, Candidate candidate, PriorityQueue<Candidate> candidates,
                                  Set<String> visited, List<Position> results) {
        visited.add(candidate.link);
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
    }

    /**
     * Logs info about found position
     */
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

    public Page getPage(String link) {
        try (MyWebDriverPoolWrapper driverWrapper = mWebDriverPool.acquire()) {
            WebDriver driver = driverWrapper.getDriver();
            driver.get(link);
            // This is here to let JavaScript load!
            // Thread.sleep(PAGE_LOAD_DELAY_MS);
            Page page = new Page(driver.getPageSource(), link);
            page.process(driver);
            return page;
        } catch (InterruptedException e) {
            logger.warn("Could not acquire web driver");
            return null;
        }
    }
}
