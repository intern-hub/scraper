package com.internhub.data.scrapers;

import java.util.Collection;

/**
 * Multithreaded scraper
 */
public interface MTScraper {
    public void load(Collection workItems);

    public Collection scrape();
}
