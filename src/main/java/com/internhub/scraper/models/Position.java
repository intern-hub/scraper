package com.internhub.scraper.models;

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

    private Company company;

    public Position() {}
}
