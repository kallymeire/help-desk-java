package br.com.helpdesk.dao;

import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Gestor;
import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.model.Tecnico;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Converte linhas do banco (ResultSet) em objetos do modelo.
 */
final class Mapeador {

    private Mapeador() { }

    private static String texto(ResultSet rs, String coluna) throws SQLException {
        String v = rs.getString(coluna);
        return v == null ? "" : v;
    }

    /** Lê uma pessoa completa (colunas id, tipo, nome, email, senha_hash, setor, telefone, nivel, especialidade). */
    static Pessoa pessoa(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String email = rs.getString("email");
        Pessoa p;
        String tipo = rs.getString("tipo");
        if ("CLIENTE".equals(tipo)) {
            p = new Cliente(id, nome, email, texto(rs, "setor"), texto(rs, "telefone"));
        } else if ("TECNICO".equals(tipo)) {
            p = new Tecnico(id, nome, email, rs.getInt("nivel"), texto(rs, "especialidade"));
        } else {
            p = new Gestor(id, nome, email);
        }
        p.setSenhaHash(rs.getString("senha_hash"));
        return p;
    }

    /** Lê o cliente de um chamado (colunas com prefixo cli_). */
    static Cliente cliente(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("cli_id"), rs.getString("cli_nome"), rs.getString("cli_email"),
                texto(rs, "cli_setor"), texto(rs, "cli_telefone"));
    }

    /** Lê o técnico de um chamado (colunas com prefixo tec_); null se o chamado não tem técnico. */
    static Tecnico tecnico(ResultSet rs) throws SQLException {
        int id = rs.getInt("tec_id");
        if (rs.wasNull()) {
            return null;
        }
        return new Tecnico(id, rs.getString("tec_nome"), rs.getString("tec_email"),
                rs.getInt("tec_nivel"), texto(rs, "tec_especialidade"));
    }

    /** Lê o autor de uma interação (colunas com prefixo aut_). */
    static Pessoa autor(ResultSet rs) throws SQLException {
        int id = rs.getInt("aut_id");
        String nome = rs.getString("aut_nome");
        String email = rs.getString("aut_email");
        String tipo = rs.getString("aut_tipo");
        if ("CLIENTE".equals(tipo)) {
            return new Cliente(id, nome, email, "", "");
        }
        if ("TECNICO".equals(tipo)) {
            return new Tecnico(id, nome, email, rs.getInt("aut_nivel"), "");
        }
        return new Gestor(id, nome, email);
    }
}
