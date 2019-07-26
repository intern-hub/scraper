package com.internhub.data.positions.scrapers;

import com.google.common.collect.Lists;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.search.GoogleSearch;
import com.internhub.data.util.ScraperUtils;

import java.net.URL;
import java.util.*;

public abstract class GooglePositionScraper implements PositionScraper {
    private static final String INTERNSHIP_SEARCH_TERM = "%s internship apply";
    private static final int TOP_N_GOOGLE_LINKS = 4;

    private GoogleSearch mGoogle;

    public GooglePositionScraper() {
        mGoogle = new GoogleSearch();
    }

    public List<Position> fetch(Company company) {
        // Start by finding TOP_N_GOOGLE_LINKS links that Google finds for the company and adding to list
        List<String> googled = Lists.newArrayList();
        String searchTerm = String.format(INTERNSHIP_SEARCH_TERM, company.getName());
        for (URL url : mGoogle.search(searchTerm, TOP_N_GOOGLE_LINKS)) {
            if (ScraperUtils.isInCompanyDomain(url, company.getWebsiteURL(), company.getAbbreviation())) {
                String link = ScraperUtils.fixLink(url.toString());
                googled.add(link);
            }
        }

        // Defer the rest of the process to the implementation of this class
        return fetchWithInitialLinks(company, googled);
    }

    protected abstract List<Position> fetchWithInitialLinks(Company company, List<String> initialLinks);


}
