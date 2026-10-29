package com.community.model.report;
import com.community.model.lookup.ReportStatus;
import com.community.model.lookup.IssueCategory;
import com.community.model.lookup.District;
import com.community.model.user.Resident;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Report {
    protected int id;
    protected String title;
    protected String description;
    protected double lat;
    protected double lng;
    protected String photoUrl;
    protected LocalDate createdAt;
    protected ReportStatus status;
    protected IssueCategory category;
    protected District district;
    protected Resident reporter;
    protected List<Report> duplicates = new ArrayList<>();

    // used by concrete reports
    protected Report(int id, String title, String description, double lat, double lng,
                     String photoUrl, ReportStatus status, IssueCategory category,
                     District district, Resident reporter) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.photoUrl = photoUrl;
        this.createdAt = LocalDate.now();
        this.status = status;
        this.category = category;
        this.district = district;
        this.reporter = reporter;
    }

    // used by ReportDecorator (it delegates everything to the wrapped report)
    protected Report() { }

    public abstract int getUrgencyScore();
    public abstract String getDescription();

    public void changeStatus(ReportStatus s) { this.status = s; }

    /** Returns the original report underneath any decorators. */
    public Report unwrap() { return this; }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getPhotoUrl() { return photoUrl; }
    public LocalDate getCreatedAt() { return createdAt; }
    public ReportStatus getStatus() { return status; }
    public IssueCategory getCategory() { return category; }
    public District getDistrict() { return district; }
    public Resident getReporter() { return reporter; }
    public List<Report> getDuplicates() { return duplicates; }
    public void addDuplicate(Report r) { duplicates.add(r); }
}

