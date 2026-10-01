package br.com.helpdesk.view;

import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.service.CadastroService;
import br.com.helpdesk.service.ChamadoService;
import br.com.helpdesk.dao.ChamadoDAOMySQL;
import br.com.helpdesk.dao.PessoaDAOMySQL;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Janela única do sistema: alterna entre a tela de login e a área principal.
 */
public class JanelaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private final CardLayout cartas = new CardLayout();
    private final JPanel raiz = new JPanel(cartas);
    private final CadastroService cadastros = new CadastroService(new PessoaDAOMySQL());
    private final ChamadoService chamados = new ChamadoService(new ChamadoDAOMySQL());
    private final PainelLogin login;
    private AppPanel app;

    public JanelaPrincipal() {
        super("Sistema Help Desk");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        login = new PainelLogin(cadastros, this::entrar);
        raiz.add(login, "login");
        setContentPane(raiz);
        mostrarLogin();
    }

    private void entrar(Pessoa usuario) {
        if (app != null) raiz.remove(app);
        app = new AppPanel(usuario, cadastros, chamados, this::mostrarLogin);
        raiz.add(app, "app");
        cartas.show(raiz, "app");
        app.irPara("painel");
        raiz.revalidate();
    }

    private void mostrarLogin() {
        cartas.show(raiz, "login");
        login.aoExibir();
    }
}
