package controller;

import DAO.ClienteDAO;
import DAO.Conexao;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import model.Cliente;
import view.ConsultarSaldoFrame;

public class ControllerConsultarSaldo {

    private final ConsultarSaldoFrame view;

    public ControllerConsultarSaldo(ConsultarSaldoFrame view) {
        this.view = view;
    }

    public void printarDados() {
        String cpf = view.getCPFConsultarSaldo().getText().trim();
        String senha = view.getSenhaConsultarSaldo().getText();
        if (cpf.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Informe o CPF e a senha.",
                    "Dados incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cliente cliente = new Cliente(null, cpf, senha);
        try (Connection conn = new Conexao().getConnection()) {
            ClienteDAO dao = new ClienteDAO(conn);
            try (ResultSet res = dao.consultarCliente(cliente)) {
                if (res.next()) {
                    view.getNome().setText(res.getString("Nome"));
                    view.getCPF().setText(res.getString("CPF"));
                    view.getSaldoreais().setText(res.getString("SaldoReais"));
                    view.getSaldoBitcoin().setText(res.getString("SaldoBitcoin"));
                    view.getSaldoEthereum().setText(res.getString("SaldoEthereum"));
                    view.getSaldoRipple().setText(res.getString("SaldoRipple"));
                } else {
                    JOptionPane.showMessageDialog(view, "CPF ou senha inválidos.",
                            "Consulta não realizada", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Não foi possível consultar o saldo. Confira a conexão com o banco.",
                    "Erro de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }
}
