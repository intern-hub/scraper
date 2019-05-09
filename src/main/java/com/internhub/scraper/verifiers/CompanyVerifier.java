package com.internhub.scraper.verifiers;

import com.internhub.scraper.search.GoogleSearch;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyVerifier {
    private GoogleSearch m_google;
    private Map<String, URL> m_search_cache;

    public CompanyVerifier() {
        this.m_google = new GoogleSearch();
        this.m_search_cache = new HashMap<>();
    }

    public boolean isCompanyValid(String companyName) {
        // Conditions for verification: well-formed name, proper careers website
        return isCompanyWellFormed(companyName) && getCompanyWebsite(companyName) != null;
    }

    private boolean isCompanyWellFormed(String companyName) {
        // Abbreviations for companies can be discarded on the basis of being too short
        if (companyName.length() < 3) {
            return false;
        }

        // Company names with punctuation can be discarded
        if (companyName.contains("/") ||
                companyName.contains(",") ||
                companyName.contains("(") ||
                companyName.contains(")")) {
            return false;
        }

        // Throw out some obvious 'companies' that are getting by the Google process
        if (companyName.equalsIgnoreCase("decacorn") ||
                companyName.equalsIgnoreCase("unicorn")) {
            return false;
        }

        return true;
    }

    public URL getCompanyWebsite(String companyName) {
        // Cache search results so we don't have to wait unnecessarily
        if (m_search_cache.containsKey(companyName)) {
            return m_search_cache.get(companyName);
        }

        // Perform one Google search for "[COMPANY] careers website" and get first link
        // Perform another Google search for "[COMPANY] internship apply" and get first link
        List<URL> websiteSearch;
        List<URL> internSearch;
        try {
            websiteSearch = m_google.search(companyName + " careers website", 1);
            internSearch = m_google.search(companyName + " internship apply", 1);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        // Any empty searches indicate that nothing can be found for that company
        if (websiteSearch.isEmpty() || internSearch.isEmpty()) {
            m_search_cache.put(companyName, null);
            return null;
        }

        //  Make sure both links have the company name in them
        URL careersWebsite = websiteSearch.get(0);
        URL internWebsite = internSearch.get(0);
        String truncatedName = companyName
                .toLowerCase()
                .replace(" ", "");
        if (!careersWebsite.getHost().contains(truncatedName) ||
                !internWebsite.getHost().contains(truncatedName)) {
            // If this fails, it is still possible that the company
            // is valid, if the websites have the same host
            if (!careersWebsite.getHost().equals(internWebsite.getHost())) {
                m_search_cache.put(companyName, null);
                return null;
            }
        }

        //  Make sure both links aren't from generic websites or job listings
        // (e.g. the companies must have their own careers website)
        String websiteRoot = careersWebsite.getHost();
        if (websiteRoot.contains("wikipedia.org") ||
                websiteRoot.contains("glassdoor.com") ||
                websiteRoot.contains("angel.co") ||
                websiteRoot.contains("lever.co") ||
                websiteRoot.contains("indeed.com")) {
            m_search_cache.put(companyName, null);
            return null;
        }

        m_search_cache.put(companyName, careersWebsite);
        return careersWebsite;
    }
}
