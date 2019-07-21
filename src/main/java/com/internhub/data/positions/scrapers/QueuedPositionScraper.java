package com.internhub.data.positions.scrapers;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.scrapers.candidates.QueuedCandidate;

import java.util.*;

public abstract class QueuedPositionScraper<C extends QueuedCandidate> implements PositionScraper {
    private Queue<C> frontier;
    protected Set<String> visited;

    public QueuedPositionScraper(Queue<C> frontier) {
        this.frontier = frontier;
        this.visited = new HashSet<>();
    }

    // ********************************************************************************
    //                                  TEMPLATE
    // ********************************************************************************

    public List<Position> fetch(Company company) {
        List<Position> results = Lists.newArrayList();

        frontier.clear();
        visited.clear();
        prepareFetch();

        for (C initial : getInitialCandidates(company)) {
            frontierAdd(initial);
            visited.add(initial.getLink());
        }

        while (!frontier.isEmpty() && shouldKeepFetching()) {
            C candidate = frontierPop();
            Position position = getPositionFromCandidate(company, candidate);
            if (position != null) {
                results.add(position);
            }
            for (C nxt : getNextCandidates(company, candidate)) {
                frontier.add(nxt);
                visited.add(nxt.getLink());
            }
        }

        return results;
    }

    // ********************************************************************************
    //                                    STEPS
    // ********************************************************************************

    protected abstract Collection<C> getInitialCandidates(Company company);

    protected abstract Position getPositionFromCandidate(Company company, C candidate);

    // CONTRACT: It is the caller's responsibility to make sure that
    // every next candidate is absent from the visited set!
    protected abstract Collection<C> getNextCandidates(Company company, C candidate);

    // ********************************************************************************
    //                                    HOOKS
    // ********************************************************************************

    protected void prepareFetch() {

    }

    protected boolean shouldKeepFetching() {
        return true;
    }

    protected C frontierPop() {
        return frontier.remove();
    }

    protected void frontierAdd(C candidate) {
        frontier.add(candidate);
    }
}
