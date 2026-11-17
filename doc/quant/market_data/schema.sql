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
    id                int8 PRIMARY KEY,
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
    id            int8    PRIMARY KEY,          -- 代理主键：兼容 MyBatis-Plus BaseMapper 单一 @TableId
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
    id           int8 PRIMARY KEY,
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



ALTER TABLE td_bar
    ADD COLUMN typ_price NUMERIC(18,8);   -- 典型价格 (high+low+close)/3

ALTER TABLE td_bar
    ADD COLUMN med_price NUMERIC(18,8);   -- 中间价 (high+low)/2

ALTER TABLE td_bar
    ADD COLUMN wcl_price NUMERIC(18,8);   -- 加权收盘价 (high+low+2*close)/4

ALTER TABLE td_bar
    ADD COLUMN avg_price NUMERIC(18,8);   -- 平均价 (open+high+low+close)/4

ALTER TABLE td_bar
    ADD COLUMN calc_time TIMESTAMPTZ;   -- 衍生价最近计算时间；NULL=尚未计算

COMMENT ON COLUMN td_bar.calc_time IS '四个衍生价(typ/med/wcl/avg)最近一次计算时间，复权重算后据此定位过期行';

COMMENT ON COLUMN td_bar.typ_price IS '典型价格 (high+low+close)/3';
COMMENT ON COLUMN td_bar.med_price IS '中间价 (high+low)/2';
COMMENT ON COLUMN td_bar.wcl_price IS '加权收盘价 (high+low+2*close)/4';
COMMENT ON COLUMN td_bar.avg_price IS '平均价 (open+high+low+close)/4';


UPDATE td_bar
SET typ_price = (high + low + close) / 3,
    med_price = (high + low) / 2,
    wcl_price = (high + low + 2 * close) / 4,
    avg_price = (open + high + low + close) / 4,
    calc_time = now()
WHERE typ_price IS NULL                        -- 幂等：只补没算过的
   OR calc_time < updated_at;                  -- 或 K 线在算完之后又被改过



-- ============================================================
-- td_bar_indicator：技术指标表（宽表）
-- 口径约定：所有价格类指标一律基于 td_bar 复权价（adj_*）计算，
--          成交量类指标使用 td_bar.volume 原始值
-- 对齐关系：与 td_bar 以 (symbol, "interval", bar_time) 一一对应
-- 维护闭环：td_bar 复权因子/OHLC 变更（updated_at 刷新）后需重算本表
-- ============================================================
CREATE TABLE td_bar_indicator (
    symbol     varchar(32)  NOT NULL,             -- 标的代码，与 td_bar.symbol 对应（逻辑外键）
    "interval" varchar(8)   NOT NULL,             -- K线周期：1min/.../1day/1week/1month
    bar_time   timestamptz  NOT NULL,             -- K线开始时间，统一 UTC
    sma20      numeric(18, 8),                    -- SMA(20)：约1个月
    sma60      numeric(18, 8),                    -- SMA(60)：约1季度
    sma200     numeric(18, 8),                    -- SMA(200)：约1年，机构牛熊分界
    calc_time  timestamptz  NOT NULL DEFAULT now(), -- 本行全部指标最近一次计算时间
    CONSTRAINT td_bar_indicator_pkey PRIMARY KEY (symbol, "interval", bar_time)
);

CREATE INDEX idx_bar_indicator_symbol_time
    ON td_bar_indicator USING btree (symbol, "interval", bar_time DESC);

-- ============ 注释 ============
COMMENT ON TABLE td_bar_indicator IS
'技术指标表（宽表）：基于 td_bar 复权价计算的所有派生指标；价格类指标用 adj_* 口径，成交量类指标用原始 volume；与 td_bar 以 (symbol, "interval", bar_time) 对齐';

COMMENT ON COLUMN td_bar_indicator.symbol IS
'标的代码，与 td_bar.symbol 对应（逻辑外键，不建 FK 约束）';

COMMENT ON COLUMN td_bar_indicator."interval" IS
'K线周期：1min/5min/15min/30min/45min/1h/2h/4h/8h/1day/1week/1month，与 td_bar."interval" 对应';

