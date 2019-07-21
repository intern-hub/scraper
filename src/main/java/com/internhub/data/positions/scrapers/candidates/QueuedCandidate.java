package com.internhub.data.positions.scrapers.candidates;

public class QueuedCandidate {
    private final String link;

    public QueuedCandidate(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }
}
