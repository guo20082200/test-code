-- =====================================================================
-- 股票目录表 DDL（Twelve Data /stocks 快照）
-- 数据来源：doc/quant/market_data/nasdaq_stocks.txt（exchange=NASDAQ，listings=4520，fetched=2026-11-05）
-- 数据库：PostgreSQL 14+
-- 约定：与 schema.sql 一致，id 为外部指定主键（无自增/默认值，由应用侧雪花算法生成）
--       目录表与标的池 td_symbol 职责分离：本表存全量目录快照（含 name/figi_code），
--       td_symbol 只存需要跟踪同步的子集
-- ---------------------------------------------------------------------

CREATE TABLE td_stock_catalog (
    id          int8 PRIMARY KEY,              -- 代理主键：外部指定（应用侧雪花算法生成）
    symbol      VARCHAR(32)  NOT NULL,         -- 标的代码：AAPL / AACIW（warrant 独立代码）
    name        VARCHAR(255),                  -- 标的全称：Apple Inc. / ATA Creativity Global
    currency    VARCHAR(8),                    -- 计价货币：USD
    exchange    VARCHAR(64)  NOT NULL,         -- 交易所名称：NASDAQ
    mic_code    VARCHAR(8),                    -- ISO 10383 市场标识码：XNGS / XNCM / XNMS / XNAS
    country     VARCHAR(64),                   -- 注册国家：United States
    type        VARCHAR(64),                   -- 工具类型：Common Stock / Preferred Stock / Warrant / American Depositary Receipt
    figi_code   VARCHAR(16),                   -- FIGI 金融工具全球标识（12 位）；部分标的为空
    fetched_at  TIMESTAMPTZ  NOT NULL,         -- 快照抓取日期（文件头 fetched=2026-11-05）
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_td_stock_catalog UNIQUE (symbol, exchange, mic_code)  -- 幂等键：ON CONFLICT 依据
);

COMMENT ON TABLE  td_stock_catalog            IS '股票目录快照：Twelve Data /stocks 全量清单（含 name/figi_code），与 td_symbol 跟踪池分离';
COMMENT ON COLUMN td_stock_catalog.symbol     IS '标的代码（Twelve Data symbol）';
COMMENT ON COLUMN td_stock_catalog.name       IS '标的全称，取自接口 name 列';
COMMENT ON COLUMN td_stock_catalog.currency   IS '计价货币，取自接口 currency 列';
COMMENT ON COLUMN td_stock_catalog.exchange   IS '交易所名称：NASDAQ';
COMMENT ON COLUMN td_stock_catalog.mic_code   IS '市场标识码：XNGS 主板 / XNCM 资本市场 / XNMS 全球市场 / XNAS 北欧';
COMMENT ON COLUMN td_stock_catalog.country    IS '注册国家，取自接口 country 列';
COMMENT ON COLUMN td_stock_catalog.type       IS '工具类型：Common Stock/Preferred Stock/Warrant/American Depositary Receipt 等';
COMMENT ON COLUMN td_stock_catalog.figi_code  IS 'FIGI 标识（OpenFIGI 12 位），需附加包时可能为空';
COMMENT ON COLUMN td_stock_catalog.fetched_at IS '快照抓取时间，同批数据取同一值';

-- 按市场标识码筛选（如只看主板 XNGS）
CREATE INDEX idx_stock_catalog_mic ON td_stock_catalog (mic_code, symbol);

-- 按工具类型筛选（如只看普通股）
CREATE INDEX idx_stock_catalog_type ON td_stock_catalog (type);