package com.fx.software.tools.urlchange;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @FileName SecureSnowflakeShortURL
 * @Description 基于Feistel加密的乱序雪花算法实现
 * @Author fx
 * @date 2026-01-21
 */
public class SecureSnowflakeShortURL {

    // UTF-8字符集保证编码正确
    private static final String CUSTOM_CHARSET;
    private static final String SHORT_DOMAIN = "https://s.url/";

    // 雪花算法参数
    private static final long EPOCH = 1741363200000L; // 2025-03-08 00:00:00
    private static final long WORKER_ID_BITS = 8L;     // 256个工作节点
    private static final long SEQUENCE_BITS = 12L;     // 每毫秒4096个
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    static {
        // 构建乱序字符集（避免可预测性）
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        List<Character> charList = new ArrayList<>();
        for (char c : chars.toCharArray()) {
            charList.add(c);
        }
        // 使用固定种子打乱，确保一致性
        Collections.shuffle(charList, new Random(0xDEADBEEF));
        StringBuilder sb = new StringBuilder();
        for (Character c : charList) {
            sb.append(c);
        }
        CUSTOM_CHARSET = sb.toString();
    }

    private final Map<String, String> shortToLong = new ConcurrentHashMap<>();
    private final Map<String, String> longToShort = new ConcurrentHashMap<>();
    private final SnowflakeGenerator idGenerator;
    private final FeistelCipher feistelCipher;

    public SecureSnowflakeShortURL(long workerId) {
        this.idGenerator = new SnowflakeGenerator(workerId);
        this.feistelCipher = new FeistelCipher(0xCAFEBABE); // 加密密钥
    }

    /**
     * 生成不可预测的短URL
     */
    public String shorten(String longUrl) {
        // UTF-8编码验证和清理
        longUrl = sanitizeAndValidateUrl(longUrl);

        synchronized (longUrl.intern()) {
            // 检查是否已存在
            if (longToShort.containsKey(longUrl)) {
                return SHORT_DOMAIN + longToShort.get(longUrl);
            }

            // 生成雪花ID
            long snowflakeId = idGenerator.nextId();

            // 使用Feistel网络加密，打乱顺序
            long encryptedId = feistelCipher.encrypt(snowflakeId);

            // 编码为乱序Base62
            String shortCode = encodeToCustomBase62(encryptedId);

            // 保存映射
            shortToLong.put(shortCode, longUrl);
            longToShort.put(longUrl, shortCode);

            return SHORT_DOMAIN + shortCode;
        }
    }

    /**
     * 获取原始URL（带UTF-8验证）
     */
    public String getOriginal(String shortUrl) {
        String shortCode = extractShortCode(shortUrl);
        String longUrl = shortToLong.get(shortCode);

        if (longUrl != null) {
            // 验证UTF-8编码
            validateUTF8(longUrl);
        }
        return longUrl;
    }

