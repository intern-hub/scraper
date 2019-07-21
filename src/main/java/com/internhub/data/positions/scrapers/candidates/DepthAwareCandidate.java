package com.internhub.data.positions.scrapers.candidates;

public class DepthAwareCandidate extends QueuedCandidate {
    private final int depth;

    public DepthAwareCandidate(String link, int depth) {
        super(link);
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }
}
