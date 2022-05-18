package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;
import net.jcip.annotations.NotThreadSafe;

import java.util.concurrent.locks.ReentrantLock;

@NotThreadSafe
public class ReentrantLockAuction implements Auction {

    private final Notifier notifier = new Notifier();

    private final ReentrantLock lock = new ReentrantLock();

    private Bid latestBid;

    public boolean propose(Bid bid) {
        lock.lock();
        try {
            if (latestBid == null) {
                latestBid = bid;
                return true;
            }
            if (bid.getPrice() > latestBid.getPrice()) {
                notifier.sendOutdatedMessage(latestBid);
                latestBid = bid;
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
}