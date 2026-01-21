package com.fx.software.core.gis;

import com.fx.software.core.exception.GisException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;

/**
 * @FileName ArcgisService
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Service
public class ArcgisService {

    private static final Logger log = LoggerFactory.getLogger(ArcgisService.class);
    @Value("${arcgis.generateToken.url:}")
    private String url;
    @Value("${arcgis.generateToken.username:}")
    private String username;
    @Value("${arcgis.generateToken.password:}")
    private String password;
    @Value("${arcgis.generateToken.expiration:}")
    private String expiration;
    @Value("${arcgis.generateToken.client:}")
    private String client;
    @Value("${arcgis.generateToken.ip:}")
    private String ip;

    public ArcgisService() {
    }

    public String generateToken() throws Exception {
        if (StringUtils.isEmpty(this.url)) {
            log.error("地址未配置");
            throw new GisException("地址未配置");
        } else {
            PostMethod postMethod = new PostMethod(this.url);
            postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            NameValuePair[] data = null;
            if ("ip".equals(this.client)) {
                log.info("========ip======");
                data = new NameValuePair[]{new NameValuePair("username", this.username), new NameValuePair("password", this.password), new NameValuePair("expiration", this.expiration), new NameValuePair("ip", this.ip), new NameValuePair("client", this.client), new NameValuePair("f", "json")};
            } else {
                log.info("==========2211=====");
                data = new NameValuePair[]{new NameValuePair("username", this.username), new NameValuePair("password", this.password), new NameValuePair("expiration", this.expiration), new NameValuePair("client", this.client), new NameValuePair("encrypted", "false"), new NameValuePair("f", "json")};
            }

            log.info("urlLLLL===" + this.url);
            log.info("usernameEEE=" + this.username + "    password=" + this.password + "    expiration=" + this.expiration + "    client=" + this.client + "    encrypted=false  f=json");
            postMethod.setRequestBody(data);
            HttpClient httpClient = new HttpClient();
            int response = httpClient.executeMethod(postMethod);
            String result = postMethod.getResponseBodyAsString();
            if (200 != response) {
                throw new GisException("调用异常:" + result);
            } else {
                log.info("调用结果:" + result);
                return result;
            }
        }
    }

}
