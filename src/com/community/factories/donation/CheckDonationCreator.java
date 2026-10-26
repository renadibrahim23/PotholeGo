package com.community.factories.donation;

import java.util.Map;

import com.community.model.donation.Campaign;
import com.community.model.donation.CheckDonation;
import com.community.model.donation.Donation;
import com.community.model.user.User;

public class CheckDonationCreator {
    public Donation createDonation(Map<String, Object> data){
        User donor = (User) data.get("donor");
        Campaign campaign = (Campaign) data.get("campaign");
        String checkNumber = (String) data.get("checkNumber");
        String bankName = (String) data.get("bankName");
        double amount = (Double) data.get("amount");
        return new CheckDonation(donor, campaign, checkNumber, bankName, amount);
    }
}
