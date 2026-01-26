package com.fx.software.tools.redbag;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class RedPacketCompetition2 {

    // 简单的红包类，使用synchronized保证线程安全
    static class RedPacket {
        private final BigDecimal totalAmount;
        private final int totalPeople;
        private int remainingPeople;
        private BigDecimal remainingAmount;

        public RedPacket(BigDecimal totalAmount, int totalPeople) {
            this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
            this.totalPeople = totalPeople;
            this.remainingPeople = totalPeople;
            this.remainingAmount = this.totalAmount;
        }

        public synchronized GrabResult grab(String userId) {
            if (remainingPeople <= 0) {
                return new GrabResult(userId, BigDecimal.ZERO, false, "红包已抢完");
            }

            BigDecimal amount;
            if (remainingPeople == 1) {
                // 最后一人拿剩余所有金额
                amount = remainingAmount;
            } else {
                // 简化算法：随机金额，确保公平  // avg = 100 / 5 = 20元（当前剩余人均金额）
                BigDecimal avg = remainingAmount.divide(
                        BigDecimal.valueOf(remainingPeople), 10, RoundingMode.HALF_UP);
                // max = 20 * 2 = 40元（本次随机金额的上限）
                BigDecimal max = avg.multiply(BigDecimal.valueOf(2));

                Random random = new Random();
                //如果随机到0.3，则 amount = 0.3 * 40 = 12.00元。
                amount = BigDecimal.valueOf(random.nextDouble())  // 生成[0, 1)的随机小数
                        .multiply(max)                               // 放大到[0, 40)元
                        .setScale(2, RoundingMode.HALF_UP);  // 四舍五入保留2位小数

                // 确保最小金额为0.01
                if (amount.compareTo(new BigDecimal("0.01")) < 0) {
                    amount = new BigDecimal("0.01");
                }

                // 确保不会分完所有钱
                BigDecimal minRemaining = new BigDecimal("0.01")
                        .multiply(BigDecimal.valueOf(remainingPeople - 1));
                amount = amount.min(remainingAmount.subtract(minRemaining));
            }

            // 更新状态
            remainingAmount = remainingAmount.subtract(amount);
            remainingPeople--;

            return new GrabResult(userId, amount, true, "抢红包成功");
        }

        static class GrabResult {
            String userId;
            BigDecimal amount;
            boolean success;
            String message;

            public GrabResult(String userId, BigDecimal amount, boolean success, String message) {
                this.userId = userId;
                this.amount = amount.setScale(2, RoundingMode.HALF_UP); // 四舍五入保留2位小数
                this.success = success;
                this.message = message;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        log.info("========== 开始100人抢1万元红包 ==========\n");

        // 创建红包
        int totalPeople = 50;
        RedPacket redPacket = new RedPacket(new BigDecimal("10000.00"), totalPeople);

        // 创建线程池，使用Callable和CompletionService
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CompletionService<RedPacket.GrabResult> completionService =
                new ExecutorCompletionService<>(executor);

        // 提交100个任务
        for (int i = 1; i <= 100; i++) {
            final String userId = "User-" + String.format("%03d", i);
            completionService.submit(() -> {
                // 添加微小随机延迟，模拟网络延迟
                //Thread.sleep((long) (Math.random() * 10));
                return redPacket.grab(userId);
            });
        }

        // 收集结果
        List<RedPacket.GrabResult> results = new ArrayList<>();
        int successCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BigDecimal> amountList = new ArrayList<>();

        // 获取结果，设置超时时间
        for (int i = 0; i < 100; i++) {
            try {
                Future<RedPacket.GrabResult> future = completionService.poll(5, TimeUnit.SECONDS);
                if (future != null) {
                    RedPacket.GrabResult result = future.get();
                    results.add(result);

                    if (result.success) {
                        successCount++;
                        totalAmount = totalAmount.add(result.amount);
                        amountList.add(result.amount);
                        log.info(" 时间：{}，用户: {}, 金额: {}",
                                System.currentTimeMillis(), result.userId, result.amount);
                    }
                } else {
                    log.info("任务超时，跳过该用户");
                }
            } catch (Exception e) {
                log.info("获取结果异常: " + e.getMessage());
            }
        }

        // 关闭线程池
        executor.shutdown();

        // 输出统计结果
        log.info("\n========== 红包抢购统计 ===============");
        log.info("抢红包成功人数: {}" ,  successCount);
        log.info("抢红包失败人数: {}", 100 - successCount);
        log.info("已抢总金额: {}", totalAmount);

        if (successCount > 0) {
            BigDecimal avg = totalAmount.divide(
                    BigDecimal.valueOf(successCount), 2, RoundingMode.HALF_UP);
            log.info("平均金额: {}", avg);

            BigDecimal max = amountList.stream()
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal min = amountList.stream()
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            log.info("最大金额: {}", max);
            log.info("最小金额: {}", min);

            // 输出前20个金额
//            log.info("\n前20个抢到的金额:");
//            int count = Math.min(20, amountList.size());
//            for (int i = 0; i < count; i++) {
//                log.info("第%2d位: %.2f元%n", i + 1, amountList.get(i));
//            }
        }

        // 验证
        if (totalAmount.compareTo(new BigDecimal("10000.00")) == 0) {
            log.info("\n✅ 验证通过：总金额10000.00元分配完毕");
        } else {
            log.info("\n⚠️ 验证失败：总金额应为10000.00元，实际分配{}", totalAmount);
        }

        // 简单分析
        log.info("\n========== 分析建议 ===============");
        if (successCount < totalPeople) {
            log.info("可能原因:");
            log.info("1. 线程池过小，任务排队时间过长");
            log.info("2. 抢红包逻辑中的锁竞争");
            log.info("3. 模拟延迟时间设置不合理");
        }
    }
}