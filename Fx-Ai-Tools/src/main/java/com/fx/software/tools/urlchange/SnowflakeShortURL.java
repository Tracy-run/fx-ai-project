package com.fx.software.tools.urlchange;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @FileName SnowflakeShortURL
 * @Description
 * 全局唯一性：雪花算法生成的ID在分布式系统中全局唯一
 * 时间有序：ID按时间递增，便于排序和索引
 * 高性能：本地生成，无需数据库交互
 * 可扩展：支持多数据中心和多工作节点
 * 短码长度可控：Base62编码可控制输出长度
 * @Author fx
 * @date 2026-01-21
 */
public class SnowflakeShortURL {

    // Base62字符集
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE62_LENGTH = BASE62.length();

    // 短域名前缀
    private static final String SHORT_DOMAIN = "https://s.url/";

    // 雪花算法参数  // 2024-01-01 00:00:00
    private static final long START_TIMESTAMP = 1704067200000L;

    // 各部分的位数
    // 序列号占用的位数
    private static final long SEQUENCE_BITS = 12L;
    // 工作ID占用的位数
    private static final long WORKER_ID_BITS = 5L;
    // 数据中心ID占用的位数
    private static final long DATACENTER_ID_BITS = 5L;

    // 各部分的最大值
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    // 各部分的位移
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    // 存储映射关系
    private final Map<String, String> shortToLongMap = new ConcurrentHashMap<>();
    private final Map<String, String> longToShortMap = new ConcurrentHashMap<>();

    // 雪花算法实例
    private final SnowflakeIdGenerator idGenerator;

    public SnowflakeShortURL(long datacenterId, long workerId) {
        this.idGenerator = new SnowflakeIdGenerator(datacenterId, workerId);
    }

    /**
     * 将长URL转换为短URL
     */
    public String shorten(String longUrl) {
        // 检查是否已存在短链接
        if (longToShortMap.containsKey(longUrl)) {
            return SHORT_DOMAIN + longToShortMap.get(longUrl);
        }

        // 生成雪花ID并转换为Base62
        long snowflakeId = idGenerator.nextId();
        String shortCode = encodeBase62(snowflakeId);

        // 保存映射关系
        shortToLongMap.put(shortCode, longUrl);
        longToShortMap.put(longUrl, shortCode);

        return SHORT_DOMAIN + shortCode;
    }

    /**
     * 根据短URL获取原始URL
     */
    public String getOriginalUrl(String shortUrl) {
        // 提取短码部分
        String shortCode = extractShortCode(shortUrl);
        return shortToLongMap.get(shortCode);
    }

    /**
     * 从短URL中提取短码
     */
    private String extractShortCode(String shortUrl) {
        if (shortUrl.startsWith(SHORT_DOMAIN)) {
            return shortUrl.substring(SHORT_DOMAIN.length());
        }
        return shortUrl;
    }

    /**
     * 将雪花ID编码为Base62
     */
    private String encodeBase62(long id) {
        StringBuilder sb = new StringBuilder();

        // 处理负数情况（实际上雪花ID是正数，但这里做通用处理）
        long num = Math.abs(id);

        do {
            int remainder = (int)(num % BASE62_LENGTH);
            sb.append(BASE62.charAt(remainder));
            num = num / BASE62_LENGTH;
        } while (num > 0);

        // 反转字符串得到正确顺序
        return sb.reverse().toString();
    }

    /**
     * 将Base62解码为雪花ID
     */
    private long decodeBase62(String code) {
        long result = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            int digit = BASE62.indexOf(c);
            result = result * BASE62_LENGTH + digit;
        }
        return result;
    }

    /**
     * 雪花ID生成器内部类
     */
    private static class SnowflakeIdGenerator {
        private final long datacenterId;
        private final long workerId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;

        public SnowflakeIdGenerator(long datacenterId, long workerId) {
            if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
                throw new IllegalArgumentException(
                        String.format("datacenterId can't be greater than %d or less than 0", MAX_DATACENTER_ID));
            }
            if (workerId > MAX_WORKER_ID || workerId < 0) {
                throw new IllegalArgumentException(
                        String.format("workerId can't be greater than %d or less than 0", MAX_WORKER_ID));
            }
            this.datacenterId = datacenterId;
            this.workerId = workerId;
        }

        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();

            // 时钟回拨处理
            if (timestamp < lastTimestamp) {
                throw new RuntimeException(
                        String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                                lastTimestamp - timestamp));
            }

            // 同一毫秒内的序列号递增
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & MAX_SEQUENCE;
                // 同一毫秒的序列号用完了，等待下一毫秒
                if (sequence == 0) {
                    timestamp = waitNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            // 生成ID
            return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                    | (datacenterId << DATACENTER_ID_SHIFT)
                    | (workerId << WORKER_ID_SHIFT)
                    | sequence;
        }

        private long waitNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }

    /**
     * 批量生成短URL
     */
    public Map<String, String> batchShorten(Map<String, String> urlMap) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
            String key = entry.getKey();
            String longUrl = entry.getValue();
            result.put(key, shorten(longUrl));
        }
        return result;
    }

    /**
     * 清理过期的映射（可选实现）
     */
    public void cleanExpiredMappings(long expireAfterMillis) {
        // 这里可以实现基于时间的清理逻辑
        // 实际生产环境中可能需要结合数据库存储
    }

    // 测试用例
    public static void main(String[] args) {
        // 创建短URL服务实例（数据中心ID=1，工作节点ID=1）
        SnowflakeShortURL shortUrlService = new SnowflakeShortURL(1, 1);

        // 测试转换
        String longUrl1 = "https://www.example.com/article/1234567890";
        String shortUrl1 = shortUrlService.shorten(longUrl1);
        System.out.println("长URL: " + longUrl1);
        System.out.println("短URL: " + shortUrl1);

        String longUrl2 = "https://www.example.com/product/9876543210";
        String shortUrl2 = shortUrlService.shorten(longUrl2);
        System.out.println("\n长URL: " + longUrl2);
        System.out.println("短URL: " + shortUrl2);

        // 测试反向查找
        String original1 = shortUrlService.getOriginalUrl(shortUrl1);
        System.out.println("\n从短URL恢复: " + original1);
        System.out.println("是否匹配: " + longUrl1.equals(original1));

        // 测试重复转换（应该返回相同的短URL）
        String shortUrl1Again = shortUrlService.shorten(longUrl1);
        System.out.println("\n重复转换测试:");
        System.out.println("第一次: " + shortUrl1);
        System.out.println("第二次: " + shortUrl1Again);
        System.out.println("是否相同: " + shortUrl1.equals(shortUrl1Again));

        // 批量测试
        System.out.println("\n批量转换测试:");
        Map<String, String> urlMap = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            urlMap.put("url" + i, "https://www.example.com/page/" + (1000 + i));
        }
        Map<String, String> shortUrls = shortUrlService.batchShorten(urlMap);
        shortUrls.forEach((key, shortUrl) -> {
            System.out.println(key + " -> " + shortUrl);
        });
    }

}
