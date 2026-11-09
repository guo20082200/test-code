package com.goaerion.testcode.http.quant.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goaerion.testcode.http.quant.entity.TdSyncCursor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 同步游标 Mapper：复合主键 (symbol, interval)，按主键的读写走 XML 手写 SQL
 */
@Mapper
public interface TdSyncCursorMapper extends BaseMapper<TdSyncCursor> {

    /**
     * 幂等写入游标：冲突键 (symbol, interval)，
     * 命中时推进 last_bar_time 并刷新 last_sync_at（断点续传依据）
     *
     * @return 受影响行数
     */
    int upsertCursor(TdSyncCursor cursor);

    /**
     * 按复合主键查询游标，未同步过返回 NULL
     */
    TdSyncCursor selectBySymbolAndInterval(@Param("symbol") String symbol,
                                           @Param("interval") String interval);
}