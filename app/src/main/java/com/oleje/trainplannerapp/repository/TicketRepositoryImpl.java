package com.oleje.trainplannerapp.repository;

import com.oleje.trainplannerapp.domain.BonusDiscountStrategy;
import com.oleje.trainplannerapp.domain.CertificateDiscount;
import com.oleje.trainplannerapp.domain.CitizenCategory;
import com.oleje.trainplannerapp.domain.InvalidTicketDataException;
import com.oleje.trainplannerapp.domain.NamedTravelTicket;
import com.oleje.trainplannerapp.domain.PromoCodeDiscount;
import com.oleje.trainplannerapp.domain.RoutePlanner;
import com.oleje.trainplannerapp.domain.StreamableTicket;
import com.oleje.trainplannerapp.domain.TransportType;
import com.oleje.trainplannerapp.domain.TravelTicket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TicketRepositoryImpl implements TicketRepository {

    private static TicketRepositoryImpl instance;

    private final List<TravelTicket> catalogTickets = new ArrayList<>();
    private final List<NamedTravelTicket> purchasedTickets = new ArrayList<>();
    private final List<BonusDiscountStrategy> userBonuses = new ArrayList<>();
    private CitizenCategory currentUserCategory = CitizenCategory.REGULAR;

    private TicketRepositoryImpl() {
        seedInitialData();
    }

    public static synchronized TicketRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new TicketRepositoryImpl();
        }
        return instance;
    }

    private void seedInitialData() {
        try {
            // Default active bonus discounts
            userBonuses.add(new CertificateDiscount(200.0));
            userBonuses.add(new PromoCodeDiscount(10.0));

            // Seed realistic Ukrzaliznytsia trains
            // Lviv -> Zhmerenka / Odesa / Kyiv / Mukachevo / Stryi
            catalogTickets.add(new TravelTicket("t101", "Львів", "Тернопіль", CitizenCategory.REGULAR, 0, 180.0, TransportType.INTERCITY, 360, 110)); // 06:00, 1h50m
            catalogTickets.add(new TravelTicket("t102", "Тернопіль", "Хмельницький", CitizenCategory.REGULAR, 0, 160.0, TransportType.COUPE, 480, 100)); // 08:00, 1h40m
            catalogTickets.add(new TravelTicket("t103", "Хмельницький", "Жмеринка", CitizenCategory.REGULAR, 0, 140.0, TransportType.PLATZKART, 590, 90)); // 09:50, 1h30m
            catalogTickets.add(new TravelTicket("t104", "Жмеринка", "Одеса-Головна", CitizenCategory.REGULAR, 0, 320.0, TransportType.COUPE, 690, 270)); // 11:30, 4h30m

            // Direct & alternative routes
            catalogTickets.add(new TravelTicket("t105", "Львів", "Одеса-Головна", CitizenCategory.REGULAR, 0, 750.0, TransportType.COUPE, 420, 720)); // 07:00, 12h
            catalogTickets.add(new TravelTicket("t106", "Львів", "Київ", CitizenCategory.REGULAR, 0, 620.0, TransportType.INTERCITY, 390, 310)); // 06:30, 5h10m
            catalogTickets.add(new TravelTicket("t107", "Львів", "Стрий", CitizenCategory.REGULAR, 0, 45.0, TransportType.SUBURBAN, 400, 65)); // 06:40, 1h05m
            catalogTickets.add(new TravelTicket("t108", "Стрий", "Мукачево", CitizenCategory.REGULAR, 0, 85.0, TransportType.SUBURBAN, 475, 140)); // 07:55, 2h20m
            catalogTickets.add(new TravelTicket("t109", "Мукачево", "Ужгород", CitizenCategory.REGULAR, 0, 40.0, TransportType.SUBURBAN, 625, 70)); // 10:25, 1h10m

            catalogTickets.add(new TravelTicket("t110", "Київ", "Одеса-Головна", CitizenCategory.REGULAR, 0, 580.0, TransportType.INTERCITY, 450, 410)); // 07:30, 6h50m
            catalogTickets.add(new TravelTicket("t111", "Київ", "Дніпро", CitizenCategory.REGULAR, 0, 520.0, TransportType.INTERCITY, 380, 340));
            catalogTickets.add(new TravelTicket("t112", "Дніпро", "Харків", CitizenCategory.REGULAR, 0, 290.0, TransportType.PLATZKART, 730, 210));

            // Initial purchased sample ticket for testing changeOwner
            TravelTicket baseForPurchased = catalogTickets.get(0);
            NamedTravelTicket samplePurchased = new NamedTravelTicket(
                    baseForPurchased,
                    "usr_100",
                    "Іван Петренко",
                    "Сертифікат -200 грн",
                    160.0
            );
            purchasedTickets.add(samplePurchased);

        } catch (InvalidTicketDataException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized List<TravelTicket> getCatalogTickets() {
        return new ArrayList<>(catalogTickets);
    }

    @Override
    public synchronized void addCatalogTicket(TravelTicket ticket) throws InvalidTicketDataException {
        if (ticket == null) throw new InvalidTicketDataException("Квиток не може бути null!");
        catalogTickets.add(new TravelTicket(ticket));
    }

    @Override
    public synchronized void updateCatalogTicket(TravelTicket ticket) throws InvalidTicketDataException {
        if (ticket == null || ticket.getId() == null) throw new InvalidTicketDataException("Недійсний квиток для оновлення!");
        for (int i = 0; i < catalogTickets.size(); i++) {
            if (catalogTickets.get(i).getId().equals(ticket.getId())) {
                catalogTickets.set(i, new TravelTicket(ticket));
                return;
            }
        }
        throw new InvalidTicketDataException("Квиток з ID " + ticket.getId() + " не знайдено!");
    }

    @Override
    public synchronized void deleteCatalogTicket(String ticketId) {
        if (ticketId == null) return;
        catalogTickets.removeIf(t -> ticketId.equals(t.getId()));
    }

    @Override
    public synchronized List<NamedTravelTicket> getPurchasedTickets() {
        return new ArrayList<>(purchasedTickets);
    }

    @Override
    public synchronized void purchasePlan(RoutePlanner.PersonalTravelPlan plan, String ownerUserId, String ownerFullName)
            throws InvalidTicketDataException {
        if (plan == null || plan.getSegments().isEmpty()) {
            throw new InvalidTicketDataException("План поїздки порожній!");
        }
        if (ownerFullName == null || ownerFullName.trim().length() < 3) {
            throw new InvalidTicketDataException("Вкажіть ПІБ пасажира (мінімум 3 символи)!");
        }

        for (TravelTicket segment : plan.getSegments()) {
            BonusDiscountStrategy bonus = plan.getAppliedDiscounts().get(segment);
            String bonusDesc = (bonus != null) ? bonus.getTitle() : "Без додаткових бонусів";
            double paidPrice = segment.calculateCategoryPrice(currentUserCategory);
            if (bonus != null) {
                paidPrice = Math.max(0.0, paidPrice - bonus.calculateSavings(paidPrice));
            }

            NamedTravelTicket namedTicket = new NamedTravelTicket(
                    segment,
                    ownerUserId,
                    ownerFullName,
                    bonusDesc,
                    paidPrice
            );
            purchasedTickets.add(namedTicket);

            // Consume used bonus from wallet if applicable
            if (bonus != null) {
                removeBonusDiscount(bonus.getId());
            }
        }
    }

    @Override
    public synchronized void transferTicketOwnership(String ticketId, String newOwnerUserId, String newOwnerFullName)
            throws InvalidTicketDataException {
        if (ticketId == null) throw new InvalidTicketDataException("Недійсний ID квитка!");
        for (NamedTravelTicket ticket : purchasedTickets) {
            if (ticketId.equals(ticket.getId())) {
                ticket.changeOwner(newOwnerUserId, newOwnerFullName);
                return;
            }
        }
        throw new InvalidTicketDataException("Куплений квиток з ID " + ticketId + " не знайдено!");
    }

    @Override
    public synchronized List<BonusDiscountStrategy> getUserBonuses() {
        return new ArrayList<>(userBonuses);
    }

    @Override
    public synchronized void addBonusDiscount(BonusDiscountStrategy bonus) {
        if (bonus != null) {
            userBonuses.add(bonus);
        }
    }

    @Override
    public synchronized void removeBonusDiscount(String bonusId) {
        if (bonusId == null) return;
        userBonuses.removeIf(b -> bonusId.equals(b.getId()));
    }

    @Override
    public synchronized CitizenCategory getCurrentUserCategory() {
        return currentUserCategory;
    }

    @Override
    public synchronized void setCurrentUserCategory(CitizenCategory category) {
        this.currentUserCategory = (category != null) ? category : CitizenCategory.REGULAR;
    }

    @Override
    public List<TravelTicket> importTicketsFromTxt(File file) throws IOException, InvalidTicketDataException {
        if (file == null || !file.exists()) {
            throw new IOException("Файл не існує або не вибраний!");
        }
        List<TravelTicket> imported = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                TravelTicket ticket = new TravelTicket();
                ticket.readFromStream(new BufferedReader(new java.io.StringReader(line)));
                imported.add(ticket);
            }
        }
        synchronized (this) {
            for (TravelTicket t : imported) {
                addCatalogTicket(t);
            }
        }
        return imported;
    }

    @Override
    public void exportTicketsToTxt(File file, List<? extends StreamableTicket> tickets) throws IOException {
        if (file == null) throw new IOException("Файл не вказаний!");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("# MAGA Train Planner Exported Tickets");
            writer.newLine();
            for (StreamableTicket ticket : tickets) {
                ticket.writeToStream(writer);
            }
        }
    }
}
