package com.goaerion.testcode.http.quant.service;

import com.goaerion.testcode.http.quant.entity.TdFetchLog;

import java.util.List;

/**
 * 拉取日志服务：记录每次任务的结果与 credits 消耗（td_fetch_log）
 */
public interface FetchLogService {

    /**
     * 记录一次成功的拉取（status = success）
     *
     * @param barsFetched 本次拉取的 K 线条数
     * @param creditsCost 本次消耗的 credits
     */
    void recordSuccess(String taskType, String symbol, String interval,
                       int barsFetched, int creditsCost);

    /**
     * 记录一次失败的拉取（status = failed），symbol/interval 可空（批量任务）
     */
    void recordFailure(String taskType, String symbol, String interval, String errorMsg);

    /**
     * 记录一次因配额不足跳过（status = skipped_no_quota，credits = 0）
     */
    void recordSkippedNoQuota(String taskType, String symbol, String interval);

    /**
     * 按任务类型查询最近 N 条日志，taskType 为空时不过滤，按开始时间倒序
     */
    List<TdFetchLog> recentLogs(String taskType, int limit);
}