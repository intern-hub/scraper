package com.internhub.scraper.scrapers;

import java.util.Set;

public interface Scraper<T> {
    Set<T> fetch();
}
