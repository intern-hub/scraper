package com.internhub.scraper.companies;

public class Company {
    private String m_name;

    public Company(String name) {
        this.m_name = name;
    }

    public String getName() {
        return m_name;
    }

    @Override
    public int hashCode() {
        return m_name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Company)) {
            return false;
        }
        return m_name.equals(((Company) o).m_name);
    }
}
