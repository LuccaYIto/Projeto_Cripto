package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Centraliza a configuração de acesso ao PostgreSQL. */
public class Conexao {

    private static final String URL_PADRAO = "jdbc:postgresql://localhost:5432/BancoCripto";

    public Connection getConnection() throws SQLException {
        String url = obterConfiguracao("db.url", "DB_URL", URL_PADRAO);
        String usuario = obterConfiguracao("db.user", "DB_USER", null);
        String senha = obterConfiguracao("db.password", "DB_PASSWORD", null);

        if (usuario == null || usuario.isBlank()) {
            throw new SQLException("Configure a variável DB_USER (ou a propriedade Java db.user) para conectar ao PostgreSQL.");
        }
        if (senha == null) {
            throw new SQLException("Configure a variável DB_PASSWORD (ou a propriedade Java db.password) para conectar ao PostgreSQL.");
        }

        return DriverManager.getConnection(url, usuario, senha);
    }

    private String obterConfiguracao(String propriedade, String variavelAmbiente, String valorPadrao) {
        String valor = System.getProperty(propriedade);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(variavelAmbiente);
        }
        if ((valor == null || valor.isBlank()) && valorPadrao != null) {
            valor = valorPadrao;
        }
        return valor;
    }
}
