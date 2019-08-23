package com.internhub.data.positions.scrapers.strategies;

import com.internhub.data.models.Company;
import com.internhub.data.positions.scrapers.PositionCallback;
import com.internhub.data.positions.scrapers.PositionScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface IPositionScraperStrategy {
    Logger logger = LoggerFactory.getLogger(PositionScraper.class);
    void fetch(Company company, List<String> initialLinks, PositionCallback callback);
}
