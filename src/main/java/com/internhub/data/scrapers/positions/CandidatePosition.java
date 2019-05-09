package com.internhub.data.scrapers.positions;

public class CandidatePosition {
    private String link;
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
