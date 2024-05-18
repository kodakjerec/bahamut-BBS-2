package com.kota.Bahamut.DataModels;


public class ShortenUrl {
    String shorten_url = "";
    String title = "";
    String url = "";
    String description = "";

    public void setShorten_url(String shorten_url) {
        this.shorten_url = shorten_url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShorten_url() {
        return shorten_url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }
}
