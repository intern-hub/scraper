package com.internhub.scraper;

import com.internhub.scraper.models.Company;
import com.internhub.scraper.scrapers.CompanyScraper;

public class Main {
    public static void main(String[] args) {
        CompanyScraper scraper = new CompanyScraper();
        for (Company company : scraper.fetch()) {
            System.out.println(company.getName());
        }
    }
}
