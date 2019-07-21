package com.internhub.data.companies.writers;

import com.internhub.data.models.Company;

import java.util.List;

public interface CompanyWriter {
    public void save(List<Company> newCompanies);
}
