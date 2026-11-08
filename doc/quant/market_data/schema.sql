-- =====================================================================
-- 行情数据模块 DDL（Twelve Data → PostgreSQL）
-- 关联技术方案：doc/quant/market_data/行情数据获取与存储技术方案.md
-- 数据库：PostgreSQL 14+
-- 约定：所有时间字段统一 UTC（TIMESTAMPTZ）；价格用 NUMERIC(18,8) 保精度
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 标的池：要跟踪哪些 symbol（同步范围的唯一来源）
-- ---------------------------------------------------------------------
CREATE TABLE td_symbol (
    id                BIGSERIAL PRIMARY KEY,
    symbol            VARCHAR(32)  NOT NULL,                 -- 标的代码：AAPL / EUR/USD / ETH/BTC；优先股用点号（BRK.A）
    exchange          VARCHAR(64),                           -- 交易所名称（如 NASDAQ），外汇/加密货币可空
    mic_code          VARCHAR(8),                            -- ISO 10383 市场标识码（如 XNAS）
    asset_type        VARCHAR(16)  NOT NULL,                 -- 粗分类：stock / forex / crypto / etf / fund / commodity / bond
    instrument_type   VARCHAR(32),                           -- 细分类（来自接口 meta.type）：Common Stock / Preferred Stock / ADR / ETF / Index 等
    currency          VARCHAR(8),                            -- 交易/计价货币（来自接口 meta.currency）：USD / CNY / USDT
    exchange_timezone VARCHAR(64),                           -- 交易所时区（来自接口 meta.exchange_timezone）：America/New_York；日线按交易所日期对齐时依赖它
    interval_set      VARCHAR(64)  NOT NULL DEFAULT '1day',  -- 需同步的 interval 集合，逗号分隔（1min,5min,1day）
    enabled           BOOLEAN      NOT NULL DEFAULT TRUE,    -- FALSE 时调度器跳过该标的
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_td_symbol UNIQUE (symbol, exchange, asset_type)
);

COMMENT ON TABLE  td_symbol                   IS '标的池：量化系统跟踪的金融工具清单（含接口 meta 属性快照）';
COMMENT ON COLUMN td_symbol.symbol            IS '标的代码（Twelve Data symbol）';
COMMENT ON COLUMN td_symbol.exchange          IS '交易所名称，可空（外汇/加密货币通常为空）';
COMMENT ON COLUMN td_symbol.mic_code          IS 'ISO 10383 市场标识码';
COMMENT ON COLUMN td_symbol.asset_type        IS '资产粗分类：stock/forex/crypto/etf/fund/commodity/bond';
COMMENT ON COLUMN td_symbol.instrument_type   IS '工具细分类，取自 meta.type';
COMMENT ON COLUMN td_symbol.currency          IS '交易/计价货币，取自 meta.currency；与 td_bar.currency 比对做漂移校验';
COMMENT ON COLUMN td_symbol.exchange_timezone IS '交易所时区，取自 meta.exchange_timezone；UTC↔交易所日期换算依赖它';
COMMENT ON COLUMN td_symbol.interval_set      IS '需同步的 K 线周期集合，逗号分隔';
COMMENT ON COLUMN td_symbol.enabled           IS '是否参与同步';

-- ---------------------------------------------------------------------
-- 2. K 线表：所有 interval 共用一张表（interval 在唯一键中）
--    写入方式：INSERT ... ON CONFLICT (symbol, interval, bar_time) DO UPDATE（幂等，复权/回补自动覆盖）
--    symbol 直接存代码（不引用 td_symbol.id）：入库路径零 JOIN；标的存在性由 td_symbol 管辖
-- ---------------------------------------------------------------------
CREATE TABLE td_bar (
    id            BIGSERIAL    PRIMARY KEY,          -- 代理主键：兼容 MyBatis-Plus BaseMapper 单一 @TableId
    symbol        VARCHAR(32)  NOT NULL,             -- 标的代码，与 td_symbol.symbol 对应（逻辑外键，不建 FK 约束）
    interval      VARCHAR(8)   NOT NULL,   -- 1min/5min/15min/30min/45min/1h/2h/4h/8h/1day/1week/1month
    bar_time      TIMESTAMPTZ  NOT NULL,   -- K 线开始时间，统一 UTC
    open          NUMERIC(18,8),           -- 原始开盘价（未复权）
    high          NUMERIC(18,8),           -- 原始最高价（未复权）
    low           NUMERIC(18,8),           -- 原始最低价（未复权）
    close         NUMERIC(18,8),           -- 原始收盘价（未复权）
    adj_close     NUMERIC(18,8),           -- 复权收盘价：回测收益/收益序列用它；技术指标用 close
    volume        NUMERIC(24,8),           -- 成交量（NUMERIC：加密货币成交量是小数，BIGINT 会丢精度）
    amount        NUMERIC(24,8),           -- 成交额；接口未提供时留空（可由近似价×量后补）
    currency      VARCHAR(8),              -- 计价货币快照（USD/CNY/USDT...），同表混币种时聚合统计依赖它
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),   -- 首次入库时间
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),   -- 最近覆盖时间（复权/回补后可见变化）
    UNIQUE (symbol, interval, bar_time)  -- 幂等键：ON CONFLICT 依据
);

