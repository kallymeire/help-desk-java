package br.com.helpdesk.dao;

import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.model.Tecnico;
import java.util.List;

/**
 * Acesso a dados de clientes, técnicos e gestores (tabelas pessoa, cliente e tecnico).
 */
public interface PessoaDAO {

    Pessoa buscarPorEmail(String email);

    Pessoa buscarPorId(int id);

    List<Cliente> listarClientes();

    List<Tecnico> listarTecnicos();

    /** Grava a pessoa e define o id gerado pelo banco. */
    void inserir(Pessoa pessoa);

    /** Atualiza nome, e-mail e dados do perfil (não altera a senha). */
    void atualizar(Pessoa pessoa);

    void atualizarSenha(Pessoa pessoa);

    void excluir(Pessoa pessoa);

    /** True se a pessoa já abriu, atendeu ou registrou andamento em algum chamado. */
    boolean possuiVinculos(Pessoa pessoa);
}
