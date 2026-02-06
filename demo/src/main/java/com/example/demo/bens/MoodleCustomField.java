package com.example.demo.bens;

public class MoodleCustomField {

    private String shortname;
    private String value;

    public MoodleCustomField() {
    }

    public MoodleCustomField(String shortname, String value) {
        this.shortname = shortname;
        this.value = value;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
