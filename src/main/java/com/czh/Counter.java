package com.czh;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 计数器类
 */
public class Counter {

    /**
     * 正在执行的任务总数
     */
    private AtomicInteger currentJobCounter = new AtomicInteger(0);

    /**
     * 成功数计数器
     */
    private AtomicInteger successCounter = new AtomicInteger(0);

    /**
     * 失败数计数器
     */
    private AtomicInteger failCounter = new AtomicInteger(0);

    /**
     * 忽略数计数器
     */
    private AtomicInteger ignoreCounter = new AtomicInteger(0);


    /**
     * 执行次数
     */
    private AtomicInteger currentRunTimeCounter = new AtomicInteger(0);

    /**
     * 重置所有计数器
     */
    public void reset(){
        resetCurrentJobCounter();
        resetFailCounter();
        resetIgnoreCounter();
        resetSuccessCounter();
        resetCurrentRunTimeCounter();
    }

    public AtomicInteger getCurrentJobCounter() {
        return currentJobCounter;
    }

    public AtomicInteger getSuccessCounter() {
        return successCounter;
    }

    public AtomicInteger getFailCounter() {
        return failCounter;
    }

    public AtomicInteger getIgnoreCounter() {
        return ignoreCounter;
    }


    public AtomicInteger getCurrentRunTimeCounter() {
        return currentRunTimeCounter;
    }

    public void resetCurrentJobCounter() {
        currentJobCounter.set(0);
    }

    public void resetSuccessCounter() {
        successCounter.set(0);
    }

    public void resetFailCounter() {
        failCounter.set(0);
    }

    public void resetIgnoreCounter() {
        ignoreCounter.set(0);
    }

    public void resetCurrentRunTimeCounter() {
        currentRunTimeCounter.set(0);
    }
}
