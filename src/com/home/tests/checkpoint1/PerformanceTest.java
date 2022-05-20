package com.home.tests.checkpoint1;


import com.home.tests.checkpoint1.implementation.ReentrantLockAuction;
import com.home.tests.checkpoint1.implementation.ReentrantReadWriteLockAuction;
import com.home.tests.checkpoint1.implementation.SynchronizedAuction;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PerformanceTest {

    private static final long MAX_LATEST_BID = 1_000_000L;
    private static final long TIMEOUT_SECONDS = 10L;

    @SneakyThrows
    public static void main(String[] args) {
        List<Supplier<Auction>> auctions = Arrays.asList(new Supplier[]{
//                DummyAuction::new,
                SynchronizedAuction::new,
                ReentrantLockAuction::new,
                ReentrantReadWriteLockAuction::new,
        });
        List<Integer> workerCounts = List.of(1, 2, 10, 100, 1000);


        // JVM warmup
        for (var auction : auctions) {
            for (int workerCount : List.of(1, 10)) {
                ExecutorService executorService1 = createCachedExecutorServiceOfCpuCoreSize();

                List<Bid> bids = generateHighlyConcurrentBidsSequence(workerCount);
                perform(auction.get(), bids, executorService1);

                finishAllTasksByTimeout(executorService1);
            }
        }

        for (var auctionSupplier : auctions) {
            System.out.println(String.format("Auction: %s", auctionSupplier.get().getClass().getSimpleName()));

            for (int workerCount : workerCounts) {
                ExecutorService executorService = createCachedExecutorServiceOfCpuCoreSize();
                Auction auction = auctionSupplier.get();

                long start = System.currentTimeMillis();

                List<Bid> bids = generateHighlyConcurrentBidsSequence(workerCount);
                perform(auction, bids, executorService);

                finishAllTasksByTimeout(executorService);

                long elapsed = System.currentTimeMillis() - start;
                System.out.println(String.format("Threads: %04d elapsed %.3f sec\tBids count: %d",
                        workerCount,
                        elapsed / 1000.,
                        auction.getBidsCount()));
            }
        }
    }

    private static ExecutorService createCachedExecutorServiceOfCpuCoreSize() {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        return new ThreadPoolExecutor(
                corePoolSize, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    private static void finishAllTasksByTimeout(ExecutorService executorService) throws InterruptedException {
        executorService.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        executorService.shutdownNow();
    }

    private static List<Bid> generateHighlyConcurrentBidsSequence(int workerCount) {
        List<Bid> result = new ArrayList<>();

        for (long currentLatestBidPrice = 0L; currentLatestBidPrice < MAX_LATEST_BID; ++currentLatestBidPrice) {
            List<Integer> participantIds = new ArrayList<>();
            for (int i = 0; i < workerCount; ++i) {
                participantIds.add(i);
            }
            Collections.shuffle(participantIds);

            for (long participantId : participantIds) {
                result.add(new Bid(participantId, participantId, currentLatestBidPrice));
            }
        }
        return result;
    }

    private static void perform(Auction auction,
                                List<Bid> bids,
                                ExecutorService executorService) {
        for (Bid bid : bids) {
            executorService.submit(() -> {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                auction.propose(bid);
            });
        }
    }

}
