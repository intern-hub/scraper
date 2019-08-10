package com.internhub.data.positions.scrapers.strategies.impl;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.pages.Page;
import com.internhub.data.positions.extractors.PositionExtractor;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Had to do something like this to escape the schedule executor service task
 */
class ScheduledDelayFinishedException extends RuntimeException {
}

public class PositionBFSMTStrategy extends PositionBFSStrategy {
    private final ScheduledExecutorService scheduler;

    public PositionBFSMTStrategy(WebDriver driver) {
        super(driver);
        scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public List<Position> fetch(Company company, List<String> initialLinks) {
        List<Position> results = Lists.newArrayList();
        PriorityQueue<Candidate> candidates = new PriorityQueue<>(new CandidateComparator());
        Set<String> visited = new HashSet<>(initialLinks);

        final AtomicInteger totalLinks = new AtomicInteger(0);
        candidates.addAll(initialLinks.stream()
                .map((link) -> new Candidate(link, 1))
                .collect(Collectors.toList()));

        ScheduledFuture future = scheduler.scheduleWithFixedDelay(
                () -> {
                    if (!candidates.isEmpty() && totalLinks.get() < MAX_TOTAL_LINKS) {
                        Candidate candidate = candidates.poll();
                        logger.info(String.format(
                                "[%d/%d] Visiting %s (depth = %d) ...",
                                totalLinks.getAndIncrement(), MAX_TOTAL_LINKS, candidate.link, candidate.depth));

                        processCandidate(company, candidate, candidates, visited, results);
                    } else {
                        throw new ScheduledDelayFinishedException();
                    }
                }, 0, PAGE_LOAD_DELAY_MS, TimeUnit.MILLISECONDS
        );

        try {
            future.get();
        } catch (InterruptedException | ExecutionException | ScheduledDelayFinishedException ignored) {
        }
        return results;
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
}
