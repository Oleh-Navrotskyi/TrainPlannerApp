package com.oleje.trainplannerapp.domain;

public enum CitizenCategory {
    REGULAR("Повна вартість", 0.0),
    STUDENT("Студент", 0.50),           // 50% discount on base price
    PRIVILEGED("Пільговик", 1.00),      // 100% discount on suburban trains
    ADMIN("Адміністратор", 0.0);

    private final String displayName;
    private final double discountRate;

    CitizenCategory(String displayName, double discountRate) {
        this.displayName = displayName;
        this.discountRate = discountRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDiscountRate() {
        return discountRate;
    }
}
