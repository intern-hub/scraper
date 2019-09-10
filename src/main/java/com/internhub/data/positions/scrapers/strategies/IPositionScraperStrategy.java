package com.internhub.data.positions.scrapers.strategies;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public interface IPositionScraperStrategy {
    Logger logger = LoggerFactory.getLogger(IPositionScraperStrategy.class);

    /**
     * Scrapes positions for the given company, using the list of initial links as
     * starting points. Any positions produced are immediately passed to the consumer
     * object to be saved, printed, collected, or otherwise discarded. When the
     * consumer reports a null object, this signals that the scraping process has
     * finished.
     *
     * @param company Company to scrape for
     * @param initialLinks Initial links to use, presumably belonging to the company
     * @param consumer User-supplied consumption algorithm
     */
    void producePositions(Company company, List<String> initialLinks, Consumer<Position> consumer);
}
