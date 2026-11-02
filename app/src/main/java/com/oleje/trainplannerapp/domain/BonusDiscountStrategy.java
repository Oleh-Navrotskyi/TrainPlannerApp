package com.oleje.trainplannerapp.domain;

/**
 * Strategy pattern interface for dynamic bonus discounts (Certificates, Promo Codes).
 */
public interface BonusDiscountStrategy {
    String getId();
    String getTitle();
    double calculateSavings(double currentTicketPrice);
}