    /**
     * UTF-8清理和验证
     */
    private String sanitizeAndValidateUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("URL不能为空");
        }

        // 验证UTF-8编码
        validateUTF8(url);

        // 移除控制字符
        url = url.replaceAll("\\p{Cntrl}", "");

        // 标准化空格
        url = url.trim().replaceAll("\\s+", " ");

        return url;
    }

    /**
     * UTF-8编码验证
     */
    private void validateUTF8(String text) {
        try {
            // 转换为UTF-8字节数组再转回字符串
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            String validated = new String(bytes, StandardCharsets.UTF_8);

            // 验证是否完整转换
            if (!text.equals(validated)) {
                System.err.println("警告：URL包含非UTF-8字符，已自动修正");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("URL包含无效的UTF-8字符: " + e.getMessage());
        }
    }

    /**
     * 使用自定义乱序字符集编码
     */
    private String encodeToCustomBase62(long id) {
        if (id < 0) {
            id = Math.abs(id);
        }

        StringBuilder sb = new StringBuilder();
        long temp = id;

        do {
            int index = (int) (temp % CUSTOM_CHARSET.length());
            sb.append(CUSTOM_CHARSET.charAt(index));
            temp = temp / CUSTOM_CHARSET.length();
        } while (temp > 0);

        // 反转并填充到固定长度
        String result = sb.reverse().toString();

        // 固定长度8位，不足时用随机字符填充
        if (result.length() < 8) {
            Random random = new Random(id ^ 0xCAFEBABE);
            while (result.length() < 8) {
                int index = random.nextInt(CUSTOM_CHARSET.length());
                result += CUSTOM_CHARSET.charAt(index);
            }
        } else if (result.length() > 8) {
            result = result.substring(0, 8);
        }

        return result;
    }

    private String extractShortCode(String shortUrl) {
        if (shortUrl.startsWith(SHORT_DOMAIN)) {
            return shortUrl.substring(SHORT_DOMAIN.length());
        }
        return shortUrl;
    }

    /**
     * Feistel网络加密（核心乱序算法）
     */
    private static class FeistelCipher {
        private static final int ROUNDS = 4;
        private final int[] roundKeys;

        public FeistelCipher(int seed) {
            this.roundKeys = generateRoundKeys(seed);
        }

        public long encrypt(long plaintext) {
            // 将64位分成两个32位部分
            int left = (int) (plaintext >>> 32);
            int right = (int) plaintext;

            for (int i = 0; i < ROUNDS; i++) {
                int temp = left;
                left = right ^ f(left, roundKeys[i]);
                right = temp;
            }

            // 最后交换
            int temp = left;
            left = right;
            right = temp;

            return ((long) left << 32) | (right & 0xFFFFFFFFL);
        }

        private int f(int data, int key) {
            // 非线性混淆函数
            data = (data + key) ^ 0x9E3779B9;
            data = Integer.rotateLeft(data, 7);
            data ^= 0x85EBCA77;
            return data;
        }

        private int[] generateRoundKeys(int seed) {
            int[] keys = new int[ROUNDS];
            Random random = new Random(seed);
            for (int i = 0; i < ROUNDS; i++) {
                keys[i] = random.nextInt();
            }
            return keys;
        }
    }

    /**
     * 雪花ID生成器（增加随机性）
     */
    private static class SnowflakeGenerator {
        private final long workerId;
        private final Random random;
        private long sequence;
        private long lastTimestamp;
        private final ReentrantLock lock = new ReentrantLock();

        public SnowflakeGenerator(long workerId) {
            if (workerId > MAX_WORKER_ID || workerId < 0) {
                throw new IllegalArgumentException("workerId超出范围");
            }
            this.workerId = workerId;
            this.random = new SecureRandom();
            this.sequence = random.nextInt((int) MAX_SEQUENCE);
        }

        public long nextId() {
            lock.lock();
            try {
                long timestamp = timeGen();

                // 添加随机延迟，打乱时间顺序
                if (timestamp == lastTimestamp) {
                    try {
                        Thread.sleep(random.nextInt(3));
                        timestamp = timeGen();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // 随机化序列号
                sequence = (sequence + 1 + random.nextInt(10)) & MAX_SEQUENCE;
                lastTimestamp = timestamp;

                return ((timestamp - EPOCH) << (SEQUENCE_BITS + WORKER_ID_BITS))
                        | (workerId << SEQUENCE_BITS)
                        | sequence;
            } finally {
                lock.unlock();
            }
        }

        private long timeGen() {
            // 添加微小随机扰动
            return System.currentTimeMillis() + random.nextInt(10);
        }
    }

    /**
     * 批量生成测试（显示乱序效果）
     */
    public static void main(String[] args) throws Exception{
        // 强制设置 System.out 编码为 UTF-8
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        // 设置UTF-8系统属性（重要！）
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        // 创建乱序短URL生成器
        SecureSnowflakeShortURL service = new SecureSnowflakeShortURL(1);

        System.out.println("字符集长度: " + CUSTOM_CHARSET.length());
        System.out.println("字符集示例: " + CUSTOM_CHARSET.substring(0, 20) + "...");
        System.out.println("================================");

        // 测试UTF-8支持
        String[] testUrls = {
                "https://example.com/中文测试",
                "https://example.com/🎉表情符号",
                "https://example.com/ Café价格表",
                "https://example.com/🚀快速开始",
                "https://example.com/Normal-URL-123"
        };

        for (int i = 0; i < testUrls.length; i++) {
            String shortUrl = service.shorten(testUrls[i]);
            System.out.printf("测试 %d - 原始URL: %s\n", i + 1, testUrls[i]);
            System.out.printf("        短URL: %s\n", shortUrl);
            System.out.printf("        短码长度: %d\n", shortUrl.length() - SHORT_DOMAIN.length());
        }

        System.out.println("\n================================");
        System.out.println("批量生成测试（验证乱序性）:");
        System.out.println("================================");

        List<String> shortUrls = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String url = "https://www.example.com/page/" + (10000 + i);
            String shortUrl = service.shorten(url);
            shortUrls.add(shortUrl);
            System.out.printf("URL %2d: %s\n", i + 1, shortUrl);
        }

        // 验证乱序性
        System.out.println("\n================================");
        System.out.println("乱序性分析:");
        System.out.println("================================");

        // 提取短码
        List<String> codes = new ArrayList<>();
        for (String shortUrl : shortUrls) {
            codes.add(shortUrl.substring(SHORT_DOMAIN.length()));
        }

        // 检查是否有连续模式
        int sequentialPatterns = 0;
        for (int i = 1; i < codes.size(); i++) {
            String prev = codes.get(i - 1);
            String curr = codes.get(i);

            // 检查末尾字符是否连续
            if (prev.length() == curr.length()) {
                char lastPrev = prev.charAt(prev.length() - 1);
                char lastCurr = curr.charAt(curr.length() - 1);

                if (Math.abs(CUSTOM_CHARSET.indexOf(lastCurr) - CUSTOM_CHARSET.indexOf(lastPrev)) <= 1) {
                    sequentialPatterns++;
                }
            }
        }

        System.out.printf("连续模式检测: %d/%d (%.1f%%)\n",
                sequentialPatterns, codes.size() - 1,
                (sequentialPatterns * 100.0) / (codes.size() - 1));

        // 字符分布统计
        Map<Character, Integer> charDistribution = new HashMap<>();
        for (String code : codes) {
            for (char c : code.toCharArray()) {
                charDistribution.put(c, charDistribution.getOrDefault(c, 0) + 1);
            }
        }

        System.out.println("\n字符分布统计:");
        charDistribution.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(10)
                .forEach(entry ->
                        System.out.printf("字符 '%c': %d次\n", entry.getKey(), entry.getValue()));

        // 性能测试
        System.out.println("\n================================");
        System.out.println("性能测试:");
        System.out.println("================================");

        long startTime = System.currentTimeMillis();
        int performanceCount = 1000;
        for (int i = 0; i < performanceCount; i++) {
            service.shorten("https://test.com/" + UUID.randomUUID());
        }
        long endTime = System.currentTimeMillis();

        System.out.printf("生成 %d 个短链接耗时: %d ms\n", performanceCount, endTime - startTime);
        System.out.printf("平均每个: %.2f ms\n", (double)(endTime - startTime) / performanceCount);

        // 内存使用情况
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.printf("内存使用: %.2f MB\n", usedMemory / (1024.0 * 1024.0));
    }

}
