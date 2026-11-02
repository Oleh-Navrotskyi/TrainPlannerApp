package com.oleje.trainplannerapp.repository;

import com.oleje.trainplannerapp.domain.BonusDiscountStrategy;
import com.oleje.trainplannerapp.domain.CitizenCategory;
import com.oleje.trainplannerapp.domain.InvalidTicketDataException;
import com.oleje.trainplannerapp.domain.NamedTravelTicket;
import com.oleje.trainplannerapp.domain.RoutePlanner;
import com.oleje.trainplannerapp.domain.StreamableTicket;
import com.oleje.trainplannerapp.domain.TravelTicket;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface TicketRepository {
    List<TravelTicket> getCatalogTickets();
    void addCatalogTicket(TravelTicket ticket) throws InvalidTicketDataException;
    void updateCatalogTicket(TravelTicket ticket) throws InvalidTicketDataException;
    void deleteCatalogTicket(String ticketId);

    List<NamedTravelTicket> getPurchasedTickets();
    void purchasePlan(RoutePlanner.PersonalTravelPlan plan, String ownerUserId, String ownerFullName) throws InvalidTicketDataException;
    void transferTicketOwnership(String ticketId, String newOwnerUserId, String newOwnerFullName) throws InvalidTicketDataException;

    List<BonusDiscountStrategy> getUserBonuses();
    void addBonusDiscount(BonusDiscountStrategy bonus);
    void removeBonusDiscount(String bonusId);

    CitizenCategory getCurrentUserCategory();
    void setCurrentUserCategory(CitizenCategory category);

    List<TravelTicket> importTicketsFromTxt(File file) throws IOException, InvalidTicketDataException;
    void exportTicketsToTxt(File file, List<? extends StreamableTicket> tickets) throws IOException;
}
