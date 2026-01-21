package com.fx.software.tools.urlchange;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
/**
 * @FileName XORScrambledShortURL
 * @Description XOR移位加密
 * @Author fx
 * @date 2026-01-21
 */
public class XORScrambledShortURL {
    private static final String BASE62 = "7nJqDcR9sF5wLzK8JyT2hVbG3xMpQ4gXeC6rAmW0iZoUaSlBdEfHuIjOkY1N";
    private static final String SHORT_DOMAIN = "https://xs.url/";

    private final ConcurrentHashMap<String, String> storage = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(ThreadLocalRandom.current().nextLong(1000000));
    private final long xorMask;

    public XORScrambledShortURL() {
        this.xorMask = ThreadLocalRandom.current().nextLong(0xFFFFFFFFFFFFL);
    }

    public String shorten(String longUrl) {
        // UTF-8验证
        validateUTF8(longUrl);

        return storage.computeIfAbsent(longUrl, url -> {
            // 生成ID
            long id = counter.incrementAndGet();

            // 随机扰动
            id ^= xorMask;
            id = Long.reverseBytes(id);
            id ^= (id << 21);
            id ^= (id >>> 35);
            id ^= (id << 4);

            // 编码
            String code = base62Encode(Math.abs(id));

            return SHORT_DOMAIN + code;
        });
    }

    private String base62Encode(long value) {
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int)(value % BASE62.length())));
            value /= BASE62.length();
        }
        // 固定长度8位
        while (sb.length() < 8) {
            sb.append(BASE62.charAt(ThreadLocalRandom.current().nextInt(BASE62.length())));
        }
        return sb.reverse().toString();
    }

    private void validateUTF8(String text) {
        try {
            // 强制UTF-8转换
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid UTF-8 encoding", e);
        }
    }

    public static void main(String[] args) {
        // 设置JVM参数确保UTF-8
        System.setProperty("file.encoding", "UTF-8");

        XORScrambledShortURL service = new XORScrambledShortURL();

        System.out.println("乱序短URL生成测试:");
        System.out.println("====================");

        for (int i = 0; i < 15; i++) {
            String url = "https://example.com/page/" + (10000 + i);
            String shortUrl = service.shorten(url);
            System.out.printf("URL %2d: %s\n", i + 1, shortUrl);

            // 添加中文URL测试
            if (i % 3 == 0) {
                String chineseUrl = "https://example.com/中文页面/" + i;
                String chineseShort = service.shorten(chineseUrl);
                System.out.printf("中文URL: %s -> %s\n",
                        chineseUrl.substring(0, Math.min(20, chineseUrl.length())) + "...",
                        chineseShort);
            }
        }
    }
}
