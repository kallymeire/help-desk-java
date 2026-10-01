package br.com.helpdesk.service;

import br.com.helpdesk.dao.PessoaDAO;
import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.model.Tecnico;
import java.util.List;

/**
 * Cadastro e autenticação de clientes, técnicos e gestores, com dados no MySQL (via {@link PessoaDAO}).
 */
public class CadastroService {

    private final PessoaDAO dao;

    public CadastroService(PessoaDAO dao) {
        this.dao = dao;
    }

    /** RF01: devolve a pessoa se e-mail e senha conferem; caso contrário, null. */
    public Pessoa autenticar(String email, String senha) {
        if (email == null || senha == null || email.trim().isEmpty()) {
            return null;
        }
        Pessoa p = dao.buscarPorEmail(email.trim());
        return (p != null && p.conferirSenha(senha)) ? p : null;
    }

    public Pessoa buscarPorEmail(String email) {
        return dao.buscarPorEmail(email);
    }

    public List<Cliente> listarClientes() { return dao.listarClientes(); }

    public List<Tecnico> listarTecnicos() { return dao.listarTecnicos(); }

    /** RF02 e RN6: e-mail válido e único. Cria (existente == null) ou atualiza o cliente. */
    public Cliente salvarCliente(Cliente existente, String nome, String email, String setor, String telefone) {
        validarBasico(nome, email, existente);
        if (existente == null) {
            existente = new Cliente(0, nome.trim(), email.trim(), setor.trim(), telefone.trim());
            dao.inserir(existente);
        } else {
            existente.setNome(nome.trim());
            existente.setEmail(email.trim());
            existente.setSetor(setor.trim());
            existente.setTelefone(telefone.trim());
            dao.atualizar(existente);
        }
        return existente;
    }

    public Tecnico salvarTecnico(Tecnico existente, String nome, String email, int nivel, String especialidade) {
        validarBasico(nome, email, existente);
        if (existente == null) {
            existente = new Tecnico(0, nome.trim(), email.trim(), nivel, especialidade.trim());
            dao.inserir(existente);
        } else {
            existente.setNome(nome.trim());
            existente.setEmail(email.trim());
            existente.setNivel(nivel);
            existente.setEspecialidade(especialidade.trim());
            dao.atualizar(existente);
        }
        return existente;
    }

    public void excluir(Pessoa pessoa) {
        dao.excluir(pessoa);
    }

    /** True se a pessoa tem chamados ou andamentos ligados a ela (nesse caso não pode ser excluída). */
    public boolean possuiVinculos(Pessoa pessoa) {
        return dao.possuiVinculos(pessoa);
    }

    /** Volta a senha da pessoa para a senha inicial (123456). */
    public void redefinirSenha(Pessoa pessoa) {
        pessoa.definirSenha(Pessoa.SENHA_INICIAL);
        dao.atualizarSenha(pessoa);
    }

    private void validarBasico(String nome, String email, Pessoa atual) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome.");
        }
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        Pessoa outra = dao.buscarPorEmail(email.trim());
        if (outra != null && (atual == null || outra.getId() != atual.getId())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        }
    }
}
