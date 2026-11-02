package com.oleje.trainplannerapp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Time-Dependent Dijkstra algorithm for railway network route search (Coursework Requirement #4).
 * Finds Top-3 optimal travel plans with transfer time checks (>= 10 minutes) and
 * applies a greedy bonus discount distribution algorithm (1 discount per ticket max).
 */
public class RoutePlanner {

    public static final int MIN_TRANSFER_MINUTES = 10;
    public static final int MAX_SEGMENTS = 4;

    public enum OptimizationMode {
        FASTEST,  // Minimum total duration
        CHEAPEST  // Personal travel plan for minimum price after discounts
    }

    public static class PersonalTravelPlan {
        private final List<TravelTicket> segments;
        private final Map<TravelTicket, BonusDiscountStrategy> appliedDiscounts;
        private final double baseTotalCost;
        private final double finalDiscountedCost;
        private final long totalDurationMinutes;

        public PersonalTravelPlan(List<TravelTicket> segments,
                                  Map<TravelTicket, BonusDiscountStrategy> appliedDiscounts,
                                  double baseTotalCost,
                                  double finalDiscountedCost,
                                  long totalDurationMinutes) {
            this.segments = segments;
            this.appliedDiscounts = appliedDiscounts;
            this.baseTotalCost = baseTotalCost;
            this.finalDiscountedCost = finalDiscountedCost;
            this.totalDurationMinutes = totalDurationMinutes;
        }

        public List<TravelTicket> getSegments() { return segments; }
        public Map<TravelTicket, BonusDiscountStrategy> getAppliedDiscounts() { return appliedDiscounts; }
        public double getBaseTotalCost() { return baseTotalCost; }
        public double getFinalDiscountedCost() { return finalDiscountedCost; }
        public double getTotalSaved() { return Math.max(0.0, baseTotalCost - finalDiscountedCost); }
        public long getTotalDurationMinutes() { return totalDurationMinutes; }
    }

    private static class PathNode implements Comparable<PathNode> {
        String currentCity;
        long currentTimeMinutes;
        long firstDepartureMinutes;
        double accumulatedCost;
        double priorityWeight;
        List<TravelTicket> pathTickets;
        Set<String> visitedCities;

        PathNode(String currentCity, long currentTimeMinutes, long firstDepartureMinutes,
                 double accumulatedCost, double priorityWeight,
                 List<TravelTicket> pathTickets, Set<String> visitedCities) {
            this.currentCity = currentCity;
            this.currentTimeMinutes = currentTimeMinutes;
            this.firstDepartureMinutes = firstDepartureMinutes;
            this.accumulatedCost = accumulatedCost;
            this.priorityWeight = priorityWeight;
            this.pathTickets = pathTickets;
            this.visitedCities = visitedCities;
        }

        @Override
        public int compareTo(PathNode other) {
            return Double.compare(this.priorityWeight, other.priorityWeight);
        }
    }

    /**
     * Top-K Time-Dependent Dijkstra Algorithm.
     */
    public List<PersonalTravelPlan> findOptimalPlans(
            List<TravelTicket> allAvailableTickets,
            String startCity,
            String endCity,
            long earliestDepartureTime,
            CitizenCategory userCategory,
            List<BonusDiscountStrategy> userBonuses,
            OptimizationMode mode,
            int topK) throws RouteNotFoundException {

        if (startCity == null || endCity == null || startCity.trim().equalsIgnoreCase(endCity.trim())) {
            throw new RouteNotFoundException("Станції відправлення та прибуття повинні відрізнятися!");
        }

        startCity = startCity.trim();
        endCity = endCity.trim();

        // Build adjacency graph
        Map<String, List<TravelTicket>> graph = new HashMap<>();
        for (TravelTicket ticket : allAvailableTickets) {
            graph.computeIfAbsent(ticket.getDepartureCity().toLowerCase(), k -> new ArrayList<>()).add(ticket);
        }

        PriorityQueue<PathNode> pq = new PriorityQueue<>();
        Map<String, Integer> visitsCount = new HashMap<>();

        Set<String> initialVisited = new HashSet<>();
        initialVisited.add(startCity.toLowerCase());

        pq.add(new PathNode(startCity, earliestDepartureTime, -1, 0.0, 0.0, new ArrayList<>(), initialVisited));

        List<PathNode> rawTopRoutes = new ArrayList<>();

        while (!pq.isEmpty() && rawTopRoutes.size() < topK) {
            PathNode current = pq.poll();
            if (current == null || current.currentCity == null) continue;

            if (current.currentCity.equalsIgnoreCase(endCity)) {
                if (!current.pathTickets.isEmpty()) {
                    rawTopRoutes.add(current);
                }
                continue;
            }

            Integer countObj = visitsCount.get(current.currentCity.toLowerCase());
            int count = countObj != null ? countObj : 0;
            if (count >= topK * 3) {
                continue;
            }
            visitsCount.put(current.currentCity.toLowerCase(), count + 1);

            if (current.pathTickets.size() >= MAX_SEGMENTS) {
                continue;
            }

            List<TravelTicket> outgoingTickets = graph.getOrDefault(current.currentCity.toLowerCase(), Collections.emptyList());
            for (TravelTicket edge : outgoingTickets) {
                String nextCity = edge.getDestinationCity();

                if (current.visitedCities.contains(nextCity.toLowerCase())) {
                    continue; // Avoid cycles
                }

                long requiredDeparture = current.pathTickets.isEmpty()
                        ? current.currentTimeMinutes
                        : current.currentTimeMinutes + MIN_TRANSFER_MINUTES;

                if (edge.getDepartureTimeMinutes() < requiredDeparture) {
                    continue; // Missed train
                }

                double segmentCost = edge.calculateCategoryPrice(userCategory);
                double newAccumulatedCost = current.accumulatedCost + segmentCost;

                long newFirstDeparture = current.pathTickets.isEmpty()
                        ? edge.getDepartureTimeMinutes()
                        : current.firstDepartureMinutes;
                long newArrivalTime = edge.getArrivalTimeMinutes();
                long totalElapsedTime = newArrivalTime - newFirstDeparture;

                double newPriorityWeight;
                if (mode == OptimizationMode.FASTEST) {
                    newPriorityWeight = totalElapsedTime + (newAccumulatedCost * 0.001);
                } else {
                    newPriorityWeight = newAccumulatedCost + (totalElapsedTime * 0.001);
                }

                List<TravelTicket> nextPath = new ArrayList<>(current.pathTickets);
                nextPath.add(edge);

                Set<String> nextVisited = new HashSet<>(current.visitedCities);
                nextVisited.add(nextCity.toLowerCase());

                pq.add(new PathNode(nextCity, newArrivalTime, newFirstDeparture,
                        newAccumulatedCost, newPriorityWeight, nextPath, nextVisited));
            }
        }

        if (rawTopRoutes.isEmpty()) {
            throw new RouteNotFoundException("Прямого або пересадочного маршруту між '" + startCity + "' та '" + endCity + "' не знайдено!");
        }

        List<PersonalTravelPlan> finalPlans = new ArrayList<>();
        for (PathNode routeNode : rawTopRoutes) {
            PersonalTravelPlan plan = applyGreedyDiscounts(routeNode, userCategory, userBonuses);
            finalPlans.add(plan);
        }

        if (mode == OptimizationMode.CHEAPEST) {
            finalPlans.sort(Comparator.comparingDouble(PersonalTravelPlan::getFinalDiscountedCost)
                    .thenComparingLong(PersonalTravelPlan::getTotalDurationMinutes));
        }

        return finalPlans;
    }

    /**
     * Greedy algorithm for dynamic bonus discount assignment (1 ticket = max 1 bonus discount).
     */
    private PersonalTravelPlan applyGreedyDiscounts(
            PathNode routeNode,
            CitizenCategory userCategory,
            List<BonusDiscountStrategy> availableBonuses) {

        List<TravelTicket> segments = routeNode.pathTickets;
        Map<TravelTicket, BonusDiscountStrategy> assignedDiscounts = new HashMap<>();

        if (availableBonuses != null && !availableBonuses.isEmpty()) {
            Set<Integer> usedTicketIndices = new HashSet<>();
            Set<Integer> usedBonusIndices = new HashSet<>();

            int maxAssignments = Math.min(segments.size(), availableBonuses.size());

            for (int step = 0; step < maxAssignments; step++) {
                double bestSavings = 0.0;
                int bestTicketIdx = -1;
                int bestBonusIdx = -1;

                for (int t = 0; t < segments.size(); t++) {
                    if (usedTicketIndices.contains(t)) continue;

                    double ticketPrice = segments.get(t).calculateCategoryPrice(userCategory);
                    if (ticketPrice <= 0) continue; // Skip free tickets

                    for (int b = 0; b < availableBonuses.size(); b++) {
                        if (usedBonusIndices.contains(b)) continue;

                        double savings = availableBonuses.get(b).calculateSavings(ticketPrice);
                        if (savings > bestSavings) {
                            bestSavings = savings;
                            bestTicketIdx = t;
                            bestBonusIdx = b;
                        }
                    }
                }

                if (bestTicketIdx != -1 && bestBonusIdx != -1 && bestSavings > 0) {
                    usedTicketIndices.add(bestTicketIdx);
                    usedBonusIndices.add(bestBonusIdx);
                    assignedDiscounts.put(segments.get(bestTicketIdx), availableBonuses.get(bestBonusIdx));
                } else {
                    break;
                }
            }
        }

        double totalSavings = 0.0;
        for (Map.Entry<TravelTicket, BonusDiscountStrategy> entry : assignedDiscounts.entrySet()) {
            double price = entry.getKey().calculateCategoryPrice(userCategory);
            totalSavings += entry.getValue().calculateSavings(price);
        }

        double baseCost = routeNode.accumulatedCost;
        double finalCost = Math.max(0.0, baseCost - totalSavings);
        long totalDuration = routeNode.currentTimeMinutes - routeNode.firstDepartureMinutes;

        return new PersonalTravelPlan(segments, assignedDiscounts, baseCost, finalCost, totalDuration);
    }
}
