package com.oleje.trainplannerapp.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Domain model for purchased Named Travel Ticket (Requirement #6).
 * Inherits from TravelTicket and adds ownership details and transfer behavior.
 */
public class NamedTravelTicket extends TravelTicket {
    private String ownerUserId;
    private String ownerFullName;
    private String appliedBonusDescription;
    private double finalPaidPrice;

    // 1. Default Constructor
    public NamedTravelTicket() {
        super();
        this.ownerUserId = "anonymous";
        this.ownerFullName = "Невідомий Пасажир";
        this.appliedBonusDescription = "Без додаткових бонусів";
        this.finalPaidPrice = 0.0;
    }

    // 2. Parameterized Constructor
    public NamedTravelTicket(TravelTicket baseTicket, String ownerUserId, String ownerFullName,
                             String appliedBonusDescription, double finalPaidPrice)
            throws InvalidTicketDataException {
        super(baseTicket);
        if (ownerFullName == null || ownerFullName.trim().length() < 3) {
            throw new InvalidTicketDataException("ПІБ власника занадто коротке або порожнє!");
        }
        this.ownerUserId = (ownerUserId != null) ? ownerUserId : "user_" + System.currentTimeMillis();
        this.ownerFullName = ownerFullName.trim();
        this.appliedBonusDescription = (appliedBonusDescription != null) ? appliedBonusDescription : "Стандартний тариф";
        this.finalPaidPrice = Math.max(0.0, finalPaidPrice);
    }

    // 3. Copy Constructor
    public NamedTravelTicket(NamedTravelTicket other) {
        super(other);
        if (other != null) {
            this.ownerUserId = other.ownerUserId;
            this.ownerFullName = other.ownerFullName;
            this.appliedBonusDescription = other.appliedBonusDescription;
            this.finalPaidPrice = other.finalPaidPrice;
        }
    }

    // Parcelable constructor
    protected NamedTravelTicket(Parcel in) {
        super(in);
        ownerUserId = in.readString();
        ownerFullName = in.readString();
        appliedBonusDescription = in.readString();
        finalPaidPrice = in.readDouble();
    }

    public static final Parcelable.Creator<NamedTravelTicket> CREATOR = new Parcelable.Creator<NamedTravelTicket>() {
        @Override
        public NamedTravelTicket createFromParcel(Parcel in) {
            return new NamedTravelTicket(in);
        }

        @Override
        public NamedTravelTicket[] newArray(int size) {
            return new NamedTravelTicket[size];
        }
    };

    /**
     * Requirement #6: Transfer ticket ownership to another person.
     */
    public void changeOwner(String newOwnerUserId, String newOwnerFullName) throws InvalidTicketDataException {
        if (newOwnerFullName == null || newOwnerFullName.trim().length() < 3) {
            throw new InvalidTicketDataException("Вкажіть коректне ПІБ нового власника (мінімум 3 символи)!");
        }
        if (newOwnerFullName.trim().equalsIgnoreCase(this.ownerFullName)) {
            throw new InvalidTicketDataException("Квиток вже належить пасажиру " + newOwnerFullName + "!");
        }
        this.ownerUserId = (newOwnerUserId != null) ? newOwnerUserId : "user_" + System.currentTimeMillis();
        this.ownerFullName = newOwnerFullName.trim();
    }

    @Override
    public void writeToStream(BufferedWriter writer) throws IOException {
        String line = String.join(";",
                id,
                departureCity,
                destinationCity,
                category.name(),
                String.valueOf(discountPercent),
                String.valueOf(basePrice),
                transportType.name(),
                String.valueOf(departureTimeMinutes),
                String.valueOf(durationMinutes),
                ownerUserId,
                ownerFullName,
                appliedBonusDescription,
                String.valueOf(finalPaidPrice)
        );
        writer.write(line);
        writer.newLine();
    }

    @Override
    public void readFromStream(BufferedReader reader) throws IOException, InvalidTicketDataException {
        String line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            throw new InvalidTicketDataException("Порожній рядок у файлі даних!");
        }
        String[] parts = line.split(";");
        if (parts.length < 13) {
            super.readFromStream(reader);
            return;
        }
        try {
            this.id = parts[0].trim();
            this.departureCity = parts[1].trim();
            this.destinationCity = parts[2].trim();
            this.category = CitizenCategory.valueOf(parts[3].trim());
            this.discountPercent = Double.parseDouble(parts[4].trim());
            this.basePrice = Double.parseDouble(parts[5].trim());
            this.transportType = TransportType.valueOf(parts[6].trim());
            this.departureTimeMinutes = Long.parseLong(parts[7].trim());
            this.durationMinutes = Integer.parseInt(parts[8].trim());
            this.ownerUserId = parts[9].trim();
            this.ownerFullName = parts[10].trim();
            this.appliedBonusDescription = parts[11].trim();
            this.finalPaidPrice = Double.parseDouble(parts[12].trim());
        } catch (Exception e) {
            throw new InvalidTicketDataException("Помилка розбору іменного квитка: " + e.getMessage(), e);
        }
    }

    // Getters and Setters
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getOwnerFullName() { return ownerFullName; }
    public void setOwnerFullName(String ownerFullName) { this.ownerFullName = ownerFullName; }

    public String getAppliedBonusDescription() { return appliedBonusDescription; }
    public void setAppliedBonusDescription(String appliedBonusDescription) { this.appliedBonusDescription = appliedBonusDescription; }

    public double getFinalPaidPrice() { return finalPaidPrice; }
    public void setFinalPaidPrice(double finalPaidPrice) { this.finalPaidPrice = finalPaidPrice; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(ownerUserId);
        dest.writeString(ownerFullName);
        dest.writeString(appliedBonusDescription);
        dest.writeDouble(finalPaidPrice);
    }
}
