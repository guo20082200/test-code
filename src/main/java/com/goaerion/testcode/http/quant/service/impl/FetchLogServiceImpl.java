package com.goaerion.testcode.http.quant.service.impl;

import com.goaerion.testcode.http.quant.entity.TdFetchLog;
import com.goaerion.testcode.http.quant.storage.TdFetchLogMapper;
import com.goaerion.testcode.http.quant.service.FetchLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 拉取日志服务实现
 */
@Service
@RequiredArgsConstructor
public class FetchLogServiceImpl implements FetchLogService {

    private final TdFetchLogMapper tdFetchLogMapper;

    @Override
    public void recordSuccess(String taskType, String symbol, String interval,
                              int barsFetched, int creditsCost) {
        insert(taskType, symbol, interval, "success", null, barsFetched, creditsCost);
    }

    @Override
    public void recordFailure(String taskType, String symbol, String interval, String errorMsg) {
        insert(taskType, symbol, interval, "failed", errorMsg, 0, 0);
    }

    @Override
    public void recordSkippedNoQuota(String taskType, String symbol, String interval) {
        insert(taskType, symbol, interval, "skipped_no_quota", null, 0, 0);
    }

    @Override
    public List<TdFetchLog> recentLogs(String taskType, int limit) {
        return tdFetchLogMapper.selectRecentLogs(taskType, limit);
    }

    /**
     * 组装并写入一条拉取日志：started_at 取当前时间（任务级耗时由调用方拆分粒度决定）
     */
    private void insert(String taskType, String symbol, String interval,
                        String status, String errorMsg, int barsFetched, int creditsCost) {
        OffsetDateTime now = OffsetDateTime.now();
        TdFetchLog log = new TdFetchLog();
        log.setTaskType(taskType);
        log.setSymbol(symbol);
        log.setInterval(interval);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        log.setBarsFetched(barsFetched);
        log.setCreditsCost(creditsCost);
        log.setStartedAt(now);
        log.setFinishedAt(now);
        tdFetchLogMapper.insert(log);
    }
}