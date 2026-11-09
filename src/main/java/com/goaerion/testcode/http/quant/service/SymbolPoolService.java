package com.goaerion.testcode.http.quant.service;

import com.goaerion.testcode.http.quant.entity.TdSymbol;

import java.util.List;

/**
 * 标的池服务：管理量化系统跟踪的金融工具清单（td_symbol）
 * <p>同步范围的唯一来源，调度器据此决定拉取哪些标的
 */
public interface SymbolPoolService {

    /**
     * 查询所有参与同步的标的（enabled = TRUE）
     */
    List<TdSymbol> listEnabled();

    /**
     * 查询全部标的（含停用）
     */
    List<TdSymbol> listAll();

    /**
     * 按标的代码查询（同代码多上市时返回第一条）
     *
     * @param symbol 标的代码，如 AAPL
     * @return 标的记录，不存在返回 NULL
     */
    TdSymbol getBySymbol(String symbol);

    /**
     * 注册新标的：id 不需设置，由应用侧雪花算法生成
     *
     * @return 生成的标的 id
     */
    Long register(TdSymbol symbol);

    /**
     * 更新标的信息（按 id 全量更新非空字段语义由调用方保证）
     */
    void update(TdSymbol symbol);

    /**
     * 启用/停用标的：停用后调度器跳过该标的
     */
    void setEnabled(Long id, boolean enabled);
}