COMMENT ON COLUMN td_bar_indicator.bar_time IS
'K线开始时间，统一 UTC，与 td_bar.bar_time 对应';

COMMENT ON COLUMN td_bar_indicator.sma20 IS
'SMA(20) = 近20根 adj_close 等权平均（约1个月）；窗口不足20根时为 NULL（对应 TA_SMA 的 lookback=19，非漏算）';

COMMENT ON COLUMN td_bar_indicator.sma60 IS
'SMA(60) = 近60根 adj_close 等权平均（约1季度）；窗口不足60根时为 NULL（对应 TA_SMA 的 lookback=59，非漏算）';

COMMENT ON COLUMN td_bar_indicator.sma200 IS
'SMA(200) = 近200根 adj_close 等权平均（约1年，机构牛熊分界）；窗口不足200根时为 NULL（对应 TA_SMA 的 lookback=199，非漏算）';

COMMENT ON COLUMN td_bar_indicator.calc_time IS
'本行全部指标最近一次计算时间；td_bar 的 adj_close/复权因子变更（updated_at 刷新）后需重算本表；加新指标列时纳入同批计算';

-- ============ 将来扩展新指标列的示例 ============
-- ALTER TABLE td_bar_indicator ADD COLUMN ema12 numeric(18, 8);
-- ALTER TABLE td_bar_indicator ADD COLUMN rsi14 numeric(18, 8);
-- ALTER TABLE td_bar_indicator ADD COLUMN macd        numeric(18, 8);
-- ALTER TABLE td_bar_indicator ADD COLUMN macd_signal numeric(18, 8);
-- ALTER TABLE td_bar_indicator ADD COLUMN macd_hist   numeric(18, 8);


-- 根据td_bar 来更新 td_bar_indicator

-- ============================================================
-- 根据 td_bar 刷新 td_bar_indicator（SMA 20/60/200，基于复权收盘价）
-- 幂等：可重复执行，ON CONFLICT 覆盖旧值
-- 策略：每日全量重刷（数据量不大时最简单可靠）
-- ============================================================
INSERT INTO td_bar_indicator (
    symbol, "interval", bar_time,
    sma20, sma60, sma200,
    calc_time
)
SELECT
    b.symbol,
    b."interval",
    b.bar_time,
    -- 每个周期独立守卫：凑不满 N 根就不出值（NULL）
    CASE WHEN cnt20  >= 20 THEN avg20  END AS sma20,
    CASE WHEN cnt60  >= 60 THEN avg60  END AS sma60,
    CASE WHEN cnt200 >= 200 THEN avg200 END AS sma200,
    now() AS calc_time
FROM (
    SELECT
        symbol,
        "interval",
        bar_time,
        adj_close,
        -- SMA20：当前行及其前 19 根
        COUNT(*)     OVER w20  AS cnt20,
        AVG(adj_close) OVER w20 AS avg20,
        COUNT(*)     OVER w60  AS cnt60,
        AVG(adj_close) OVER w60 AS avg60,
        COUNT(*)     OVER w200 AS cnt200,
        AVG(adj_close) OVER w200 AS avg200
    FROM td_bar
    WINDOW
        w20  AS (PARTITION BY symbol, "interval"
                 ORDER BY bar_time
                 ROWS BETWEEN 19   PRECEDING AND CURRENT ROW),
        w60  AS (PARTITION BY symbol, "interval"
                 ORDER BY bar_time
                 ROWS BETWEEN 59   PRECEDING AND CURRENT ROW),
        w200 AS (PARTITION BY symbol, "interval"
                 ORDER BY bar_time
                 ROWS BETWEEN 199  PRECEDING AND CURRENT ROW)
) b
WHERE b.cnt20 >= 20   -- 至少要有 SMA20 才值得落一行
ON CONFLICT (symbol, "interval", bar_time) DO UPDATE SET
    sma20     = EXCLUDED.sma20,
    sma60     = EXCLUDED.sma60,
    sma200    = EXCLUDED.sma200,
    calc_time = EXCLUDED.calc_time;


 -- 计算sma20的斜率值

 ALTER TABLE td_bar_indicator
     ADD COLUMN IF NOT EXISTS sma20_slope numeric(18, 8),
     ADD COLUMN IF NOT EXISTS sma20_r2    numeric(10, 6);

 COMMENT ON COLUMN td_bar_indicator.sma20_slope IS
     'SMA20 回归斜率，k=10 点最小二乘，归一化为 %/天；正=向上，负=向下';
 COMMENT ON COLUMN td_bar_indicator.sma20_r2    IS
     'SMA20 回归拟合优度 R²（0~1）；>0.5 视为线性趋势成立，过滤震荡市';


