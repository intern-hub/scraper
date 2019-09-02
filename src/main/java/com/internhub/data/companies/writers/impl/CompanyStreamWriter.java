package com.internhub.data.companies.writers.impl;

import com.internhub.data.companies.writers.ICompanyWriter;
import com.internhub.data.models.Company;

import java.io.PrintStream;
import java.util.List;

public class CompanyStreamWriter implements ICompanyWriter {
    private PrintStream mStream;

    public CompanyStreamWriter(PrintStream stream) {
        mStream = stream;
    }

    @Override
    public synchronized void save(Company newCompany) {
        mStream.println("Scraped company: " + newCompany);
    }

    @Override
    public synchronized void save(List<Company> newCompanies) {
        mStream.println("Scraped " + newCompanies.size() + " companies: " + newCompanies);
    }
}
