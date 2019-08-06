package com.internhub.data.positions.scrapers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.scrapers.strategies.IPositionScraperStrategy;
import com.internhub.data.positions.scrapers.strategies.InitialLinkStrategy;

import java.util.List;

public class PositionScraper implements IPositionScraper {
    private InitialLinkStrategy mInitialStrategy;
    private IPositionScraperStrategy mSearchStrategy;

    public PositionScraper(InitialLinkStrategy initialStrategy, IPositionScraperStrategy searchStrategy) {
        this.mInitialStrategy = initialStrategy;
        this.mSearchStrategy = searchStrategy;
    }

    public List<Position> fetch(Company company) {
        return mSearchStrategy.fetch(company, mInitialStrategy.fetchInitialLinks(company));
    }
}
