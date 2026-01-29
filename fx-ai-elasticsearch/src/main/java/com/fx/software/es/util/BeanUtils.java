package com.fx.software.es.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @FileName BeanUtils
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Slf4j
public class BeanUtils {

    /**
     * 对象序列化为byte数组
     *
     * @param obj
     * @return
     */
    public static byte[] beanToByte(Object obj) {
        byte[] bb = null;
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteArray)){
            outputStream.writeObject(obj);
            outputStream.flush();
            bb = byteArray.toByteArray();
        } catch (IOException e) {
            log.error("beanToByte:", e);
        }
        return bb;
    }

    /**
     * 字节数组转为Object对象
     *
     * @param bytes
     * @return
     */
    public static Object byteToObj(byte[] bytes) {
        Object readObject = null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             ObjectInputStream inputStream = new ObjectInputStream(in)){
            readObject = inputStream.readObject();
        } catch (Exception e) {
            log.error("byteToObj:", e);
            //反序列化失败，则直接结束程序，保证数据完整
            System.err.println("反序列化对象失败！");

        }
        return readObject;
    }

}
