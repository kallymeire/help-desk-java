package br.com.helpdesk.dao;

import br.com.helpdesk.model.Chamado;
import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Interacao;
import br.com.helpdesk.model.Prioridade;
import br.com.helpdesk.model.StatusChamado;
import br.com.helpdesk.model.Tecnico;
import java.time.LocalDate;
import java.util.List;

/**
 * Acesso a dados de chamados e do histórico de andamento (tabelas chamado e interacao).
 */
public interface ChamadoDAO {

    /** Grava o chamado e as interações já existentes nele, em uma única transação. */
    void inserir(Chamado chamado);

    /** Atualiza status, técnico responsável e data de fechamento. */
    void atualizar(Chamado chamado);

    /** Grava uma nova interação do chamado e define o id gerado. */
    void inserirInteracao(Chamado chamado, Interacao interacao);

    /** Busca o chamado com o histórico completo; null se não existir. */
    Chamado buscarPorId(int id);

    /** Consulta com filtros opcionais (qualquer parâmetro pode ser null), do mais novo para o mais antigo. */
    List<Chamado> buscar(String texto, StatusChamado status, Prioridade prioridade, Cliente cliente);

    /** Chamados abertos no período (datas inclusivas), com filtros opcionais. */
    List<Chamado> relatorio(LocalDate inicio, LocalDate fim, Tecnico tecnico, StatusChamado status);
}
