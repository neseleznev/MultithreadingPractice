package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;
import lombok.Getter;
import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class DummyAuction implements Auction {

    private final Notifier notifier = new Notifier();

    private Bid latestBid;

    @Getter
    private int bidsCount = 0;

    public boolean propose(Bid bid) {
        if (latestBid == null) {
            latestBid = bid;
            ++bidsCount;
            return true;
        }
        if (bid.getPrice() > latestBid.getPrice()) {
            notifier.sendOutdatedMessage(latestBid);
            latestBid = bid;
            ++bidsCount;
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}