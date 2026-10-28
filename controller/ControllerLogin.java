package controller;

import DAO.ClienteDAO;
import DAO.Conexao;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import model.Cliente;
import view.LoginFrame;

public class ControllerLogin {

    private final LoginFrame view;
    private String cpf_logado;

    public ControllerLogin(LoginFrame view) {
        this.view = view;
    }

    public String getCpf_logado() {
        return cpf_logado;
    }

    public void setCpf_logado(String cpf_logado) {
        this.cpf_logado = cpf_logado;
    }

    /** Retorna o CPF em caso de sucesso e "0" em caso de falha, conforme o fluxo legado da interface. */
    public String loginCliente() {
        String cpf = view.getCPFLogin().getText().trim();
        String senha = view.getSenhaLogin().getText();
        if (cpf.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Informe o CPF e a senha.",
                    "Dados incompletos", JOptionPane.WARNING_MESSAGE);
            return "0";
        }

        Cliente cliente = new Cliente(null, cpf, senha);
        try (Connection conn = new Conexao().getConnection()) {
            ClienteDAO dao = new ClienteDAO(conn);
            try (ResultSet res = dao.consultarCliente(cliente)) {
                if (res.next()) {
                    cpf_logado = res.getString("CPF");
                    JOptionPane.showMessageDialog(view, "Login realizado.",
                            "Login", JOptionPane.INFORMATION_MESSAGE);
                    return cpf_logado;
                }
            }
            JOptionPane.showMessageDialog(view, "CPF ou senha inválidos.",
                    "Falha no login", JOptionPane.ERROR_MESSAGE);
            return "0";
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view,
                    "Não foi possível consultar o banco de dados. Confira a configuração de conexão.",
                    "Erro de conexão", JOptionPane.ERROR_MESSAGE);
            return "0";
        }
    }
}
