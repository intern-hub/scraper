package com.internhub.data.positions.scrapers.strategies;

import com.internhub.data.models.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface InitialLinkStrategy {
    Logger logger = LoggerFactory.getLogger(InitialLinkStrategy.class);

    List<String> fetchInitialLinks(Company company);
}
