package com.fx.software.demo.dayTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @FileName CampletableFutrueTest
 * @Description
 * @Author fx
 * @date 2025-12-02
 */
public class CampletableFutrueTest {

    static final CountDownLatch LATCH_a = new CountDownLatch(2);
    static final CountDownLatch LATCH_b = new CountDownLatch(2);

    public static void main1(String[] args) {
        CompletableFuture.runAsync(() -> System.out.println("A --- 1"))
                .thenRun(() -> System.out.println("BB---2"))
                .thenRun(() -> System.out.println("C---3"))
                .join();


    }


    public static void main(String[] args) throws Exception{

        new Thread(() ->{
            System.out.println("aaa ----1 ");
            LATCH_a.countDown();
        }).start();

        //阻塞
        LATCH_a.await();

        new Thread(() ->{
            System.out.println("bbb ----2 ");
            LATCH_b.countDown();
        }).start();
        LATCH_b.await();
        System.out.println("cccc----3");
    }

}
