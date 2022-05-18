package com.home.tests.checkpoint1;

public interface Auction {

    boolean propose(Bid bid);

    Bid getLatestBid();

    int getBidsCount();

}
