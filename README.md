# scraper

This repository contains our scraping code. 
It pulls companies from /r/cscareerquestions and crawls the web for positions relating to those companies.

## Installation

Make sure the following dependencies have been installed on your system.

* Google Chrome/Chromium v74
* Java 8
* Gradle

Afterwards, clone this repository and `cd` in.

## Usage

To fetch all companies and save them to the database, ignoring duplicates, use:

`gradle run --args=-c`

To fetch all positions for each company in the database and then save them to the database, ignoring duplicates, use:

`gradle run --args=-c`
