package com.fx.software.tools.urlchange;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @FileName DistributedSnowflakeShortURL
 * @Description
 * @Author fx
 * @date 2026-01-21
 */
public class DistributedSnowflakeShortURL {
    // Base62字符集（优化顺序，避免相似字符）
    //private static final String BASE62 = "q3xG7NcMpR9sF5wLzK8JyT2hVbD1gXeC6rAmW4nQvP0iZoUaSlBdEfHuIjOkY";
    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int BASE62_LENGTH = BASE62.length();
    private static final String SHORT_DOMAIN = "https://s.url/";

    // 雪花算法常量 // 2024-01-01 00:00:00
    private static final long EPOCH = 1704067200000L;
    // 机器ID位数（最多1024台机器）
    private static final long WORKER_ID_BITS = 10L;
    // 序列号位数（每毫秒4096个）
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 位移
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    // 存储
    private final Map<String, String> shortToLong = new ConcurrentHashMap<>();
    private final Map<String, String> longToShort = new ConcurrentHashMap<>();
    private final Map<String, AccessStats> urlStats = new ConcurrentHashMap<>();

    // 分布式机器ID管理
    private final MachineIdManager machineIdManager;
    private final SnowflakeGenerator idGenerator;

    // 缓存配置
    private final URLCache urlCache;

    public DistributedSnowflakeShortURL(String clusterName, String zkAddress) {
        this.machineIdManager = new ZookeeperMachineIdManager(clusterName, zkAddress);
        long workerId = machineIdManager.getWorkerId();
        this.idGenerator = new SnowflakeGenerator(workerId);
        // 缓存1万个URL
        this.urlCache = new LRUURLCache(10000);
    }

    /**
     * 短URL生成入口
     */
    public String shorten(String longUrl) {
        // 默认永不过期
        return shorten(longUrl, -1);
    }

    public String shorten(String longUrl, int expireDays) {
        // 1. 检查缓存
        String cached = urlCache.getShortUrl(longUrl);
        if (cached != null) {
            return cached;
        }

        // 2. 检查是否已存在
        synchronized (longUrl.intern()) {
            if (longToShort.containsKey(longUrl)) {
                String shortCode = longToShort.get(longUrl);
                return SHORT_DOMAIN + shortCode;
            }

            // 3. 生成新短链接
            long snowflakeId = idGenerator.nextId();
            String shortCode = encodeBase62(snowflakeId);

            // 4. 保存映射
            saveMapping(shortCode, longUrl, expireDays);

            // 5. 更新缓存
            String shortUrl = SHORT_DOMAIN + shortCode;
            urlCache.put(longUrl, shortUrl);

            return shortUrl;
        }
    }

    /**
     * 获取原始URL
     */
    public String getOriginal(String shortUrl) {
        String shortCode = extractShortCode(shortUrl);

        // 1. 检查缓存
        String cached = urlCache.getLongUrl(shortCode);
        if (cached != null) {
            updateStats(shortCode);
            return cached;
        }

        // 2. 从存储获取
        String longUrl = shortToLong.get(shortCode);
        if (longUrl != null) {
            urlCache.put(shortCode, longUrl);
            updateStats(shortCode);
            return longUrl;
        }

        // 3. 可以扩展：从数据库查询
        return null;
    }

    /**
     * 批量生成短链接
     */
    public Map<String, String> batchShorten(Map<String, String> longUrls) {
        Map<String, String> results = new ConcurrentHashMap<>();
        longUrls.entrySet().parallelStream().forEach(entry -> {
            String key = entry.getKey();
            String shortUrl = shorten(entry.getValue());
            results.put(key, shortUrl);
        });
        return results;
    }

    /**
     * 获取访问统计
     */
    public AccessStats getStats(String shortCode) {
        return urlStats.get(shortCode);
    }

    /**
     * Base62编码（优化版）
     */
    private String encodeBase62(long id) {
        if (id == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            int remainder = (int) (id % BASE62_LENGTH);
            sb.append(BASE62.charAt(remainder));
            id = id / BASE62_LENGTH;
        }
        return sb.reverse().toString();
    }

