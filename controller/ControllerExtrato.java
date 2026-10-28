package controller;

import DAO.Conexao;
import DAO.OperacaoDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Transacao;
import view.ConsultarExtratoFrame;

/** Carrega o histórico de movimentações do cliente autenticado. */
public class ControllerExtrato {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final ConsultarExtratoFrame view;
    private final String cpfLogado;

    public ControllerExtrato(ConsultarExtratoFrame view, String cpfLogado) {
        this.view = view;
        this.cpfLogado = cpfLogado;
    }

    public void carregar() {
        DefaultTableModel modelo = view.getTableModel();
        modelo.setRowCount(0);
        try (Connection conn = new Conexao().getConnection()) {
            List<Transacao> transacoes = new OperacaoDAO(conn).listarTransacoes(cpfLogado);
            for (Transacao t : transacoes) {
                modelo.addRow(new Object[]{
                    t.getDataHora() == null ? "—" : FORMATO_DATA.format(t.getDataHora()),
                    t.getTipo(), t.getMoeda(), t.getQuantidade(), t.getCotacao(),
                    t.getValorBruto(), t.getTaxa().multiply(java.math.BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%",
                    t.getValorLiquido()
                });
            }
            view.getStatusLabel().setText(transacoes.size() + " movimentação(ões) encontrada(s). Exibindo no máximo 500 registros.");
        } catch (SQLException e) {
            view.getStatusLabel().setText("Erro ao carregar o extrato.");
            JOptionPane.showMessageDialog(view,
                    "Não foi possível carregar o extrato. Execute database/schema.sql e confira a conexão.\n\n"
                    + e.getMessage(), "Erro de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }
}
