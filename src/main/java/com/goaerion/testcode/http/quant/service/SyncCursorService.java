package com.goaerion.testcode.http.quant.service;

import com.goaerion.testcode.http.quant.entity.TdSyncCursor;

import java.time.OffsetDateTime;

/**
 * 同步游标服务：管理每个 symbol+interval 的同步进度（td_sync_cursor）
 * <p>全量回补与日常增量共用，支持断点续传
 */
public interface SyncCursorService {

    /**
     * 查询同步游标
     *
     * @return 游标记录，从未同步过返回 NULL
     */
    TdSyncCursor getCursor(String symbol, String interval);

    /**
     * 查询本轮拉取的起点时间（断点续传依据）
     *
     * @return 已同步到的最后一条 K 线时间；从未同步返回 NULL（表示需全量回补）
     */
    OffsetDateTime lastSyncedTime(String symbol, String interval);

    /**
     * 推进游标：幂等写入，命中复合主键 (symbol, interval) 时覆盖并刷新 last_sync_at
     *
     * @param lastBarTime 本轮已同步到的最后一条 K 线时间
     */
    void advance(String symbol, String interval, OffsetDateTime lastBarTime);
}