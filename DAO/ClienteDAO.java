package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Cliente;
import model.Extrato;

/** Operações de persistência relacionadas a clientes e cotações. */
public class ClienteDAO {

    private final Connection conn;

    public ClienteDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Consulta o cliente pelas credenciais informadas.
     * O chamador deve fechar o ResultSet e a conexão.
     */
    public ResultSet consultarCliente(Cliente cliente) throws SQLException {
        String sql = "SELECT * FROM public.\"Cliente\" WHERE \"CPF\" = ? AND \"Senha\" = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, cliente.getCPF());
        statement.setString(2, cliente.getSenha());
        return statement.executeQuery();
    }

    /**
     * Consulta os dados de um cliente pelo CPF.
     * O chamador deve fechar o ResultSet e a conexão.
     */
    public ResultSet consultarSacarDepositar(Cliente cliente) throws SQLException {
        String sql = "SELECT * FROM public.\"Cliente\" WHERE \"CPF\" = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, cliente.getCPF());
        return statement.executeQuery();
    }

    /** Insere um cliente com saldos iniciais zerados. */
    public void inserir(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO public.\"Cliente\" "
                + "(\"Nome\", \"CPF\", \"Senha\", \"SaldoReais\", \"SaldoBitcoin\", \"SaldoEthereum\", \"SaldoRipple\") "
                + "VALUES (?, ?, ?, 0, 0, 0, 0)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, cliente.getNome());
            statement.setString(2, cliente.getCPF());
            statement.setString(3, cliente.getSenha());
            statement.executeUpdate();
        }
    }

    public void atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE public.\"Cliente\" SET \"Senha\" = ? WHERE \"CPF\" = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, cliente.getSenha());
            statement.setString(2, cliente.getCPF());
            statement.executeUpdate();
        }
    }

    /** Retorna o número de linhas alteradas (zero indica CPF inexistente ou valor inválido). */
    public int atualizarSaldoDepositar(Cliente cliente, double valor) throws SQLException {
        if (!Double.isFinite(valor) || valor <= 0) {
            return 0;
        }
        String sql = "UPDATE public.\"Cliente\" SET \"SaldoReais\" = \"SaldoReais\" + ? WHERE \"CPF\" = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setDouble(1, valor);
            statement.setString(2, cliente.getCPF());
            return statement.executeUpdate();
        }
    }

    /** Impede saque com valor não positivo ou superior ao saldo disponível. */
    public int atualizarSaldoSacar(Cliente cliente, double valor) throws SQLException {
        if (!Double.isFinite(valor) || valor <= 0) {
            return 0;
        }
        String sql = "UPDATE public.\"Cliente\" SET \"SaldoReais\" = \"SaldoReais\" - ? "
                + "WHERE \"CPF\" = ? AND \"SaldoReais\" >= ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setDouble(1, valor);
            statement.setString(2, cliente.getCPF());
            statement.setDouble(3, valor);
            return statement.executeUpdate();
        }
    }

    public void deletar(Cliente cliente) throws SQLException {
        String sql = "DELETE FROM public.\"Cliente\" WHERE \"CPF\" = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, cliente.getCPF());
            statement.executeUpdate();
        }
    }

    /**
     * Consulta o registro de cotações associado ao usuário.
     * O chamador deve fechar o ResultSet e a conexão.
     */
    public ResultSet consultarExtrato(Extrato extrato) throws SQLException {
        String sql = "SELECT * FROM public.\"Extrato\" WHERE \"CPF\" = ? AND \"Senha\" = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, extrato.getCPF());
        statement.setString(2, extrato.getSenha());
        return statement.executeQuery();
    }

    /**
     * Debita os saldos informados de forma atômica.
     * Os valores representam quantidades a debitar e devem ser não negativos.
     */
    public int atualizarSaldoCripto(Cliente cliente, double saldoReais, double saldoBitcoin,
            double saldoEthereum, double saldoRipple) throws SQLException {
        if (!valorValido(saldoReais) || !valorValido(saldoBitcoin)
                || !valorValido(saldoEthereum) || !valorValido(saldoRipple)) {
            throw new IllegalArgumentException("Os valores a debitar devem ser finitos e não negativos.");
        }

        String sql = "UPDATE public.\"Cliente\" SET "
                + "\"SaldoReais\" = \"SaldoReais\" - ?, "
                + "\"SaldoBitcoin\" = \"SaldoBitcoin\" - ?, "
                + "\"SaldoEthereum\" = \"SaldoEthereum\" - ?, "
                + "\"SaldoRipple\" = \"SaldoRipple\" - ? "
                + "WHERE \"CPF\" = ? "
                + "AND \"SaldoReais\" >= ? AND \"SaldoBitcoin\" >= ? "
                + "AND \"SaldoEthereum\" >= ? AND \"SaldoRipple\" >= ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setDouble(1, saldoReais);
            statement.setDouble(2, saldoBitcoin);
            statement.setDouble(3, saldoEthereum);
            statement.setDouble(4, saldoRipple);
            statement.setString(5, cliente.getCPF());
            statement.setDouble(6, saldoReais);
            statement.setDouble(7, saldoBitcoin);
            statement.setDouble(8, saldoEthereum);
            statement.setDouble(9, saldoRipple);
            return statement.executeUpdate();
        }
    }

    private boolean valorValido(double valor) {
        return Double.isFinite(valor) && valor >= 0;
    }
}
