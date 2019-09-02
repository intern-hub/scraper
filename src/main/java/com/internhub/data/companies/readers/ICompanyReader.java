package com.internhub.data.companies.readers;

import com.internhub.data.models.Company;

import java.util.List;

public interface ICompanyReader {
    List<Company> getAll();
    List<Company> getByName(String name);
}
