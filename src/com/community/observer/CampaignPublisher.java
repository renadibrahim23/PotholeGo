package com.community.observer;

import java.util.ArrayList;
import java.util.List;

public class CampaignPublisher implements Publisher{
    private List<Subscriber> donors = new ArrayList<>();

    @Override 
    public void subscribe(Subscriber s){
        donors.add(s);
    }

    @Override
    public void unsubscribe(Subscriber s){
        donors.remove(s);
    }

    @Override
    public void notifyDonors(){
        for(Subscriber s : donors){
            s.update();
        }
    }
}
