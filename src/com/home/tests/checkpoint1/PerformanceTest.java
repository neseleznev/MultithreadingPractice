package com.home.tests.checkpoint1;


import com.home.tests.checkpoint1.implementation.*;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PerformanceTest {

    private static final long MAX_LATEST_BID = 10_000_000L;
    private static final long STOP_AUCTION_AFTER_BID = 1_000_000L;
    private static final long TIMEOUT_SECONDS = 10L;

    @SneakyThrows
    public static void main(String[] args) {
        List<Supplier<Auction>> auctions = Arrays.asList(new Supplier[]{
//                DummyAuction::new,
                OptimisticWriteAuction::new,
                StampedLockAuction::new,
                SynchronizedAuction::new,
                ReentrantLockAuction::new,
                ReentrantReadWriteLockAuction::new,
        });
        List<Integer> workerCounts = List.of(1, 2, 10, 100, 1000);


        // JVM warmup
//        for (var auction : auctions) {
//            for (int workerCount : List.of(1)) {
//                ExecutorService executorService1 = createCachedExecutorServiceOfCpuCoreSize();
//                List<Bid> bids = generateHighlyConcurrentBidsSequence(workerCount);
//                long start = System.currentTimeMillis() - (TIMEOUT_SECONDS - 1) * 1_000;
//
//                perform(auction.get(), bids, executorService1,workerCount);
//
//                finishAllTasksByTimeout(executorService1, start);
//            }
//        }

        for (var auctionSupplier : auctions) {
            System.out.println(String.format("Auction: %s", auctionSupplier.get().getClass().getSimpleName()));

            for (int workerCount : workerCounts) {
                ExecutorService executorService = createCachedExecutorServiceOfCpuCoreSize();
                Auction auction = auctionSupplier.get();
                List<Bid> bids = generateHighlyConcurrentBidsSequence();

                long start = System.currentTimeMillis();

                perform(auction, bids, executorService);

                finishAllTasksByTimeout(executorService, start);

                long elapsed = System.currentTimeMillis() - start;
                System.out.println(String.format("Threads: %04d " +
                                "elapsed %.3f sec\t" +
                                "Bids count: %d\t" +
                                "Bids/sec: %d\t" +
                                "Bid price: %d",
                        workerCount,
                        elapsed / 1000.,
                        auction.getBidsCount(),
                        (int) (auction.getBidsCount() / (elapsed / 1000.)),
                        getBidPrice(auction)));
            }
        }
    }

    private static ExecutorService createCachedExecutorServiceOfCpuCoreSize() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                corePoolSize, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    private static void finishAllTasksByTimeout(ExecutorService executorService,
                                                long start) throws InterruptedException {
        executorService.awaitTermination(TIMEOUT_SECONDS * 1_000 - start, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();
    }

    private static Long getBidPrice(Auction auction) {
        return Optional.ofNullable(auction.getLatestBid())
                .map(Bid::getPrice)
                .orElse(1L);
    }

    private static List<@Nullable Bid> generateHighlyConcurrentBidsSequence() {
        List<Bid> result = new ArrayList<>();

        for (long currentLatestBidPrice = 0L; currentLatestBidPrice < MAX_LATEST_BID; ++currentLatestBidPrice) {
            if (currentLatestBidPrice == STOP_AUCTION_AFTER_BID) {
                result.add(null);
                continue;
            }
            long participantId = 1L;
            result.add(new Bid(participantId, participantId, currentLatestBidPrice));
        }
        return result;
    }

    private static void perform(Auction auction,
                                List<@Nullable Bid> bids,
                                ExecutorService executorService) {
        long start = System.currentTimeMillis();

        for (Bid bid : bids) {
            if (System.currentTimeMillis() - start > 1_000 * TIMEOUT_SECONDS) {
                // Do not load too many tasks, it is pointless
                break;
            }
            if (bid == null) {
                executorService.submit(auction::stopAuction);
                continue;
            }
            executorService.submit(() -> {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                auction.propose(bid);
            });
        }
    }

}
