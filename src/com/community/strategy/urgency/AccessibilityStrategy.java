package com.community.strategy.urgency;

import com.community.model.report.AccessibilityReport;
import com.community.model.report.Report;

public class AccessibilityStrategy implements UrgencyStrategy {
    @Override
    public int calculate(Report r) {
        int bonus = (r instanceof AccessibilityReport) ? 50 : 0;
        return r.getUrgencyScore() + bonus;
    }
}
