package com.oleje.trainplannerapp.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Base domain model for Travel Ticket (Variant #24).
 * Satisfies coursework requirements:
 * 1. Default constructor
 * 2. Parameterized constructor with validation
 * 3. Copy constructor
 * 4. Stream I/O implementation (StreamableTicket - replacement for C++ << and >>)
 * 5. Parcelable for Android
 */
public class TravelTicket implements StreamableTicket, Parcelable {
    protected String id;
    protected String departureCity;
    protected String destinationCity;
    protected CitizenCategory category;
    protected double discountPercent;
    protected double basePrice;
    protected TransportType transportType;
    protected long departureTimeMinutes; // Minutes from midnight or Unix Epoch minutes
    protected int durationMinutes;

    // 1. Default Constructor
    public TravelTicket() {
        this.id = UUID.randomUUID().toString();
        this.departureCity = "Невідомо";
        this.destinationCity = "Невідомо";
        this.category = CitizenCategory.REGULAR;
        this.discountPercent = 0.0;
        this.basePrice = 0.0;
        this.transportType = TransportType.SUBURBAN;
        this.departureTimeMinutes = 480; // Default 08:00 AM
        this.durationMinutes = 60;
    }

    // 2. Parameterized Constructor
    public TravelTicket(String id, String departureCity, String destinationCity,
                        CitizenCategory category, double discountPercent, double basePrice,
                        TransportType transportType, long departureTimeMinutes, int durationMinutes)
            throws InvalidTicketDataException {
        if (departureCity == null || departureCity.trim().isEmpty()) {
            throw new InvalidTicketDataException("Місто відправлення не може бути порожнім!");
        }
        if (destinationCity == null || destinationCity.trim().isEmpty()) {
            throw new InvalidTicketDataException("Місто призначення не може бути порожнім!");
        }
        if (basePrice < 0) {
            throw new InvalidTicketDataException("Базова ціна не може бути від'ємною!");
        }
        if (durationMinutes <= 0) {
            throw new InvalidTicketDataException("Тривалість поїздки повинна бути більшою за 0 хвилин!");
        }

        this.id = (id != null && !id.trim().isEmpty()) ? id : UUID.randomUUID().toString();
        this.departureCity = departureCity.trim();
        this.destinationCity = destinationCity.trim();
        this.category = category != null ? category : CitizenCategory.REGULAR;
        this.discountPercent = Math.max(0.0, Math.min(100.0, discountPercent));
        this.basePrice = basePrice;
        this.transportType = transportType != null ? transportType : TransportType.SUBURBAN;
        this.departureTimeMinutes = Math.max(0, departureTimeMinutes);
        this.durationMinutes = durationMinutes;
    }

    // 3. Copy Constructor
    public TravelTicket(TravelTicket other) {
        if (other != null) {
            this.id = other.id;
            this.departureCity = other.departureCity;
            this.destinationCity = other.destinationCity;
            this.category = other.category;
            this.discountPercent = other.discountPercent;
            this.basePrice = other.basePrice;
            this.transportType = other.transportType;
            this.departureTimeMinutes = other.departureTimeMinutes;
            this.durationMinutes = other.durationMinutes;
        } else {
            this.id = UUID.randomUUID().toString();
            this.departureCity = "Невідомо";
            this.destinationCity = "Невідомо";
            this.category = CitizenCategory.REGULAR;
            this.discountPercent = 0.0;
            this.basePrice = 0.0;
            this.transportType = TransportType.SUBURBAN;
            this.departureTimeMinutes = 480;
            this.durationMinutes = 60;
        }
    }

    // Parcelable constructor
    protected TravelTicket(Parcel in) {
        id = in.readString();
        departureCity = in.readString();
        destinationCity = in.readString();
        category = CitizenCategory.valueOf(in.readString());
        discountPercent = in.readDouble();
        basePrice = in.readDouble();
        transportType = TransportType.valueOf(in.readString());
        departureTimeMinutes = in.readLong();
        durationMinutes = in.readInt();
    }

    public static final Creator<TravelTicket> CREATOR = new Creator<TravelTicket>() {
        @Override
        public TravelTicket createFromParcel(Parcel in) {
            return new TravelTicket(in);
        }

        @Override
        public TravelTicket[] newArray(int size) {
            return new TravelTicket[size];
        }
    };

    public long getArrivalTimeMinutes() {
        return departureTimeMinutes + durationMinutes;
    }

    /**
     * Calculates final price according to user citizen category:
     * - Privileged on Suburban trains -> FREE (0.0 UAH)
     * - Student -> 50% discount on base price
     * - Regular / Other -> applying discountPercent
     */
    public double calculateCategoryPrice(CitizenCategory userCategory) {
        if (userCategory == CitizenCategory.PRIVILEGED && transportType == TransportType.SUBURBAN) {
            return 0.0; // Free for privileged on suburban
        }
        double activeDiscount = (userCategory == CitizenCategory.STUDENT) ? 50.0 : discountPercent;
        double price = basePrice * (1.0 - activeDiscount / 100.0);
        return Math.max(0.0, price);
    }

    // Replacement for C++ operator << (Stream output)
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
                String.valueOf(durationMinutes)
        );
        writer.write(line);
        writer.newLine();
    }

    // Replacement for C++ operator >> (Stream input)
    @Override
    public void readFromStream(BufferedReader reader) throws IOException, InvalidTicketDataException {
        String line = reader.readLine();
        if (line == null || line.trim().isEmpty()) {
            throw new InvalidTicketDataException("Порожній рядок у файлі даних!");
        }
        String[] parts = line.split(";");
        if (parts.length < 9) {
            throw new InvalidTicketDataException("Некоректний формат рядка квитка: " + line);
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
        } catch (Exception e) {
            throw new InvalidTicketDataException("Помилка розбору даних квитка: " + e.getMessage(), e);
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }

    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }

    public CitizenCategory getCategory() { return category; }
    public void setCategory(CitizenCategory category) { this.category = category; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public TransportType getTransportType() { return transportType; }
    public void setTransportType(TransportType transportType) { this.transportType = transportType; }

    public long getDepartureTimeMinutes() { return departureTimeMinutes; }
    public void setDepartureTimeMinutes(long departureTimeMinutes) { this.departureTimeMinutes = departureTimeMinutes; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(departureCity);
        dest.writeString(destinationCity);
        dest.writeString(category.name());
        dest.writeDouble(discountPercent);
        dest.writeDouble(basePrice);
        dest.writeString(transportType.name());
        dest.writeLong(departureTimeMinutes);
        dest.writeInt(durationMinutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelTicket that = (TravelTicket) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
