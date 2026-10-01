package br.com.helpdesk.view;

import br.com.helpdesk.model.Chamado;
import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Gestor;
import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.service.CadastroService;
import br.com.helpdesk.service.ChamadoService;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Estrutura principal do sistema: menu lateral, barra de título com o usuário
 * e a área onde as telas são trocadas (CardLayout).
 */
public class AppPanel extends JPanel implements Navegador {

    private static final long serialVersionUID = 1L;

    private final Pessoa usuario;
    private final ChamadoService chamados;
    private final CadastroService cadastros;
    private final Runnable aoSair;
    private final CardLayout cartas = new CardLayout();
    private final JPanel conteudo = new JPanel(cartas);
    private final JLabel titulo = new JLabel(" ");
    private final Map<String, JComponent> telas = new HashMap<>();
    private final Map<String, ItemMenu> itens = new HashMap<>();
    private final PainelAtendimento atendimento;
    private final PainelChamados painel;
    private String atual = "";

    public AppPanel(Pessoa usuario, CadastroService cadastros, ChamadoService chamados, Runnable aoSair) {
        this.usuario = usuario;
        this.cadastros = cadastros;
        this.chamados = chamados;
        this.aoSair = aoSair;
        setLayout(new BorderLayout());
        setBackground(Estilo.FUNDO);

        // menu lateral
        JPanel menu = new JPanel();
        menu.setBackground(Estilo.MENU);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(220, 100));
        menu.setBorder(BorderFactory.createEmptyBorder(22, 10, 16, 10));
        JLabel logo = new JLabel("HELP DESK");
        logo.setFont(Estilo.SUBTITULO);
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(logo);
        menu.add(Box.createVerticalStrut(24));
        boolean cliente = usuario instanceof Cliente;
        boolean gestor = usuario instanceof Gestor;
        adicionarItem(menu, "painel", "Painel de chamados", 'P');
        adicionarItem(menu, "novo", "Novo chamado", 'N');
        adicionarItem(menu, "atendimento", "Atendimento", 'A');
        if (gestor) adicionarItem(menu, "cadastros", "Cadastros", 'C');
        if (!cliente) adicionarItem(menu, "relatorios", "Relatórios", 'R');
        menu.add(Box.createVerticalGlue());
        adicionarItem(menu, "sair", "Sair", 'S');
        add(menu, BorderLayout.WEST);

