package com.internhub.scraper;

import com.internhub.scraper.companies.Company;
import com.internhub.scraper.companies.CompanyScraper;

public class Main {
    public static void main(String[] args) {
        CompanyScraper scraper = new CompanyScraper();
        for (Company company : scraper.fetch()) {
            System.out.println(company.getName());
        }
    }
}