-- ============================================================
-- SMA20 斜率（k=10 最小二乘回归，%/天）+ R²
-- 幂等：可重复执行
-- ============================================================
UPDATE td_bar_indicator i
SET sma20_slope = r.slope_pct,
    sma20_r2    = r.r2,
    calc_time   = now()
FROM (
    SELECT
        symbol, "interval", bar_time,
        -- 归一化：每根 bar 的斜率 ÷ 窗口均价 × 100 = %/天
        slope / avg_y * 100          AS slope_pct,
        -- R² = num² / (den_x × den_y)；全线走平时 den_y=0 → NULL
        num * num / NULLIF(den_x * den_y, 0) AS r2
    FROM (
        SELECT
            symbol, "interval", bar_time,
            cnt,
            avg_y,
            -- 对 x=rownum, y=sma20 做最小二乘：
            -- slope = (k·Σxy − Σx·Σy) / (k·Σx² − (Σx)²)
            k * sxy - sx * sy            AS num,
            k * sxx - sx * sx            AS den_x,   -- 窗口固定时恒为 825
            k * syy - sy * sy            AS den_y
        FROM (
            SELECT
                symbol, "interval", bar_time,
                10 AS k,
                AVG(sma20)               OVER w10 AS avg_y,
                COUNT(sma20)             OVER w10 AS cnt,
                SUM(rn)                  OVER w10 AS sx,
                SUM(rn * rn)             OVER w10 AS sxx,
                SUM(sma20)               OVER w10 AS sy,
                SUM(sma20 * sma20)       OVER w10 AS syy,
                SUM(rn * sma20)          OVER w10 AS sxy
            FROM (
                SELECT symbol, "interval", bar_time, sma20,
                       ROW_NUMBER() OVER (PARTITION BY symbol, "interval"
                                          ORDER BY bar_time) AS rn
                FROM td_bar_indicator
                WHERE sma20 IS NOT NULL      -- 先剔除前 19 根空值，保证 rn 连续
            ) t
            WINDOW w10 AS (PARTITION BY symbol, "interval"
                           ORDER BY bar_time
                           ROWS BETWEEN 9 PRECEDING AND CURRENT ROW)
        ) s
    ) m
    WHERE cnt = 10          -- 凑满 10 个有效 SMA20 点才回归
      AND den_x <> 0
) r
WHERE i.symbol     = r.symbol
  AND i."interval" = r."interval"
  AND i.bar_time   = r.bar_time;


