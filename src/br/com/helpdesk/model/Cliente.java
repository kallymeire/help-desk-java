package br.com.helpdesk.model;

/**
 * Usuário que abre chamados (funcionário de qualquer setor da empresa).
 */
public class Cliente extends Pessoa {

    private String setor;
    private String telefone;

    public Cliente(int id, String nome, String email, String setor, String telefone) {
        super(id, nome, email);
        this.setor = setor;
        this.telefone = telefone;
    }

    @Override
    public String getPerfil() { return "Cliente"; }

    @Override
    public String getTipo() { return "CLIENTE"; }

    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
