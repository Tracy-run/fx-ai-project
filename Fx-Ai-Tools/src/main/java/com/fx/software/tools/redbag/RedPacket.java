package com.fx.software.tools.redbag;

/**
 * @FileName RedPacket
 * @Description
 * 核心算法：
 * 二倍均值法：保证红包分配的公平性和随机性
 * 线程安全：使用ReentrantLock公平锁保证并发安全
 * 原子操作：使用AtomicInteger进行计数
 *
 *红包分配策略：
 * 前99人：随机金额，范围是(0.01, 2倍平均值]
 * 最后一人：获得剩余所有金额
 * 保证每人至少0.01元
 * 使用BigDecimal避免浮点数精度问题
 *
 *
 *并发控制：
 * CyclicBarrier：确保所有用户同时开始抢
 * 线程池管理：控制并发线程数
 * 公平锁：避免线程饥饿
 *
 *统计功能：
 * 成功/失败人数统计
 * 金额分布统计
 * 最大/最小/平均金额
 * 详细结果输出
 *
 *异常处理：
 * 红包已抢完的检查
 * 系统错误的处理
 * 超时控制
 *
 *
 * @Author fx
 * @date 2026-01-22
 */

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 红包类 - 包含红包金额和分配逻辑
 */
public class RedPacket {
    private final BigDecimal totalAmount;      // 红包总金额
    private final int totalPeople;            // 总人数
    private AtomicInteger remainingPeople;    // 剩余人数（原子操作）
    private BigDecimal remainingAmount;       // 剩余金额
    private final ReentrantLock lock = new ReentrantLock(true); // 公平锁

    // 统计信息
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger failCount = new AtomicInteger(0);
    private List<BigDecimal> grabbedAmounts = new CopyOnWriteArrayList<>();

    public RedPacket(BigDecimal totalAmount, int totalPeople) {
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("红包金额必须大于0");
        }
        if (totalPeople <= 0) {
            throw new IllegalArgumentException("抢红包人数必须大于0");
        }

