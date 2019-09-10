package com.internhub.data.positions.scrapers.strategies.impl;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.positions.scrapers.strategies.IInitialLinkStrategy;
import com.internhub.data.search.GoogleSearch;
import com.internhub.data.util.ScraperUtils;

import java.net.URL;
import java.util.List;

public class GoogleInitialLinkStrategy implements IInitialLinkStrategy {
    private static final String INTERNSHIP_SEARCH_TERM = "%s internship apply";
    private static final int MAX_GOOGLE_LINKS = 4;

    private GoogleSearch mGoogle;

    public GoogleInitialLinkStrategy() {
        mGoogle = new GoogleSearch();
    }

    @Override
    public List<String> getLinks(Company company) {
        // Take the first ${MAX_GOOGLE_LINKS} links that Google finds
        List<String> googled = Lists.newArrayList();
        String searchTerm = String.format(INTERNSHIP_SEARCH_TERM, company.getName());
        for (URL url : mGoogle.search(searchTerm, MAX_GOOGLE_LINKS)) {
            if (ScraperUtils.isInCompanyDomain(url, company.getWebsiteURL(), company.getAbbreviation())) {
                String link = ScraperUtils.fixLink(url.toString());
                googled.add(link);
            }
        }

        if (googled.isEmpty()) {
            logger.warn(String.format("No initial links found for '%s'.", company.getName()));
        }
        else {
            logger.info(String.format("Found %d initial links for '%s'.", googled.size(), company.getName()));
        }

        return googled;
    }
}
