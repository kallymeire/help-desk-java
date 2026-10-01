package br.com.helpdesk.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexões com o banco MySQL. Os dados de acesso ficam em config/banco.properties
 * (host, porta, banco, usuário e senha); se o arquivo não existir, usa os valores padrão.
 * Requer o driver MySQL Connector/J nas bibliotecas do projeto.
 */
public final class ConexaoMySQL {

    private static final String ARQUIVO = "config/banco.properties";
    private static Properties config;

    private ConexaoMySQL() { }

    private static synchronized Properties config() {
        if (config == null) {
            Properties p = new Properties();
            p.setProperty("host", "localhost");
            p.setProperty("porta", "3306");
            p.setProperty("banco", "helpdesk");
            p.setProperty("usuario", "root");
            p.setProperty("senha", "");
            File f = new File(ARQUIVO);
            if (f.isFile()) {
                try (InputStream in = new FileInputStream(f)) {
                    p.load(in);
                } catch (IOException e) {
                    throw new AcessoDadosException("Não foi possível ler o arquivo " + ARQUIVO + ".", e);
                }
            }
            config = p;
        }
        return config;
    }

    /** Monta a URL JDBC a partir da configuração. */
    static String url() {
        Properties p = config();
        return "jdbc:mysql://" + p.getProperty("host").trim() + ":" + p.getProperty("porta").trim() + "/"
                + p.getProperty("banco").trim()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8";
    }

    private static void carregarDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e2) {
                throw new AcessoDadosException("Driver MySQL (Connector/J) não encontrado nas bibliotecas do projeto.", e2);
            }
        }
    }

    public static Connection getConexao() throws SQLException {
        carregarDriver();
        Properties p = config();
        return DriverManager.getConnection(url(), p.getProperty("usuario"), p.getProperty("senha"));
    }

    /** Testa a conexão; lança AcessoDadosException com uma mensagem clara se não for possível conectar. */
    public static void testar() {
        try (Connection c = getConexao()) {
            c.isValid(3);
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível conectar ao MySQL: " + e.getMessage(), e);
        }
    }
}
