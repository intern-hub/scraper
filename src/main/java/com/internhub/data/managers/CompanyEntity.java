package com.internhub.data.managers;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.models.Season;

@Entity
public class CompanyEntity {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "website")
    private String website;

    public CompanyEntity() {}
    public CompanyEntity(String name) {
        this.name = name;
    }
}