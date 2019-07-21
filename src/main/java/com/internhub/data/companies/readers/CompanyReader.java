package com.internhub.data.companies.readers;

import com.internhub.data.models.Company;

import java.util.List;

public interface CompanyReader {
    public List<Company> getAll();
    public List<Company> getByName(String name);
}
