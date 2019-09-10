package com.internhub.data.positions.scrapers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.scrapers.strategies.IInitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.IPositionScraperStrategy;

import java.util.function.Consumer;

public class PositionScraper implements IPositionScraper {
    private IInitialLinkStrategy mInitialStrategy;
    private IPositionScraperStrategy mScraperStrategy;

    public PositionScraper(IInitialLinkStrategy initialStrategy,
                           IPositionScraperStrategy scraperStrategy) {
        mInitialStrategy = initialStrategy;
        mScraperStrategy = scraperStrategy;
    }

    @Override
    public void scrape(Company company, Consumer<Position> consumer) {
        mScraperStrategy.producePositions(company,
                mInitialStrategy.getLinks(company), consumer);
    }
}