CREATE TABLE tmp_sma20_indicator AS
SELECT
    symbol,                          -- 品种代码（透传）
    "interval",                      -- K线周期（透传）
    bar_time,                        -- K线时间（透传）

    10 AS k,                         -- 回归窗口长度：10 根 K 线（即 k=10 点最小二乘）

    -- ===== 下面 7 个都是「最近 10 根」窗口内的统计量 =====
    -- 窗口定义 w10：按时间排序，当前行 + 前 9 行

    AVG(sma20)         OVER w10 AS avg_y,   -- 窗口内 sma20 的均值 ȳ，用于斜率归一化（%/天 = slope/ȳ×100）

    COUNT(sma20)       OVER w10 AS cnt,     -- 窗口内有效 sma20 的个数；=10 才凑满一个完整窗口，不足则跳过

    SUM(rn)            OVER w10 AS sx,      -- Σx：窗口内序号之和。x 取窗口内连续序号（如 1..10），恒 = 55

    SUM(rn * rn)       OVER w10 AS sxx,     -- Σx²：序号平方和。恒 = 385。配合 sx 得回归分母 den_x = k·Σx² − (Σx)² = 825

    SUM(sma20)         OVER w10 AS sy,      -- Σy：窗口内 sma20 之和（y = sma20，即回归对象是均线序列本身）

    SUM(sma20 * sma20) OVER w10 AS syy,     -- Σy²：sma20 平方和。配合 sy 得 den_y = k·Σy² − (Σy)²，用于算 R²；
                                            --   均线走平（所有值相等）时 den_y = 0，R² 无定义 → 置 NULL

    SUM(rn * sma20)    OVER w10 AS sxy      -- Σxy：序号与 sma20 的交叉乘积和。分子 num = k·Σxy − Σx·Σy：
                                            --   num/den_x = 斜率（每根 K 线的均线变化量）；
                                            --   num²/(den_x·den_y) = R²（趋势的“直线性”程度）
FROM (
    SELECT
        symbol, "interval", bar_time,
        sma20,
        ROW_NUMBER() OVER (PARTITION BY symbol, "interval"
                           ORDER BY bar_time) AS rn   -- 窗口内序号 x：同一品种同周期下按时间递增编号 1,2,3...
                                                       -- 先剔除 sma20 为空的行再编号，保证窗口内 x 严格连续等差
                                                       -- （最小二乘公式要求 x 等差，若有空洞斜率就算错）
    FROM td_bar_indicator
    WHERE sma20 IS NOT NULL
) t
WINDOW w10 AS (PARTITION BY symbol, "interval"        -- 每个品种×周期独立计算
               ORDER BY bar_time
               ROWS BETWEEN 9 PRECEDING AND CURRENT ROW);  -- 物理帧：当前行 + 前 9 行，共 10 行


-- ============================================================
-- tmp_sma20_indicator 表注释
-- （CTAS 建的表默认没有注释，需手动补）
-- ============================================================
COMMENT ON TABLE tmp_sma20_indicator IS
    'SMA20 回归中间量表：对 sma20 做 k=10 点最小二乘回归的窗口统计量；den_x = k·sxx − sx² 恒为 825（等差平移不变），num = k·sxy − sx·sy；slope = num/825，R² = num²/(825·den_y)，den_y = k·syy − sy²';

COMMENT ON COLUMN tmp_sma20_indicator.symbol IS '品种代码';

COMMENT ON COLUMN tmp_sma20_indicator."interval" IS 'K线周期（如 1d、60m）';

COMMENT ON COLUMN tmp_sma20_indicator.bar_time IS 'K线时间';

COMMENT ON COLUMN tmp_sma20_indicator.k IS '回归窗口长度，固定 10 根 K 线';

COMMENT ON COLUMN tmp_sma20_indicator.avg_y IS '窗口内 sma20 均值 ȳ；斜率归一化用：%/天 = slope/ȳ×100';

COMMENT ON COLUMN tmp_sma20_indicator.cnt IS '窗口内有效 sma20 点数；=10 才凑满完整窗口，不足 10 不参与回归';

COMMENT ON COLUMN tmp_sma20_indicator.sx IS 'Σx：窗口内 10 个序号之和；rn 是全序列编号，随行位置递增（首个窗口=55，之后每个窗口+10），不恒定';

COMMENT ON COLUMN tmp_sma20_indicator.sxx IS 'Σx²：窗口内序号平方和，随行位置变化，不恒定';

COMMENT ON COLUMN tmp_sma20_indicator.sy IS 'Σy：窗口内 sma20 之和';

COMMENT ON COLUMN tmp_sma20_indicator.syy IS 'Σy²：sma20 平方和；den_y = k·syy − sy²，均线走平时 den_y=0 → R² 置 NULL';

COMMENT ON COLUMN tmp_sma20_indicator.sxy IS 'Σxy：序号×sma20 交叉和；分子 num = k·sxy − sx·sy，num/den_x 即斜率';


