package DAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Cotacoes;
import model.ResultadoOperacao;
import model.Transacao;

/**
 * Persistência das operações que afetam saldos, cotações e histórico.
 * Cada movimentação financeira e seu registro no extrato são gravados na mesma transação SQL.
 */
public class OperacaoDAO {
    private static final int ESCALA_MOEDA = 2;
    private static final int ESCALA_CRIPTO = 8;
    private final Connection conn;

    public OperacaoDAO(Connection conn) {
        this.conn = conn;
    }

    public Cotacoes buscarCotacoes() throws SQLException {
        String sql = "SELECT \"CotacaoBitcoin\", \"CotacaoEthereum\", \"CotacaoRipple\" "
                + "FROM public.\"Cotacao\" WHERE \"Id\" = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Nenhuma cotação cadastrada. Execute database/schema.sql.");
            }
            return new Cotacoes(rs.getBigDecimal("CotacaoBitcoin"),
                    rs.getBigDecimal("CotacaoEthereum"), rs.getBigDecimal("CotacaoRipple"));
        }
    }

    public void atualizarCotacoes(Cotacoes cotacoes) throws SQLException {
        String sql = "INSERT INTO public.\"Cotacao\" (\"Id\", \"CotacaoBitcoin\", \"CotacaoEthereum\", \"CotacaoRipple\", \"AtualizadaEm\") "
                + "VALUES (1, ?, ?, ?, CURRENT_TIMESTAMP) "
                + "ON CONFLICT (\"Id\") DO UPDATE SET \"CotacaoBitcoin\" = EXCLUDED.\"CotacaoBitcoin\", "
                + "\"CotacaoEthereum\" = EXCLUDED.\"CotacaoEthereum\", \"CotacaoRipple\" = EXCLUDED.\"CotacaoRipple\", "
                + "\"AtualizadaEm\" = CURRENT_TIMESTAMP";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, cotacoes.getBitcoin());
            ps.setBigDecimal(2, cotacoes.getEthereum());
            ps.setBigDecimal(3, cotacoes.getRipple());
            ps.executeUpdate();
        }
    }

    public BigDecimal obterSaldo(String cpf, String moeda) throws SQLException {
        String coluna = colunaSaldo(moeda);
        String sql = "SELECT \"" + coluna + "\" FROM public.\"Cliente\" WHERE \"CPF\" = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Cliente não encontrado.");
                }
                return rs.getBigDecimal(1);
            }
        }
    }

    public ResultadoOperacao comprar(String cpf, String moeda, BigDecimal valorReais, BigDecimal taxa)
            throws SQLException {
        validarValorPositivo(valorReais, "O valor da compra");
        validarTaxa(taxa);
        String codigo = normalizarMoeda(moeda);
        boolean autoCommitAnterior = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            BigDecimal cotacao = buscarCotacaoParaOperacao(codigo);
            BigDecimal valorBruto = valorReais.setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);
            if (valorBruto.signum() <= 0) {
                throw new IllegalArgumentException("O valor da compra deve ser de pelo menos R$ 0,01.");
            }
            BigDecimal valorLiquido = valorBruto.multiply(BigDecimal.ONE.subtract(taxa))
                    .setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);
            BigDecimal quantidade = valorLiquido.divide(cotacao, ESCALA_CRIPTO, RoundingMode.HALF_UP);
            if (quantidade.signum() <= 0) {
                throw new IllegalArgumentException("O valor informado é muito pequeno para comprar essa criptomoeda.");
            }

            String coluna = colunaSaldo(codigo);
            String sql = "UPDATE public.\"Cliente\" SET \"SaldoReais\" = \"SaldoReais\" - ?, \""
                    + coluna + "\" = \"" + coluna + "\" + ? WHERE \"CPF\" = ? AND \"SaldoReais\" >= ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, valorBruto);
                ps.setBigDecimal(2, quantidade);
                ps.setString(3, cpf);
                ps.setBigDecimal(4, valorBruto);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Compra não realizada: cliente inexistente ou saldo em reais insuficiente.");
                }
            }
            registrarTransacao(cpf, "COMPRA", codigo, quantidade, cotacao, valorBruto, taxa, valorLiquido);
            conn.commit();
            return new ResultadoOperacao(nomeMoeda(codigo), quantidade, cotacao, valorBruto, taxa, valorLiquido);
        } catch (SQLException | RuntimeException e) {
            rollbackSilencioso();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitAnterior);
        }
    }

    public ResultadoOperacao vender(String cpf, String moeda, BigDecimal quantidadeVendida, BigDecimal taxa)
            throws SQLException {
        validarValorPositivo(quantidadeVendida, "A quantidade a vender");
        validarTaxa(taxa);
        String codigo = normalizarMoeda(moeda);
        BigDecimal quantidade = quantidadeVendida.setScale(ESCALA_CRIPTO, RoundingMode.HALF_UP);
        if (quantidade.signum() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ter pelo menos oito casas decimais de precisão positiva.");
        }
        boolean autoCommitAnterior = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            BigDecimal cotacao = buscarCotacaoParaOperacao(codigo);
            BigDecimal valorBruto = quantidade.multiply(cotacao).setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);
            BigDecimal valorLiquido = valorBruto.multiply(BigDecimal.ONE.subtract(taxa))
                    .setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);
            if (valorLiquido.signum() <= 0) {
                throw new IllegalArgumentException("A venda resulta em um valor líquido igual a zero.");
            }

            String coluna = colunaSaldo(codigo);
            String sql = "UPDATE public.\"Cliente\" SET \"" + coluna + "\" = \"" + coluna
                    + "\" - ?, \"SaldoReais\" = \"SaldoReais\" + ? "
                    + "WHERE \"CPF\" = ? AND \"" + coluna + "\" >= ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, quantidade);
                ps.setBigDecimal(2, valorLiquido);
                ps.setString(3, cpf);
                ps.setBigDecimal(4, quantidade);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Venda não realizada: cliente inexistente ou saldo de criptomoeda insuficiente.");
                }
            }
            registrarTransacao(cpf, "VENDA", codigo, quantidade, cotacao, valorBruto, taxa, valorLiquido);
            conn.commit();
            return new ResultadoOperacao(nomeMoeda(codigo), quantidade, cotacao, valorBruto, taxa, valorLiquido);
        } catch (SQLException | RuntimeException e) {
            rollbackSilencioso();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitAnterior);
        }
    }

    public boolean depositar(String cpf, BigDecimal valor) throws SQLException {
        validarValorPositivo(valor, "O depósito");
        BigDecimal valorFinal = valor.setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);
        if (valorFinal.signum() <= 0) throw new IllegalArgumentException("O depósito mínimo é de R$ 0,01.");
        boolean autoCommitAnterior = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            String sql = "UPDATE public.\"Cliente\" SET \"SaldoReais\" = \"SaldoReais\" + ? WHERE \"CPF\" = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, valorFinal);
                ps.setString(2, cpf);
                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }
            registrarTransacao(cpf, "DEPOSITO", "BRL", BigDecimal.ZERO, BigDecimal.ONE,
                    valorFinal, BigDecimal.ZERO, valorFinal);
            conn.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            rollbackSilencioso();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitAnterior);
        }
    }

    public boolean sacar(String cpf, BigDecimal valor) throws SQLException {
        validarValorPositivo(valor, "O saque");
        BigDecimal valorFinal = valor.setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);
        if (valorFinal.signum() <= 0) throw new IllegalArgumentException("O saque mínimo é de R$ 0,01.");
        boolean autoCommitAnterior = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            String sql = "UPDATE public.\"Cliente\" SET \"SaldoReais\" = \"SaldoReais\" - ? "
                    + "WHERE \"CPF\" = ? AND \"SaldoReais\" >= ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, valorFinal);
                ps.setString(2, cpf);
                ps.setBigDecimal(3, valorFinal);
                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }
            registrarTransacao(cpf, "SAQUE", "BRL", BigDecimal.ZERO, BigDecimal.ONE,
                    valorFinal, BigDecimal.ZERO, valorFinal);
            conn.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            rollbackSilencioso();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitAnterior);
        }
    }

    public List<Transacao> listarTransacoes(String cpf) throws SQLException {
        String sql = "SELECT \"DataHora\", \"Tipo\", \"Moeda\", \"Quantidade\", \"Cotacao\", "
                + "\"ValorBruto\", \"Taxa\", \"ValorLiquido\" FROM public.\"Transacao\" "
                + "WHERE \"CPF\" = ? ORDER BY \"DataHora\" DESC, \"Id\" DESC LIMIT 500";
        List<Transacao> transacoes = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("DataHora");
                    transacoes.add(new Transacao(timestamp == null ? null : timestamp.toLocalDateTime(),
                            rs.getString("Tipo"), rs.getString("Moeda"), rs.getBigDecimal("Quantidade"),
                            rs.getBigDecimal("Cotacao"), rs.getBigDecimal("ValorBruto"),
                            rs.getBigDecimal("Taxa"), rs.getBigDecimal("ValorLiquido")));
                }
            }
        }
        return transacoes;
    }

    private BigDecimal buscarCotacaoParaOperacao(String codigo) throws SQLException {
        String coluna = colunaCotacao(codigo);
        String sql = "SELECT \"" + coluna + "\" FROM public.\"Cotacao\" WHERE \"Id\" = 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Não há cotações cadastradas. Abra Atualizar Cotação ou execute database/schema.sql.");
            }
            BigDecimal cotacao = rs.getBigDecimal(1);
            if (cotacao == null || cotacao.signum() <= 0) {
                throw new SQLException("A cotação disponível é inválida.");
            }
            return cotacao;
        }
    }

    private void registrarTransacao(String cpf, String tipo, String moeda, BigDecimal quantidade,
            BigDecimal cotacao, BigDecimal valorBruto, BigDecimal taxa, BigDecimal valorLiquido) throws SQLException {
        String sql = "INSERT INTO public.\"Transacao\" (\"CPF\", \"Tipo\", \"Moeda\", \"Quantidade\", "
                + "\"Cotacao\", \"ValorBruto\", \"Taxa\", \"ValorLiquido\") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            ps.setString(2, tipo);
            ps.setString(3, moeda);
            ps.setBigDecimal(4, quantidade);
            ps.setBigDecimal(5, cotacao);
            ps.setBigDecimal(6, valorBruto);
            ps.setBigDecimal(7, taxa);
            ps.setBigDecimal(8, valorLiquido);
            ps.executeUpdate();
        }
    }

    private void rollbackSilencioso() {
        try { conn.rollback(); } catch (SQLException ignored) { /* preserva a exceção original */ }
    }

    private static void validarValorPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero.");
        }
    }

    private static void validarTaxa(BigDecimal taxa) {
        if (taxa == null || taxa.signum() < 0 || taxa.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("A taxa deve estar entre 0 (inclusive) e 1 (exclusive).");
        }
    }

    private static String normalizarMoeda(String moeda) {
        if (moeda == null) throw new IllegalArgumentException("Informe uma criptomoeda.");
        String valor = moeda.trim().toUpperCase(java.util.Locale.ROOT);
        if (valor.equals("BTC") || valor.contains("BITCOIN")) return "BTC";
        if (valor.equals("ETH") || valor.contains("ETHEREUM")) return "ETH";
        if (valor.equals("XRP") || valor.contains("RIPPLE")) return "XRP";
        throw new IllegalArgumentException("Criptomoeda não suportada: " + moeda);
    }

    private static String nomeMoeda(String codigo) {
        switch (codigo) {
            case "BTC": return "Bitcoin (BTC)";
            case "ETH": return "Ethereum (ETH)";
            case "XRP": return "Ripple (XRP)";
            default: return codigo;
        }
    }

    private static String colunaSaldo(String moeda) {
        switch (normalizarMoeda(moeda)) {
            case "BTC": return "SaldoBitcoin";
            case "ETH": return "SaldoEthereum";
            case "XRP": return "SaldoRipple";
            default: throw new IllegalArgumentException("Saldo de moeda inválido.");
        }
    }

    private static String colunaCotacao(String moeda) {
        switch (normalizarMoeda(moeda)) {
            case "BTC": return "CotacaoBitcoin";
            case "ETH": return "CotacaoEthereum";
            case "XRP": return "CotacaoRipple";
            default: throw new IllegalArgumentException("Cotação de moeda inválida.");
        }
    }
}
