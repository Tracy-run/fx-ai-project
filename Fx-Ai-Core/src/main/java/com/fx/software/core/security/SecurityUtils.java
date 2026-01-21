package com.fx.software.core.security;

import com.fx.software.core.utils.ThreadLocalUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.web.context.request.RequestContextHolder;
import org.apache.kafka.common.header.Header;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

/**
 * @FileName SecurityUtils
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
public class SecurityUtils {

    private static final String BEARER_STARTSTR = "Bearer ";
    private static final String TENANT_KEY = "tenants";

    public SecurityUtils() {
    }

    public static Optional<String> getCurrentUserLogin() {
        return null != RequestContextHolder.getRequestAttributes() ? Optional.ofNullable((String)RequestContextHolder.getRequestAttributes().getAttribute("TINY-AUTH-USERACCOUNT", 0)) : getCurrentUserLoginFromThreadLocal();
    }

    public static Optional<String> getRequestId() {
        return null != RequestContextHolder.getRequestAttributes() ? Optional.ofNullable((String)RequestContextHolder.getRequestAttributes().getAttribute("TINY-REQUEST-ID", 0)) : getRequestIdFromThreadLocal();
    }

    public static Optional<String> getTxContext() {
        if (null != RequestContextHolder.getRequestAttributes()) {
            Object attribute = RequestContextHolder.getRequestAttributes().getAttribute("tx-context", 0);
            return null == attribute ? getTxContextFromThreadLocal() : Optional.ofNullable((String)attribute);
        } else {
            return getTxContextFromThreadLocal();
        }
    }

    private static Optional<String> getTxContextFromThreadLocal() {
        Object o = ThreadLocalUtils.get("tx-context");
        return null != o ? Optional.ofNullable((String)o) : Optional.empty();
    }

    private static Optional<String> getRequestIdFromThreadLocal() {
        Object o = ThreadLocalUtils.get("TINY-REQUEST-ID");
        return null != o ? Optional.ofNullable((String)o) : Optional.empty();
    }

    private static Optional<String> getCurrentUserLoginFromThreadLocal() {
        Object o = ThreadLocalUtils.get("TINY-AUTH-USERACCOUNT");
        return null != o ? Optional.ofNullable((String)o) : Optional.empty();
    }

    public static Optional<String> getCurrentUserJWT() {
        return null != RequestContextHolder.getRequestAttributes() ? Optional.ofNullable((String)RequestContextHolder.getRequestAttributes().getAttribute("Authorization", 0)) : getCurrentUserJWTFromThreadLocal();
    }

    private static Optional<String> getCurrentUserJWTFromThreadLocal() {
        Object o = ThreadLocalUtils.get("Authorization");
        return null != o ? Optional.ofNullable((String)o) : Optional.empty();
    }

    /** @deprecated */
    @Deprecated
    public static Optional<String> getCurrentUserLoginFormKafka(ConsumerRecord<String, Object> consumerRecord) {
        Header header = consumerRecord.headers().lastHeader("TINY-AUTH-USERACCOUNT");
        return null == header ? Optional.empty() : Optional.of(new String(header.value()));
    }

    public static Optional<String> getRequestIdFormKafka(ConsumerRecord<String, Object> consumerRecord) {
        Header header = consumerRecord.headers().lastHeader("TINY-REQUEST-ID");
        return null == header ? Optional.empty() : Optional.of(new String(header.value()));
    }

    /** @deprecated */
    @Deprecated
    public static Optional<String> getCurrentUserJwtFormKafka(ConsumerRecord<String, Object> consumerRecord) {
        Header header = consumerRecord.headers().lastHeader("Authorization");
        return null == header ? Optional.empty() : Optional.of(new String(header.value()));
    }

    public static String createDebugToken(String secret, String useraccount, String tenants) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        long now = (new Date()).getTime();
        Date validity = new Date(now + 100000000000L);
        return "Bearer " + Jwts.builder().setSubject(useraccount).claim("tenants", tenants).signWith(key, SignatureAlgorithm.HS512).setExpiration(validity).compact();
    }

    public static Optional<String> getTxContextFormKafka(ConsumerRecord<String, Object> consumerRecord) {
        Header header = consumerRecord.headers().lastHeader("tx-context");
        return null == header ? Optional.empty() : Optional.of(new String(header.value()));
    }

}
