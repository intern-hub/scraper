# scraper

This repository contains our scraping code.
The scraper pulls companies from [/r/cscareerquestions](https://reddit.com/r/cscareerquestions)
and crawls the web for positions relating to those companies.
We are also actively looking for other reliable sources
of company names.

## Installation

Make sure the following dependencies have been installed on your system.

* Docker

You will also need to acquire a valid **hibernate.cfg.xml** file and place it
in the **src/main/resources** folder. This file is responsible for
providing SQL database connection details, enabling the scraper to
read and write companies/positions.

## Usage

The following commands are assumed to be run from the root of the repository directory.

To fetch all companies and save them to the database, ignoring duplicates, use:

```
scripts/start_docker.sh -c
```

To fetch all positions for each company in the database
and then save them to the database, ignoring duplicates, use:

```
scripts/start_docker.sh -p
```
