package br.com.helpdesk.model;

/**
 * Profissional de suporte que atende os chamados (nível 1, 2 ou 3).
 */
public class Tecnico extends Pessoa {

    private int nivel;
    private String especialidade;

    public Tecnico(int id, String nome, String email, int nivel, String especialidade) {
        super(id, nome, email);
        if (nivel < 1 || nivel > 3) {
            throw new IllegalArgumentException("O nível do técnico deve ser 1, 2 ou 3.");
        }
        this.nivel = nivel;
        this.especialidade = especialidade;
    }

    @Override
    public String getPerfil() { return "Técnico N" + nivel; }

    @Override
    public String getTipo() { return "TECNICO"; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
}
