package com.fx.software.cloud.client;

import com.fx.software.core.security.SecurityUtils;
import org.springframework.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @FileName RibbonRestTemplateConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
@ConditionalOnClass({RestTemplate.class, HttpClient.class})
public class RibbonRestTemplateConfiguration {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";

    public RibbonRestTemplateConfiguration() {
    }

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(this.getUserInterceptor());
        return restTemplate;
    }

    private List<ClientHttpRequestInterceptor> getUserInterceptor() {
        ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                SecurityUtils.getCurrentUserJWT().ifPresent((s) -> {
                    request.getHeaders().add("Authorization", String.format("%s %s", "Bearer", s));
                });
                SecurityUtils.getTxContext().ifPresent((s) -> {
                    request.getHeaders().add("tx-context", s);
                });
                return execution.execute(request, body);
            }
        };
        return Arrays.asList(interceptor);
    }

}