        // topo
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Color.WHITE);
        topo.setPreferredSize(new Dimension(100, 60));
        topo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.LINHA), BorderFactory.createEmptyBorder(0, 24, 0, 24)));
        titulo.setFont(Estilo.TITULO);
        titulo.setForeground(Estilo.TEXTO);
        topo.add(titulo, BorderLayout.WEST);
        topo.add(painelUsuario(), BorderLayout.EAST);

        // telas
        painel = new PainelChamados(this);
        atendimento = new PainelAtendimento(this);
        registrar("painel", painel);
        registrar("novo", new PainelNovoChamado(this));
        registrar("atendimento", atendimento);
        if (gestor) registrar("cadastros", new PainelCadastros(this));
        if (!cliente) registrar("relatorios", new PainelRelatorios(this));

        JPanel direita = new JPanel(new BorderLayout());
        direita.add(topo, BorderLayout.NORTH);
        conteudo.setBackground(Estilo.FUNDO);
        direita.add(conteudo, BorderLayout.CENTER);
        add(direita, BorderLayout.CENTER);

        // atalhos globais
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "novo");
        getActionMap().put("novo", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { irPara("novo"); }
        });
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "atualizar");
        getActionMap().put("atualizar", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { atualizarTela(); }
        });
    }

    private void registrar(String chave, JComponent tela) {
        telas.put(chave, tela);
        conteudo.add(tela, chave);
    }

    private void adicionarItem(JPanel menu, String chave, String texto, char mnemonico) {
        ItemMenu b = new ItemMenu(texto);
        b.setMnemonic(mnemonico);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> {
            if ("sair".equals(chave)) {
                if (Dialogos.confirmar(this, "Deseja sair do sistema?", "Sair")) aoSair.run();
            } else {
                irPara(chave);
            }
        });
        itens.put(chave, b);
        menu.add(b);
        menu.add(Box.createVerticalStrut(6));
    }

    private JPanel painelUsuario() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JComponent avatar = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override public Dimension getPreferredSize() { return new Dimension(36, 36); }
            @Override public Dimension getMaximumSize() { return new Dimension(36, 36); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Estilo.PRIMARIA);
                g2.fillOval(0, 0, 35, 35);
                g2.setColor(Color.WHITE);
                g2.setFont(Estilo.PEQUENA_NEGRITO);
                FontMetrics fm = g2.getFontMetrics();
                String s = usuario.getIniciais();
                g2.drawString(s, (36 - fm.stringWidth(s)) / 2, (36 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.getAccessibleContext().setAccessibleName("Iniciais de " + usuario.getNome());
        JPanel textos = new JPanel(new java.awt.GridLayout(2, 1));
        textos.setOpaque(false);
        JLabel nome = new JLabel(usuario.getNome());
        nome.setFont(Estilo.NEGRITO);
        nome.setForeground(Estilo.TEXTO);
        JLabel perfil = Estilo.apoio(usuario.getPerfil());
        textos.add(nome);
        textos.add(perfil);
        p.add(Box.createVerticalStrut(60));
        p.add(avatar);
        p.add(Box.createHorizontalStrut(10));
        p.add(textos);
        return p;
    }

    private static String tituloPadrao(String tela) {
        switch (tela) {
            case "painel": return "Painel de chamados";
            case "novo": return "Abertura de chamado";
            case "atendimento": return "Atendimento do chamado";
            case "cadastros": return "Cadastro de clientes e técnicos";
            default: return "Relatório de chamados";
        }
    }

    private void atualizarTela() {
        if ("painel".equals(atual)) {
            painel.atualizar();
        } else if (telas.get(atual) instanceof Atualizavel) {
            ((Atualizavel) telas.get(atual)).aoExibir();
        }
    }

    // ---------- Navegador ----------

    @Override
    public void irPara(String tela) {
        JComponent t = telas.get(tela);
        if (t == null) return;
        atual = tela;
        titulo.setText(tituloPadrao(tela));
        cartas.show(conteudo, tela);
        for (Map.Entry<String, ItemMenu> e : itens.entrySet()) e.getValue().setAtivo(e.getKey().equals(tela));
        if (t instanceof Atualizavel) ((Atualizavel) t).aoExibir();
    }

    @Override
    public void abrirAtendimento(Chamado chamado) {
        atendimento.setChamado(chamado);
        irPara("atendimento");
    }

    @Override public void setTitulo(String texto) { titulo.setText(texto); }
    @Override public Pessoa usuario() { return usuario; }
    @Override public ChamadoService chamados() { return chamados; }
    @Override public CadastroService cadastros() { return cadastros; }

    public String telaAtual() { return atual; }
    public PainelChamados getPainelChamados() { return painel; }
    public PainelAtendimento getAtendimento() { return atendimento; }
    public JComponent tela(String chave) { return telas.get(chave); }
    public boolean temTela(String chave) { return telas.containsKey(chave); }

    /** Botão do menu lateral, com destaque para a tela ativa e anel de foco branco. */
    private static class ItemMenu extends JButton {
        private static final long serialVersionUID = 1L;
        private boolean ativo;

        ItemMenu(String texto) {
            super(texto);
            setFont(Estilo.FONTE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(200, 40));
            getAccessibleContext().setAccessibleName(texto);
        }

        void setAtivo(boolean ativo) {
            this.ativo = ativo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (ativo) {
                g2.setColor(Estilo.PRIMARIA);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(0x22355A));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
            }
            if (isFocusOwner()) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);
            }
            g2.setFont(ativo ? Estilo.NEGRITO : Estilo.FONTE);
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("▪  " + getText(), 14, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
