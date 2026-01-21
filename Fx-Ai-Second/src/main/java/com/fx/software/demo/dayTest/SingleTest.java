package com.fx.software.demo.dayTest;

/**
 * @FileName SingleTest
 * @Description
 * @Author fx
 * @date 2025-11-18
 */
public class SingleTest {


    private SingleTest(){};

    private static SingleTest singleSantence;

    public static synchronized SingleTest SingleTest(){
        if(null == singleSantence){
            singleSantence = new SingleTest();
        }
        return singleSantence;
    }


}
