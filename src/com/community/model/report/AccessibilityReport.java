package com.community.model.report;

import com.community.model.lookup.ReportStatus;
import com.community.model.lookup.IssueCategory;
import com.community.model.lookup.District;
import com.community.model.user.Resident;

public class AccessibilityReport extends Report {
    private String facilityName;

    public AccessibilityReport(int id, String title, String description, double lat, double lng,
                               String photoUrl, ReportStatus status, IssueCategory category,
                               District district, Resident reporter, String facilityName) {
        super(id, title, description, lat, lng, photoUrl, status, category, district, reporter);
        this.facilityName = facilityName;
    }

    @Override
    public int getUrgencyScore() {
       // return category.getBaseSeverity() + 10;
        return 10; //placeholder
    }

    @Override
    public String getDescription() {
        return description + " (facility: " + facilityName + ")";
    }
}
