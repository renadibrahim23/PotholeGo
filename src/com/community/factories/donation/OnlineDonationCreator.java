package com.community.factories.donation;

import java.util.Map;

import com.community.model.donation.Campaign;
import com.community.model.donation.Donation;
import com.community.model.donation.OnlineDonation;
import com.community.model.user.User;

public class OnlineDonationCreator {
    public Donation createDonation(Map<String, Object> data){
        User donor = (User) data.get("donor");
        Campaign campaign = (Campaign) data.get("campaign");
        String transactionId = (String) data.get("transactionId");
        double amount = (Double) data.get("amount");
        return new OnlineDonation(donor, campaign, transactionId, amount);
    }
}