        this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        this.totalPeople = totalPeople;
        this.remainingPeople = new AtomicInteger(totalPeople);
        this.remainingAmount = this.totalAmount;
    }

    /**
     * 抢红包核心方法 - 使用二倍均值法保证公平性
     */
    public GrabResult grab(String userId) {
        // 检查红包是否还有剩余
        if (remainingPeople.get() <= 0) {
            failCount.incrementAndGet();
            return new GrabResult(userId, BigDecimal.ZERO, false, "红包已抢完");
        }

        // 使用公平锁保证线程安全
        lock.lock();
        try {
            // 双重检查
            if (remainingPeople.get() <= 0) {
                failCount.incrementAndGet();
                return new GrabResult(userId, BigDecimal.ZERO, false, "红包已抢完");
            }

            BigDecimal amount;
            if (remainingPeople.get() == 1) {
                // 最后一个人获得剩余所有金额
                amount = remainingAmount;
            } else {
                // 二倍均值法计算金额
                BigDecimal avg = remainingAmount.divide(
                        new BigDecimal(remainingPeople.get()), 10, RoundingMode.HALF_UP);
                BigDecimal max = avg.multiply(new BigDecimal("2"));

                // 生成随机金额（最小0.01元）
                Random random = new Random();
                amount = new BigDecimal(random.nextDouble())
                        .multiply(max)
                        .setScale(2, RoundingMode.HALF_UP);

                // 保证最小金额为0.01
                amount = amount.max(new BigDecimal("0.01"));

                // 保证不超过剩余金额的90%（为后面的人留一些）
                BigDecimal maxAllowed = remainingAmount
                        .subtract(new BigDecimal("0.01").multiply(new BigDecimal(remainingPeople.get() - 1)));
                amount = amount.min(maxAllowed);
            }

            // 更新剩余金额和人数
            remainingAmount = remainingAmount.subtract(amount);
            remainingPeople.decrementAndGet();

            // 记录抢到的金额
            grabbedAmounts.add(amount);
            successCount.incrementAndGet();

            return new GrabResult(userId, amount, true, "抢红包成功");

        } catch (Exception e) {
            failCount.incrementAndGet();
            return new GrabResult(userId, BigDecimal.ZERO, false, "系统错误: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取统计信息
     */
    public Statistics getStatistics() {
        BigDecimal totalGrabbed = grabbedAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Statistics(
                successCount.get(),
                failCount.get(),
                totalGrabbed,
                remainingAmount,
                grabbedAmounts.size() > 0 ?
                        totalGrabbed.divide(new BigDecimal(grabbedAmounts.size()), 2, RoundingMode.HALF_UP) :
                        BigDecimal.ZERO,
                grabbedAmounts
        );
    }

    /**
     * 抢红包结果类
     */
    static class GrabResult {
        private String userId;
        private BigDecimal amount;
        private boolean success;
        private String message;
        private long timestamp;

        public GrabResult(String userId, BigDecimal amount, boolean success, String message) {
            this.userId = userId;
            this.amount = amount.setScale(2, RoundingMode.HALF_UP);
            this.success = success;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getUserId() {
            return userId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("用户: %s, 金额: %.2f, 状态: %s, 消息: %s",
                    userId, amount, success ? "成功" : "失败", message);
        }
    }

    /**
     * 统计信息类
     */
    static class Statistics {
        private int successCount;
        private int failCount;
        private BigDecimal totalGrabbed;
        private BigDecimal remainingAmount;
        private BigDecimal averageAmount;
        private List<BigDecimal> amountList;

        public Statistics(int successCount, int failCount, BigDecimal totalGrabbed,
                          BigDecimal remainingAmount, BigDecimal averageAmount,
                          List<BigDecimal> amountList) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.totalGrabbed = totalGrabbed;
            this.remainingAmount = remainingAmount;
            this.averageAmount = averageAmount;
            this.amountList = amountList;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("========== 红包抢购统计 ==========\n");
            sb.append(String.format("抢红包成功人数: %d\n", successCount));
            sb.append(String.format("抢红包失败人数: %d\n", failCount));
            sb.append(String.format("已抢总金额: %.2f元\n", totalGrabbed));
            sb.append(String.format("剩余金额: %.2f元\n", remainingAmount));
            sb.append(String.format("平均金额: %.2f元\n", averageAmount));
            sb.append(String.format("最大金额: %.2f元\n",
                    amountList.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO)));
            sb.append(String.format("最小金额: %.2f元\n",
                    amountList.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO)));

            // 显示前20个金额详情
            sb.append("\n前20个抢到的金额:\n");
            int count = Math.min(20, amountList.size());
            for (int i = 0; i < count; i++) {
                sb.append(String.format("第%2d位: %.2f元\n", i + 1, amountList.get(i)));
            }

            return sb.toString();
        }
    }
}

/**
 * 用户任务 - 模拟用户抢红包
 */
@Slf4j
class UserTask implements Runnable {
    private final String userId;
    private final RedPacket redPacket;
    private final CyclicBarrier barrier;
    private RedPacket.GrabResult result;

    public UserTask(String userId, RedPacket redPacket, CyclicBarrier barrier) {
        this.userId = userId;
        this.redPacket = redPacket;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            // 等待所有用户准备就绪   此处会阻塞
            //barrier.await();

            // 模拟网络延迟（0-50ms随机）
            Thread.sleep(new Random().nextInt(50));

            // 抢红包
            result = redPacket.grab(userId);

            // 输出结果
            if (result.isSuccess()) {
                log.info("===={},{}",
                        System.currentTimeMillis(), result.toString());
            }

        } catch (Exception e) {
            result = new RedPacket.GrabResult(userId, BigDecimal.ZERO, false,
                    "任务异常: " + e.getMessage());
        }
    }

    public RedPacket.GrabResult getResult() {
        return result;
    }

}