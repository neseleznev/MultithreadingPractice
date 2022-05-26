package com.home.tests.module3.delivery;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

class Item { /*...*/
}

class PaymentInfo { /*...*/
}

enum Status {DELIVERED}

@Value
@Builder(toBuilder = true)
public class Order {

    @With
    Long id;

    @Getter(value = AccessLevel.NONE)
    List<Item> items;

    @With
    PaymentInfo paymentInfo;

    @With
    boolean isPacked;

    @With
    Status status;

    private Order(Long id, List<Item> items, PaymentInfo paymentInfo, boolean isPacked, Status status) {
        this.id = id;
        this.items = immutableCopyOf(items);
        this.paymentInfo = paymentInfo;
        this.isPacked = isPacked;
        this.status = status;
    }

    public static Order ofId(Long id) {
        return Order.builder()
                .id(id)
                .build();
    }

    @NotNull
    private static <T> List<T> immutableCopyOf(List<T> items) {
        return items != null
                ? Collections.unmodifiableList(items)
                : Collections.emptyList();
    }

    public Order withItems(List<Item> items) {
        return toBuilder()
                .items(items)
                .build();
    }

    public boolean isReadyToDeliver() {
        return !items.isEmpty() && paymentInfo != null && isPacked;
    }

}