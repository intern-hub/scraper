package com.internhub.data.positions.scrapers.strategies.impl;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.pages.Page;
import com.internhub.data.positions.extractors.PositionExtractor;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Although this strategy is faster than its single-threaded counterpart,
 * it does not pause at the correct time.
 */
public class PositionBFSMTStrategy implements IPositionScraperStrategy, IPositionBFSScraperStrategy {
    private final InternWebDriverPool mDriverPool;
    private final PositionExtractor mPositionExtractor;
    private ScheduledExecutorService mService;

    public PositionBFSMTStrategy(InternWebDriverPool pool, ScheduledExecutorService service) {
        mDriverPool = pool;
        mPositionExtractor = new PositionExtractor();
        mService = service;
    }

    @Override
    public void producePositions(Company company, List<String> initialLinks, Consumer<Position> consumer) {
        PriorityQueue<Candidate> candidates = new PriorityQueue<>(new CandidateComparator());
        Set<String> visited = new HashSet<>();

        final AtomicInteger totalLinks = new AtomicInteger(0);
        candidates.addAll(initialLinks.stream()
                .map((link) -> new Candidate(link, 1))
                .collect(Collectors.toList()));

        mService.execute(() -> process(company, candidates, totalLinks, visited, consumer));
    }

    private void process(Company company, PriorityQueue<Candidate> candidates,
                         final AtomicInteger totalLinks, Set<String> visited,
                         Consumer<Position> consumer) {
        if (!candidates.isEmpty() && totalLinks.get() < MAX_TOTAL_LINKS) {
            Candidate candidate = candidates.poll();
            if (visited.contains(candidate.link)) {
                return;
            }
            visited.add(candidate.link);
            logger.info(String.format(
                    "[%d/%d] Visiting %s (depth = %d) ...",
                    totalLinks.getAndIncrement(), MAX_TOTAL_LINKS, candidate.link, candidate.depth));
            try {
                processCandidate(company, candidate, candidates, visited, consumer);
            } catch (TimeoutException e) {
                logger.error(String.format("[%d/%d] Encountered timeout exception on %s. (depth = %d)",
                        totalLinks.get(), MAX_TOTAL_LINKS, candidate.link, candidate.depth));
            } catch (Exception e) {
                logger.error(String.format(
                        "[%d/%d] Unknown error encountered on %s, swallowed exception. (depth = %d)",
                        totalLinks.get(), MAX_TOTAL_LINKS, candidate.link, candidate.depth), e);
            } finally {
                mService.schedule(
                        () -> process(company, candidates, totalLinks, visited, consumer),
                        JAVASCRIPT_LOAD_MILLISECONDS, TimeUnit.MILLISECONDS);
            }
        } else {
            consumer.accept(null);
        }
    }

    private void processCandidate(Company company, Candidate candidate,
                                  PriorityQueue<Candidate> candidates,
                                  Set<String> visited, Consumer<Position> consumer) {
        Page page = getPage(candidate.link);
        PositionExtractor.ExtractionResult extraction = mPositionExtractor.extract(page, company);

        logPosition(extraction.position, candidate.link);

        if (extraction.position != null) {
            consumer.accept(extraction.position);
        }

        if (candidate.depth < MAX_DEPTH) {
            candidates.addAll(extraction.nextPositions.stream()
                    .filter((link) -> !visited.contains(link))
                    .map((link) -> new Candidate(link, candidate.depth + 1))
                    .collect(Collectors.toList()));
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

    private Page getPage(String link) {
        try (InternWebDriverPoolConnection driverConnection = mDriverPool.acquire()) {
            WebDriver driver = driverConnection.getDriver();
            driver.get(link);
            Page page = new Page(driver.getPageSource(), link);
            page.process(driver);
            return page;
        } catch (InterruptedException e) {
            logger.error("Unable to acquire web driver.", e);
            return null;
        }
    }
}

