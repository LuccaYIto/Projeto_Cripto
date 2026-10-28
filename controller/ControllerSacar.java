package controller;

import DAO.ClienteDAO;
import DAO.OperacaoDAO;
import DAO.Conexao;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import javax.swing.JOptionPane;
import model.Cliente;
import view.SacarFrame;

public class ControllerSacar {

    private final SacarFrame view;

    public ControllerSacar(SacarFrame view) {
        this.view = view;
    }

    public boolean atualiza() {
        final double valor;
        try {
            valor = lerValor(view.getValorSacar().getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Informe um valor numérico válido.",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (!Double.isFinite(valor) || valor <= 0) {
            JOptionPane.showMessageDialog(view, "O valor do saque deve ser maior que zero.",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Cliente cliente = new Cliente(null, view.getCPFSacar().getText().trim(), "", 0, 0, 0, 0);
        try (Connection conn = new Conexao().getConnection()) {
            boolean realizado = new OperacaoDAO(conn).sacar(cliente.getCPF(), BigDecimal.valueOf(valor));
            if (!realizado) {
                JOptionPane.showMessageDialog(view,
                        "Saque não realizado. Confira o cliente e se há saldo suficiente.",
                        "Saque não realizado", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            JOptionPane.showMessageDialog(view, "Saque registrado com sucesso no simulador e no extrato.",
                    "Saque", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, "Não foi possível realizar o saque. Verifique a conexão com o banco.",
                    "Erro no saque", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void printaDados() {
        Cliente cliente = new Cliente(null, view.getCPFSacar().getText().trim(), "", 0, 0, 0, 0);
        try (Connection conn = new Conexao().getConnection()) {
            ClienteDAO dao = new ClienteDAO(conn);
            try (ResultSet res = dao.consultarSacarDepositar(cliente)) {
                if (res.next()) {
                    view.getPrintReal().setText(res.getString("SaldoReais"));
                } else {
                    view.getPrintReal().setText("N/D");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Não foi possível consultar o saldo.",
                    "Erro de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double lerValor(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }
}
