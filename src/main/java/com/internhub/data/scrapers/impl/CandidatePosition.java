package com.internhub.data.scrapers.impl;

/**
 * Represents a position
 */
public class CandidatePosition {
    // Link to the position
    private String link;
    // At what depth position was found
    private int depth;

    public CandidatePosition(String link, int depth) {
        this.link = link;
        this.depth = depth;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
