package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class OptimisticWriteAuction implements Auction {

    private final Notifier notifier = new Notifier();

    // Price is initialized for simplicity
    // mark := isStopped
    private final AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(
            new Bid(1L, 1L, 0L),
            false);

    private final AtomicInteger bidsCount = new AtomicInteger(0);

    public boolean propose(Bid bid) {
//            if (latestBid == null) {
//                latestBid = bid;
//                bidsCount.incrementAndGet();
//                return true;
//            }
        boolean success;
        Bid localLatestBid;
        boolean priceUpdated;

        do {
            if (latestBid.isMarked()) {
                return false;
            }
            localLatestBid = latestBid.getReference();

            if (localLatestBid.getPrice() < bid.getPrice()) {
                success = latestBid.compareAndSet(localLatestBid, bid, false, false);
                priceUpdated = true;
            } else {
                success = true;
                priceUpdated = false;
            }
        } while (!success);

        if (priceUpdated) {
            notifier.sendOutdatedMessage(localLatestBid);
            bidsCount.incrementAndGet();
        }
        return priceUpdated;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    @Override
    public int getBidsCount() {
        return bidsCount.get();
    }

    @Override
    public void stopAuction() {
        latestBid.set(latestBid.getReference(), true);
    }

}