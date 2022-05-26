package com.home.tests.module3;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import static java.util.stream.Collectors.toSet;

class Restaurant {
}

public class RestaurantSearchService {

    private final ConcurrentHashMap<String, LongAdder> stat = new ConcurrentHashMap<>();

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return null /*...*/;
    }

    public void addToStat(String restaurantName) {
        stat.computeIfAbsent(restaurantName, key -> new LongAdder())
                .increment();
    }

    public Set<String> printStat() {
        return stat
                .entrySet()
                .stream()
                .map(e -> String.format("%s %d", e.getKey(), e.getValue().longValue()))
                .collect(toSet());
    }
}
