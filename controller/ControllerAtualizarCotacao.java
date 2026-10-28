package controller;

import DAO.Conexao;
import DAO.OperacaoDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import model.Cotacoes;
import view.AtualizarCotacaoFrame;

/** Controla a atualização manual das cotações de demonstração. */
public class ControllerAtualizarCotacao {
    private final AtualizarCotacaoFrame view;

    public ControllerAtualizarCotacao(AtualizarCotacaoFrame view) {
        this.view = view;
    }

    public void carregarCotacoes() {
        try (Connection conn = new Conexao().getConnection()) {
            Cotacoes c = new OperacaoDAO(conn).buscarCotacoes();
            view.getBitcoinField().setText(c.getBitcoin().toPlainString());
            view.getEthereumField().setText(c.getEthereum().toPlainString());
            view.getRippleField().setText(c.getRipple().toPlainString());
            view.getStatusLabel().setText("Últimas cotações carregadas do banco de dados.");
        } catch (SQLException | IllegalArgumentException e) {
            view.getStatusLabel().setText("Não foi possível carregar as cotações.");
            JOptionPane.showMessageDialog(view,
                    "Não foi possível carregar as cotações. Execute database/schema.sql e confira a conexão.\n\n"
                    + e.getMessage(), "Erro de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void salvarCotacoes() {
        final Cotacoes cotacoes;
        try {
            cotacoes = new Cotacoes(
                    new BigDecimal(view.getBitcoinField().getText().trim().replace(',', '.')),
                    new BigDecimal(view.getEthereumField().getText().trim().replace(',', '.')),
                    new BigDecimal(view.getRippleField().getText().trim().replace(',', '.')));
        } catch (IllegalArgumentException | NullPointerException e) {
            JOptionPane.showMessageDialog(view,
                    "Informe valores numéricos maiores que zero para as três cotações.",
                    "Cotações inválidas", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(view,
                "Esses valores serão usados por todos os usuários deste simulador.\nDeseja salvar as cotações?",
                "Confirmar atualização", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacao != JOptionPane.YES_OPTION) return;

        try (Connection conn = new Conexao().getConnection()) {
            new OperacaoDAO(conn).atualizarCotacoes(cotacoes);
            view.getStatusLabel().setText("Cotações atualizadas com sucesso.");
            JOptionPane.showMessageDialog(view, "As novas cotações foram salvas.",
                    "Atualização concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view,
                    "Não foi possível salvar as cotações. Verifique o banco de dados.\n\n" + e.getMessage(),
                    "Erro ao atualizar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
