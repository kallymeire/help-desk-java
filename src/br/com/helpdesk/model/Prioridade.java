package br.com.helpdesk.model;

/**
 * Prioridade do chamado, com o prazo (SLA) de atendimento em horas.
 */
public enum Prioridade {
    BAIXA(72), MEDIA(48), ALTA(24), CRITICA(4);

    private final int prazoHoras;

    Prioridade(int prazoHoras) { this.prazoHoras = prazoHoras; }

    public int getPrazoHoras() { return prazoHoras; }
}
