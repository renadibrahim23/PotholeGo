package com.community.strategy.payment;

public record PaymentResult(boolean success, String transactionId, String message) {
}
