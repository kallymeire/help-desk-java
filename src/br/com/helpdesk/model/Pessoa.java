package br.com.helpdesk.model;

import br.com.helpdesk.util.Seguranca;

/**
 * Classe abstrata que representa qualquer pessoa cadastrada no sistema.
 */
public abstract class Pessoa {

    public static final String SENHA_INICIAL = "123456";

    private int id;
    private String nome;
    private String email;
    private String senhaHash;

    public Pessoa(int id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        definirSenha(SENHA_INICIAL);
    }

    /** Retorna o perfil da pessoa (Cliente, Técnico ou Gestor). */
    public abstract String getPerfil();

    /** Valor gravado na coluna pessoa.tipo do banco de dados. */
    public abstract String getTipo();

    public void definirSenha(String senha) {
        this.senhaHash = Seguranca.hash(senha);
    }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public boolean conferirSenha(String senha) {
        return senhaHash.equals(Seguranca.hash(senha));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIniciais() {
        String[] partes = nome.trim().split("\\s+");
        String ini = partes[0].substring(0, 1);
        if (partes.length > 1) {
            ini += partes[partes.length - 1].substring(0, 1);
        }
        return ini.toUpperCase();
    }

    /** Duas pessoas são a mesma quando têm o mesmo tipo e o mesmo id (chave primária no banco). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pessoa outra = (Pessoa) o;
        return id != 0 && id == outra.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return getNome();
    }
}
