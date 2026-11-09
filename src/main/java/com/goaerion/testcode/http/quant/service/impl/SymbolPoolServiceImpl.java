package com.goaerion.testcode.http.quant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.goaerion.testcode.http.quant.entity.TdSymbol;
import com.goaerion.testcode.http.quant.storage.TdSymbolMapper;
import com.goaerion.testcode.http.quant.service.SymbolPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标的池服务实现
 */
@Service
@RequiredArgsConstructor
public class SymbolPoolServiceImpl implements SymbolPoolService {

    private final TdSymbolMapper tdSymbolMapper;

    @Override
    public List<TdSymbol> listEnabled() {
        return tdSymbolMapper.selectEnabledSymbols();
    }

    @Override
    public List<TdSymbol> listAll() {
        return tdSymbolMapper.selectList(
                new LambdaQueryWrapper<TdSymbol>().orderByAsc(TdSymbol::getSymbol));
    }

    @Override
    public TdSymbol getBySymbol(String symbol) {
        return tdSymbolMapper.selectList(new LambdaQueryWrapper<TdSymbol>()
                        .eq(TdSymbol::getSymbol, symbol))
                .stream().findFirst().orElse(null);
    }

    @Override
    public Long register(TdSymbol symbol) {
        tdSymbolMapper.insert(symbol);
        return symbol.getId();
    }

    @Override
    public void update(TdSymbol symbol) {
        tdSymbolMapper.updateById(symbol);
    }

    @Override
    public void setEnabled(Long id, boolean enabled) {
        tdSymbolMapper.update(null, new LambdaUpdateWrapper<TdSymbol>()
                .eq(TdSymbol::getId, id)
                .set(TdSymbol::getEnabled, enabled));
    }
}