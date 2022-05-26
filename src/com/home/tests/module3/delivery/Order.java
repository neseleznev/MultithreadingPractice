package com.home.tests.module3.delivery;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

class Item { /*...*/
}

class PaymentInfo { /*...*/
}

enum Status {DELIVERED}

public class Order {

    @Getter
    @Setter
    private Long id;

    @Getter(value = AccessLevel.NONE)
    private final List<Item> items;

    @Setter
    private PaymentInfo paymentInfo;

    @Setter
    private boolean isPacked;

    @Setter
    private Status status;

    public Order(List<Item> items) {
        this.items = items != null
                ? Collections.unmodifiableList(items)
                : Collections.emptyList();
    }

    public boolean isReadyToDeliver() {
        return !items.isEmpty() && paymentInfo != null && isPacked;
    }

}