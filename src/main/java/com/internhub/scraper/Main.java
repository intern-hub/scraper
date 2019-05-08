package com.internhub.scraper;

import com.internhub.scraper.models.Company;
import com.internhub.scraper.scrapers.CompanyRedditScraper;

public class Main {
    public static void main(String[] args) {
        CompanyRedditScraper scraper = new CompanyRedditScraper();
        for (Company company : scraper.fetch()) {
            System.out.println(company.getName());
        }
    }
}
