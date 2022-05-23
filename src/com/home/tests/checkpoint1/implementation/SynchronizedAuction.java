package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;

import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizedAuction implements Auction {

    private final Notifier notifier = new Notifier();

    private Bid latestBid;

    private final AtomicInteger bidsCount = new AtomicInteger(0);

    public synchronized boolean propose(Bid bid) {
        if (latestBid == null) {
            latestBid = bid;
            bidsCount.incrementAndGet();
            return true;
        }
        if (bid.getPrice() > latestBid.getPrice()) {
            notifier.sendOutdatedMessage(latestBid);
            latestBid = bid;
            bidsCount.incrementAndGet();
            return true;
        }
        return false;
    }

    public synchronized Bid getLatestBid() {
        return latestBid;
    }

    @Override
    public int getBidsCount() {
        return bidsCount.get();
    }
}