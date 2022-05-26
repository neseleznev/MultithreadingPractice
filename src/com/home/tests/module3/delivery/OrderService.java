package com.home.tests.module3.delivery;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

public class OrderService {

    private final Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(0L);

    private long nextId() {
        return nextId.getAndIncrement();
    }

    public long createOrder(List<Item> items) {
        long id = nextId();

        Order order = getOrCreateOrderByIdAndModify(
                id,
                o -> o.withItems(items));

        deliverIfReady(order);
        return id;
    }

    public void updatePaymentInfo(long cartId, PaymentInfo paymentInfo) {
        Order order = getOrCreateOrderByIdAndModify(
                cartId,
                o -> o.withPaymentInfo(paymentInfo));

        deliverIfReady(order);
    }

    public void setPacked(long cartId) {
        Order order = getOrCreateOrderByIdAndModify(
                cartId,
                o -> o.withPacked(true));

        deliverIfReady(order);
    }

    private synchronized Order getOrCreateOrderByIdAndModify(long cartId,
                                                             UnaryOperator<Order> unaryOperator) {
        return currentOrders.compute(
                cartId,
                (key, existing) -> unaryOperator.apply(Optional
                        .ofNullable(existing)
                        .orElse(Order.ofId(cartId))));
    }

    private void deliverIfReady(Order order) {
        if (order.isReadyToDeliver()) {
            deliver(order);
        }
    }

    private synchronized void deliver(Order order) {
        /*...*/
        //FIXME: Resolve dual-write issue
        // Should update status via transactional outbox
        currentOrders.get(order.getId()).withStatus(Status.DELIVERED);
    }
}