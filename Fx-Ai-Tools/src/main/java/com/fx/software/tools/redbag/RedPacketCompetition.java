package com.fx.software.tools.redbag;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @FileName RedPacketCompetition
 * @Description 主程序 - 模拟100人抢1万元红包
 * @Author fx
 * @date 2026-01-22
 */
@Slf4j
public class RedPacketCompetition {

    public static void main(String[] args) {
        log.info("========== 开始100人抢1万元红包 ==========\n");
        int userCount = 5;
        // 1. 创建红包（1万元，100人）
        RedPacket redPacket = new RedPacket(new BigDecimal("10000.00"), userCount);

        // 2. 创建线程池和屏障
        ExecutorService executor = Executors.newFixedThreadPool(2);

        //最后一个到达的线程执行屏障任务（() -> log.info(...)），此任务完成前其他线程仍被阻塞
        /********************/
        CyclicBarrier barrier = new CyclicBarrier(userCount,
                () -> log.info("所有用户准备就绪，开始抢红包..."));


        // 3. 创建用户任务
        List<UserTask> tasks = new ArrayList<>();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 1; i <= userCount; i++) {
            UserTask task = new UserTask("User-" + String.format("%03d", i), redPacket, barrier);
            tasks.add(task);
            futures.add(executor.submit(task));
        }

        // 4. 等待所有任务完成123Z456
        try {
            // 设置超时时间
            for (Future<?> future : futures) {
                future.get(15, TimeUnit.SECONDS);
            }

            // 等待线程池关闭
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }

        } catch (Exception e) {
            log.error("======" + e);
            executor.shutdownNow();
            log.info("任务执行超时或被中断");
        }

        // 5. 输出统计信息
        log.info("\n" + redPacket.getStatistics().toString());

        // 6. 输出详细结果
        log.info("\n========== 详细抢红包结果 ==========");
        int successCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (UserTask task : tasks) {
            RedPacket.GrabResult result = task.getResult();
            if (result != null && result.isSuccess()) {
                successCount++;
                totalAmount = totalAmount.add(result.getAmount());
            }
        }

        log.info("实际成功人数: {}", successCount);
        log.info("实际抢到总金额: {}元", totalAmount);

        // 验证金额是否正确
        if (totalAmount.compareTo(new BigDecimal("10000.00")) == 0) {
            log.info("✅ 金额验证通过：总金额10000.00元分配完毕");
        } else {
            log.info("⚠ 金额验证失败：总金额应为10000.00元，实际分配{}元\n", totalAmount);
        }
    }
}
