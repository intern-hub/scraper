package com.internhub.data.positions.scrapers.strategies.impl;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.positions.scrapers.strategies.InitialLinkStrategy;
import com.internhub.data.search.GoogleSearch;
import com.internhub.data.util.ScraperUtils;

import java.net.URL;
import java.util.*;

public class GoogleInitialLinkStrategy implements InitialLinkStrategy {
    private static final String INTERNSHIP_SEARCH_TERM = "%s internship apply";
    private static final int TOP_N_GOOGLE_LINKS = 4;

    private GoogleSearch mGoogle;

    public GoogleInitialLinkStrategy() {
        mGoogle = new GoogleSearch();
    }

    @Override
    public List<String> fetchInitialLinks(Company company) {
        // Start by finding TOP_N_GOOGLE_LINKS links that Google finds for the company and adding to list
        List<String> googled = Lists.newArrayList();
        String searchTerm = String.format(INTERNSHIP_SEARCH_TERM, company.getName());
        for (URL url : mGoogle.search(searchTerm, TOP_N_GOOGLE_LINKS)) {
            if (ScraperUtils.isInCompanyDomain(url, company.getWebsiteURL(), company.getAbbreviation())) {
                String link = ScraperUtils.fixLink(url.toString());
                googled.add(link);
            }
        }
        return googled;
    }
}
