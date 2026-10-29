package com.community.strategy.payment;

import com.community.model.user.User;

public interface PaymentStrategy {
    PaymentResult pay(User payer, double amount);
}
