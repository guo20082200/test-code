package com.goaerion.testcode.http.quant.service.impl;

import com.goaerion.testcode.http.quant.entity.TdSyncCursor;
import com.goaerion.testcode.http.quant.storage.TdSyncCursorMapper;
import com.goaerion.testcode.http.quant.service.SyncCursorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * 同步游标服务实现
 */
@Service
@RequiredArgsConstructor
public class SyncCursorServiceImpl implements SyncCursorService {

    private final TdSyncCursorMapper tdSyncCursorMapper;

    @Override
    public TdSyncCursor getCursor(String symbol, String interval) {
        return tdSyncCursorMapper.selectBySymbolAndInterval(symbol, interval);
    }

    @Override
    public OffsetDateTime lastSyncedTime(String symbol, String interval) {
        TdSyncCursor cursor = getCursor(symbol, interval);
        return cursor == null ? null : cursor.getLastBarTime();
    }

    @Override
    public void advance(String symbol, String interval, OffsetDateTime lastBarTime) {
        TdSyncCursor cursor = new TdSyncCursor();
        cursor.setSymbol(symbol);
        cursor.setInterval(interval);
        cursor.setLastBarTime(lastBarTime);
        tdSyncCursorMapper.upsertCursor(cursor);
    }
}