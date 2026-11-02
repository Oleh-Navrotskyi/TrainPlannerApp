package com.oleje.trainplannerapp;

import com.oleje.trainplannerapp.domain.BonusDiscountStrategy;
import com.oleje.trainplannerapp.domain.CertificateDiscount;
import com.oleje.trainplannerapp.domain.CitizenCategory;
import com.oleje.trainplannerapp.domain.PromoCodeDiscount;
import com.oleje.trainplannerapp.domain.RouteNotFoundException;
import com.oleje.trainplannerapp.domain.RoutePlanner;
import com.oleje.trainplannerapp.domain.TransportType;
import com.oleje.trainplannerapp.domain.TravelTicket;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RoutePlannerTest {

    private RoutePlanner routePlanner;
    private List<TravelTicket> networkTickets;
    private List<BonusDiscountStrategy> userBonuses;

    @Before
    public void setUp() throws Exception {
        routePlanner = new RoutePlanner();
        networkTickets = new ArrayList<>();

        // Chain 1: Львів -> Тернопіль -> Жмеринка -> Одеса
        networkTickets.add(new TravelTicket("t1", "Львів", "Тернопіль", CitizenCategory.REGULAR, 0, 180.0, TransportType.INTERCITY, 360, 110)); // 06:00-07:50
        networkTickets.add(new TravelTicket("t2", "Тернопіль", "Жмеринка", CitizenCategory.REGULAR, 0, 200.0, TransportType.COUPE, 480, 120)); // 08:00-10:00 (10 min transfer ok)
        networkTickets.add(new TravelTicket("t3", "Жмеринка", "Одеса", CitizenCategory.REGULAR, 0, 300.0, TransportType.COUPE, 620, 240)); // 10:20-14:20

        // Direct: Львів -> Одеса
        networkTickets.add(new TravelTicket("t4", "Львів", "Одеса", CitizenCategory.REGULAR, 0, 750.0, TransportType.COUPE, 400, 720)); // 06:40-18:40

        // Active bonuses
        userBonuses = new ArrayList<>();
        userBonuses.add(new CertificateDiscount(200.0));
        userBonuses.add(new PromoCodeDiscount(10.0));
    }

    @Test
    public void testFindOptimalPlansTopRoutesFound() throws RouteNotFoundException {
        List<RoutePlanner.PersonalTravelPlan> plans = routePlanner.findOptimalPlans(
                networkTickets,
                "Львів",
                "Одеса",
                360,
                CitizenCategory.REGULAR,
                userBonuses,
                RoutePlanner.OptimizationMode.CHEAPEST,
                3
        );

        assertNotNull(plans);
        assertFalse(plans.isEmpty());
        assertTrue(plans.size() <= 3);

        // Verify greedy discount calculation reduced cost
        RoutePlanner.PersonalTravelPlan topPlan = plans.get(0);
        assertTrue(topPlan.getFinalDiscountedCost() < topPlan.getBaseTotalCost());
        assertTrue(topPlan.getTotalSaved() > 0);
    }

    @Test(expected = RouteNotFoundException.class)
    public void testRouteNotFoundThrowsException() throws RouteNotFoundException {
        routePlanner.findOptimalPlans(
                networkTickets,
                "Львів",
                "Харків", // No train to Kharkiv in network
                360,
                CitizenCategory.REGULAR,
                userBonuses,
                RoutePlanner.OptimizationMode.CHEAPEST,
                3
        );
    }

    @Test
    public void testPrivilegedSuburbanCategoryIsFree() throws Exception {
        List<TravelTicket> suburbanTickets = new ArrayList<>();
        suburbanTickets.add(new TravelTicket("s1", "Львів", "Стрий", CitizenCategory.REGULAR, 0, 45.0, TransportType.SUBURBAN, 400, 65));

        List<RoutePlanner.PersonalTravelPlan> plans = routePlanner.findOptimalPlans(
                suburbanTickets,
                "Львів",
                "Стрий",
                360,
                CitizenCategory.PRIVILEGED, // Privileged passenger
                userBonuses,
                RoutePlanner.OptimizationMode.CHEAPEST,
                1
        );

        assertEquals(1, plans.size());
        assertEquals(0.0, plans.get(0).getFinalDiscountedCost(), 0.001); // 0 грн for privileged on suburban!
    }
}
