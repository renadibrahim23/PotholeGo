package com.community.strategy.urgency;

import com.community.model.report.Report;

public class ReportCountStrategy implements UrgencyStrategy {
    @Override
    public int calculate(Report r) {
        return r.getUrgencyScore() + 10 * r.getDuplicates().size();
    }
}
