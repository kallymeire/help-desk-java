package br.com.helpdesk.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Chamado de suporte técnico aberto por um cliente.
 */
public class Chamado {

    private int id;
    private String titulo;
    private String descricao;
    private Prioridade prioridade;
    private StatusChamado status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private Cliente cliente;
    private Tecnico tecnico;
    private final List<Interacao> interacoes = new ArrayList<>();

    public Chamado(int id, String titulo, String descricao, Prioridade prioridade, Cliente cliente) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.cliente = cliente;
        this.status = StatusChamado.ABERTO;
        this.dataAbertura = LocalDateTime.now();
    }

    public void adicionarInteracao(Interacao interacao) {
        interacoes.add(interacao);
    }

    /** Data limite de atendimento, calculada pela prioridade (SLA). */
    public LocalDateTime getPrazoLimite() {
        return dataAbertura.plusHours(prioridade.getPrazoHoras());
    }

    /** Chamado em aberto (não resolvido nem fechado) com o prazo vencido. */
    public boolean estaAtrasado() {
        return status != StatusChamado.FECHADO && status != StatusChamado.RESOLVIDO
                && LocalDateTime.now().isAfter(getPrazoLimite());
    }

    /** True se algum técnico já registrou andamento no chamado (usado na RN4). */
    public boolean temInteracaoDeTecnico() {
        for (Interacao i : interacoes) {
            if (i.getAutor() instanceof Tecnico) {
                return true;
            }
        }
        return false;
    }

    /** Texto curto do prazo para a tabela: "em 40 h", "⚠ há 1 h 10 min" ou "—". */
    public String getPrazoResumo() {
        if (status == StatusChamado.FECHADO || status == StatusChamado.RESOLVIDO) {
            return "—";
        }
        long minutos = Duration.between(LocalDateTime.now(), getPrazoLimite()).toMinutes();
        if (minutos < 0) {
            return "⚠ há " + formatar(-minutos);
        }
        return "em " + formatar(minutos);
    }

    private static String formatar(long minutos) {
        if (minutos < 60) {
            return minutos + " min";
        }
        if (minutos < 24 * 60) {
            long h = minutos / 60;
            long m = minutos % 60;
            return h + " h" + (m > 0 ? " " + m + " min" : "");
        }
        return (minutos / (24 * 60)) + " d " + ((minutos % (24 * 60)) / 60) + " h";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Prioridade getPrioridade() { return prioridade; }
    public StatusChamado getStatus() { return status; }
    public void setStatus(StatusChamado status) { this.status = status; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }
    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }
    public Cliente getCliente() { return cliente; }
    public Tecnico getTecnico() { return tecnico; }
    public void setTecnico(Tecnico tecnico) { this.tecnico = tecnico; }
    public List<Interacao> getInteracoes() { return interacoes; }

    /** Atualiza este objeto com o estado mais recente lido do banco (status, técnico, fechamento e histórico). */
    public void copiarEstadoDe(Chamado outro) {
        this.status = outro.status;
        this.tecnico = outro.tecnico;
        this.dataFechamento = outro.dataFechamento;
        this.interacoes.clear();
        this.interacoes.addAll(outro.interacoes);
    }

    /** Dois chamados são o mesmo quando têm o mesmo id (chave primária no banco). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id != 0 && id == ((Chamado) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "#" + id + " · " + titulo;
    }
}
