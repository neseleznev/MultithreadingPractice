package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockAuction implements Auction {

    private final Notifier notifier = new Notifier();

    private final ReentrantLock lock = new ReentrantLock();

    private Bid latestBid;

    private final AtomicInteger bidsCount = new AtomicInteger(0);

    public boolean propose(Bid bid) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    public Bid getLatestBid() {
        lock.lock();
        try {
            return latestBid;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getBidsCount() {
        return bidsCount.get();
    }
}