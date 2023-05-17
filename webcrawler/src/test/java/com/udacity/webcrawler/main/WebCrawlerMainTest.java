package com.udacity.webcrawler.main;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.truth.Truth.assertThat;

public class WebCrawlerMainTest {


    @Test
    public void testThreadSafe() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for(int i = 0; i< numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    WebCrawlerMain.main(new String[]{"src/main/config/sample_config.json"});
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                latch.countDown();
            });
        }
        latch.await();
        service.shutdown();
    }
}