COMMENT ON TABLE  td_bar              IS 'K 线表：日/周/月/分钟线共用，(symbol, interval, bar_time) 幂等';
COMMENT ON COLUMN td_bar.symbol       IS '标的代码，对应 td_symbol.symbol（逻辑关联，无 FK 约束）';
COMMENT ON COLUMN td_bar.bar_time     IS 'K 线开始时间（UTC）';
COMMENT ON COLUMN td_bar.adj_close    IS '复权收盘价（Twelve Data adjusted 口径）；收益计算用，价格展示用 close';
COMMENT ON COLUMN td_bar.volume       IS '成交量；加密货币为小数（如 0.5371 BTC），故用 NUMERIC';
COMMENT ON COLUMN td_bar.amount       IS '成交额；美股 time_series 不返回，可留空';
COMMENT ON COLUMN td_bar.currency     IS '计价货币快照，取自响应 meta.currency';
COMMENT ON COLUMN td_bar.created_at   IS '首次入库时间';
COMMENT ON COLUMN td_bar.updated_at   IS '最近一次覆盖入库时间（复权修正后更新）';

-- 按时间倒序查询的最常用索引（取最近 N 根 K 线）
CREATE INDEX idx_bar_symbol_time ON td_bar (symbol, interval, bar_time DESC);

-- ---------------------------------------------------------------------
-- 3. 同步游标：每个 symbol+interval 已同步到的时间点
--    全量回补与日常增量共用（断点续传：中断后从 last_bar_time 继续）
-- ---------------------------------------------------------------------
CREATE TABLE td_sync_cursor (
    symbol        VARCHAR(32)  NOT NULL,             -- 标的代码，与 td_bar.symbol 同口径
    interval      VARCHAR(8)   NOT NULL,
    last_bar_time TIMESTAMPTZ,             -- 已同步的最后一条 K 线时间（UTC）；NULL 表示尚未开始
    last_sync_at  TIMESTAMPTZ,             -- 最近一次同步动作的时间
    PRIMARY KEY (symbol, interval)
);

COMMENT ON TABLE  td_sync_cursor               IS '同步游标：增量拉取起点，支持断点续传';
COMMENT ON COLUMN td_sync_cursor.symbol        IS '标的代码（与 td_bar.symbol 同口径）';
COMMENT ON COLUMN td_sync_cursor.last_bar_time IS '已同步到的最后一条 K 线时间（UTC），NULL=未开始';

-- ---------------------------------------------------------------------
-- 4. 拉取日志：每次任务的结果与 credits 消耗
-- ---------------------------------------------------------------------
CREATE TABLE td_fetch_log (
    id           BIGSERIAL PRIMARY KEY,
    task_type    VARCHAR(32)  NOT NULL,    -- time_series_backfill / time_series_increment / quote / api_usage
    symbol       VARCHAR(32),              -- 记录便于排查；批量任务可空
    interval     VARCHAR(8),
    bars_fetched INT             NOT NULL DEFAULT 0,
    status       VARCHAR(16)   NOT NULL,    -- success / partial / failed / skipped_no_quota
    error_msg    TEXT,
    credits_cost INT             NOT NULL DEFAULT 1,
    started_at   TIMESTAMPTZ   NOT NULL,
    finished_at  TIMESTAMPTZ
);

COMMENT ON TABLE  td_fetch_log             IS '拉取日志：任务级结果记录与 credits 消耗追踪';
COMMENT ON COLUMN td_fetch_log.task_type   IS '任务类型：time_series_backfill/time_series_increment/quote/api_usage';
COMMENT ON COLUMN td_fetch_log.status      IS '结果状态：success/partial/failed/skipped_no_quota';

-- 日志查询索引：按任务类型+时间排查
CREATE INDEX idx_fetch_log_type_time ON td_fetch_log (task_type, started_at DESC);
CREATE INDEX idx_fetch_log_symbol    ON td_fetch_log (symbol, started_at DESC);
