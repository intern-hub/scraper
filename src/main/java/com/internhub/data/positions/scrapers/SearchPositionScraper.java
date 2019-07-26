package com.internhub.data.positions.scrapers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.scrapers.strategies.InitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.SearchPositionStrategy;

import java.util.List;

public class SearchPositionScraper implements PositionScraper {
    private InitialLinkStrategy mInitialStrategy;
    private SearchPositionStrategy mSearchStrategy;

    public SearchPositionScraper(InitialLinkStrategy initialStrategy, SearchPositionStrategy searchStrategy) {
        this.mInitialStrategy = initialStrategy;
        this.mSearchStrategy = searchStrategy;
    }

    public List<Position> fetch(Company company) {
        return mSearchStrategy.fetchWithInitialLinks(company, mInitialStrategy.fetchInitialLinks(company));
    }
}
