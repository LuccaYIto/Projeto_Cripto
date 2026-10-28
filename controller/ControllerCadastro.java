package controller;

import DAO.ClienteDAO;
import DAO.Conexao;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import model.Cliente;
import view.CadastroFrame;

public class ControllerCadastro {

    private final CadastroFrame view;

    public ControllerCadastro(CadastroFrame view) {
        this.view = view;
    }

    public void salvarCliente() {
        String nome = view.getNomeCadastro().getText().trim();
        String cpf = view.getCPFCadastro().getText().trim();
        String senha = view.getSenhaCadastro().getText();
        String confirmaSenha = view.getConfirmaSenhaCadastro().getText();

        if (nome.isEmpty() || cpf.isEmpty() || senha.isEmpty() || confirmaSenha.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Preencha todos os campos.",
                    "Dados incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!senha.equals(confirmaSenha)) {
            JOptionPane.showMessageDialog(view, "As senhas informadas não coincidem.",
                    "Verifique a senha", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cliente cliente = new Cliente(nome, cpf, senha);
        Conexao conexao = new Conexao();

        try (Connection conn = conexao.getConnection()) {
            ClienteDAO dao = new ClienteDAO(conn);
            dao.inserir(cliente);
            JOptionPane.showMessageDialog(view, "Cadastro realizado com sucesso.",
                    "Cadastro", JOptionPane.INFORMATION_MESSAGE);
            view.dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view,
                    "Não foi possível cadastrar o cliente. Verifique a conexão com o banco "
                    + "e se o CPF já está cadastrado.",
                    "Falha no cadastro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
