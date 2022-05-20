package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;
import net.jcip.annotations.NotThreadSafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

@NotThreadSafe
public class StampedLockAuction implements Auction {

    private final Notifier notifier = new Notifier();

    private final StampedLock lock = new StampedLock();

    private Bid latestBid;

    private final AtomicInteger bidsCount = new AtomicInteger(0);

    public boolean propose(Bid bid) {
        long stamp = lock.writeLock();

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
            lock.unlock(stamp);
        }
    }

    public Bid getLatestBid() {
        long stamp;
        do {
            stamp = lock.tryOptimisticRead();
        } while (!lock.validate(stamp));

        return latestBid;
    }

    @Override
    public int getBidsCount() {
        long stamp;
        do {
            stamp = lock.tryOptimisticRead();
        } while (!lock.validate(stamp));

        return bidsCount.get();
    }
}