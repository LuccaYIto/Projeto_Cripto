-- Esquema e migração mínima para o Projeto Cripto (PostgreSQL).
-- Execute conectado ao banco BancoCripto. Faça backup antes de aplicar em banco já existente.

CREATE TABLE IF NOT EXISTS public."Cliente" (
    "Nome" VARCHAR(160) NOT NULL,
    "CPF" VARCHAR(20) NOT NULL,
    "Senha" VARCHAR(255) NOT NULL,
    "SaldoReais" NUMERIC(28, 8) NOT NULL DEFAULT 0,
    "SaldoBitcoin" NUMERIC(28, 8) NOT NULL DEFAULT 0,
    "SaldoEthereum" NUMERIC(28, 8) NOT NULL DEFAULT 0,
    "SaldoRipple" NUMERIC(28, 8) NOT NULL DEFAULT 0
);

-- Corrige nomes usados em uma versão anterior do projeto, sem sobrescrever dados.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'Cliente' AND column_name = 'SaldoBiticoin')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'Cliente' AND column_name = 'SaldoBitcoin') THEN
        ALTER TABLE public."Cliente" RENAME COLUMN "SaldoBiticoin" TO "SaldoBitcoin";
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'Cliente' AND column_name = 'SaldoEtherium')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'Cliente' AND column_name = 'SaldoEthereum') THEN
        ALTER TABLE public."Cliente" RENAME COLUMN "SaldoEtherium" TO "SaldoEthereum";
    END IF;
END $$;

ALTER TABLE public."Cliente" ADD COLUMN IF NOT EXISTS "SaldoReais" NUMERIC(28, 8) DEFAULT 0;
ALTER TABLE public."Cliente" ADD COLUMN IF NOT EXISTS "SaldoBitcoin" NUMERIC(28, 8) DEFAULT 0;
ALTER TABLE public."Cliente" ADD COLUMN IF NOT EXISTS "SaldoEthereum" NUMERIC(28, 8) DEFAULT 0;
ALTER TABLE public."Cliente" ADD COLUMN IF NOT EXISTS "SaldoRipple" NUMERIC(28, 8) DEFAULT 0;

-- Converte colunas de saldo VARCHAR da versão inicial para NUMERIC.
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoReais" TYPE NUMERIC(28, 8)
    USING COALESCE(NULLIF(BTRIM("SaldoReais"::TEXT), ''), '0')::NUMERIC;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoBitcoin" TYPE NUMERIC(28, 8)
    USING COALESCE(NULLIF(BTRIM("SaldoBitcoin"::TEXT), ''), '0')::NUMERIC;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoEthereum" TYPE NUMERIC(28, 8)
    USING COALESCE(NULLIF(BTRIM("SaldoEthereum"::TEXT), ''), '0')::NUMERIC;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoRipple" TYPE NUMERIC(28, 8)
    USING COALESCE(NULLIF(BTRIM("SaldoRipple"::TEXT), ''), '0')::NUMERIC;

UPDATE public."Cliente" SET
    "SaldoReais" = COALESCE("SaldoReais", 0),
    "SaldoBitcoin" = COALESCE("SaldoBitcoin", 0),
    "SaldoEthereum" = COALESCE("SaldoEthereum", 0),
    "SaldoRipple" = COALESCE("SaldoRipple", 0);
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoReais" SET DEFAULT 0;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoReais" SET NOT NULL;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoBitcoin" SET DEFAULT 0;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoBitcoin" SET NOT NULL;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoEthereum" SET DEFAULT 0;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoEthereum" SET NOT NULL;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoRipple" SET DEFAULT 0;
ALTER TABLE public."Cliente" ALTER COLUMN "SaldoRipple" SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS "Cliente_CPF_unico" ON public."Cliente" ("CPF");

