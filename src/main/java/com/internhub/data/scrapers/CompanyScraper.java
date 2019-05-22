package com.internhub.data.scrapers;

import com.internhub.data.models.Company;

import java.util.List;

public interface CompanyScraper {
    List<Company> fetch();
}
