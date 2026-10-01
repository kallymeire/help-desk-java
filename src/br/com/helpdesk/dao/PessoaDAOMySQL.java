package br.com.helpdesk.dao;

import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.model.Tecnico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação JDBC/MySQL de {@link PessoaDAO}. A generalização Pessoa → Cliente/Técnico
 * é gravada nas tabelas pessoa (dados comuns) e cliente/tecnico (dados do perfil).
 */
public class PessoaDAOMySQL implements PessoaDAO {

    private static final String SELECT_BASE =
            "SELECT p.id, p.tipo, p.nome, p.email, p.senha_hash, cl.setor, cl.telefone, te.nivel, te.especialidade "
            + "FROM pessoa p "
            + "LEFT JOIN cliente cl ON cl.pessoa_id = p.id "
            + "LEFT JOIN tecnico te ON te.pessoa_id = p.id ";
    static final String SQL_POR_EMAIL = SELECT_BASE + "WHERE LOWER(p.email) = LOWER(?)";
    static final String SQL_POR_ID = SELECT_BASE + "WHERE p.id = ?";
    static final String SQL_LISTAR_POR_TIPO = SELECT_BASE + "WHERE p.tipo = ? ORDER BY p.nome";
    static final String SQL_INSERIR_PESSOA = "INSERT INTO pessoa (tipo, nome, email, senha_hash) VALUES (?, ?, ?, ?)";
    static final String SQL_INSERIR_CLIENTE = "INSERT INTO cliente (pessoa_id, setor, telefone) VALUES (?, ?, ?)";
    static final String SQL_INSERIR_TECNICO = "INSERT INTO tecnico (pessoa_id, nivel, especialidade) VALUES (?, ?, ?)";
    static final String SQL_ATUALIZAR_PESSOA = "UPDATE pessoa SET nome = ?, email = ? WHERE id = ?";
    static final String SQL_ATUALIZAR_CLIENTE = "UPDATE cliente SET setor = ?, telefone = ? WHERE pessoa_id = ?";
    static final String SQL_ATUALIZAR_TECNICO = "UPDATE tecnico SET nivel = ?, especialidade = ? WHERE pessoa_id = ?";
    static final String SQL_ATUALIZAR_SENHA = "UPDATE pessoa SET senha_hash = ? WHERE id = ?";
    static final String SQL_EXCLUIR_CLIENTE = "DELETE FROM cliente WHERE pessoa_id = ?";
    static final String SQL_EXCLUIR_TECNICO = "DELETE FROM tecnico WHERE pessoa_id = ?";
    static final String SQL_EXCLUIR_PESSOA = "DELETE FROM pessoa WHERE id = ?";
    static final String SQL_VINCULOS =
            "SELECT (SELECT COUNT(*) FROM chamado WHERE cliente_id = ? OR tecnico_id = ?) "
            + "+ (SELECT COUNT(*) FROM interacao WHERE autor_id = ?)";

    @Override
    public Pessoa buscarPorEmail(String email) {
        return buscarUma(SQL_POR_EMAIL, email);
    }

    @Override
    public Pessoa buscarPorId(int id) {
        return buscarUma(SQL_POR_ID, id);
    }

