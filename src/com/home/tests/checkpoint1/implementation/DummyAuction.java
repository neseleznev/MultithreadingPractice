package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;
import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class DummyAuction implements Auction {

    private final Notifier notifier = new Notifier();

    private Bid latestBid;

    public boolean propose(Bid bid) {
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
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}