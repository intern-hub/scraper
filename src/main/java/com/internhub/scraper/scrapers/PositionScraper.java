package com.internhub.scraper.scrapers;

import com.internhub.scraper.models.Company;
import com.internhub.scraper.models.Position;

import java.util.List;

public interface PositionScraper {
    List<Position> fetch(Company company);
}
