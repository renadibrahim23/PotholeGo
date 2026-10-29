package com.community.observer;
import java.util.ArrayList;
import java.util.List;

public class ReportPublisher implements Publisher {
    private final List<Subscriber> subscribers = new ArrayList<>();

    @Override
    public void subscribe(Subscriber s) { subscribers.add(s); }

    @Override
    public void unsubscribe(Subscriber s) { subscribers.remove(s); }

    @Override
    public void notifyDonors() {
        for (Subscriber s : new ArrayList<>(subscribers)) s.update();
    }
}
