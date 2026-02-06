package com.example.demo.bens;

import java.util.List;

public class MoodleCourseRequest {

    private List<MoodleCourse> courses;

    public MoodleCourseRequest() {
    }

    public MoodleCourseRequest(List<MoodleCourse> courses) {
        this.courses = courses;
    }

    public List<MoodleCourse> getCourses() {
        return courses;
    }

    public void setCourses(List<MoodleCourse> courses) {
        this.courses = courses;
    }
}