-- Cotações globais do simulador. Os valores iniciais são exemplos didáticos, não preços de mercado.
CREATE TABLE IF NOT EXISTS public."Cotacao" (
    "Id" INTEGER PRIMARY KEY CHECK ("Id" = 1),
    "CotacaoBitcoin" NUMERIC(28, 8) NOT NULL CHECK ("CotacaoBitcoin" > 0),
    "CotacaoEthereum" NUMERIC(28, 8) NOT NULL CHECK ("CotacaoEthereum" > 0),
    "CotacaoRipple" NUMERIC(28, 8) NOT NULL CHECK ("CotacaoRipple" > 0),
    "AtualizadaEm" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO public."Cotacao" ("Id", "CotacaoBitcoin", "CotacaoEthereum", "CotacaoRipple")
VALUES (1, 392557.00, 13884.00, 2.91)
ON CONFLICT ("Id") DO NOTHING;

-- Se o banco antigo já tinha cotações na tabela Extrato, copia uma linha válida
-- para a nova tabela Cotacao apenas quando ela ainda contém os valores de exemplo.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'Extrato')
       AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'Extrato' AND column_name = 'CotacaoBitcoin')
       AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'Extrato' AND column_name = 'CotacaoEthereum')
       AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'Extrato' AND column_name = 'CotacaoRipple') THEN
        EXECUTE $migration$
            UPDATE public."Cotacao" c
            SET "CotacaoBitcoin" = legado.btc,
                "CotacaoEthereum" = legado.eth,
                "CotacaoRipple" = legado.xrp,
                "AtualizadaEm" = CURRENT_TIMESTAMP
            FROM (
                SELECT
                    NULLIF(BTRIM("CotacaoBitcoin"::TEXT), '')::NUMERIC AS btc,
                    NULLIF(BTRIM("CotacaoEthereum"::TEXT), '')::NUMERIC AS eth,
                    NULLIF(BTRIM("CotacaoRipple"::TEXT), '')::NUMERIC AS xrp
                FROM public."Extrato"
                WHERE BTRIM("CotacaoBitcoin"::TEXT) ~ '^[0-9]+([.][0-9]+)?$'
                  AND BTRIM("CotacaoEthereum"::TEXT) ~ '^[0-9]+([.][0-9]+)?$'
                  AND BTRIM("CotacaoRipple"::TEXT) ~ '^[0-9]+([.][0-9]+)?$'
                  AND NULLIF(BTRIM("CotacaoBitcoin"::TEXT), '')::NUMERIC > 0
                  AND NULLIF(BTRIM("CotacaoEthereum"::TEXT), '')::NUMERIC > 0
                  AND NULLIF(BTRIM("CotacaoRipple"::TEXT), '')::NUMERIC > 0
                LIMIT 1
            ) legado
            WHERE c."Id" = 1
              AND c."CotacaoBitcoin" = 392557.00
              AND c."CotacaoEthereum" = 13884.00
              AND c."CotacaoRipple" = 2.91
        $migration$;
    END IF;
END $$;

-- Histórico. Taxa é armazenada como fração (0.02 = 2%).
CREATE TABLE IF NOT EXISTS public."Transacao" (
    "Id" BIGSERIAL PRIMARY KEY,
    "CPF" VARCHAR(20) NOT NULL REFERENCES public."Cliente" ("CPF") ON DELETE CASCADE,
    "DataHora" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "Tipo" VARCHAR(20) NOT NULL CHECK ("Tipo" IN ('DEPOSITO', 'SAQUE', 'COMPRA', 'VENDA')),
    "Moeda" VARCHAR(8) NOT NULL,
    "Quantidade" NUMERIC(28, 8) NOT NULL DEFAULT 0,
    "Cotacao" NUMERIC(28, 8) NOT NULL DEFAULT 1,
    "ValorBruto" NUMERIC(28, 8) NOT NULL,
    "Taxa" NUMERIC(8, 6) NOT NULL DEFAULT 0,
    "ValorLiquido" NUMERIC(28, 8) NOT NULL
);

CREATE INDEX IF NOT EXISTS "Transacao_CPF_DataHora_idx"
    ON public."Transacao" ("CPF", "DataHora" DESC);
