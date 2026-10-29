package com.community.strategy.urgency;

import com.community.model.report.Report;

public interface UrgencyStrategy {
    int calculate(Report r);
}
