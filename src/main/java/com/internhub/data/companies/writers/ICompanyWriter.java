package com.internhub.data.companies.writers;

import com.internhub.data.models.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface ICompanyWriter {
    Logger logger = LoggerFactory.getLogger(ICompanyWriter.class);

    void save(Company newCompany);
    void save(List<Company> newCompanies);
}
