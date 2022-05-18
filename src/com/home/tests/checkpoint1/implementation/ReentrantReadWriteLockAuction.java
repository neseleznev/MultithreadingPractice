package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;
import net.jcip.annotations.NotThreadSafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@NotThreadSafe
public class ReentrantReadWriteLockAuction implements Auction {

    private final Notifier notifier = new Notifier();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private Bid latestBid;

    private final AtomicInteger bidsCount = new AtomicInteger(0);

    public boolean propose(Bid bid) {
        lock.writeLock().lock();
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
            lock.writeLock().unlock();
        }
    }

    public Bid getLatestBid() {
        lock.readLock().lock();
        try {
            return latestBid;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int getBidsCount() {
        return bidsCount.get();
    }
}