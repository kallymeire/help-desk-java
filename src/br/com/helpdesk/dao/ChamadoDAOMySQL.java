package br.com.helpdesk.dao;

import br.com.helpdesk.model.Chamado;
import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Interacao;
import br.com.helpdesk.model.Prioridade;
import br.com.helpdesk.model.StatusChamado;
import br.com.helpdesk.model.Tecnico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação JDBC/MySQL de {@link ChamadoDAO}.
 */
public class ChamadoDAOMySQL implements ChamadoDAO {

    static final String SQL_SELECT =
            "SELECT ch.id, ch.titulo, ch.descricao, ch.prioridade, ch.status, ch.data_abertura, ch.data_fechamento, "
            + "pc.id AS cli_id, pc.nome AS cli_nome, pc.email AS cli_email, cl.setor AS cli_setor, cl.telefone AS cli_telefone, "
            + "pt.id AS tec_id, pt.nome AS tec_nome, pt.email AS tec_email, te.nivel AS tec_nivel, te.especialidade AS tec_especialidade "
            + "FROM chamado ch "
            + "JOIN pessoa pc ON pc.id = ch.cliente_id "
            + "JOIN cliente cl ON cl.pessoa_id = pc.id "
            + "LEFT JOIN pessoa pt ON pt.id = ch.tecnico_id "
            + "LEFT JOIN tecnico te ON te.pessoa_id = pt.id ";
    static final String SQL_POR_ID = SQL_SELECT + "WHERE ch.id = ?";
    static final String SQL_INTERACOES =
            "SELECT i.id, i.texto, i.data_hora, p.id AS aut_id, p.tipo AS aut_tipo, p.nome AS aut_nome, "
            + "p.email AS aut_email, te.nivel AS aut_nivel "
            + "FROM interacao i JOIN pessoa p ON p.id = i.autor_id "
            + "LEFT JOIN tecnico te ON te.pessoa_id = p.id "
            + "WHERE i.chamado_id = ? ORDER BY i.data_hora, i.id";
    static final String SQL_INSERIR =
            "INSERT INTO chamado (titulo, descricao, prioridade, status, data_abertura, cliente_id, tecnico_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    static final String SQL_ATUALIZAR = "UPDATE chamado SET status = ?, tecnico_id = ?, data_fechamento = ? WHERE id = ?";
    static final String SQL_INSERIR_INTERACAO =
            "INSERT INTO interacao (chamado_id, autor_id, texto, data_hora) VALUES (?, ?, ?, ?)";

    @Override
    public void inserir(Chamado chamado) {
        try (Connection con = ConexaoMySQL.getConexao()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, chamado.getTitulo());
                    ps.setString(2, chamado.getDescricao());
                    ps.setString(3, chamado.getPrioridade().name());
                    ps.setString(4, chamado.getStatus().name());
                    ps.setTimestamp(5, Timestamp.valueOf(chamado.getDataAbertura()));
                    ps.setInt(6, chamado.getCliente().getId());
                    if (chamado.getTecnico() == null) {
                        ps.setNull(7, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(7, chamado.getTecnico().getId());
                    }
                    ps.executeUpdate();
                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                        if (chaves.next()) chamado.setId(chaves.getInt(1));
                    }
                }
                for (Interacao i : chamado.getInteracoes()) {
                    gravarInteracao(con, chamado.getId(), i);
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
    public void atualizar(Chamado chamado) {
        try (Connection con = ConexaoMySQL.getConexao(); PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, chamado.getStatus().name());
            if (chamado.getTecnico() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, chamado.getTecnico().getId());
            }
            if (chamado.getDataFechamento() == null) {
                ps.setNull(3, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, Timestamp.valueOf(chamado.getDataFechamento()));
            }
            ps.setInt(4, chamado.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw erro(e);
        }
    }

    @Override
    public void inserirInteracao(Chamado chamado, Interacao interacao) {
        try (Connection con = ConexaoMySQL.getConexao()) {
            gravarInteracao(con, chamado.getId(), interacao);
        } catch (SQLException e) {
            throw erro(e);
        }
    }

