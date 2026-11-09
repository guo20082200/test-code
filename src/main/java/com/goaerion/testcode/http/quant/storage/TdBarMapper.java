package com.goaerion.testcode.http.quant.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goaerion.testcode.http.quant.entity.TdBar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * K 线 Mapper：单表 CRUD 走 BaseMapper，
 * 批量幂等 upsert（ON CONFLICT DO UPDATE）等复杂 SQL 见 TdBarMapper.xml
 */
@Mapper
public interface TdBarMapper extends BaseMapper<TdBar> {

    /**
     * 批量幂等写入 K 线：冲突键 (symbol, interval, bar_time)，
     * 命中时用新值覆盖 OHLC/复权价/量额（复权、回补自动覆盖）
     *
     * @param bars K 线列表，非空
     * @return 受影响行数（含更新）
     */
    int batchUpsert(@Param("list") List<TdBar> bars);

    /**
     * 取某标的某周期最近 N 根 K 线，按时间正序返回（方便直接画图/算指标）
     */
    List<TdBar> selectRecentBars(@Param("symbol") String symbol,
                                 @Param("interval") String interval,
                                 @Param("limit") int limit);
}