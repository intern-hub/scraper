package com.internhub.data.positions.scrapers;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;

import java.util.function.Consumer;

public interface IPositionScraper {
    /**
     * Scrapes internship positions from the Internet belonging to the given company.
     * Any valid positions that are found are passed to the user-supplied
     * consumer object. When the consumer reports a null object, this
     * signals that the scraping process has finished.
     *
     * @param company Company to scrape positions for
     * @param consumer User-supplied consumption algorithm for handling positions
     */
    void scrape(Company company, Consumer<Position> consumer);
}
