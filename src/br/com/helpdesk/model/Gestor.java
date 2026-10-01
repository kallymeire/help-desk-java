package br.com.helpdesk.model;

/**
 * Responsável pela equipe de suporte: cadastra pessoas e acompanha relatórios.
 */
public class Gestor extends Pessoa {

    public Gestor(int id, String nome, String email) {
        super(id, nome, email);
    }

    @Override
    public String getPerfil() { return "Gestor de suporte"; }

    @Override
    public String getTipo() { return "GESTOR"; }
}
