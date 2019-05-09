package com.internhub.scraper.scrapers;

import com.internhub.scraper.models.Company;
import com.internhub.scraper.verifiers.CompanyVerifier;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.SearchPaginator;
import net.dean.jraw.references.SubmissionReference;
import net.dean.jraw.references.SubredditReference;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.net.URL;
import java.util.*;

public class RedditCompanyScraper implements CompanyScraper {
    private static final String REDDIT_CLIENT_ID = "WlcJSnReHivl-Q";
    private static final String REDDIT_CLIENT_SECRET = "ohxqq8KaDWMlAsWHPm-u-lOCDUw";
    private static final String REDDIT_COMPANY_LABEL = "Company/Industry: ";

    private Map<URL, Company> m_unique_results;
    private RedditClient m_reddit;
    private CompanyVerifier m_verifier;

    public RedditCompanyScraper() {
        this.m_unique_results = new HashMap<>();

        // Set up tools for scraping
        UserAgent userAgent = new UserAgent("bot", "com.internhub.scraper", "1.0.0", "dmhacker");
        NetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
        this.m_reddit = OAuthHelper.automatic(networkAdapter, Credentials.userless(REDDIT_CLIENT_ID, REDDIT_CLIENT_SECRET, UUID.randomUUID()));

        // Set up tools for verification
        this.m_verifier = new CompanyVerifier();
    }

    @Override
    public List<Company> fetch() {
        scrape();
        List<Company> aggregated = new ArrayList<>(m_unique_results.values());
        m_unique_results.clear();
        return aggregated;
    }

    private void scrape() {
        // Find all official salary sharing threads in /r/cscareerquestions
        SubredditReference subreddit = m_reddit.subreddit("cscareerquestions");
        SearchPaginator paginator = subreddit.search().query("Salary Sharing thread").build();
        for (Listing<Submission> submissions : paginator) {
            for (Submission submission : submissions) {
                if (submission.getTitle().startsWith("[OFFICIAL] Salary Sharing thread")) {
                    scrapeSubmission(m_reddit.submission(submission.getId()));
                }
            }
        }
    }

    private void scrapeSubmission(SubmissionReference submission) {
        // For each salary sharing thread/submission, find all top-level region comments
        RootCommentNode root = submission.comments();
        for (CommentNode node : root.getReplies()) {
            String region = node.getSubject().getBody();
            // Stick to just looking at US-based comments
            if (region != null && region.contains("Region -") && region.contains("US")) {
                for (CommentNode child : ((List<CommentNode>) node.getReplies())) {
                    scrapeComment(child.getSubject().getBody());
                }
            }
        }
    }

    private void scrapeComment(String comment) {
        // Clean up the comment by removing formatting
        String commentClean = comment
                .replace("**", "") // Strip bold words
                .replace(" *", "") // Strip bullet points
                .trim();

        // Find all instances in the comment where we get a company label
        // This label indicates that a company name will appear right after it
        int companyIndex = commentClean.indexOf(REDDIT_COMPANY_LABEL);
        while (companyIndex >= 0) {
            // Isolate the company name
            int beginIndex = companyIndex + REDDIT_COMPANY_LABEL.length();
            int endIndex = beginIndex;
            while (endIndex < commentClean.length() &&
                    commentClean.charAt(endIndex) != '\n' &&
                    commentClean.charAt(endIndex) != '(') {
                endIndex++;
            }
            String companyName = commentClean.substring(beginIndex, endIndex).trim();

            // TODO: Add proper logging to this section

            // Use our verification utilities to make sure we have a valid company
            if (m_verifier.isCompanyValid(companyName)) {
                // Use the company's website to prune out duplicates
                URL companyWebsite = m_verifier.getCompanyWebsite(companyName);
                Company existing = m_unique_results.get(companyWebsite);
                // We always settle for the company name that minimizes edit
                // distance between it and the domain of the careers website
                LevenshteinDistance editDistance = LevenshteinDistance.getDefaultInstance();
                if (existing == null ||
                        editDistance.apply(companyName, companyWebsite.getHost()) <
                                editDistance.apply(existing.getName(), companyWebsite.getHost())) {
                    Company company = new Company();
                    company.setName(companyName);
                    company.setWebsite(companyWebsite.toString());
                    m_unique_results.put(companyWebsite, company);
                    if (existing == null) {
                        System.out.println("Identified new company (" + companyName +
                                ", " + companyWebsite + ").");
                    }
                    else{
                        System.out.println("Updating company (" + existing.getName() +
                                " => " + companyName + ", " + companyWebsite + ").");
                    }
                }
                else {
                    System.out.println("Company already verified: " + companyName + " => " + existing.getName());
                }
            }
            else {
                System.out.println("Company verification failed: " + companyName);
            }
            companyIndex = commentClean.indexOf(REDDIT_COMPANY_LABEL, companyIndex + 1);
        }
    }
}
