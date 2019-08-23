package com.internhub.data.positions.scrapers;

import com.internhub.data.models.Company;
import com.internhub.data.positions.scrapers.strategies.IPositionScraperStrategy;
import com.internhub.data.positions.scrapers.strategies.InitialLinkStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionScraper implements IPositionScraper {
    private InitialLinkStrategy mInitialStrategy;
    private IPositionScraperStrategy mSearchStrategy;

    private Map<String, List<String>> mLinkCache;

    public PositionScraper(InitialLinkStrategy initialStrategy, IPositionScraperStrategy searchStrategy) {
        this.mInitialStrategy = initialStrategy;
        this.mSearchStrategy = searchStrategy;
        this.mLinkCache = new HashMap<>();
    }

    public void setup(Company company) {
        mLinkCache.put(company.getName(), mInitialStrategy.fetchInitialLinks(company));
    }

    public void fetch(Company company, PositionCallback callback) {
        List<String> initialLinks = mLinkCache.containsKey(company.getName()) ?
                mLinkCache.get(company.getName()) : mInitialStrategy.fetchInitialLinks(company);
        mSearchStrategy.fetch(company, initialLinks, callback);
    }
}
