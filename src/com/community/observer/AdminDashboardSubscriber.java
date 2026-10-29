package com.community.observer;

import com.community.view.AdminDashboardView;

public class AdminDashboardSubscriber implements Subscriber {
    private final AdminDashboardView view;

    public AdminDashboardSubscriber(AdminDashboardView view) {
        this.view = view;
    }

    @Override
    public void update() {
        view.refresh();
    }
}
