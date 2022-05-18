package com.home.tests.checkpoint1;


import com.home.tests.checkpoint1.implementation.DummyAuction;
import com.home.tests.checkpoint1.implementation.ReentrantLockAuction;
import com.home.tests.checkpoint1.implementation.ReentrantReadWriteLockAuction;
import com.home.tests.checkpoint1.implementation.SynchronizedAuction;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PerformanceTest {

    private static final Auction DUMMY_AUCTION = new DummyAuction();
    private static final Auction SYNCHRONIZED_AUCTION = new SynchronizedAuction();

    @SneakyThrows
    public static void main(String[] args) {
        List<Supplier<Auction>> auctions = Arrays.asList(new Supplier[]{
//                DummyAuction::new,
                SynchronizedAuction::new,
                ReentrantLockAuction::new,
                ReentrantReadWriteLockAuction::new,
        });
//        List<Integer> threadCounts = List.of(1, 2, 5, 10, 100, 200, 500, 1000);
        List<Integer> threadCounts = List.of(1, 2, 10, 100, 1000);
        long timeoutSeconds = 10L;


        // JVM warmup
        for (var auction : auctions) {
            long maxBidPrice = 1_000_000;
            for (int threadCount : List.of(1, 1000)) {
                ExecutorService executorService1 = new ThreadPoolExecutor(
                        threadCount, threadCount,
                        60L, TimeUnit.SECONDS,
                        new SynchronousQueue<>());
                perform(auction.get(), maxBidPrice, threadCount, executorService1);
                executorService1.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
                executorService1.shutdownNow();
            }
        }

        for (var auctionSupplier : auctions) {
            System.out.println(String.format("Auction: %s", auctionSupplier.get().getClass().getSimpleName()));

            for (int threadCount : threadCounts) {
                ExecutorService executorService = new ThreadPoolExecutor(
                        threadCounts.get(threadCounts.size() - 1), threadCounts.get(threadCounts.size() - 1),
                        60L, TimeUnit.SECONDS,
                        new SynchronousQueue<>());
                Auction auction = auctionSupplier.get();
                long maxBidPrice = 1_000_000;

                long start = System.currentTimeMillis();

                perform(auction, maxBidPrice, threadCount, executorService);
                executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
                executorService.shutdownNow();

                long elapsed = System.currentTimeMillis() - start;
                System.out.println(String.format("Threads: %04d elapsed %.3f sec\tLast price: %d",
                        threadCount,
                        elapsed / 1000.,
                        getBidPrice(auction)));
            }
        }
    }

    private static void perform(Auction auction,
                                long maxBidPrice,
                                int threadCount,
                                ExecutorService executorService) {
        for (int i = 0; i < threadCount; ++i) {
            long participantId = i;

            executorService.submit(() -> {
                long bidPrice = getBidPrice(auction);
                int unsuccessfulAttempts = 0;


                outerLoop:
                while (bidPrice < maxBidPrice) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    while (bidPrice < maxBidPrice
                            && !auction.propose(new Bid(participantId, participantId, bidPrice))) {
                        if (Thread.currentThread().isInterrupted()) {
                            break outerLoop;
                        }
                        bidPrice = getBidPrice(auction) + unsuccessfulAttempts + 1;
                        ++unsuccessfulAttempts;
                    }
                    if (bidPrice < maxBidPrice) {
//                        randomVeryShortTask();
//                        System.out.println(String.format("%s: %d / %d", Thread.currentThread().getName(), bidPrice, unsuccessfulAttempts));
                    }
                    unsuccessfulAttempts = 0;
                }
            });
        }
    }

    private static Long getBidPrice(Auction auction) {
        return Optional.ofNullable(auction.getLatestBid())
                .map(Bid::getPrice)
                .orElse(1L);
    }

    private static final Random random = new Random();

    private static void randomVeryShortTask() {
        int randInt = random.nextInt(10_000);
        for (int cnt = 1000; cnt < 1000 + randInt; ++cnt) {
            int square = (int) (cnt * cnt / (double) (cnt + cnt << 1));
            ++square;
        }
    }
}
