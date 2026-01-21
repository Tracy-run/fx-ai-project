package com.fx.software.core.security;

import com.fx.software.core.config.TinyConfiguration;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Enumeration;

/**
 * @FileName JwtFilter
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
public class JwtFilter implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    private static final String BEARER_STARTSTR = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTH_PREFIX = "/api/";

    TinyConfiguration tinyConfiguration;
    private Key key;

    public JwtFilter(TinyConfiguration tinyConfiguration) {
        this.tinyConfiguration = tinyConfiguration;
        this.key = Keys.hmacShaKeyFor(tinyConfiguration.getSecurity().getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private boolean excloudUri(String uri) {
        String exclude = (String) StringUtils.defaultIfEmpty(this.tinyConfiguration.getSecurity().getExclude(), "");
        return StringUtils.contains(exclude, uri);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String jwt = this.resolveToken(request);
        if (jwt == null) {
            if (!request.getRequestURI().startsWith("/api/")) {
                log.warn("依照TinyFramework约定：本次访问的路径{}并没有以{}为前缀，不做权限限制", request.getRequestURI(), "/api/");
                return true;
            } else if (this.excloudUri(request.getRequestURI())) {
                return true;
            } else {
                response.setStatus(401);
                return false;
            }
        } else if (this.excloudUri(request.getRequestURI())) {
            return true;
        } else {
            String user = this.verifyTokenAndGetUseraccount(jwt);
            if (org.springframework.util.StringUtils.isEmpty(user)) {
                response.setStatus(401);
                return false;
            } else {
                request.setAttribute("TINY-AUTH-USERACCOUNT", user);
                request.setAttribute("TINY-REQUEST-ID", request.getHeader("TINY-REQUEST-ID"));
                request.setAttribute("Authorization", request.getHeader("Authorization"));
                this.setOtherAttributesFromHeader(request);
                return true;
            }
        }
    }

    private void setOtherAttributesFromHeader(HttpServletRequest request) {
        Enumeration headerNames = request.getHeaderNames();

        while(headerNames.hasMoreElements()) {
            String headerKey = (String)headerNames.nextElement();
            if (!StringUtils.equalsAny(headerKey, new CharSequence[]{"Connection", "Accept", "User-Agent", "Content-Type", "Origin", "Referer", "Accept-Language"})) {
                request.setAttribute(headerKey, request.getHeader(headerKey));
            }
        }

        request.setAttribute("Authorization", request.getHeader("Authorization"));
        request.setAttribute("tx-context", request.getHeader("tx-context"));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return org.springframework.util.StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ") ? bearerToken.substring(7, bearerToken.length()) : null;
    }

    String verifyTokenAndGetUseraccount(String jwtStr) throws SecurityException, MalformedJwtException, ExpiredJwtException, UnsupportedJwtException, IllegalArgumentException {
        if (!jwtStr.isEmpty()) {
            Claims claims = (Claims) Jwts.parser().setSigningKey(this.key).parseClaimsJws(jwtStr).getBody();
            String user = claims.getSubject();
            return user != null && !user.isEmpty() ? user : null;
        } else {
            return null;
        }
    }


}