    private Pessoa buscarUma(String sql, Object parametro) {
        try (Connection con = ConexaoMySQL.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Mapeador.pessoa(rs) : null;
            }
        } catch (SQLException e) {
            throw erro(e);
        }
    }

    @Override
    public List<Cliente> listarClientes() {
        List<Cliente> r = new ArrayList<>();
        for (Pessoa p : listar("CLIENTE")) r.add((Cliente) p);
        return r;
    }

    @Override
    public List<Tecnico> listarTecnicos() {
        List<Tecnico> r = new ArrayList<>();
        for (Pessoa p : listar("TECNICO")) r.add((Tecnico) p);
        return r;
    }

    private List<Pessoa> listar(String tipo) {
        List<Pessoa> r = new ArrayList<>();
        try (Connection con = ConexaoMySQL.getConexao(); PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_TIPO)) {
            ps.setString(1, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) r.add(Mapeador.pessoa(rs));
            }
        } catch (SQLException e) {
            throw erro(e);
        }
        return r;
    }

    @Override
    public void inserir(Pessoa pessoa) {
        try (Connection con = ConexaoMySQL.getConexao()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(SQL_INSERIR_PESSOA, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, pessoa.getTipo());
                    ps.setString(2, pessoa.getNome());
                    ps.setString(3, pessoa.getEmail());
                    ps.setString(4, pessoa.getSenhaHash());
                    ps.executeUpdate();
                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                        if (chaves.next()) pessoa.setId(chaves.getInt(1));
                    }
                }
                if (pessoa instanceof Cliente) {
                    Cliente c = (Cliente) pessoa;
                    try (PreparedStatement ps = con.prepareStatement(SQL_INSERIR_CLIENTE)) {
                        ps.setInt(1, c.getId());
                        ps.setString(2, c.getSetor());
                        ps.setString(3, c.getTelefone());
                        ps.executeUpdate();
                    }
                } else if (pessoa instanceof Tecnico) {
                    Tecnico t = (Tecnico) pessoa;
                    try (PreparedStatement ps = con.prepareStatement(SQL_INSERIR_TECNICO)) {
                        ps.setInt(1, t.getId());
                        ps.setInt(2, t.getNivel());
                        ps.setString(3, t.getEspecialidade());
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw erro(e);
        }
    }

    @Override
    public void atualizar(Pessoa pessoa) {
        try (Connection con = ConexaoMySQL.getConexao()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR_PESSOA)) {
                    ps.setString(1, pessoa.getNome());
                    ps.setString(2, pessoa.getEmail());
                    ps.setInt(3, pessoa.getId());
                    ps.executeUpdate();
                }
                if (pessoa instanceof Cliente) {
                    Cliente c = (Cliente) pessoa;
                    try (PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR_CLIENTE)) {
                        ps.setString(1, c.getSetor());
                        ps.setString(2, c.getTelefone());
                        ps.setInt(3, c.getId());
                        ps.executeUpdate();
                    }
                } else if (pessoa instanceof Tecnico) {
                    Tecnico t = (Tecnico) pessoa;
                    try (PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR_TECNICO)) {
                        ps.setInt(1, t.getNivel());
                        ps.setString(2, t.getEspecialidade());
                        ps.setInt(3, t.getId());
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw erro(e);
        }
    }

    @Override
    public void atualizarSenha(Pessoa pessoa) {
        try (Connection con = ConexaoMySQL.getConexao(); PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR_SENHA)) {
            ps.setString(1, pessoa.getSenhaHash());
            ps.setInt(2, pessoa.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw erro(e);
        }
    }

    @Override
    public void excluir(Pessoa pessoa) {
        try (Connection con = ConexaoMySQL.getConexao()) {
            con.setAutoCommit(false);
            try {
                String perfil = pessoa instanceof Cliente ? SQL_EXCLUIR_CLIENTE : (pessoa instanceof Tecnico ? SQL_EXCLUIR_TECNICO : null);
                if (perfil != null) {
                    try (PreparedStatement ps = con.prepareStatement(perfil)) {
                        ps.setInt(1, pessoa.getId());
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(SQL_EXCLUIR_PESSOA)) {
                    ps.setInt(1, pessoa.getId());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw erro(e);
        }
    }

    @Override
    public boolean possuiVinculos(Pessoa pessoa) {
        try (Connection con = ConexaoMySQL.getConexao(); PreparedStatement ps = con.prepareStatement(SQL_VINCULOS)) {
            ps.setInt(1, pessoa.getId());
            ps.setInt(2, pessoa.getId());
            ps.setInt(3, pessoa.getId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw erro(e);
        }
    }

    private static AcessoDadosException erro(SQLException e) {
        return new AcessoDadosException("Erro ao acessar o banco de dados: " + e.getMessage(), e);
    }
}
