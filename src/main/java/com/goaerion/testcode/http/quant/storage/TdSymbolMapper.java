package com.goaerion.testcode.http.quant.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goaerion.testcode.http.quant.entity.TdSymbol;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 标的池 Mapper：单表 CRUD 走 BaseMapper，自定义查询见 TdSymbolMapper.xml
 */
@Mapper
public interface TdSymbolMapper extends BaseMapper<TdSymbol> {

    /**
     * 查询所有参与同步的标的（enabled = TRUE），调度器同步范围的唯一来源
     */
    List<TdSymbol> selectEnabledSymbols();
}