package com.github.chenmingang.snowflake;

import com.github.chenmingang.snowflake.paxos.Proposer;

public class IdGenerator {

    public static IdGenerator INSTANCE = new IdGenerator();

    private IdGenerator() {
    }

    private void findWorkId() {
        workerId = Proposer.INSTANCE.proposal();
    }

    public static long genNextWorkId() {
        return INSTANCE.workerId + 1 & INSTANCE.maxWorkerId;
    }

    public static void main(String[] args) {
        long l1 = currentMillis();
        for (int i = 0; i < 100000000; i++) {
//            System.out.println(INSTANCE.nextId());
            INSTANCE.nextId();
        }
        long l2 = currentMillis();
        System.out.println(l2 - l1);
    }

    private volatile long workerId = 0;
    private final long startTime = 1519455270615L;   // 时间起始标记点，作为基准，一般取系统的最近时间
    private final long workerIdBits = 5L;      // 机器标识位数

    private final long maxWorkerId = -1L ^ -1L << this.workerIdBits;// 机器ID最大值: 31
    private long sequence = 0L;                   // 0，并发控制
    private final long sequenceBits = 17L;      //毫秒内自增位

    private final long workerIdShift = this.sequenceBits;                             // 12
    private final long timestampLeftShift = this.sequenceBits + this.workerIdBits;// 22
    private final long sequenceMask = -1L ^ -1L << this.sequenceBits;                 //131071,17位
    private long lastTimestamp = -1L;

    public long nextId() {
//        findWorkId();

        long current = currentMillis();

        checkClock(current);

        if (this.lastTimestamp == current) {
            this.sequence = this.sequence + 1 & this.sequenceMask;
            if (this.sequence == 0) {
                current = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            this.sequence = 0;
        }

        this.lastTimestamp = current;
        return genId(current);
    }

    /**
     * 时钟拨动
     *
     * @param current
     */
    private void checkClock(long current) {
        if (current < this.lastTimestamp) {
            throw new RuntimeException(String.format("clock moved backwards.Refusing to generate id " +
                    "for %d milliseconds", (this.lastTimestamp - current)));
        }
    }

    private long genId(long current) {
        return current - this.startTime << this.timestampLeftShift | this.workerId << this.workerIdShift | this.sequence;
    }

    /**
     * 等待下一个毫秒的到来, 保证返回的毫秒数在参数lastTimestamp之后
     */
    private long tilNextMillis(long lastTimestamp) {
        long current = currentMillis();
        while (current <= lastTimestamp) {
            current = currentMillis();
        }
        return current;
    }

    /**
     * 获得系统当前毫秒数
     */
    public static long currentMillis() {
        return System.currentTimeMillis();
    }
    public static long getMaxWorkerId() {
        return INSTANCE.maxWorkerId;
    }

    public long getWorkerId() {
        return workerId;
    }
}
