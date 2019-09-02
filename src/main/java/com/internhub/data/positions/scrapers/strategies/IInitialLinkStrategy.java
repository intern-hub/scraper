package com.internhub.data.positions.scrapers.strategies;

import com.internhub.data.models.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface IInitialLinkStrategy {
    Logger logger = LoggerFactory.getLogger(IInitialLinkStrategy.class);

    /**
     * Gathers and returns a list of initial links for the given company.
     * The children of these links are explored during the scraping process,
     * as each child has the potential to yield a valid internship position.
     *
     * @param company The company to gather initial links for
     * @return A list of links for the company
     */
    List<String> getLinks(Company company);
}
