package com.xingchen.backend.util.id;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Random;

/**
 * 雪花算法ID生成器
 * 
 * 结构：
 * - 1位符号位（始终为0）
 * - 41位时间戳（毫秒级，约69年）
 * - 10位工作机器ID（5位数据中心ID + 5位机器ID）
 * - 12位序列号（每毫秒最多4096个ID）
 *
 * @author xingchen
 * @date 2026-03-22
 */
@Slf4j
public class SnowflakeIdWorker {

    // ============================== 常量定义 ==============================
    /** 开始时间戳 (2024-01-01) */
    private final long twepoch = 1704067200000L;

    /** 机器ID所占的位数 */
    private final long workerIdBits = 5L;

    /** 数据中心ID所占的位数 */
    private final long datacenterIdBits = 5L;

    /** 支持的最大机器ID，结果是31 */
    private final long maxWorkerId = ~(-1L << workerIdBits);

    /** 支持的最大数据中心ID，结果是31 */
    private final long maxDatacenterId = ~(-1L << datacenterIdBits);

    /** 序列在ID中占的位数 */
    private final long sequenceBits = 12L;

    /** 机器ID向左移12位 */
    private final long workerIdShift = sequenceBits;

    /** 数据中心ID向左移17位 */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /** 时间戳向左移22位 */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /** 生成序列的掩码，这里为4095 */
    private final long sequenceMask = ~(-1L << sequenceBits);

    // ============================== 成员变量 ==============================
    /** 工作机器ID(0~31) */
    private long workerId;

    /** 数据中心ID(0~31) */
    private long datacenterId;

    /** 毫秒内序列(0~4095) */
    private long sequence = 0L;

    /** 上次生成ID的时间戳 */
    private long lastTimestamp = -1L;

    // ============================== 单例模式 ==============================
    private static volatile SnowflakeIdWorker instance;

    /**
     * 获取单例实例
     */
    public static SnowflakeIdWorker getInstance() {
        if (instance == null) {
            synchronized (SnowflakeIdWorker.class) {
                if (instance == null) {
                    instance = new SnowflakeIdWorker();
                }
            }
        }
        return instance;
    }

    /**
     * 私有构造函数
     */
    private SnowflakeIdWorker() {
        this.datacenterId = getDatacenterId();
        this.workerId = getWorkerId();
        log.info("SnowflakeIdWorker初始化 - datacenterId: {}, workerId: {}", datacenterId, workerId);
    }

    /**
     * 带参数的构造函数（用于测试）
     *
     * @param workerId     工作机器ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("workerId不能大于%d或小于0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenterId不能大于%d或小于0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // ============================== 核心方法 ==============================

    /**
     * 获取下一个ID
     *
     * @return 雪花算法生成的ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
        if (timestamp < lastTimestamp) {
            log.error("时钟回退，拒绝生成ID。lastTimestamp: {}, timestamp: {}", lastTimestamp, timestamp);
            throw new RuntimeException(String.format(
                    "时钟回退，拒绝生成ID。lastTimestamp: %d, timestamp: %d", lastTimestamp, timestamp));
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒，获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        // 上次生成ID的时间戳
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    /**
     * 生成字符串类型的ID（带前缀）
     *
     * @param prefix 前缀，如 "PAY"、"ORD"
     * @return 带前缀的ID字符串
     */
    public String nextId(String prefix) {
        return prefix + nextId();
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }

    // ============================== 辅助方法 ==============================

    /**
     * 获取数据中心ID
     * 根据IP地址生成
     */
    private long getDatacenterId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                return new Random().nextInt((int) maxDatacenterId + 1);
            }
            byte[] mac = network.getHardwareAddress();
            if (mac == null) {
                return new Random().nextInt((int) maxDatacenterId + 1);
            }
            long id = ((0x000000FF & (long) mac[mac.length - 2])
                    | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
            return id % (maxDatacenterId + 1);
        } catch (Exception e) {
            log.warn("获取数据中心ID失败，使用随机值", e);
            return new Random().nextInt((int) maxDatacenterId + 1);
        }
    }

    /**
     * 获取工作机器ID
     * 根据进程ID生成
     */
    private long getWorkerId() {
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            int pid = Integer.parseInt(name.split("@")[0]);
            return pid % (maxWorkerId + 1);
        } catch (Exception e) {
            log.warn("获取工作机器ID失败，使用随机值", e);
            return new Random().nextInt((int) maxWorkerId + 1);
        }
    }

    /**
     * 解析ID获取时间戳
     *
     * @param id 雪花算法生成的ID
     * @return 时间戳
     */
    public static long extractTimestamp(long id) {
        return (id >> 22) + 1704067200000L;
    }

    /**
     * 解析ID获取生成时间
     *
     * @param id 雪花算法生成的ID
     * @return 生成时间字符串
     */
    public static String extractTime(long id) {
        long timestamp = extractTimestamp(id);
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .format(new java.util.Date(timestamp));
    }
}
