package com.example.demo.bens;

public class CourseRequest {

    private String fullname;
    private String shortname;
    private Integer categoryid;
    private String summary;

    public CourseRequest() {
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public Integer getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(Integer categoryid) {
        this.categoryid = categoryid;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "CourseRequest{" +
                "fullname='" + fullname + '\'' +
                ", shortname='" + shortname + '\'' +
                ", categoryid=" + categoryid +
                ", summary='" + summary + '\'' +
                '}';
    }
}
