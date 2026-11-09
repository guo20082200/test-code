package com.goaerion.testcode.http.quant.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goaerion.testcode.http.quant.entity.TdStockCatalog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 股票目录 Mapper：单表 CRUD 走 BaseMapper，
 * 批量幂等 upsert（ON CONFLICT DO UPDATE）见 TdStockCatalogMapper.xml
 */
@Mapper
public interface TdStockCatalogMapper extends BaseMapper<TdStockCatalog> {

    /**
     * 批量幂等写入目录快照：冲突键 (symbol, exchange, mic_code)，
     * 命中时用新快照覆盖 name/type/figi_code 等并刷新 updated_at（重复抓取自动合并）
     *
     * @param list 目录列表，非空；id 由应用侧雪花算法生成后传入
     * @return 受影响行数（含更新）
     */
    int batchUpsert(@Param("list") List<TdStockCatalog> list);
}