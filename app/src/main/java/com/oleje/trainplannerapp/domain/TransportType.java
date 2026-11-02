package com.oleje.trainplannerapp.domain;

public enum TransportType {
    SUBURBAN("Приміський", 0.4),
    PLATZKART("Плацкарт", 1.0),
    COUPE("Купе", 1.6),
    INTERCITY("Інтерсіті", 2.2);

    private final String displayName;
    private final double priceMultiplier;

    TransportType(String displayName, double priceMultiplier) {
        this.displayName = displayName;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }
}
