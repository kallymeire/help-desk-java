package br.com.helpdesk.dao;

/**
 * Erro de acesso ao banco de dados (encapsula SQLException para a interface tratar de forma simples).
 */
public class AcessoDadosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AcessoDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
