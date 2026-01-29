package com.fx.software.es.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @FileName TransResultEvent
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Data
@Slf4j
public class TransResultEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String intId;

    private String zhLabel;

    private String indexValue;

    private String classEnName;

    /**
     * 操作类型，add,update,delete
     */
    private String opFlag;

    public byte[] toBytes(){
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bo);
            oos.writeObject(this);
            oos.flush();
            oos.close();
            bo.close();
        } catch (IOException e) {
            log.error("toBytes：",e);
        }
        return bo.toByteArray();
    }

    public TransResultEvent toObject(byte[] bytes){
        TransResultEvent document = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
            ObjectInputStream ois = new ObjectInputStream (bis);
            document = (TransResultEvent) ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ex) {
            log.error("Id2NameResultEvent：",ex);
        } catch (ClassNotFoundException ex) {
            log.error("Id2NameResultEvent：",ex);
        }
        return document;
    }


}
