package com.internhub.data.companies.scrapers;

import com.internhub.data.models.Company;

import java.util.List;

public interface ICompanyScraper {
    List<Company> fetch();
}
