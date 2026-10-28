package controller;

import DAO.Conexao;
import DAO.OperacaoDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JOptionPane;
import model.Bitcoin;
import model.Cotacoes;
import model.Ethereum;
import model.ResultadoOperacao;
import model.Ripple;
import view.ComprarCriptoFrame;

/** Controlador de consulta de cotações, saldos e compra simulada persistida. */
public class ControllerComprarCripto {
    private final ComprarCriptoFrame view;
    private final String cpfLogado;
    @SuppressWarnings("unused")
    private final String senhaLogada; // Mantido na assinatura por compatibilidade com a navegação existente.

    public ControllerComprarCripto(ComprarCriptoFrame view, String cpfLogado, String senhaLogada) {
        this.view = view;
        this.cpfLogado = cpfLogado;
        this.senhaLogada = senhaLogada;
    }

    public void mostrarCotacoes() {
        try (Connection conn = new Conexao().getConnection()) {
            Cotacoes c = new OperacaoDAO(conn).buscarCotacoes();
            view.getCotacaoBitcoin().setText(formatar(c.getBitcoin()));
            view.getCotacaoEthereum().setText(formatar(c.getEthereum()));
            view.getCotacaoRipple().setText(formatar(c.getRipple()));
        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view,
                    "Não foi possível carregar as cotações. Execute database/schema.sql e confira a conexão com o banco.\n\n"
                    + e.getMessage(), "Erro de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mostrarSaldoReais() {
        String sql = "SELECT \"SaldoReais\", \"SaldoBitcoin\", \"SaldoEthereum\", \"SaldoRipple\" "
                + "FROM public.\"Cliente\" WHERE \"CPF\" = ?";
        try (Connection conn = new Conexao().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpfLogado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    view.getSaldoReais().setText("R$ " + formatar(rs.getBigDecimal("SaldoReais")));
                    view.getSaldoBitcoin().setText(formatar(rs.getBigDecimal("SaldoBitcoin")) + " BTC");
                    view.getSaldoEthereum().setText(formatar(rs.getBigDecimal("SaldoEthereum")) + " ETH");
                    view.getSaldoRipple().setText(formatar(rs.getBigDecimal("SaldoRipple")) + " XRP");
                } else {
                    JOptionPane.showMessageDialog(view, "Não foi possível localizar o saldo do usuário.",
                            "Saldo indisponível", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Não foi possível consultar o saldo.\n" + e.getMessage(),
                    "Erro de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void compraBitcoin() {
        realizarCompra("BTC", "Bitcoin", BigDecimal.valueOf(new Bitcoin().getTaxaCompra()));
    }

    public void compraEthereum() {
        realizarCompra("ETH", "Ethereum", BigDecimal.valueOf(new Ethereum().getTaxaCompra()));
    }

    public void compraRipple() {
        realizarCompra("XRP", "Ripple", BigDecimal.valueOf(new Ripple().getTaxaCompra()));
    }

    private void realizarCompra(String moeda, String nome, BigDecimal taxa) {
        final BigDecimal valor;
        try {
            valor = new BigDecimal(view.getAplicarReais().getText().trim().replace(',', '.'));
        } catch (NumberFormatException | NullPointerException e) {
            JOptionPane.showMessageDialog(view, "Informe um valor em reais válido.",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (valor.signum() <= 0) {
            JOptionPane.showMessageDialog(view, "O valor aplicado deve ser maior que zero.",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        NumberFormat br = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        int confirmar = JOptionPane.showConfirmDialog(view,
                "Confirmar compra de " + nome + "?\nValor debitado da carteira: " + br.format(valor)
                + "\nTaxa de compra: " + taxa.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()
                + "%\nA operação será gravada no banco e no extrato.",
                "Confirmar compra", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) return;

        try (Connection conn = new Conexao().getConnection()) {
            ResultadoOperacao resultado = new OperacaoDAO(conn).comprar(cpfLogado, moeda, valor, taxa);
            JOptionPane.showMessageDialog(view,
                    "Compra realizada com sucesso!\nCriptomoeda: " + nome
                    + "\nQuantidade adquirida: " + resultado.getQuantidade().toPlainString()
                    + "\nCotação utilizada: " + br.format(resultado.getCotacao())
                    + "\nValor debitado: " + br.format(resultado.getValorBruto())
                    + "\nValor convertido após a taxa: " + br.format(resultado.getValorLiquido()),
                    "Compra concluída", JOptionPane.INFORMATION_MESSAGE);
            mostrarSaldoReais();
        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, "Não foi possível concluir a compra. Nenhum saldo foi alterado.\n\n"
                    + e.getMessage(), "Compra não realizada", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatar(BigDecimal valor) {
        if (valor == null) return "0";
        return valor.stripTrailingZeros().toPlainString();
    }
}
