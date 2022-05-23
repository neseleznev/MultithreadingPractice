package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicInteger;

public class OptimisticWriteAuction implements Auction {

    private final Notifier notifier = new Notifier();

    private Bid latestBid = new Bid(1L, 1L, 0L); // Initialized price for simplicity

    private final AtomicInteger bidsCount = new AtomicInteger(0);

    private volatile boolean stopped = false;

    public boolean propose(Bid bid) {
        if (stopped) {
            return false;
        }
//            if (latestBid == null) {
//                latestBid = bid;
//                bidsCount.incrementAndGet();
//                return true;
//            }
        boolean success;
        Bid localLatestBid;
        boolean priceUpdated;

        do {
            localLatestBid = latestBid;

            if (latestBid.getPrice() < bid.getPrice()) {
                success = LATEST_BID.compareAndSet(this, latestBid, bid);
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
        return latestBid;
    }

    @Override
    public int getBidsCount() {
        return bidsCount.get();
    }

    @Override
    public void stopAuction() {
        stopped = true;
    }

    // VarHandle mechanics
    private static final VarHandle LATEST_BID;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            LATEST_BID = l.findVarHandle(OptimisticWriteAuction.class, "latestBid", Bid.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}