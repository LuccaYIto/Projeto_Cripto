package controller;

import view.ConfirmaCompraFrame;
import DAO.ClienteDAO;
import DAO.Conexao;
import model.Cliente;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ControllerConfirmaCompra {

    private final ConfirmaCompraFrame view;
    private final String cpf_logado;
    private String senha_logada;

    public String getSenha_logada() {
        return senha_logada;
    }

    public void setSenha_logada(String senha_logada) {
        this.senha_logada = senha_logada;
    }

    public ControllerConfirmaCompra(ConfirmaCompraFrame view, String cpfLogado) {
        this.view = view;
        this.cpf_logado = cpfLogado;
    }

    /** Retorna a senha informada após validação ou "0" quando a autenticação falha. */
    public String loginCliente() {
        String senhaInformada = view.getSenhaComprar().getText();
        if (senhaInformada == null || senhaInformada.isBlank()) {
            JOptionPane.showMessageDialog(view, "Informe sua senha.",
                    "Dados incompletos", JOptionPane.WARNING_MESSAGE);
            return "0";
        }

        Cliente cliente = new Cliente(null, cpf_logado, senhaInformada, 0, 0, 0, 0);
        try (Connection conn = new Conexao().getConnection()) {
            ClienteDAO dao = new ClienteDAO(conn);
            try (ResultSet res = dao.consultarCliente(cliente)) {
                if (res.next()) {
                    senha_logada = senhaInformada;
                    JOptionPane.showMessageDialog(view, "Senha confirmada.",
                            "Confirmação", JOptionPane.INFORMATION_MESSAGE);
                    return senha_logada;
                }
            }
            JOptionPane.showMessageDialog(view, "Senha incorreta.",
                    "Falha na confirmação", JOptionPane.ERROR_MESSAGE);
            return "0";
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Não foi possível consultar o banco de dados.",
                    "Erro de conexão", JOptionPane.ERROR_MESSAGE);
            return "0";
        }
    }
}
