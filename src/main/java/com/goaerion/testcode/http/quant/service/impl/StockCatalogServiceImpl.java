package com.goaerion.testcode.http.quant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.goaerion.testcode.http.quant.entity.TdStockCatalog;
import com.goaerion.testcode.http.quant.service.StockCatalogService;
import com.goaerion.testcode.http.quant.storage.TdStockCatalogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 股票目录服务实现
 */
@Service
@RequiredArgsConstructor
public class StockCatalogServiceImpl implements StockCatalogService {

    private final TdStockCatalogMapper tdStockCatalogMapper;

    @Override
    public int saveAll(List<TdStockCatalog> catalogs) {
        if (catalogs == null || catalogs.isEmpty()) {
            return 0;
        }
        return tdStockCatalogMapper.batchUpsert(catalogs);
    }

    @Override
    public List<TdStockCatalog> listByMic(String micCode) {
        return tdStockCatalogMapper.selectList(new LambdaQueryWrapper<TdStockCatalog>()
                .eq(TdStockCatalog::getMicCode, micCode)
                .orderByAsc(TdStockCatalog::getSymbol));
    }

    @Override
    public List<TdStockCatalog> listByType(String type) {
        return tdStockCatalogMapper.selectList(new LambdaQueryWrapper<TdStockCatalog>()
                .eq(TdStockCatalog::getType, type)
                .orderByAsc(TdStockCatalog::getSymbol));
    }

    @Override
    public TdStockCatalog getBySymbol(String symbol) {
        return tdStockCatalogMapper.selectList(new LambdaQueryWrapper<TdStockCatalog>()
                        .eq(TdStockCatalog::getSymbol, symbol))
                .stream().findFirst().orElse(null);
    }

    @Override
    public long countByFetchedAt(OffsetDateTime fetchedAt) {
        return tdStockCatalogMapper.selectCount(new LambdaQueryWrapper<TdStockCatalog>()
                .eq(TdStockCatalog::getFetchedAt, fetchedAt));
    }
}