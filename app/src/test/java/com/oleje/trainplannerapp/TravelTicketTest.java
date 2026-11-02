package com.oleje.trainplannerapp;

import com.oleje.trainplannerapp.domain.CitizenCategory;
import com.oleje.trainplannerapp.domain.InvalidTicketDataException;
import com.oleje.trainplannerapp.domain.NamedTravelTicket;
import com.oleje.trainplannerapp.domain.TransportType;
import com.oleje.trainplannerapp.domain.TravelTicket;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TravelTicketTest {

    @Test
    public void testThreeConstructors() throws InvalidTicketDataException {
        // 1. Default constructor
        TravelTicket t1 = new TravelTicket();
        assertNotNull(t1.getId());
        assertEquals("Невідомо", t1.getDepartureCity());

        // 2. Parameterized constructor
        TravelTicket t2 = new TravelTicket("id123", "Львів", "Київ", CitizenCategory.REGULAR, 10.0, 500.0, TransportType.INTERCITY, 420, 300);
        assertEquals("Львів", t2.getDepartureCity());
        assertEquals("Київ", t2.getDestinationCity());
        assertEquals(500.0, t2.getBasePrice(), 0.001);

        // 3. Copy constructor
        TravelTicket t3 = new TravelTicket(t2);
        assertEquals(t2.getId(), t3.getId());
        assertEquals(t2.getDepartureCity(), t3.getDepartureCity());
        assertEquals(t2.getBasePrice(), t3.getBasePrice(), 0.001);
    }

    @Test(expected = InvalidTicketDataException.class)
    public void testInvalidPriceThrowsException() throws InvalidTicketDataException {
        new TravelTicket("id123", "Львів", "Київ", CitizenCategory.REGULAR, 0, -100.0, TransportType.COUPE, 400, 100);
    }

    @Test
    public void testStreamOperatorReplacements() throws Exception {
        TravelTicket original = new TravelTicket("t_stream", "Львів", "Одеса", CitizenCategory.REGULAR, 0.0, 600.0, TransportType.COUPE, 480, 500);

        // Test writeToStream (operator << replacement)
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);
        original.writeToStream(writer);
        writer.flush();

        String serializedLine = sw.toString();

        // Test readFromStream (operator >> replacement)
        TravelTicket restored = new TravelTicket();
        BufferedReader reader = new BufferedReader(new StringReader(serializedLine));
        restored.readFromStream(reader);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getDepartureCity(), restored.getDepartureCity());
        assertEquals(original.getDestinationCity(), restored.getDestinationCity());
        assertEquals(original.getBasePrice(), restored.getBasePrice(), 0.001);
    }

    @Test
    public void testNamedTicketOwnershipChange() throws Exception {
        TravelTicket base = new TravelTicket("t_base", "Львів", "Тернопіль", CitizenCategory.REGULAR, 0.0, 180.0, TransportType.INTERCITY, 360, 110);
        NamedTravelTicket named = new NamedTravelTicket(base, "u1", "Петро Коваль", "Без бонусів", 180.0);

        assertEquals("Петро Коваль", named.getOwnerFullName());

        // Change owner (Requirement #6)
        named.changeOwner("u2", "Марія Сидоренко");
        assertEquals("Марія Сидоренко", named.getOwnerFullName());
    }
}
