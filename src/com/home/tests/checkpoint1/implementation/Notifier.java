package com.home.tests.checkpoint1.implementation;

import com.home.tests.checkpoint1.Bid;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.home.tests.checkpoint1.Constants.MESSAGE_SEND_TIME_MILLIS;

public class Notifier {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1_000);

    public void sendOutdatedMessage(Bid bid) {
        executorService.submit(() -> {
            try {
                Thread.sleep(MESSAGE_SEND_TIME_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

}
