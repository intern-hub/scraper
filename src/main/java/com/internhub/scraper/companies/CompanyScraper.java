package com.internhub.scraper.companies;

import com.internhub.scraper.googlesearch.Searcher;
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

import java.net.URL;
import java.util.*;

public class CompanyScraper {
    private static final String REDDIT_CLIENT_ID = "WlcJSnReHivl-Q";
    private static final String REDDIT_CLIENT_SECRET = "ohxqq8KaDWMlAsWHPm-u-lOCDUw";
    private static final String REDDIT_COMPANY_LABEL = "Company/Industry: ";

    private RedditClient m_reddit;
    private Searcher m_google;
    private Map<String, String> m_verified;

    public CompanyScraper() {
        // Set up Reddit client
        UserAgent userAgent = new UserAgent("bot", "com.internhub.scraper", "1.0.0", "dmhacker");
        NetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
        this.m_reddit = OAuthHelper.automatic(networkAdapter, Credentials.userless(REDDIT_CLIENT_ID, REDDIT_CLIENT_SECRET, UUID.randomUUID()));

        this.m_google = new Searcher();
        this.m_verified = new HashMap<>();
    }

    public Set<Company> fetch() {
        Set<Company> companies = new HashSet<>();
        scrapeReddit(companies);
        return companies;
    }

    private String verify(String potentialCompany) {
        if (m_verified.containsKey(potentialCompany)) {
            return m_verified.get(potentialCompany);
        }

        String result = verifyOnGoogle(potentialCompany);
        m_verified.put(potentialCompany, result);
        return result;
    }

    private String verifyOnGoogle(String potentialCompany) {
        // Perform one Google search for "[COMPANY] careers website" and get first link
        // Perform another Google search for "[COMPANY] internship apply" and get first link
        List<URL> websiteSearch = m_google.search(potentialCompany + " careers website", 1);
        List<URL> internSearch = m_google.search(potentialCompany + " internship apply", 1);
        if (websiteSearch.isEmpty() || internSearch.isEmpty()) {
            return null;
        }

        //  Make sure both links come from the same domain
        URL careersWebsite = websiteSearch.get(0);
        URL internWebsite = internSearch.get(0);
        if (!careersWebsite.getHost().equals(internWebsite.getHost())) {
            return null;
        }

        //  Make sure both links aren't from Wikipedia, Glassdoor, or Indeed
        String websiteRoot = careersWebsite.getHost();
        if (websiteRoot.contains("wikipedia") || websiteRoot.contains("glassdoor") || websiteRoot.contains("indeed")) {
            return null;
        }

        // TODO: Look at website for proper company name
        return potentialCompany;
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
            String inputCompany = commentClean.substring(beginIndex, endIndex).trim();
            String company = verify(inputCompany);
            if (company != null) {
                companies.add(new Company(company));
            }
            companyIndex = commentClean.indexOf(REDDIT_COMPANY_LABEL, companyIndex + 1);
        }
    }
}
