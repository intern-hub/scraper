package com.internhub.data.managers;

import javax.persistence.*;
import com.internhub.data.models.Company;
import com.internhub.data.models.Position;
import com.internhub.data.models.Season;

@Entity
@Table(name = "positions")
public class PositionEntity1 {
    @GeneratedValue
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "link")
    private String link;
    @ManyToOne
    @JoinColumn(name = "companyId")
    private long company;
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


    public PositionEntity1(long id, String name, String website) {
        this.setCompany(id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getCompany() {
        return company;
    }

    public void setCompany(long company_id) {
        this.company = company_id;
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