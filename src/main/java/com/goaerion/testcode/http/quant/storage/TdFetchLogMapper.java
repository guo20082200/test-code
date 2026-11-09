package com.goaerion.testcode.http.quant.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goaerion.testcode.http.quant.entity.TdFetchLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拉取日志 Mapper：单表 CRUD 走 BaseMapper，日志查询见 TdFetchLogMapper.xml
 */
@Mapper
public interface TdFetchLogMapper extends BaseMapper<TdFetchLog> {

    /**
     * 按任务类型倒序查询最近 N 条拉取日志（taskType 为空时不过滤）
     */
    List<TdFetchLog> selectRecentLogs(@Param("taskType") String taskType,
                                      @Param("limit") int limit);
}