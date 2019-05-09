package com.internhub.data.models;

import javax.persistence.*;

@Entity
@Table(name = "POSITIONS")
public class Position {
    @GeneratedValue
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "link")
    private String link;
    @ManyToOne
    @JoinColumn(name = "companyId")
    private Company company;
    @Column(name = "title")
    private String title;
    @Column(name = "season")
    @Enumerated(EnumType.ORDINAL)
    private Season season;
    @Column(name = "year")
    private int year;
    @Column(name = "degree")
    private String degree;
    @Column(name = "location")
    private String location;

    public Position() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
