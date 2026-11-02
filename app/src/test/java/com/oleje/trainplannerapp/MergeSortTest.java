package com.oleje.trainplannerapp;

import com.oleje.trainplannerapp.domain.CitizenCategory;
import com.oleje.trainplannerapp.domain.TicketSorter;
import com.oleje.trainplannerapp.domain.TransportType;
import com.oleje.trainplannerapp.domain.TravelTicket;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MergeSortTest {

    private List<TravelTicket> sampleTickets;

    @Before
    public void setUp() throws Exception {
        sampleTickets = new ArrayList<>();
        sampleTickets.add(new TravelTicket("t1", "Львів", "Одеса", CitizenCategory.REGULAR, 0, 750.0, TransportType.COUPE, 400, 720));
        sampleTickets.add(new TravelTicket("t2", "Львів", "Тернопіль", CitizenCategory.REGULAR, 0, 180.0, TransportType.INTERCITY, 360, 110));
        sampleTickets.add(new TravelTicket("t3", "Тернопіль", "Хмельницький", CitizenCategory.REGULAR, 0, 160.0, TransportType.COUPE, 480, 100));
        sampleTickets.add(new TravelTicket("t4", "Львів", "Стрий", CitizenCategory.REGULAR, 0, 45.0, TransportType.SUBURBAN, 400, 65));
    }

    @Test
    public void testMergeSortByPriceAscending() {
        List<TravelTicket> sorted = TicketSorter.mergeSort(sampleTickets, TicketSorter.BY_PRICE_ASC);

        assertEquals(4, sorted.size());
        assertEquals(45.0, sorted.get(0).getBasePrice(), 0.001); // 45 грн (Стрий)
        assertEquals(160.0, sorted.get(1).getBasePrice(), 0.001); // 160 грн (Хмельницький)
        assertEquals(180.0, sorted.get(2).getBasePrice(), 0.001); // 180 грн (Тернопіль)
        assertEquals(750.0, sorted.get(3).getBasePrice(), 0.001); // 750 грн (Одеса)
    }

    @Test
    public void testMergeSortByDurationAscending() {
        List<TravelTicket> sorted = TicketSorter.mergeSort(sampleTickets, TicketSorter.BY_DURATION_ASC);

        assertEquals(4, sorted.size());
        assertEquals(65, sorted.get(0).getDurationMinutes());  // 65 min
        assertEquals(100, sorted.get(1).getDurationMinutes()); // 100 min
        assertEquals(110, sorted.get(2).getDurationMinutes()); // 110 min
        assertEquals(720, sorted.get(3).getDurationMinutes()); // 720 min
    }

    @Test
    public void testMergeSortEmptyAndSingleElementList() {
        List<TravelTicket> emptyList = new ArrayList<>();
        List<TravelTicket> sortedEmpty = TicketSorter.mergeSort(emptyList, TicketSorter.BY_PRICE_ASC);
        assertTrue(sortedEmpty.isEmpty());

        List<TravelTicket> singleList = new ArrayList<>();
        singleList.add(sampleTickets.get(0));
        List<TravelTicket> sortedSingle = TicketSorter.mergeSort(singleList, TicketSorter.BY_PRICE_ASC);
        assertEquals(1, sortedSingle.size());
        assertEquals("t1", sortedSingle.get(0).getId());
    }
}
