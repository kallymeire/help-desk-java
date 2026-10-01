package br.com.helpdesk.view;

import br.com.helpdesk.model.Chamado;
import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.service.CadastroService;
import br.com.helpdesk.service.ChamadoService;

/**
 * Permite que as telas naveguem entre si e acessem os serviços e o usuário logado.
 */
public interface Navegador {
    void irPara(String tela);
    void abrirAtendimento(Chamado chamado);
    void setTitulo(String titulo);
    Pessoa usuario();
    ChamadoService chamados();
    CadastroService cadastros();
}
