package com.fx.software.cloud.client;

import com.fx.software.core.security.SecurityUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @FileName UserFeignClientInterceptor
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Component
public class UserFeignClientInterceptor implements RequestInterceptor {

    public UserFeignClientInterceptor() {
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        SecurityUtils.getCurrentUserJWT().ifPresent((s) -> {
            requestTemplate.header("Authorization", new String[]{s});
        });
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
            String requestId = request.getHeader("TINY-REQUEST-ID");
            if (StringUtils.isNotEmpty(requestId)) {
                requestTemplate.header("TINY-REQUEST-ID", new String[]{requestId});
            }

            SecurityUtils.getCurrentUserLogin();
            String useraccount = request.getHeader("TINY-AUTH-USERACCOUNT");
            if (StringUtils.isNotEmpty(useraccount)) {
                requestTemplate.header("TINY-AUTH-USERACCOUNT", new String[]{useraccount});
            }

            String txContext = request.getHeader("tx-context");
            if (StringUtils.isNotEmpty(txContext)) {
                requestTemplate.header("tx-context", new String[]{txContext});
            } else {
                SecurityUtils.getTxContext().ifPresent((s) -> {
                    requestTemplate.header("tx-context", new String[]{s});
                });
            }

            String ip = request.getHeader("X-Forwarded-For");
            if (StringUtils.isNotEmpty(ip)) {
                requestTemplate.header("X-Forwarded-For", new String[]{ip});
            }
        } else {
            SecurityUtils.getCurrentUserLogin().ifPresent((s) -> {
                requestTemplate.header("TINY-AUTH-USERACCOUNT", new String[]{s});
            });
            SecurityUtils.getRequestId().ifPresent((s) -> {
                requestTemplate.header("TINY-REQUEST-ID", new String[]{s});
            });
            SecurityUtils.getTxContext().ifPresent((s) -> {
                requestTemplate.header("tx-context", new String[]{s});
            });
        }

    }
}