    private void gravarInteracao(Connection con, int idChamado, Interacao i) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERIR_INTERACAO, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idChamado);
            ps.setInt(2, i.getAutor().getId());
            ps.setString(3, i.getTexto());
            ps.setTimestamp(4, Timestamp.valueOf(i.getDataHora()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) i.setId(chaves.getInt(1));
            }
        }
    }

    @Override
    public Chamado buscarPorId(int id) {
        try (Connection con = ConexaoMySQL.getConexao()) {
            Chamado chamado = null;
            try (PreparedStatement ps = con.prepareStatement(SQL_POR_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) chamado = ler(rs);
                }
            }
            if (chamado != null) {
                try (PreparedStatement ps = con.prepareStatement(SQL_INTERACOES)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Interacao i = new Interacao(rs.getInt("id"), Mapeador.autor(rs), rs.getString("texto"));
                            i.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
                            chamado.adicionarInteracao(i);
                        }
                    }
                }
            }
            return chamado;
        } catch (SQLException e) {
            throw erro(e);
        }
    }

    @Override
    public List<Chamado> buscar(String texto, StatusChamado status, Prioridade prioridade, Cliente cliente) {
        StringBuilder sql = new StringBuilder(SQL_SELECT).append("WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (cliente != null) {
            sql.append(" AND ch.cliente_id = ?");
            args.add(cliente.getId());
        }
        if (status != null) {
            sql.append(" AND ch.status = ?");
            args.add(status.name());
        }
        if (prioridade != null) {
            sql.append(" AND ch.prioridade = ?");
            args.add(prioridade.name());
        }
        String t = texto == null ? "" : texto.trim().toLowerCase().replace("#", "");
        if (!t.isEmpty()) {
            sql.append(" AND (CAST(ch.id AS CHAR) LIKE ? OR LOWER(ch.titulo) LIKE ? OR LOWER(pc.nome) LIKE ?)");
            String like = "%" + t + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY ch.id DESC");
        return consultar(sql.toString(), args);
    }

    @Override
    public List<Chamado> relatorio(LocalDate inicio, LocalDate fim, Tecnico tecnico, StatusChamado status) {
        StringBuilder sql = new StringBuilder(SQL_SELECT).append("WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (inicio != null) {
            sql.append(" AND ch.data_abertura >= ?");
            args.add(Timestamp.valueOf(inicio.atStartOfDay()));
        }
        if (fim != null) {
            sql.append(" AND ch.data_abertura < ?");
            args.add(Timestamp.valueOf(fim.plusDays(1).atStartOfDay()));
        }
        if (tecnico != null) {
            sql.append(" AND ch.tecnico_id = ?");
            args.add(tecnico.getId());
        }
        if (status != null) {
            sql.append(" AND ch.status = ?");
            args.add(status.name());
        }
        sql.append(" ORDER BY ch.id");
        return consultar(sql.toString(), args);
    }

    private List<Chamado> consultar(String sql, List<Object> args) {
        List<Chamado> r = new ArrayList<>();
        try (Connection con = ConexaoMySQL.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) r.add(ler(rs));
            }
        } catch (SQLException e) {
            throw erro(e);
        }
        return r;
    }

    private Chamado ler(ResultSet rs) throws SQLException {
        Chamado c = new Chamado(rs.getInt("id"), rs.getString("titulo"), rs.getString("descricao"),
                Prioridade.valueOf(rs.getString("prioridade")), Mapeador.cliente(rs));
        c.setStatus(StatusChamado.valueOf(rs.getString("status")));
        c.setDataAbertura(rs.getTimestamp("data_abertura").toLocalDateTime());
        Timestamp fim = rs.getTimestamp("data_fechamento");
        c.setDataFechamento(fim == null ? null : fim.toLocalDateTime());
        c.setTecnico(Mapeador.tecnico(rs));
        return c;
    }

    private static AcessoDadosException erro(SQLException e) {
        return new AcessoDadosException("Erro ao acessar o banco de dados: " + e.getMessage(), e);
    }

}
