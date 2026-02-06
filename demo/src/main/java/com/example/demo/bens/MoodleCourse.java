package com.example.demo.bens;

import java.util.List;

public class MoodleCourse {

    private String fullname;
    private String shortname;
    private Integer categoryid;
    private String idnumber;
    private String summary;
    private Integer summaryformat;
    private String format;
    private Integer showgrades;
    private Integer newsitems;
    private Integer startdate;
    private Integer enddate;
    private Integer numsections;
    private Integer maxbytes;
    private Integer showreports;
    private Integer visible;
    private Integer hiddensections;
    private Integer groupmode;
    private Integer groupmodeforce;
    private Integer defaultgroupingid;
    private Integer enablecompletion;
    private Integer completionnotify;
    private String lang;
    private String forcetheme;
    private List<MoodleCourseFormatOption> courseformatoptions;
    private List<MoodleCustomField> customfields;

    public MoodleCourse() {
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

    public String getIdnumber() {
        return idnumber;
    }

    public void setIdnumber(String idnumber) {
        this.idnumber = idnumber;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getSummaryformat() {
        return summaryformat;
    }

    public void setSummaryformat(Integer summaryformat) {
        this.summaryformat = summaryformat;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getShowgrades() {
        return showgrades;
    }

    public void setShowgrades(Integer showgrades) {
        this.showgrades = showgrades;
    }

    public Integer getNewsitems() {
        return newsitems;
    }

    public void setNewsitems(Integer newsitems) {
        this.newsitems = newsitems;
    }

    public Integer getStartdate() {
        return startdate;
    }

    public void setStartdate(Integer startdate) {
        this.startdate = startdate;
    }

    public Integer getEnddate() {
        return enddate;
    }

    public void setEnddate(Integer enddate) {
        this.enddate = enddate;
    }

    public Integer getNumsections() {
        return numsections;
    }

    public void setNumsections(Integer numsections) {
        this.numsections = numsections;
    }

    public Integer getMaxbytes() {
        return maxbytes;
    }

    public void setMaxbytes(Integer maxbytes) {
        this.maxbytes = maxbytes;
    }

    public Integer getShowreports() {
        return showreports;
    }

    public void setShowreports(Integer showreports) {
        this.showreports = showreports;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    public Integer getHiddensections() {
        return hiddensections;
    }

    public void setHiddensections(Integer hiddensections) {
        this.hiddensections = hiddensections;
    }

    public Integer getGroupmode() {
        return groupmode;
    }

    public void setGroupmode(Integer groupmode) {
        this.groupmode = groupmode;
    }

    public Integer getGroupmodeforce() {
        return groupmodeforce;
    }

    public void setGroupmodeforce(Integer groupmodeforce) {
        this.groupmodeforce = groupmodeforce;
    }

    public Integer getDefaultgroupingid() {
        return defaultgroupingid;
    }

    public void setDefaultgroupingid(Integer defaultgroupingid) {
        this.defaultgroupingid = defaultgroupingid;
    }

    public Integer getEnablecompletion() {
        return enablecompletion;
    }

    public void setEnablecompletion(Integer enablecompletion) {
        this.enablecompletion = enablecompletion;
    }

    public Integer getCompletionnotify() {
        return completionnotify;
    }

    public void setCompletionnotify(Integer completionnotify) {
        this.completionnotify = completionnotify;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getForcetheme() {
        return forcetheme;
    }

    public void setForcetheme(String forcetheme) {
        this.forcetheme = forcetheme;
    }

    public List<MoodleCourseFormatOption> getCourseformatoptions() {
        return courseformatoptions;
    }

    public void setCourseformatoptions(List<MoodleCourseFormatOption> courseformatoptions) {
        this.courseformatoptions = courseformatoptions;
    }

    public List<MoodleCustomField> getCustomfields() {
        return customfields;
    }

    public void setCustomfields(List<MoodleCustomField> customfields) {
        this.customfields = customfields;
    }
}