-- 验证
SELECT DISTINCT k * sxx - sx * sx AS den_x   -- 应该只返回一行：825
FROM tmp_sma20_indicator
WHERE cnt = 10;

-- 正式表
CREATE TABLE td_indicator_slope (
    symbol     varchar(32)   NOT NULL,                 -- 品种代码
    "interval" varchar(8)    NOT NULL,                 -- K线周期
    bar_time   timestamptz   NOT NULL,                 -- K线时间（回归窗口的最后一根）
    k          integer       NOT NULL DEFAULT 10,      -- 回归窗口长度
    avg_y      numeric(18, 8),                         -- 窗口内 sma20 均值 ȳ
    cnt        integer,                                -- 窗口内有效 sma20 点数
    sx         numeric(20, 0),                         -- Σx
    sxx        numeric(30, 0),                         -- Σx²
    sy         numeric(30, 8),                         -- Σy
    syy        numeric(38, 8),                         -- Σy²
    sxy        numeric(38, 8),                         -- Σxy
    num        numeric(38, 8),                         -- 回归分子 num = k·sxy − sx·sy
    den_x      numeric(38, 8),                         -- 回归分母 den_x = k·sxx − sx²，恒为 825
    den_y      numeric(38, 8),                         -- R² 分母 den_y = k·syy − sy²
    slope_pct  numeric(18, 8),                         -- 归一化斜率 %/天
    r2         numeric(10, 6),                         -- 拟合优度 0~1
    calc_time  timestamptz   NOT NULL DEFAULT now(),   -- 计算时间
    CONSTRAINT td_indicator_slope_pkey PRIMARY KEY (symbol, "interval", bar_time)
);

CREATE INDEX IF NOT EXISTS idx_indicator_slope_bar_time
    ON td_indicator_slope (bar_time DESC);

    COMMENT ON TABLE td_indicator_slope IS
        'SMA20 趋势斜率表：对 sma20 做 k=10 点最小二乘回归，保留全部回归中间量与最终结果 slope_pct、r2，用于趋势判定门控';

    COMMENT ON COLUMN td_indicator_slope.symbol     IS '品种代码';
    COMMENT ON COLUMN td_indicator_slope."interval" IS 'K线周期（如 1d、60m）';
    COMMENT ON COLUMN td_indicator_slope.bar_time   IS 'K线时间（回归窗口的最后一根 bar）';
    COMMENT ON COLUMN td_indicator_slope.k          IS '回归窗口长度（点数），默认 10';

    COMMENT ON COLUMN td_indicator_slope.avg_y IS '窗口内 sma20 均值 ȳ；斜率归一化分母';
    COMMENT ON COLUMN td_indicator_slope.cnt   IS '窗口内有效 sma20 点数；=k 才参与回归';
    COMMENT ON COLUMN td_indicator_slope.sx    IS 'Σx：窗口内 10 个序号之和，随行位置递增（首个窗口=55，之后每个窗口+10）';
    COMMENT ON COLUMN td_indicator_slope.sxx   IS 'Σx²：窗口内序号平方和，随行位置变化';
    COMMENT ON COLUMN td_indicator_slope.sy    IS 'Σy：窗口内 sma20 之和';
    COMMENT ON COLUMN td_indicator_slope.syy   IS 'Σy²：窗口内 sma20 平方和';
    COMMENT ON COLUMN td_indicator_slope.sxy   IS 'Σxy：序号×sma20 交叉乘积和';

    COMMENT ON COLUMN td_indicator_slope.num   IS '回归分子：num = k·sxy − sx·sy';
    COMMENT ON COLUMN td_indicator_slope.den_x IS '回归分母：den_x = k·sxx − sx²，x 等差平移不变，恒为 825';
    COMMENT ON COLUMN td_indicator_slope.den_y IS 'R² 分母：den_y = k·syy − sy²；均线完全走平时为 0';

    COMMENT ON COLUMN td_indicator_slope.slope_pct IS
        '归一化斜率 = num/den_x/avg_y×100，单位 %/天；正=向上，负=向下';
    COMMENT ON COLUMN td_indicator_slope.r2 IS
        '拟合优度 = num²/(den_x·den_y)，0~1；>0.5 视为线性趋势成立；den_y=0 时为 NULL';
    COMMENT ON COLUMN td_indicator_slope.calc_time IS '本行计算时间';

