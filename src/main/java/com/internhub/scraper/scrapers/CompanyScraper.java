package com.internhub.scraper.scrapers;

import com.internhub.scraper.models.Company;

import java.util.List;

public interface CompanyScraper {
    List<Company> fetch();
}
