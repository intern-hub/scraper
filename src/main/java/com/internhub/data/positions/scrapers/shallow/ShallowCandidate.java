package com.internhub.data.positions.scrapers.shallow;

public class ShallowCandidate {
    private final String mLink;
    private final int mDepth;

    public ShallowCandidate(String link, int depth) {
        mLink = link;
        mDepth = depth;
    }

    public String getLink() {
        return mLink;
    }

    public int getDepth() {
        return mDepth;
    }
}
