package com.fx.software.tools.redbag;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @FileName SimpleRedPacketCompetition
 * @Description
 * @Author fx
 * @date 2026-01-22
 */
@Slf4j
public class SimpleRedPacketCompetition {

    static class RedPacket {
        private final BigDecimal totalAmount;
        private final int totalPeople;
        private int remainingPeople;
        private BigDecimal remainingAmount;

        public RedPacket(BigDecimal totalAmount, int totalPeople) {
            this.totalAmount = totalAmount;
            this.totalPeople = totalPeople;
            this.remainingPeople = totalPeople;
            this.remainingAmount = totalAmount;
        }

        public synchronized String grab(String userId) {
            if (remainingPeople <= 0) {
                return userId + ": 红包已抢完";
            }

            Random random = new Random();
            BigDecimal amount;

            if (remainingPeople == 1) {
                amount = remainingAmount;
            } else {
                // 简单随机算法
                double rate = random.nextDouble() * 0.5 + 0.5; // 0.5-1.0之间的随机数
                amount = remainingAmount.multiply(BigDecimal.valueOf(rate))
                        .divide(BigDecimal.valueOf(remainingPeople), 2, RoundingMode.HALF_UP);

                if (amount.compareTo(new BigDecimal("0.01")) < 0) {
                    amount = new BigDecimal("0.01");
                }
            }

            remainingAmount = remainingAmount.subtract(amount);
            remainingPeople--;

            return String.format("%s: 抢到 %.2f元", userId, amount);
        }

        public BigDecimal getRemaining() {
            return remainingAmount;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        log.info("========== 简单版本：100人抢1万元红包 ==========\n");

        RedPacket redPacket = new RedPacket(new BigDecimal("10000.00"), 100);
        List<Thread> threads = new ArrayList<>();
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // 创建100个线程
        for (int i = 1; i <= 100; i++) {
            final String userId = "User-" + i;
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 100)); // 随机延迟
                    String result = redPacket.grab(userId);
                    results.add(result);
                    log.info(result);
                } catch (InterruptedException e) {
                    log.info(userId + ": 被中断");
                }
            });
            threads.add(thread);
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 统计
        log.info("\n========== 统计结果 ===============");
        log.info("总参与人数: " + threads.size());
        log.info("抢到红包人数: " + results.size());
        log.info("剩余金额: " + redPacket.getRemaining() + "元");
    }
}
