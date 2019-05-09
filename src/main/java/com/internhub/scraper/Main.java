package com.internhub.scraper;

import com.internhub.scraper.models.Company;
import com.internhub.scraper.scrapers.RedditCompanyScraper;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        RedditCompanyScraper scraper = new RedditCompanyScraper();
        for (Company company : scraper.fetch()) {
            System.out.println(company.getName());
        }
    }
}