    /**
     * Base62解码
     */
    private long decodeBase62(String code) {
        long result = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            int digit = BASE62.indexOf(c);
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            result = result * BASE62_LENGTH + digit;
        }
        return result;
    }

    private String extractShortCode(String shortUrl) {
        if (shortUrl.startsWith(SHORT_DOMAIN)) {
            return shortUrl.substring(SHORT_DOMAIN.length());
        }
        return shortUrl;
    }

    private void saveMapping(String shortCode, String longUrl, int expireDays) {
        longToShort.put(longUrl, shortCode);
        shortToLong.put(shortCode, longUrl);

        // 可以扩展：保存到数据库
        // saveToDatabase(shortCode, longUrl, expireDays);
    }

    private void updateStats(String shortCode) {
        urlStats.compute(shortCode, (k, v) -> {
            if (v == null) {
                return new AccessStats(1);
            }
            v.increment();
            return v;
        });
    }

    /**
     * 分布式雪花ID生成器
     */
    private static class SnowflakeGenerator {
        private final long workerId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;
        private final ReentrantLock lock = new ReentrantLock();

        public SnowflakeGenerator(long workerId) {
            if (workerId > MAX_WORKER_ID || workerId < 0) {
                throw new IllegalArgumentException(
                        String.format("workerId must be between 0 and %d", MAX_WORKER_ID));
            }
            this.workerId = workerId;
        }

        public long nextId() {
            lock.lock();
            try {
                long timestamp = timeGen();

                // 时钟回拨处理
                if (timestamp < lastTimestamp) {
                    throw new RuntimeException(
                            String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                                    lastTimestamp - timestamp));
                }

                // 同一毫秒内
                if (lastTimestamp == timestamp) {
                    sequence = (sequence + 1) & MAX_SEQUENCE;
                    if (sequence == 0) {
                        timestamp = tilNextMillis(lastTimestamp);
                    }
                } else {
                    sequence = 0L;
                }

                lastTimestamp = timestamp;

                // 生成ID
                return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                        | (workerId << WORKER_ID_SHIFT)
                        | sequence;
            } finally {
                lock.unlock();
            }
        }

        private long tilNextMillis(long lastTimestamp) {
            long timestamp = timeGen();
            while (timestamp <= lastTimestamp) {
                timestamp = timeGen();
            }
            return timestamp;
        }

        private long timeGen() {
            return System.currentTimeMillis();
        }
    }

    /**
     * 机器ID管理器接口
     */
    private interface MachineIdManager {
        long getWorkerId();
        void releaseWorkerId();
    }

    /**
     * 基于ZooKeeper的机器ID管理器
     */
    private static class ZookeeperMachineIdManager implements MachineIdManager {
        private final String clusterName;
        private final String zkAddress;
        private long workerId = -1;

        public ZookeeperMachineIdManager(String clusterName, String zkAddress) {
            this.clusterName = clusterName;
            this.zkAddress = zkAddress;
            this.workerId = allocateWorkerId();
        }

        private long allocateWorkerId() {
            // 模拟ZooKeeper分配逻辑
            // 实际实现应使用ZooKeeper临时顺序节点
            try {
                // 这里简化为随机分配（生产环境应使用分布式协调）
                return (long) (Math.random() * MAX_WORKER_ID);
            } catch (Exception e) {
                throw new RuntimeException("Failed to allocate worker ID", e);
            }
        }

        @Override
        public long getWorkerId() {
            return workerId;
        }

        @Override
        public void releaseWorkerId() {
            // 释放机器ID（比如应用关闭时）
            // 实际实现应删除ZooKeeper节点
        }
    }

    /**
     * 基于数据库的机器ID管理器（备选方案）
     */
    private static class DatabaseMachineIdManager implements MachineIdManager {
        private final String serviceName;
        private final String hostIp;
        private long workerId = -1;

        public DatabaseMachineIdManager(String serviceName, String hostIp) {
            this.serviceName = serviceName;
            this.hostIp = hostIp;
            this.workerId = allocateFromDB();
        }

        private long allocateFromDB() {
            // 从数据库获取或分配workerId
            // 实现逻辑：
            // 1. 检查是否有空闲workerId
            // 2. 如果没有，分配新的
            // 3. 定期心跳维持租约
            return 1L; // 简化实现
        }

        @Override
        public long getWorkerId() {
            return workerId;
        }

        @Override
        public void releaseWorkerId() {
            // 更新数据库状态
        }
    }

    /**
     * URL缓存接口
     */
    private interface URLCache {
        void put(String key, String value);
        String getShortUrl(String longUrl);
        String getLongUrl(String shortCode);
    }

    /**
     * LRU缓存实现
     */
    private static class LRUURLCache implements URLCache {
        private final Map<String, String> shortCache;
        private final Map<String, String> longCache;
        private final int maxSize;

        public LRUURLCache(int maxSize) {
            this.maxSize = maxSize;
            this.shortCache = new ConcurrentHashMap<>();
            this.longCache = new ConcurrentHashMap<>();
        }

        @Override
        public void put(String key, String value) {
            // 简化实现，实际应使用真正的LRU
            if (shortCache.size() >= maxSize) {
                // 清理策略（可以随机清理或LRU）
                if (!shortCache.isEmpty()) {
                    String firstKey = shortCache.keySet().iterator().next();
                    shortCache.remove(firstKey);
                }
            }

            // 双向缓存
            if (value.startsWith(SHORT_DOMAIN)) {
                // key是longUrl, value是shortUrl
                longCache.put(key, value);
            } else {
                // key是shortCode, value是longUrl
                shortCache.put(key, value);
            }
        }

        @Override
        public String getShortUrl(String longUrl) {
            return longCache.get(longUrl);
        }

        @Override
        public String getLongUrl(String shortCode) {
            return shortCache.get(shortCode);
        }
    }

    /**
     * 访问统计类
     */
    public static class AccessStats {
        private final AtomicLong accessCount;
        private final long createdTime;
        private volatile long lastAccessTime;

        public AccessStats(long initialCount) {
            this.accessCount = new AtomicLong(initialCount);
            this.createdTime = System.currentTimeMillis();
            this.lastAccessTime = createdTime;
        }

        public void increment() {
            accessCount.incrementAndGet();
            lastAccessTime = System.currentTimeMillis();
        }

        public long getAccessCount() {
            return accessCount.get();
        }

        public long getCreatedTime() {
            return createdTime;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public double getAverageDailyAccess() {
            long days = (System.currentTimeMillis() - createdTime) / (24 * 60 * 60 * 1000) + 1;
            return (double) accessCount.get() / days;
        }
    }

    /**
     * 监控和统计组件
     */
    public static class ShortURLMonitor {
        private final Map<String, PerformanceStats> performanceStats = new ConcurrentHashMap<>();

        public void recordGenerateTime(String shortCode, long timeMs) {
            performanceStats.compute(shortCode, (k, v) -> {
                if (v == null) {
                    return new PerformanceStats(timeMs);
                }
                v.recordTime(timeMs);
                return v;
            });
        }

        public PerformanceStats getPerformanceStats(String shortCode) {
            return performanceStats.get(shortCode);
        }

        public static class PerformanceStats {
            private long totalTime;
            private long count;
            private long maxTime;
            private long minTime;

            public PerformanceStats(long firstTime) {
                this.totalTime = firstTime;
                this.count = 1;
                this.maxTime = firstTime;
                this.minTime = firstTime;
            }

            public void recordTime(long timeMs) {
                totalTime += timeMs;
                count++;
                maxTime = Math.max(maxTime, timeMs);
                minTime = Math.min(minTime, timeMs);
            }

            public double getAverageTime() {
                return count == 0 ? 0 : (double) totalTime / count;
            }

            public long getMaxTime() {
                return maxTime;
            }

            public long getMinTime() {
                return minTime;
            }

            public long getTotalCount() {
                return count;
            }
        }
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) {
        // 创建分布式短URL服务
        DistributedSnowflakeShortURL service = new DistributedSnowflakeShortURL(
                "url-cluster-1",
                "localhost:2181"
        );

        // 测试单次生成
        String longUrl1 = "https://www.example.com/article/分布式系统架构设计与实践";
        String shortUrl1 = service.shorten(longUrl1);
        System.out.println("长URL: " + longUrl1);
        System.out.println("短URL: " + shortUrl1);

        // 测试重复生成（应该返回相同的短链接）
        String shortUrl1Again = service.shorten(longUrl1);
        System.out.println("重复生成是否一致: " + shortUrl1.equals(shortUrl1Again));

        // 测试获取原始URL
        String original1 = service.getOriginal(shortUrl1);
        System.out.println("原始URL: " + original1);
        System.out.println("是否匹配: " + longUrl1.equals(original1));

        // 测试批量生成
        System.out.println("\n批量生成测试:");
        for (int i = 0; i < 10; i++) {
            String url = "https://www.example.com/page/" + (10000 + i);
            String shortUrl = service.shorten(url);
            System.out.println("新的URL " + i + ": " + shortUrl);

            // 模拟访问
            for (int j = 0; j < i; j++) {
                service.getOriginal(shortUrl);
            }
        }

        // 查看统计信息
        System.out.println("\n访问统计:");
        service.urlStats.forEach((code, stats) -> {
            System.out.println("短码 " + code + ": 访问次数=" + stats.getAccessCount() +
                    ", 日均访问=" + String.format("%.2f", stats.getAverageDailyAccess()));
        });

        // 性能测试
        System.out.println("\n性能测试:");
        long startTime = System.currentTimeMillis();
        int testCount = 1000;
        for (int i = 0; i < testCount; i++) {
            service.shorten("https://test.com/" + System.nanoTime());
        }
        long endTime = System.currentTimeMillis();
        System.out.println("生成 " + testCount + " 个短链接耗时: " + (endTime - startTime) + "ms");
        System.out.println("平均每个: " + (double)(endTime - startTime) / testCount + "ms");
    }
}
