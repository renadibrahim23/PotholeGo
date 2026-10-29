package com.community.strategy.urgency;

import com.community.model.report.Report;

public class SafetyFirstStrategy implements UrgencyStrategy {
    @Override
    public int calculate(Report r) {
        // getUrgencyScore() already includes decorators (school/hospital/etc.)
        return r.getUrgencyScore() * 2;
    }
}