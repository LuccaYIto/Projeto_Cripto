package controller;

import DAO.Conexao;
import DAO.OperacaoDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JOptionPane;
import model.Bitcoin;
import model.Cotacoes;
import model.Ethereum;
import model.ResultadoOperacao;
import model.Ripple;
import view.VenderCriptoFrame;

/** Controla a venda simulada, debitando cripto e creditando reais de forma atômica. */
public class ControllerVenderCripto {
    private final VenderCriptoFrame view;
    private final String cpfLogado;

    public ControllerVenderCripto(VenderCriptoFrame view, String cpfLogado) {
        this.view = view;
        this.cpfLogado = cpfLogado;
    }

    public void atualizarResumo() {
        String moeda = codigoSelecionado();
        try (Connection conn = new Conexao().getConnection()) {
            OperacaoDAO dao = new OperacaoDAO(conn);
            Cotacoes cotacoes = dao.buscarCotacoes();
            BigDecimal cotacao = cotacaoDaMoeda(cotacoes, moeda);
            BigDecimal saldo = dao.obterSaldo(cpfLogado, moeda);
            view.getCotacaoLabel().setText("Cotação atual: R$ " + cotacao.toPlainString());
            view.getSaldoLabel().setText("Saldo disponível: " + saldo.toPlainString() + " " + moeda);
        } catch (SQLException | IllegalArgumentException e) {
            view.getCotacaoLabel().setText("Cotação indisponível");
            view.getSaldoLabel().setText("Saldo indisponível");
            JOptionPane.showMessageDialog(view,
                    "Não foi possível carregar cotação e saldo. Confira o PostgreSQL e execute database/schema.sql.\n\n"
                    + e.getMessage(), "Erro de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void vender() {
        final BigDecimal quantidade;
        try {
            quantidade = new BigDecimal(view.getQuantidadeField().getText().trim().replace(',', '.'));
        } catch (NumberFormatException | NullPointerException e) {
            JOptionPane.showMessageDialog(view, "Informe uma quantidade válida.",
                    "Quantidade inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (quantidade.signum() <= 0) {
            JOptionPane.showMessageDialog(view, "A quantidade deve ser maior que zero.",
                    "Quantidade inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String moeda = codigoSelecionado();
        BigDecimal taxa = taxaVenda(moeda);
        int confirmar = JOptionPane.showConfirmDialog(view,
                "Confirma a venda de " + quantidade.toPlainString() + " " + moeda + "?\n"
                + "Taxa de venda: " + taxa.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%\n"
                + "O valor será calculado pela cotação cadastrada no momento da confirmação.",
                "Confirmar venda", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) return;

        try (Connection conn = new Conexao().getConnection()) {
            ResultadoOperacao resultado = new OperacaoDAO(conn).vender(cpfLogado, moeda, quantidade, taxa);
            NumberFormat br = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
            JOptionPane.showMessageDialog(view,
                    "Venda realizada com sucesso!\nQuantidade vendida: " + resultado.getQuantidade().toPlainString()
                    + " " + moeda + "\nCotação utilizada: " + br.format(resultado.getCotacao())
                    + "\nValor bruto: " + br.format(resultado.getValorBruto())
                    + "\nValor creditado após a taxa: " + br.format(resultado.getValorLiquido()),
                    "Venda concluída", JOptionPane.INFORMATION_MESSAGE);
            view.getQuantidadeField().setText("");
            atualizarResumo();
        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view,
                    "Não foi possível concluir a venda. Nenhum saldo foi alterado.\n\n" + e.getMessage(),
                    "Venda não realizada", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String codigoSelecionado() {
        Object selecionado = view.getMoedaCombo().getSelectedItem();
        String texto = selecionado == null ? "BTC" : selecionado.toString();
        if (texto.contains("ETH")) return "ETH";
        if (texto.contains("XRP")) return "XRP";
        return "BTC";
    }

    private BigDecimal taxaVenda(String moeda) {
        switch (moeda) {
            case "BTC": return BigDecimal.valueOf(new Bitcoin().getTaxaVenda());
            case "ETH": return BigDecimal.valueOf(new Ethereum().getTaxaVenda());
            case "XRP": return BigDecimal.valueOf(new Ripple().getTaxaVenda());
            default: throw new IllegalArgumentException("Moeda não suportada.");
        }
    }

    private BigDecimal cotacaoDaMoeda(Cotacoes cotacoes, String moeda) {
        switch (moeda) {
            case "BTC": return cotacoes.getBitcoin();
            case "ETH": return cotacoes.getEthereum();
            case "XRP": return cotacoes.getRipple();
            default: throw new IllegalArgumentException("Moeda não suportada.");
        }
    }
}
