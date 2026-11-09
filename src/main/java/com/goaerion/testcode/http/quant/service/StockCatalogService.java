package com.goaerion.testcode.http.quant.service;

import com.goaerion.testcode.http.quant.entity.TdStockCatalog;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 股票目录服务：td_stock_catalog 快照的导入与查询
 */
public interface StockCatalogService {

    /**
     * 批量幂等导入目录快照：冲突键 (symbol, exchange, mic_code)，
     * 命中时覆盖 name/type/figi_code 等（重复抓取自动合并）
     *
     * @param catalogs 目录列表
     * @return 受影响行数，空列表直接返回 0
     */
    int saveAll(List<TdStockCatalog> catalogs);

    /**
     * 按市场标识码查询（如 XNGS 主板）
     */
    List<TdStockCatalog> listByMic(String micCode);

    /**
     * 按工具类型查询（如 Common Stock）
     */
    List<TdStockCatalog> listByType(String type);

    /**
     * 按标的代码查询（同代码多市场时返回第一条）
     *
     * @return 目录记录，不存在返回 NULL
     */
    TdStockCatalog getBySymbol(String symbol);

    /**
     * 统计某快照时间的目录条数，用于核对导入完整性
     */
    long countByFetchedAt(OffsetDateTime fetchedAt);
}