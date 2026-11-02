package com.oleje.trainplannerapp.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Custom recursive MergeSort implementation for TravelTicket objects (Coursework Requirement).
 * Uses Comparator<TravelTicket> for flexible sorting by price, duration, transport type, or cities.
 * Avoids using built-in Collections.sort().
 */
public class TicketSorter {

    public static <T extends TravelTicket> List<T> mergeSort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1) {
            return list != null ? new ArrayList<>(list) : new ArrayList<>();
        }

        int mid = list.size() / 2;
        List<T> leftHalf = mergeSort(new ArrayList<>(list.subList(0, mid)), comparator);
        List<T> rightHalf = mergeSort(new ArrayList<>(list.subList(mid, list.size())), comparator);

        return merge(leftHalf, rightHalf, comparator);
    }

    private static <T extends TravelTicket> List<T> merge(List<T> left, List<T> right, Comparator<T> comparator) {
        List<T> merged = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            T leftItem = left.get(i);
            T rightItem = right.get(j);

            if (comparator.compare(leftItem, rightItem) <= 0) {
                merged.add(leftItem);
                i++;
            } else {
                merged.add(rightItem);
                j++;
            }
        }

        while (i < left.size()) {
            merged.add(left.get(i));
            i++;
        }

        while (j < right.size()) {
            merged.add(right.get(j));
            j++;
        }

        return merged;
    }

    // Pre-built Comparators for convenience
    public static final Comparator<TravelTicket> BY_PRICE_ASC = (t1, t2) ->
            Double.compare(t1.getBasePrice(), t2.getBasePrice());

    public static final Comparator<TravelTicket> BY_PRICE_DESC = (t1, t2) ->
            Double.compare(t2.getBasePrice(), t1.getBasePrice());

    public static final Comparator<TravelTicket> BY_DURATION_ASC = (t1, t2) ->
            Integer.compare(t1.getDurationMinutes(), t2.getDurationMinutes());

    public static final Comparator<TravelTicket> BY_TRANSPORT_TYPE = (t1, t2) ->
            t1.getTransportType().name().compareTo(t2.getTransportType().name());

    public static final Comparator<TravelTicket> BY_DEPARTURE_CITY = (t1, t2) ->
            t1.getDepartureCity().compareToIgnoreCase(t2.getDepartureCity());
}
