package br.com.helpdesk.service;

import br.com.helpdesk.dao.ChamadoDAO;
import br.com.helpdesk.model.Chamado;
import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Interacao;
import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.model.Prioridade;
import br.com.helpdesk.model.StatusChamado;
import br.com.helpdesk.model.Tecnico;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de negócio dos chamados. Os dados são lidos e gravados no MySQL por meio do {@link ChamadoDAO}.
 */
public class ChamadoService {

    private final ChamadoDAO dao;

    public ChamadoService(ChamadoDAO dao) {
        this.dao = dao;
    }

    /** RN1: todo chamado precisa de cliente, título, descrição e prioridade. */
    public Chamado abrirChamado(Cliente cliente, String titulo, String descricao, Prioridade prioridade) {
        if (cliente == null) {
            throw new IllegalArgumentException("O chamado precisa de um cliente.");
        }
        if (titulo == null || titulo.trim().isEmpty() || descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Título e descrição são obrigatórios.");
        }
        if (prioridade == null) {
            throw new IllegalArgumentException("Informe a prioridade do chamado.");
        }
        Chamado chamado = new Chamado(0, titulo.trim(), descricao.trim(), prioridade, cliente);
        chamado.adicionarInteracao(new Interacao(0, cliente, "Chamado aberto: " + chamado.getTitulo() + "."));
        dao.inserir(chamado);
        return chamado;
    }

    /** RN2: chamados de prioridade ALTA ou CRITICA só podem ser atribuídos a técnicos nível 2 ou 3. */
    public void atribuirTecnico(Chamado chamado, Tecnico tecnico) {
        sincronizar(chamado);
        verificarNaoFechado(chamado);
        boolean exigeNivelAlto = chamado.getPrioridade() == Prioridade.ALTA
                || chamado.getPrioridade() == Prioridade.CRITICA;
        if (exigeNivelAlto && tecnico.getNivel() < 2) {
            throw new IllegalStateException("Chamado de prioridade " + chamado.getPrioridade().name().toLowerCase()
                    + " exige técnico de nível 2 ou 3.");
        }
        chamado.setTecnico(tecnico);
        if (chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        }
        dao.atualizar(chamado);
    }

    public Interacao registrarInteracao(Chamado chamado, Pessoa autor, String texto) {
        sincronizar(chamado);
        verificarNaoFechado(chamado);
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Escreva o andamento antes de enviar.");
        }
        Interacao interacao = new Interacao(0, autor, texto.trim());
        dao.inserirInteracao(chamado, interacao);
        chamado.adicionarInteracao(interacao);
        return interacao;
    }

    public void alterarStatus(Chamado chamado, StatusChamado novoStatus) {
        sincronizar(chamado);
        verificarNaoFechado(chamado);
        if (novoStatus == StatusChamado.FECHADO) {
            throw new IllegalStateException("Use a opção Fechar chamado para encerrar.");
        }
        chamado.setStatus(novoStatus);
        dao.atualizar(chamado);
    }

    /** RN4: para fechar, o chamado precisa estar resolvido e ter andamento registrado por um técnico. */
    public void fecharChamado(Chamado chamado) {
        sincronizar(chamado);
        verificarNaoFechado(chamado);
        if (chamado.getStatus() != StatusChamado.RESOLVIDO) {
            throw new IllegalStateException("Só é possível fechar um chamado com status Resolvido.");
        }
        if (!chamado.temInteracaoDeTecnico()) {
            throw new IllegalStateException("Registre a solução antes de fechar o chamado.");
        }
        chamado.setStatus(StatusChamado.FECHADO);
        chamado.setDataFechamento(LocalDateTime.now());
        dao.atualizar(chamado);
    }

    /** Regra usada pela tela: só dá para fechar quando resolvido e com solução registrada. */
    public boolean podeFechar(Chamado chamado) {
        return chamado.getStatus() == StatusChamado.RESOLVIDO && chamado.temInteracaoDeTecnico();
    }

    /** Busca o chamado completo (com histórico) no banco; null se não existir. */
    public Chamado obter(int id) {
        return dao.buscarPorId(id);
    }

    public List<Chamado> listarTodos() {
        return dao.buscar(null, null, null, null);
    }

    public List<Chamado> listarDoCliente(Cliente cliente) {
        return dao.buscar(null, null, null, cliente);
    }

    /** RF08: consulta com filtros opcionais (qualquer parâmetro pode ser null). */
    public List<Chamado> buscar(String texto, StatusChamado status, Prioridade prioridade, Cliente somenteDoCliente) {
        return dao.buscar(texto, status, prioridade, somenteDoCliente);
    }

    /** RF09: chamados abertos dentro do período, com filtros opcionais. */
    public List<Chamado> filtrarRelatorio(LocalDate inicio, LocalDate fim, Tecnico tecnico, StatusChamado status) {
        return dao.relatorio(inicio, fim, tecnico, status);
    }

    public static long contar(List<Chamado> lista, StatusChamado status) {
        long n = 0;
        for (Chamado c : lista) {
            if (c.getStatus() == status) n++;
        }
        return n;
    }

    public static long contarAtrasados(List<Chamado> lista) {
        long n = 0;
        for (Chamado c : lista) {
            if (c.estaAtrasado()) n++;
        }
        return n;
    }

    public static long contarFechadosHoje(List<Chamado> lista) {
        long n = 0;
        for (Chamado c : lista) {
            if (c.getStatus() == StatusChamado.FECHADO && c.getDataFechamento() != null
                    && c.getDataFechamento().toLocalDate().equals(LocalDate.now())) {
                n++;
            }
        }
        return n;
    }

    /** Traz do banco o estado mais recente do chamado (outro usuário pode ter alterado). */
    private void sincronizar(Chamado chamado) {
        Chamado atual = dao.buscarPorId(chamado.getId());
        if (atual == null) {
            throw new IllegalStateException("O chamado #" + chamado.getId() + " não existe mais.");
        }
        chamado.copiarEstadoDe(atual);
    }

    /** RN3: chamado fechado não pode mais ser alterado. */
    private void verificarNaoFechado(Chamado chamado) {
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new IllegalStateException("O chamado #" + chamado.getId() + " já está fechado.");
        }
    }
}
