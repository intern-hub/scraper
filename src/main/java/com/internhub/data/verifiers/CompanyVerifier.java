package com.internhub.data.verifiers;

import com.internhub.data.search.GoogleSearch;
import fastily.jwiki.core.Wiki;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyVerifier {
    private static final String CAREERS_SEARCH_TERM = "%s careers website";
    private static final String INTERNSHIP_SEARCH_TERM = "%s internship apply";
    private static final String WIKI_SEARCH_TERM = "%s company";
    private static final int MAX_WIKI_PAGES = 4;

    private GoogleSearch m_google;
    private Map<String, URL> m_searchCache;
    private Wiki m_wiki;

    public CompanyVerifier() {
        this.m_google = new GoogleSearch();
        this.m_searchCache = new HashMap<>();
        this.m_wiki = new Wiki("en.wikipedia.org");
    }

    public boolean isCompanyValid(String companyName) throws IOException {
        // Conditions for verification: well-formed name, proper careers website
        return isCompanyWellFormed(companyName) && getCompanyWebsite(companyName) != null;
    }

    private boolean isCompanyWellFormed(String companyName) {
        // Abbreviations for companies can be discarded on the basis of being too short
        if (companyName.length() < 3) {
            return false;
        }

        // Company names must less than or equal to 3 words
        if (companyName.chars().filter(Character::isWhitespace).count() > 2) {
            return false;
        }

        // Company names with punctuation can be discarded
        if (companyName.contains("/") ||
                companyName.contains(",") ||
                companyName.contains("\\") ||
                companyName.contains("[") ||
                companyName.contains("]") ||
                companyName.contains("(") ||
                companyName.contains(")")) {
            return false;
        }

        // Throw out some obvious 'companies' that are getting by the Google process
        if (companyName.toLowerCase().contains("decacorn") ||
                companyName.toLowerCase().contains("unicorn")) {
            return false;
        }

        return true;
    }

    public URL getCompanyWebsite(String companyName) throws IOException {
        // Cache search results so we don't have to wait unnecessarily
        if (m_searchCache.containsKey(companyName)) {
            return m_searchCache.get(companyName);
        }

        // Perform one Google search for "[COMPANY] careers website" and get first link
        // Perform another Google search for "[COMPANY] internship apply" and get first link
        List<URL> websiteSearch = m_google.search(String.format(CAREERS_SEARCH_TERM, companyName), 1);
        List<URL> internSearch = m_google.search(String.format(INTERNSHIP_SEARCH_TERM, companyName), 1);

        // Any empty searches indicate that nothing can be found for that company
        if (websiteSearch.isEmpty() || internSearch.isEmpty()) {
            m_searchCache.put(companyName, null);
            return null;
        }

        //  Make sure both links have the company name in them
        URL careersWebsite = websiteSearch.get(0);
        URL internWebsite = internSearch.get(0);
        String truncatedName = companyName
                .toLowerCase()
                .replace(" ", "")
                .replace(".", "")
                .replace("&", "");
        if (!careersWebsite.getHost().contains(truncatedName) ||
                !internWebsite.getHost().contains(truncatedName)) {
            // If this fails, it is still possible that the company
            // is valid, if the websites have the same host
            if (!careersWebsite.getHost().equals(internWebsite.getHost())) {
                m_searchCache.put(companyName, null);
                return null;
            }
        }

        //  Make sure both links aren't from generic websites or job listings
        // (e.g. the companies must have their own careers website)
        String websiteRoot = careersWebsite.getHost();
        if (websiteRoot.contains(".org") ||
                websiteRoot.contains("quantstart.com") ||
                websiteRoot.contains("careershift.com") ||
                websiteRoot.contains("financesonline.com") ||
                websiteRoot.contains("glassdoor.com") ||
                websiteRoot.contains("angel.co") ||
                websiteRoot.contains("lever.co") ||
                websiteRoot.contains("indeed.com")) {
            m_searchCache.put(companyName, null);
            return null;
        }

        m_searchCache.put(companyName, careersWebsite);
        return careersWebsite;
    }

    public String[] getCompanyNameAndDescription(String companyName, URL companyWebsite) {
        LevenshteinDistance editDistance = LevenshteinDistance.getDefaultInstance();
        String companyDescription = "No description could be found for this company.";
        int descriptionScore = -1;
        int bonusScore = MAX_WIKI_PAGES;

        // Use Wikipedia to search for company descriptions
        // Prioritize technology and financial company pages
        for (String pageTitle : m_wiki.search(String.format(WIKI_SEARCH_TERM, companyName), MAX_WIKI_PAGES)) {
            String page = m_wiki.getTextExtract(pageTitle);
            String fullPage = m_wiki.getPageText(pageTitle);
            String pageLower = page.toLowerCase();
            if (fullPage.contains("{{Infobox") && pageLower.contains("company")) {
                int score = bonusScore;
                if (pageLower.contains(companyName.toLowerCase())) {
                    score += 100;
                }
                if (pageLower.contains("tech")) {
                    score += 20;
                }
                if (pageLower.contains("financial") || pageLower.contains("investment")) {
                    score += 10;
                }
                if (score > descriptionScore) {
                    companyDescription = page;
                    descriptionScore = score;

                    // Page title might actually have proper capitalization
                    if (editDistance.apply(pageTitle.toLowerCase(), companyWebsite.getHost()) <=
                            editDistance.apply(companyName.toLowerCase(), companyWebsite.getHost()) &&
                            !pageTitle.contains("("))
                    {
                        companyName = pageTitle;
                    }
                }
                bonusScore--;
            }
        }
        return new String[] {companyName, companyDescription};
    }
}
