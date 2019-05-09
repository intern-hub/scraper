package com.internhub.data;

import com.internhub.data.models.Company;
import com.internhub.data.scrapers.companies.RedditCompanyScraper;

public class Main {
    public static void main(String[] args) {
        RedditCompanyScraper scraper = new RedditCompanyScraper();
        for (Company company : scraper.fetch()) {
            System.out.println(company.getName() + " @ " + company.getWebsite());
        }
    }
}
