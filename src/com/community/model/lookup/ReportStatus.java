package com.community.model.lookup;

public class ReportStatus {
    private int id;
    private String name; // e.g. Submitted, Verified, Assigned, Fixed

    public ReportStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

