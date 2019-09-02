package com.internhub.data.companies.writers;

import com.internhub.data.models.Company;

import java.util.List;

public interface ICompanyWriter {
    void save(Company newCompany);
    void save(List<Company> newCompanies);
}
