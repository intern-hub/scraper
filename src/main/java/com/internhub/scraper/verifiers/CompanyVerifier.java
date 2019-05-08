package com.internhub.scraper.verifiers;

import com.internhub.scraper.search.GoogleSearch;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyVerifier {
    private GoogleSearch m_google;
    private Map<String, Boolean> m_verified;

    public CompanyVerifier() {
        this.m_google = new GoogleSearch();
        this.m_verified = new HashMap<>();
    }

    public boolean isCompanyValid(String companyName) {
        // Use cached result if we need for previous verifications
        if (m_verified.containsKey(companyName)) {
            return m_verified.get(companyName);
        }

        // If the result isn't cached, verify it on Google and then cache result
        boolean verified = isCompanyWellFormed(companyName) && isCompanySearchable(companyName);
        m_verified.put(companyName, verified);

        // TODO: properly log result
        if (verified)
            System.out.println(companyName + " has been successfully verified.");
        else
            System.out.println("Ignoring " + companyName + " ...");

        return verified;
    }

    public boolean isCompanyWellFormed(String companyName) {
        // Abbreviations for companies can be discarded on the basis of being too short
        if (companyName.length() <= 3) {
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

    public boolean isCompanySearchable(String companyName) {
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
            return false;
        }

        //  Make sure both links have the company name in them
        URL careersWebsite = websiteSearch.get(0);
        URL internWebsite = internSearch.get(0);
        String truncatedName = companyName
                .toLowerCase()
                .replace(" ", "");
        if (!careersWebsite.getHost().contains(truncatedName) ||
                !internWebsite.getHost().contains(truncatedName)) {
            return false;
        }

        //  Make sure both links aren't from generic websites or job listings
        // (e.g. the companies must have their own careers website)
        String websiteRoot = careersWebsite.getHost();
        if (websiteRoot.contains("wikipedia.org") ||
                websiteRoot.contains("glassdoor.com") ||
                websiteRoot.contains("angel.co") ||
                websiteRoot.contains("lever.co") ||
                websiteRoot.contains("indeed.com")) {
            return false;
        }

        return true;
    }
}
