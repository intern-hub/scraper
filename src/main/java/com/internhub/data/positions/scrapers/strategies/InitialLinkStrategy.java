package com.internhub.data.positions.scrapers.strategies;

import com.internhub.data.models.Company;

import java.util.List;

public interface InitialLinkStrategy {
    public List<String> fetchInitialLinks(Company company);
}
