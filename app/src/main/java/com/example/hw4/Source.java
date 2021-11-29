package com.example.hw4;

public class Source {

    private String id;
    private String name;
    private String url;
    private String category;
    private String country;
    private String language;

    public Source(String id, String name, String url, String category, String country, String language) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.category = category;
        this.country = country;
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public String getCountry() { return country; }

    public String getLanguage() { return language; }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCountry(String country) { this.country = country; }

    public void setLanguage(String country) { this.language = language; }

    @Override
    public String toString() {
        return name;
    }
}
