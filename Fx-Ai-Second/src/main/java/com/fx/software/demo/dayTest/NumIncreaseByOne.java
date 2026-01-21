package com.fx.software.demo.dayTest;

/**
 * @FileName NumIncreaseByOne
 * @Description
 * @Author fx
 * @date 2025-11-24
 */
public class NumIncreaseByOne {


    private static final Object lock = new Object();
    private static int number = 1;
    private static final int MAX = 100;

    public static void main(String[] args) {
        Thread threadA = new Thread(new PrintTask(0), "Thread-A");
        Thread threadB = new Thread(new PrintTask(1), "Thread-B");
        Thread threadC = new Thread(new PrintTask(2), "Thread-C");

        threadA.start();
        threadB.start();
        threadC.start();

    }

    static class PrintTask implements Runnable {
        private final int remainder;

        public PrintTask(int remainder) {
            this.remainder = remainder;
        }

        @Override
        public void run() {
            while (number <= MAX) {
                synchronized (lock) {
                    // 检查当前线程是否该执行
                    while (number % 3 != remainder) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    if (number <= MAX) {
                        System.out.println(Thread.currentThread().getName() + ": " + number);
                        number++;
                    }
                    lock.notifyAll();
                }
            }
        }
    }







}
