package com.community.strategy.urgency;

import com.community.model.report.Report;

public class UrgencyEvaluator {
    private UrgencyStrategy strategy = new SafetyFirstStrategy(); // default

    public void setStrategy(UrgencyStrategy s) {
        this.strategy = s;
    }

    public int evaluate(Report r) {
        return strategy.calculate(r);
    }
}
