package com.fx.software.demo.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class CallableThreadDemo implements Callable {


    @Override
    public Object call() throws Exception {
        return Thread.currentThread().getName() + "执行完毕";
    }
}


class MainRun{
    public static void main(String[] args) {
       try {
           Callable<String> callAble = new CallableThreadDemo();
           FutureTask<String> stringFutureTask = new FutureTask<>(callAble);


           Thread thread = new Thread(stringFutureTask);
           thread.start();

           //获取线程执行结果（会阻塞直到完成）
           String s = stringFutureTask.get();
           System.out.println(s + "执行结果");
       }catch (Exception e){
           System.out.println(e.getMessage());
       }

    }




}

