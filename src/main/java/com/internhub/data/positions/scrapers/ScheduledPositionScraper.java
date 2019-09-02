package com.internhub.data.positions.scrapers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.positions.scrapers.strategies.IInitialLinkStrategy;
import com.internhub.data.positions.scrapers.strategies.IPositionScraperStrategy;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class ScheduledPositionScraper implements IPositionScraper {
    private IInitialLinkStrategy mInitialStrategy;
    private IPositionScraperStrategy mScraperStrategy;
    private ScheduledExecutorService mService;

    public ScheduledPositionScraper(IInitialLinkStrategy initialStrategy,
                                    IPositionScraperStrategy scraperStrategy,
                                    ScheduledExecutorService service) {
        mInitialStrategy = initialStrategy;
        mScraperStrategy = scraperStrategy;
        mService = service;
    }

    @Override
    public void scrape(Company company, Consumer<Position> consumer) {
        mService.execute(() -> {
            List<String> initialLinks = mInitialStrategy.getLinks(company);
            mService.execute(() -> {
                mScraperStrategy.producePositions(company, initialLinks, consumer);
            });
        });
    }
}
