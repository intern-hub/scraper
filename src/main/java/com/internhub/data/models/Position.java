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
}
