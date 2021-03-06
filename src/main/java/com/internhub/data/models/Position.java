package com.internhub.data.models;

import javax.persistence.*;

@Entity
@Table(name = "positions")
public class Position {
    @GeneratedValue
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "link")
    private String link;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    @Column(name = "title")
    private String title;
    @Column(name = "season")
    @Enumerated(EnumType.ORDINAL)
    private Season season;
    @Column(name = "year")
    private Integer year;
    @Column(name = "degree")
    private String degree;
    @Column(name = "location")
    private String location;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
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

    public String toString() {
        return String.format("\n" +
                        "Company: %s \n" +
                        "Position Title: %s \n" +
                        "Link: %s \n" +
                        "Season: %s \n" +
                        "Year: %s \n" +
                        "Degree: %s \n" +
                        "Location: %s \n", company.toString(), title, link,
                season, year, degree, location);
    }
}
