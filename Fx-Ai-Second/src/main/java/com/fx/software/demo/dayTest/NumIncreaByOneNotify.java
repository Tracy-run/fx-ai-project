package com.fx.software.demo.dayTest;


import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @FileName NumIncreaByOneNotify
 * @Description
 * @Author fx
 * @date 2025-11-24
 */
public class NumIncreaByOneNotify {

    //同步方案的synchronized + wait/notify
    private final Object lock = new Object();

    private final int MAX=100;

    private  int counter = 1;


    public void test(String threadName,boolean printOld){
        while (true){
            synchronized (lock){
                //数值超过max  退出
                if(counter > MAX){
                    lock.notifyAll();
                    break;
                }

                if((counter % 2 ==1) == printOld){
                    System.out.printf("%s:%d " , threadName , counter);
                    System.out.println();
                    counter++;
                    lock.notify();
                }else{
                    try {
                        lock.wait();
                    }catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }


    public static void main(String[] args) {
        NumIncreaByOneNotify numIncreaByOneNotify = new NumIncreaByOneNotify();
        Thread threadA = new Thread(() -> numIncreaByOneNotify.test("TTTTT111 ",true),"thread1111");
        Thread threadB = new Thread(() -> numIncreaByOneNotify.test("TTTTT22222 ",false),"thread222222");

        threadA.start();
        threadB.start();

        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        concurrentHashMap.put(null,null);
        System.out.println(concurrentHashMap.keySet().iterator());
        new TreeSet();

        ArrayList<Object> objects = new ArrayList<>();

        try{
            threadA.join();
            threadB.join();
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }

    }






}
