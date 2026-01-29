package com.fx.software.file.utils;

import com.fx.software.file.vo.ImageInitData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @FileName FileUtils
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Slf4j
public class FileUtils {

    public static String BUCKET_NAME = "filebucket";
    /**
     * 截取文件的后缀
     * 包含.号时，截取最后一个.的内容，若无.时，返回空字符串
     *
     * @param str 要截取的文件名
     * @return 文件的后缀
     */
    public static String getSuffix(String str) {
        if (StringUtils.isNotBlank(str)) {
            String containsValue = ".";
            if (str.contains(containsValue)) {
                String[] arr = str.split("\\.");
                return arr[arr.length - 1];
            }
        }
        return "";
    }



    public static String getId(){

        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String id = df.format(new Date());

        Random random = new SecureRandom() ;
        int min = 1000;
        int max = 9999;
        int s = random.nextInt(max) % (max - min + 1) + min;

        id = id + s ;
        return id;
    }

    /**
     * 将获取的字节数组保存为文件写入硬盘
     *
     * @param data
     * @param fileName
     */
    public static void writeImageToDisk(byte[] data, String fileName) throws Exception {
        FileOutputStream fops = null;
        try {
            // 本地目录
            File file = new File(fileName);
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
                file.createNewFile();
            }
            fops = new FileOutputStream(file);
            fops.write(data);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (fops != null) {
                fops.flush();
                fops.close();
            }

        }
    }

    /**
     * 获取远程http地址视图片
     *
     * @param strUrl
     * @return
     */
    public static byte[] getImageFromNetByUrl(String strUrl, ImageInitData initData) throws Exception {
        byte[] btData = new byte[0];
        ;
        HttpURLConnection conn = null;
        InputStream inStream = null;
        try {
            String method = initData.getPicRequestMethod();
            if(StringUtils.isBlank(method)){
                method="GET";
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            //添加属性信息
            String requestProperty = initData.getPicRequestProperty();
            if(StringUtils.isNotBlank(requestProperty)){
                JSONObject pros = JSONObject.parseObject(requestProperty);
                for (String key : pros.keySet()) {
                    conn.setRequestProperty(key,pros.getString(key));
                }
            }
            conn.setConnectTimeout(5 * 1000);
            inStream = conn.getInputStream();
            btData = readInputStream(inStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return btData;

    }

    /**
     * 读取流
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    private static byte[] readInputStream(InputStream inStream) throws Exception {
        byte[] array = new byte[0];
        ByteArrayOutputStream outStream = null;
        try {
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            array = outStream.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }
        return array;
    }
}
