package com.community.model.donation;

import java.util.ArrayList;
import java.util.List;

import com.community.model.report.Report;

public class Campaign {
    // MORSI TODO
    // fetch the last valid id from postgres db when its implemented
    // instead of using a runtime counter
    private static int serialCounter = 0;

    private int id;
    private String title;
    private double goalAmount;
    private double raisedAmount;
    private Report linkedReport;
    private List<Donation> donations = new ArrayList<Donation>();
    
    public Campaign(String title, double goalAmount, Report linkedReport) {
        this.id = serialCounter++;
        this.goalAmount = goalAmount;
        this.raisedAmount = 0;
        this.linkedReport = linkedReport; 
    }

    public void addDonation(Donation d){
        donations.add(d);
        raisedAmount += d.getAmount();
        d.campaign = this;
    } 

    public double getProgress(){
        return (raisedAmount / goalAmount) * 100;
    }

    public int getId() {
        return id;
    }

    public static int getSerialCounter() {
        return serialCounter;
    }

    public String getTitle() {
        return title;
    }

    public double getGoalAmount() {
        return goalAmount;
    }

    public double getRaisedAmount() {
        return raisedAmount;
    }

    public Report getLinkedReport() {
        return linkedReport;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static void setSerialCounter(int serialCounter) {
        Campaign.serialCounter = serialCounter;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
    }

    public void setRaisedAmount(double raisedAmount) {
        this.raisedAmount = raisedAmount;
    }

    public void setLinkedReport(Report linkedReport) {
        this.linkedReport = linkedReport;
    }

    public List<Donation> getDonations() {
        return donations;
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
    }
}
