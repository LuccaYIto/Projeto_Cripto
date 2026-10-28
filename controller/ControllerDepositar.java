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
import view.DepositarFrame;

public class ControllerDepositar {

    private final DepositarFrame view;

    public ControllerDepositar(DepositarFrame view) {
        this.view = view;
    }

    public boolean atualiza() {
        final double valor;
        try {
            valor = lerValor(view.getValorDepositar().getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Informe um valor numérico válido.",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (!Double.isFinite(valor) || valor <= 0) {
            JOptionPane.showMessageDialog(view, "O valor do depósito deve ser maior que zero.",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Cliente cliente = new Cliente(null, view.getCPFDepositar().getText().trim(), "", 0, 0, 0, 0);
        try (Connection conn = new Conexao().getConnection()) {
            boolean realizado = new OperacaoDAO(conn).depositar(cliente.getCPF(), BigDecimal.valueOf(valor));
            if (!realizado) {
                JOptionPane.showMessageDialog(view, "Cliente não encontrado. O saldo não foi alterado.",
                        "Depósito não realizado", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            JOptionPane.showMessageDialog(view, "Depósito registrado com sucesso no simulador e no extrato.",
                    "Depósito", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, "Não foi possível realizar o depósito. Verifique a conexão com o banco.",
                    "Erro no depósito", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void printaDados() {
        Cliente cliente = new Cliente(null, view.getCPFDepositar().getText().trim(), "", 0, 0, 0, 0);
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
