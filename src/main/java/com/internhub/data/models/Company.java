package com.internhub.data.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.MalformedURLException;
import java.net.URL;

@Entity
@Table(name = "companies")
public class Company {
    private static final Logger logger = LoggerFactory.getLogger(Company.class);

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "website")
    private String website;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "popularity")
    private Integer popularity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return name.toLowerCase()
                .replace(" ", "")
                .replace(".", "")
                .replace("&", "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public URL getWebsiteURL() {
        try {
            return new URL(website);
        } catch (MalformedURLException e) {
            logger.error(String.format("Company %s (%d) has an invalid website: %s",
                    getName(), getId(), getWebsite()), e);
            return null;
        }
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public String toString() {
        return name;
    }
}
