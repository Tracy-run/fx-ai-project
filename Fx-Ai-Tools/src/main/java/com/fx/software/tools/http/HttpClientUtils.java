package com.fx.software.tools.http;

import com.fx.software.core.exception.UnknowException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @FileName HttpClientUtils
 * @Description
 * @Author fx
 * @date 2026-01-17
 */
@Slf4j
public class HttpClientUtils {

    /**
     * 处理get请求.
     * @param url  请求路径
     * @return  json
     */
    public String get(String url)
    {
        //实例化httpclient
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //实例化get方法
        HttpGet httpget = new HttpGet(url);
        //请求结果
        CloseableHttpResponse response = null;
        String content = "";
        try
        {
            //执行get方法
            response = httpclient.execute(httpget);
            if(response.getStatusLine().getStatusCode() == 200)
            {
                content = EntityUtils.toString(response.getEntity(), "utf-8");
                log.debug(content);
            }
            else
                log.error(response.toString());
        }
        catch(Exception e)
        {
            log.error(e.getMessage(), e);
        }finally {
            try {
                httpclient.close();
            }catch (Exception e){
                log.error("==httpclient==关闭===");
            }
        }
        return content;
    }

    /**
     * 处理post请求.
     * @param url  请求路径
     * @param params  参数
     * @return  json
     */
    public String post(String url, Map<String, String> headers, Map<String, String> params)
    {
        //实例化httpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //实例化post方法
        HttpPost httpPost = new HttpPost(url);
        //处理header
        if (headers != null ) {
            for(String key : headers.keySet())
            {
                httpPost.setHeader(key, headers.get(key));
            }
        }
        //处理参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keySet = params.keySet();
        for(String key : keySet)
        {
            nvps.add(new BasicNameValuePair(key, params.get(key)));
        }
        //结果
        CloseableHttpResponse response = null;
        String content = "";
        try
        {
            //提交的参数
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(nvps, "UTF-8");
            //将参数给post方法
            httpPost.setEntity(uefEntity);
            //执行post方法
            response = httpclient.execute(httpPost);
            if(response.getStatusLine().getStatusCode() == 200)
            {
                content = EntityUtils.toString(response.getEntity(), "utf-8");
                log.debug(content);
            }
            else
                log.error(response.toString());
        }
        catch(Exception e)
        {
            log.error(e.getMessage(), e);
        }finally {
            try {
                httpclient.close();
            }catch (Exception e){
                log.error("==httpclient==关闭===");
            }
        }
        return content;
    }

    public String doPost(String url, String params, String token) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        if (token != null) {
            httpPost.setHeader("token", token);
            httpPost.setHeader("Authorization", token);
        }
        String charSet = "UTF-8";
        StringEntity entity = new StringEntity(params, charSet);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            if (state == org.apache.http.HttpStatus.SC_OK) {
                HttpEntity responseEntity = response.getEntity();
                String jsonString = EntityUtils.toString(responseEntity);
                return jsonString;
            } else {
                log.error("请求返回:" + state + "(" + url + ")");
            }
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *
     * @param url
     * @param jsonArray
     * @param encoding
     * @return
     */
    public String postJson(String url, JSONArray jsonArray, String encoding){
        String body = "";
        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            //创建post方式请求对象
            HttpPost httpPost = new HttpPost(url);
            //装填参数
            StringEntity s = new StringEntity(jsonArray.toString(), "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
            //设置参数到请求对象中
            httpPost.setEntity(s);
            httpPost.setHeader("Content-type", "application/json");
            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpPost);
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, encoding);
            }
            EntityUtils.consume(entity);
            //释放链接
            response.close();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                client.close();
            }catch (Exception e){
                log.error("==httpclient==关闭===");
            }
        }
        return body;
    }

    /**
     * @Description: JSON传参方式
     * @Date: 2022/3/17
     * @Param: [url, json]
     * @return java.lang.String
     */
    public String post(String url, JSONObject json, String token) throws Exception{
        HttpPost httpPost = null;
        String body = "";
        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            //创建post方式请求对象
            httpPost = new HttpPost(url);
            if (token != null) {
                httpPost.setHeader("token", token);
                httpPost.setHeader("Authorization", token);
            }
            //装填参数
            StringEntity s = new StringEntity(json.toString(), "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
            //设置参数到请求对象中
            httpPost.setEntity(s);
            httpPost.setHeader("Content-type", "application/json");
            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpPost);
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            //释放链接
            response.close();
            log.info("==============post接口返回===={}=====",body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnknowException("接口【" + url + "】获取参数异常");
        }finally{
            if(httpPost != null){
                try {
                    httpPost.releaseConnection();
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            client.close();
        }
    }
}