INSERT INTO td_indicator_slope (
    symbol, "interval", bar_time,
    k, avg_y, cnt, sx, sxx, sy, syy, sxy,
    num, den_x, den_y,
    slope_pct, r2,
    calc_time
)
WITH base AS (
    -- 对 x=rn, y=sma20 的 10 行窗口统计
    SELECT
        symbol, "interval", bar_time,
        AVG(sma20)         OVER w10 AS avg_y,
        COUNT(sma20)       OVER w10 AS cnt,
        SUM(rn)            OVER w10 AS sx,
        SUM(rn * rn)       OVER w10 AS sxx,
        SUM(sma20)         OVER w10 AS sy,
        SUM(sma20 * sma20) OVER w10 AS syy,
        SUM(rn * sma20)    OVER w10 AS sxy
    FROM (
        SELECT symbol, "interval", bar_time, sma20,
               ROW_NUMBER() OVER (PARTITION BY symbol, "interval"
                                  ORDER BY bar_time) AS rn
        FROM td_bar_indicator
        WHERE sma20 IS NOT NULL
    ) t
    WINDOW w10 AS (PARTITION BY symbol, "interval"
                   ORDER BY bar_time
                   ROWS BETWEEN 9 PRECEDING AND CURRENT ROW)
)
SELECT
    symbol,
    "interval",
    bar_time,
    10                  AS k,
    avg_y,
    cnt,
    sx,
    sxx,
    sy,
    syy,
    sxy,
    10 * sxy - sx * sy  AS num,
    10 * sxx - sx * sx  AS den_x,   -- 恒为 825
    10 * syy - sy * sy  AS den_y,
    CASE WHEN cnt = 10
         THEN (10 * sxy - sx * sy) / (10 * sxx - sx * sx) / avg_y * 100
    END AS slope_pct,
    CASE WHEN cnt = 10
         THEN (10 * sxy - sx * sy) * (10 * sxy - sx * sy)
              / NULLIF((10 * sxx - sx * sx) * (10 * syy - sy * sy), 0)
    END AS r2,
    now() AS calc_time
FROM base
WHERE cnt = 10                     -- 只落凑满窗口的行
ON CONFLICT (symbol, "interval", bar_time) DO UPDATE SET
    avg_y     = EXCLUDED.avg_y,
    cnt       = EXCLUDED.cnt,
    sx        = EXCLUDED.sx,
    sxx       = EXCLUDED.sxx,
    sy        = EXCLUDED.sy,
    syy       = EXCLUDED.syy,
    sxy       = EXCLUDED.sxy,
    num       = EXCLUDED.num,
    den_x     = EXCLUDED.den_x,
    den_y     = EXCLUDED.den_y,
    slope_pct = EXCLUDED.slope_pct,
    r2        = EXCLUDED.r2,
    k         = EXCLUDED.k,
    calc_time = EXCLUDED.calc_time;

--- 趋势确认
SELECT
    symbol, "interval", bar_time, slope_pct, r2,
    -- 最近 20 天里"显著为负"的占比
    AVG(CASE WHEN slope_pct < -0.05 THEN 1.0 ELSE 0.0 END)
        OVER (PARTITION BY symbol, "interval"
              ORDER BY bar_time
              ROWS BETWEEN 19 PRECEDING AND CURRENT ROW) AS down_ratio,
    -- 下跌趋势确认
    CASE WHEN AVG(CASE WHEN slope_pct < -0.05 THEN 1.0 ELSE 0.0 END)
              OVER (PARTITION BY symbol, "interval"
                    ORDER BY bar_time
                    ROWS BETWEEN 19 PRECEDING AND CURRENT ROW) >= 0.8
         THEN 1 ELSE 0 END AS trend_down
FROM td_indicator_slope;