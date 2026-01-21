package com.fx.software.demo.dayTest;

import java.util.concurrent.Semaphore;

/**
 * @FileName SemaphoreNumIncrOne
 * @Description
 * @Author fx
 * @date 2025-11-24
 */
public class SemaphoreNumIncrOne {

    private static int number = 1;
    private static final int MAX = 100;
    private static Semaphore semaphoreA = new Semaphore(1); // A先执行
    private static Semaphore semaphoreB = new Semaphore(0); // B等待

    public static void main(String[] args) {
        Thread threadA = new Thread(() -> {
            while (number <= MAX) {
                try {
                    semaphoreA.acquire();
                    if (number <= MAX) {
                        System.out.println("Thread-AAAAA: " + number++);
                    }
                    semaphoreB.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread threadB = new Thread(() -> {
            while (number <= MAX) {
                try {
                    semaphoreB.acquire();
                    if (number <= MAX) {
                        System.out.println("Thread-B: " + number++);
                    }
                    semaphoreA.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        threadA.start();
        threadB.start();
    }
}
