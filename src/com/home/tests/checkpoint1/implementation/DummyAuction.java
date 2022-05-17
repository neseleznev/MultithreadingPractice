package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Auction;
import com.home.tests.checkpoint1.Bid;
import lombok.SneakyThrows;
import net.jcip.annotations.NotThreadSafe;

import static com.home.tests.checkpoint1.Constants.MESSAGE_SEND_TIME_MILLIS;

@NotThreadSafe
public class DummyAuction implements Auction {

    public static class Notifier {
        public void sendOutdatedMessage(Bid bid) {
            try {
                Thread.sleep(MESSAGE_SEND_TIME_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

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