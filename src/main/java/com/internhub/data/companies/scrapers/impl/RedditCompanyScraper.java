package com.internhub.data.companies.scrapers.impl;

import com.internhub.data.companies.scrapers.ICompanyScraper;
import com.internhub.data.models.Company;
import com.internhub.data.companies.verifiers.CompanyVerifier;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RedditCompanyScraper implements ICompanyScraper {
    private static final String REDDIT_CLIENT_ID = "Wd8K7RKBs4Z-5g";
    private static final String REDDIT_CLIENT_SECRET = "OH9Bl_aZ8fQ2NXW66g77e9dNiNg";
    private static final String REDDIT_COMPANY_LABEL = "Company/Industry: ";

    private static final Logger logger = LoggerFactory.getLogger(RedditCompanyScraper.class);

    private Map<URL, Company> m_uniqueResults;
    private RedditClient m_reddit;
    private CompanyVerifier m_verifier;

    public RedditCompanyScraper() {
        UserAgent userAgent = new UserAgent("bot", "com.internhub", "1.0.0", "internhub");
        NetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
        this.m_reddit = OAuthHelper.automatic(networkAdapter, Credentials.userless(REDDIT_CLIENT_ID, REDDIT_CLIENT_SECRET, UUID.randomUUID()));
        this.m_verifier = new CompanyVerifier();
        this.m_uniqueResults = new HashMap<>();
    }

    @Override
    public List<Company> fetch() {
        scrape();
        List<Company> aggregated = new ArrayList<>(m_uniqueResults.values());
        m_uniqueResults.clear();
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

            try {
                // Use our verification utilities to make sure we have a valid company
                if (m_verifier.isCompanyValid(companyName)) {
                    // Use the company's website to prune out duplicates
                    URL companyWebsite = m_verifier.getCompanyWebsite(companyName);
                    Company existing = m_uniqueResults.get(companyWebsite);
                    // We always settle for the company name that minimizes edit
                    // distance between it and the domain of the careers website
                    LevenshteinDistance editDistance = LevenshteinDistance.getDefaultInstance();
                    if (existing == null ||
                            editDistance.apply(companyName.toLowerCase(), companyWebsite.getHost()) <
                                    editDistance.apply(existing.getName().toLowerCase(), companyWebsite.getHost())) {

                        // Use our verification tools to get additional information about the company
                        String[] companyUpdates = m_verifier.getCompanyNameAndDescription(companyName, companyWebsite);
                        companyName = companyUpdates[0];
                        String companyDescription = companyUpdates[1];

                        // Create a transient company object that can be inserted into the database
                        Company company = new Company();
                        company.setName(companyName);
                        company.setDescription(companyDescription);
                        company.setWebsite(companyWebsite.toString());
                        company.setPopularity(0);

                        m_uniqueResults.put(companyWebsite, company);
                        if (existing == null) {
                            logger.info(String.format(
                                    "[%s] Identified! Website is %s.", companyName, companyWebsite));
                            logger.info(String.format("[%s] Description is: %s", companyName, companyDescription));
                        }
                        else {
                            logger.info(String.format(
                                    "[%s] Replacing current company %s.", companyName, existing.getName()));
                        }
                    }
                    else {
                        logger.info(String.format(
                                "[%s] Skipping. Already exists as %s.", companyName, existing.getName()));
                    }
                }
                else {
                    logger.info(String.format(
                            "[%s] Skipping. Unknown company.", companyName));
                }
            } catch (IOException ex) {
                logger.error("Verification is failing.", ex);
            }

            companyIndex = commentClean.indexOf(REDDIT_COMPANY_LABEL, companyIndex + 1);
        }
    }
}
