package com.example.demo.bens;

import java.util.List;

public class CourseSyncRequest {

    private String groupId;
    private CourseRequest courseData;
    private List<StudentData> students;
    private List<TeacherData> teachers;
    private String objectives;
    private String content;
    private String methodology;
    private String evaluation;
    private String bibliographyDescription;

    public CourseSyncRequest() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public CourseRequest getCourseData() {
        return courseData;
    }

    public void setCourseData(CourseRequest courseData) {
        this.courseData = courseData;
    }

    public List<StudentData> getStudents() {
        return students;
    }

    public void setStudents(List<StudentData> students) {
        this.students = students;
    }

    public List<TeacherData> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<TeacherData> teachers) {
        this.teachers = teachers;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMethodology() {
        return methodology;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public String getBibliographyDescription() {
        return bibliographyDescription;
    }

    public void setBibliographyDescription(String bibliographyDescription) {
        this.bibliographyDescription = bibliographyDescription;
    }

    @Override
    public String toString() {
        return "CourseSyncRequest{" +
                "groupId='" + groupId + '\'' +
                ", courseData=" + courseData +
                ", students=" + (students != null ? students.size() : 0) + " students" +
                ", teachers=" + (teachers != null ? teachers.size() : 0) + " teachers" +
                '}';
    }

    public static class StudentData {
        private String personId;
        private String username;
        private String name;
        private String shortname;
        private String email;

        public StudentData() {
        }

        public String getPersonId() {
            return personId;
        }

        public void setPersonId(String personId) {
            this.personId = personId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getShortname() {
            return shortname;
        }

        public void setShortname(String shortname) {
            this.shortname = shortname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "StudentData{" +
                    "personId='" + personId + '\'' +
                    ", username='" + username + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static class TeacherData {
        private String personId;
        private String username;
        private String name;
        private String shortname;
        private String email;

        public TeacherData() {
        }

        public String getPersonId() {
            return personId;
        }

        public void setPersonId(String personId) {
            this.personId = personId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getShortname() {
            return shortname;
        }

        public void setShortname(String shortname) {
            this.shortname = shortname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "TeacherData{" +
                    "personId='" + personId + '\'' +
                    ", username='" + username + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
