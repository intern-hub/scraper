package com.internhub.scraper.scrapers;

import com.internhub.scraper.models.Company;
import com.internhub.scraper.search.GoogleSearch;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.SearchPaginator;
import net.dean.jraw.references.SubredditReference;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CompanyScraper {
    private static final String REDDIT_CLIENT_ID = "WlcJSnReHivl-Q";
    private static final String REDDIT_CLIENT_SECRET = "ohxqq8KaDWMlAsWHPm-u-lOCDUw";
    private static final String REDDIT_COMPANY_LABEL = "Company/Industry: ";

    private RedditClient m_reddit;
    private GoogleSearch m_google;
    private Map<String, Boolean> m_verified;

    public CompanyScraper() {
        // Set up tools for scraping
        UserAgent userAgent = new UserAgent("bot", "com.internhub.scraper", "1.0.0", "dmhacker");
        NetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
        this.m_reddit = OAuthHelper.automatic(networkAdapter, Credentials.userless(REDDIT_CLIENT_ID, REDDIT_CLIENT_SECRET, UUID.randomUUID()));

        // Set up tools for verification
        this.m_google = new GoogleSearch();
        this.m_verified = new HashMap<>();
    }

    public Set<Company> fetch() {
        Set<Company> companies = new HashSet<>();
        scrapeReddit(companies);
        return companies;
    }

    private boolean isCompanyValid(String companyName) {
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

    private boolean isCompanyWellFormed(String companyName) {
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

    private boolean isCompanySearchable(String companyName) {
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

    private void scrapeReddit(Set<Company> companies) {
        // Find all official salary sharing threads in /r/cscareerquestions
        SubredditReference subreddit = m_reddit.subreddit("cscareerquestions");
        SearchPaginator paginator = subreddit.search().query("Salary Sharing thread").build();
        for (Listing<Submission> submissions : paginator) {
            for (Submission submission : submissions) {
                if (submission.getTitle().startsWith("[OFFICIAL] Salary Sharing thread")) {
                    scrapeRedditSubmission(companies, submission.getId());
                }
            }
        }
    }

    private void scrapeRedditSubmission(Set<Company> companies, String submissionId) {
        // For each salary sharing thread/submission, find all top-level region comments
        RootCommentNode root = m_reddit.submission(submissionId).comments();
        for (CommentNode node : root.getReplies()) {
            String region = node.getSubject().getBody();
            // Stick to just looking at US-based comments
            if (region != null && region.contains("Region -") && region.contains("US")) {
                for (CommentNode child : ((List<CommentNode>) node.getReplies())) {
                    scrapeRedditComment(companies, child.getSubject().getBody());
                }
            }
        }
    }

    private void scrapeRedditComment(Set<Company> companies, String comment) {
        // Clean up the comment by removing formatting
        String commentClean = comment
                .replace("**", "") // Strip bolded words
                .replace(" *", "") // Strip bullet points
                .trim();

        int companyIndex = commentClean.indexOf(REDDIT_COMPANY_LABEL);
        while (companyIndex >= 0) {
            int beginIndex = companyIndex + REDDIT_COMPANY_LABEL.length();
            int endIndex = beginIndex;
            while (endIndex < commentClean.length() && commentClean.charAt(endIndex) != '\n') {
                endIndex++;
            }
            String companyName = commentClean.substring(beginIndex, endIndex).trim();
            if (isCompanyValid(companyName)) {
                companies.add(new Company(companyName));
            }
            companyIndex = commentClean.indexOf(REDDIT_COMPANY_LABEL, companyIndex + 1);
        }
    }
}
