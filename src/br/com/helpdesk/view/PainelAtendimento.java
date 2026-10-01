package br.com.helpdesk.view;

import br.com.helpdesk.model.Chamado;
import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Interacao;
import br.com.helpdesk.model.StatusChamado;
import br.com.helpdesk.model.Tecnico;
import br.com.helpdesk.service.ChamadoService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Tela 4 – Atendimento do chamado (RF04, RF05 e RF06).
 */
public class PainelAtendimento extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String SEM_TECNICO = "— sem técnico —";

    private final Navegador nav;
    private Chamado atual;
    private boolean carregando = false;

    private final JComboBox<Chamado> comboChamado = new JComboBox<>();
    private final JLabel lblTitulo = Estilo.titulo(" ");
    private final Etiqueta etStatus = new Etiqueta();
    private final Etiqueta etPrioridade = new Etiqueta();
    private final JLabel vCliente = valor();
    private final JLabel vAberto = valor();
    private final JLabel vPrazo = valor();
    private final JLabel vSituacao = valor();
    private final JComboBox<Object> comboTecnico = new JComboBox<>();
    private final JComboBox<StatusChamado> comboStatus = new JComboBox<>(StatusChamado.values());
    private final JTextArea descricao = new JTextArea();
    private final BotaoEstilo aplicar = new BotaoEstilo("Aplicar alterações", BotaoEstilo.Tipo.SECUNDARIO);
    private final JLabel msg = Estilo.mensagem();
    private final JPanel historico = new JPanel();
    private final JScrollPane rolagemHistorico;
    private final JTextArea andamento = new JTextArea();
    private final BotaoEstilo enviar = new BotaoEstilo("Enviar", BotaoEstilo.Tipo.PRIMARIO);
    private final BotaoEstilo resolver = new BotaoEstilo("Marcar como resolvido", BotaoEstilo.Tipo.SECUNDARIO);
    private final BotaoEstilo fechar = new BotaoEstilo("Fechar chamado", BotaoEstilo.Tipo.PRIMARIO);
    private final JLabel notaFechar = Estilo.apoio(" ");
    private final JLabel rotAndamento = Estilo.rotulo("Registrar andamento");

    public PainelAtendimento(Navegador nav) {
        this.nav = nav;
        setBackground(Estilo.FUNDO);
        setLayout(new BorderLayout(16, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ----- coluna esquerda: detalhes -----
        CartaoPanel esquerda = new CartaoPanel(null);
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.Y_AXIS));
        esquerda.setPreferredSize(new Dimension(400, 100));
        Estilo.estilizar(comboChamado);
        comboChamado.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v, i, s, f);
            }
        });
        comboChamado.addActionListener(e -> {
            if (!carregando && comboChamado.getSelectedItem() != null) {
                atual = (Chamado) comboChamado.getSelectedItem();
                carregar();
            }
        });
        JPanel pSel = Estilo.comRotulo("Chamado", comboChamado);
        pSel.setAlignmentX(Component.LEFT_ALIGNMENT);
        esquerda.add(Layouts.fixarAltura(pSel));
        esquerda.add(Box.createVerticalStrut(14));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        esquerda.add(lblTitulo);
        esquerda.add(Box.createVerticalStrut(6));
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badges.setOpaque(false);
        badges.add(etStatus);
        badges.add(Box.createHorizontalStrut(8));
        badges.add(etPrioridade);
        badges.setAlignmentX(Component.LEFT_ALIGNMENT);
        esquerda.add(Layouts.fixarAltura(badges));
        esquerda.add(Box.createVerticalStrut(10));
        esquerda.add(linha("Cliente", vCliente));
        esquerda.add(linha("Aberto em", vAberto));
        esquerda.add(linha("Prazo limite", vPrazo));
        esquerda.add(linha("Situação do prazo", vSituacao));
        esquerda.add(Box.createVerticalStrut(10));

        comboTecnico.setFont(Estilo.FONTE);
        comboTecnico.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                String t = v instanceof Tecnico ? ((Tecnico) v).getNome() + " (N" + ((Tecnico) v).getNivel() + ")" : String.valueOf(v);
                return super.getListCellRendererComponent(l, t, i, s, f);
            }
        });
        Estilo.estilizar(comboTecnico);
        JPanel pTec = Estilo.comRotulo("Técnico responsável", comboTecnico);
        pTec.setAlignmentX(Component.LEFT_ALIGNMENT);
        esquerda.add(Layouts.fixarAltura(pTec));
        esquerda.add(Box.createVerticalStrut(10));
        comboStatus.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v instanceof StatusChamado ? Estilo.texto((StatusChamado) v) : v, i, s, f);
            }
        });
        Estilo.estilizar(comboStatus);
        JPanel pSt = Estilo.comRotulo("Status", comboStatus);
        pSt.setAlignmentX(Component.LEFT_ALIGNMENT);
        esquerda.add(Layouts.fixarAltura(pSt));
        esquerda.add(Box.createVerticalStrut(10));
        aplicar.setAlignmentX(Component.LEFT_ALIGNMENT);
        aplicar.addActionListener(e -> aplicarAlteracoes());
        esquerda.add(aplicar);
        esquerda.add(Box.createVerticalStrut(10));
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        esquerda.add(msg);
        esquerda.add(Box.createVerticalStrut(8));
        JLabel rd = Estilo.rotulo("Descrição do cliente");
        rd.setAlignmentX(Component.LEFT_ALIGNMENT);
        esquerda.add(rd);
        esquerda.add(Box.createVerticalStrut(4));
        descricao.setEditable(false);
        descricao.setLineWrap(true);
        descricao.setWrapStyleWord(true);
        descricao.setOpaque(false);
        descricao.setFont(Estilo.FONTE);
        descricao.setForeground(Estilo.TEXTO2);
        descricao.setFocusable(true);
        Estilo.nome(descricao, "Descrição do cliente");
        descricao.setAlignmentX(Component.LEFT_ALIGNMENT);
        esquerda.add(descricao);
        esquerda.add(Box.createVerticalGlue());
        add(esquerda, BorderLayout.WEST);

        // ----- coluna direita: histórico e ações -----
        CartaoPanel direita = new CartaoPanel(new BorderLayout(0, 10));
        JLabel th = Estilo.titulo("Histórico de andamento");
        direita.add(th, BorderLayout.NORTH);
        historico.setLayout(new BoxLayout(historico, BoxLayout.Y_AXIS));
        historico.setOpaque(false);
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(historico, BorderLayout.NORTH);
        rolagemHistorico = Estilo.rolagem(envoltorio);
        rolagemHistorico.getViewport().setBackground(Estilo.SUPERFICIE);
        rolagemHistorico.setBorder(BorderFactory.createEmptyBorder());
        rolagemHistorico.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Estilo.nome(rolagemHistorico, "Histórico de andamento do chamado");
        direita.add(rolagemHistorico, BorderLayout.CENTER);

        JPanel base = new JPanel();
        base.setOpaque(false);
        base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
        rotAndamento.setAlignmentX(Component.LEFT_ALIGNMENT);
        rotAndamento.setLabelFor(andamento);
        base.add(rotAndamento);
        base.add(Box.createVerticalStrut(4));
        JScrollPane spA = Layouts.areaTexto(andamento, 4);
        Estilo.nome(andamento, "Registrar andamento");
        spA.setAlignmentX(Component.LEFT_ALIGNMENT);
        base.add(spA);
        base.add(Box.createVerticalStrut(8));
        enviar.setToolTipText("Enviar andamento (Ctrl+Enter)");
        enviar.addActionListener(e -> enviarAndamento());
        JPanel pEnviar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pEnviar.setOpaque(false);
        pEnviar.add(enviar);
        pEnviar.setAlignmentX(Component.LEFT_ALIGNMENT);
        base.add(Layouts.fixarAltura(pEnviar));
        base.add(Box.createVerticalStrut(12));
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        acoes.setOpaque(false);
        resolver.addActionListener(e -> marcarResolvido());
        fechar.addActionListener(e -> fecharChamado());
        acoes.add(resolver);
        acoes.add(fechar);
        acoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        base.add(Layouts.fixarAltura(acoes));
        base.add(Box.createVerticalStrut(6));
        notaFechar.setAlignmentX(Component.LEFT_ALIGNMENT);
        base.add(notaFechar);
        direita.add(base, BorderLayout.SOUTH);
        add(direita, BorderLayout.CENTER);

        andamento.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "enviar");
        andamento.getActionMap().put("enviar", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { enviarAndamento(); }
        });
    }

    private static JLabel valor() {
        JLabel l = new JLabel(" ");
        l.setFont(Estilo.FONTE);
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    private static JPanel linha(String rotulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel r = Estilo.apoio(rotulo);
        r.setFont(Estilo.PEQUENA_NEGRITO);
        r.setPreferredSize(new Dimension(130, 24));
        r.setLabelFor(valor);
        p.add(r, BorderLayout.WEST);
        p.add(valor, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        Estilo.nome(valor, rotulo);
        return p;
    }

    /** Define qual chamado será exibido na próxima vez que a tela aparecer. */
    public void setChamado(Chamado c) {
        this.atual = c;
    }

    @Override
    public void aoExibir() {
        List<Chamado> visiveis = visiveis();
        carregando = true;
        comboChamado.removeAllItems();
        for (Chamado c : visiveis) comboChamado.addItem(c);
        if (atual == null || !visiveis.contains(atual)) {
            atual = null;
            for (Chamado c : visiveis) {
                if (c.getStatus() != StatusChamado.FECHADO) { atual = c; break; }
            }
            if (atual == null && !visiveis.isEmpty()) atual = visiveis.get(0);
        }
        comboChamado.setSelectedItem(atual);
        carregando = false;
        Estilo.limpar(msg);
        carregar();
    }

    private List<Chamado> visiveis() {
        ChamadoService svc = nav.chamados();
        List<Chamado> lista = nav.usuario() instanceof Cliente
                ? svc.listarDoCliente((Cliente) nav.usuario()) : svc.listarTodos();
        lista.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return lista;
    }

    private void carregar() {
        carregando = true;
        boolean ehCliente = nav.usuario() instanceof Cliente;
        if (atual != null) {
            // traz do banco o estado mais recente, com o histórico completo
            Chamado atualizado = nav.chamados().obter(atual.getId());
            if (atualizado != null) atual = atualizado;
        }
        if (atual == null) {
            nav.setTitulo("Atendimento");
            lblTitulo.setText("Nenhum chamado disponível");
            historico.removeAll();
            for (JComponent c : new JComponent[]{comboTecnico, comboStatus, aplicar, andamento, enviar, resolver, fechar}) c.setEnabled(false);
            notaFechar.setText(" ");
            descricao.setText("");
            carregando = false;
            revalidate();
            repaint();
            return;
        }
        Chamado c = atual;
        nav.setTitulo("Atendimento do chamado #" + c.getId());
        lblTitulo.setText(c.getTitulo());
        etStatus.definir(Estilo.texto(c.getStatus()), Estilo.cores(c.getStatus()));
        etPrioridade.definir(Estilo.texto(c.getPrioridade()), Estilo.cores(c.getPrioridade()));
        vCliente.setText(c.getCliente().getNome() + " · " + c.getCliente().getSetor());
        vAberto.setText(c.getDataAbertura().format(DATA_HORA));
        vPrazo.setText(c.getPrazoLimite().format(DATA_HORA));
        vSituacao.setForeground(Estilo.TEXTO);
        vSituacao.setFont(Estilo.FONTE);
        if (c.getStatus() == StatusChamado.FECHADO) {
            vSituacao.setText("Fechado em " + c.getDataFechamento().format(DATA_HORA));
        } else if (c.getStatus() == StatusChamado.RESOLVIDO) {
            vSituacao.setText("Resolvido");
        } else if (c.estaAtrasado()) {
            vSituacao.setText("⚠ Atrasado " + c.getPrazoResumo().substring(2));
            vSituacao.setForeground(new java.awt.Color(0xA11D1D));
            vSituacao.setFont(Estilo.NEGRITO);
        } else {
            vSituacao.setText("Vence " + c.getPrazoResumo());
        }
        comboTecnico.removeAllItems();
        comboTecnico.addItem(SEM_TECNICO);
        for (Tecnico t : nav.cadastros().listarTecnicos()) comboTecnico.addItem(t);
        comboTecnico.setSelectedItem(c.getTecnico() == null ? SEM_TECNICO : c.getTecnico());
        comboStatus.setSelectedItem(c.getStatus());
        descricao.setText(c.getDescricao());
        reconstruirHistorico();
        carregando = false;
        atualizarEstado(ehCliente);
        revalidate();
        repaint();
    }

    private void atualizarEstado(boolean ehCliente) {
        boolean fechado = atual.getStatus() == StatusChamado.FECHADO;
        boolean equipe = !ehCliente && !fechado;
        comboTecnico.setEnabled(equipe);
        comboStatus.setEnabled(equipe);
        aplicar.setVisible(!ehCliente);
        resolver.setVisible(!ehCliente);
        fechar.setVisible(!ehCliente);
        aplicar.setEnabled(equipe);
        resolver.setEnabled(equipe && atual.getStatus() != StatusChamado.RESOLVIDO);
        andamento.setEnabled(!fechado);
        enviar.setEnabled(!fechado);
        rotAndamento.setText(ehCliente ? "Responder ao técnico" : "Registrar andamento");
        boolean pode = nav.chamados().podeFechar(atual);
        fechar.setEnabled(equipe && pode);
        if (ehCliente) {
            notaFechar.setText(fechado ? "Este chamado foi fechado." : " ");
        } else if (fechado) {
            notaFechar.setText("Chamado fechado: não é possível fazer novas alterações.");
        } else if (pode) {
            notaFechar.setText("O chamado está resolvido e com solução registrada. Já é possível fechá-lo.");
        } else {
            notaFechar.setText("Fechar só é possível após marcar como resolvido e registrar a solução.");
        }
    }

    private void reconstruirHistorico() {
        historico.removeAll();
        for (Interacao i : atual.getInteracoes()) {
            JPanel item = new JPanel(new BorderLayout(0, 4));
            item.setBackground(Estilo.FUNDO);
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Estilo.LINHA), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            JLabel autor = new JLabel(i.getAutor().getNome() + " (" + i.getAutor().getPerfil() + ")");
            autor.setFont(Estilo.PEQUENA_NEGRITO);
            autor.setForeground(Estilo.TEXTO);
            JLabel hora = Estilo.apoio(i.getDataHora().format(DATA_HORA));
            JPanel topo = new JPanel(new BorderLayout());
            topo.setOpaque(false);
            topo.add(autor, BorderLayout.WEST);
            topo.add(hora, BorderLayout.EAST);
            String html = i.getTexto().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
            JLabel texto = new JLabel("<html><body style='width:360px'>" + html + "</body></html>");
            texto.setFont(Estilo.FONTE);
            texto.setForeground(Estilo.TEXTO);
            item.add(topo, BorderLayout.NORTH);
            item.add(texto, BorderLayout.CENTER);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            historico.add(item);
            historico.add(Box.createVerticalStrut(8));
        }
        historico.revalidate();
        SwingUtilities.invokeLater(() -> rolagemHistorico.getVerticalScrollBar().setValue(rolagemHistorico.getVerticalScrollBar().getMaximum()));
    }

    private void aplicarAlteracoes() {
        if (atual == null || carregando) return;
        Object t = comboTecnico.getSelectedItem();
        StatusChamado novo = (StatusChamado) comboStatus.getSelectedItem();
        boolean statusMudou = novo != atual.getStatus();
        boolean tecnicoMudou = t instanceof Tecnico && !t.equals(atual.getTecnico());
        if (!statusMudou && !tecnicoMudou) {
            Estilo.mostrarErro(msg, "Nenhuma alteração para salvar.");
            return;
        }
        try {
            if (tecnicoMudou) nav.chamados().atribuirTecnico(atual, (Tecnico) t);
            if (statusMudou) nav.chamados().alterarStatus(atual, novo);
            carregar();
            Estilo.mostrarSucesso(msg, "Alterações salvas.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            carregar();
            Estilo.mostrarErro(msg, ex.getMessage());
        }
    }

    private void enviarAndamento() {
        if (atual == null || !enviar.isEnabled()) return;
        try {
            nav.chamados().registrarInteracao(atual, nav.usuario(), andamento.getText());
            andamento.setText("");
            carregar();
            Estilo.mostrarSucesso(msg, "Andamento registrado.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            Estilo.mostrarErro(msg, ex.getMessage());
            andamento.requestFocusInWindow();
        }
    }

    private void marcarResolvido() {
        if (atual == null) return;
        try {
            nav.chamados().alterarStatus(atual, StatusChamado.RESOLVIDO);
            carregar();
            Estilo.mostrarSucesso(msg, "Marcado como resolvido. Registre a solução e feche o chamado.");
        } catch (IllegalStateException ex) {
            Estilo.mostrarErro(msg, ex.getMessage());
        }
    }

    private void fecharChamado() {
        if (atual == null) return;
        if (!Dialogos.confirmar(this, "Fechar o chamado #" + atual.getId() + "? Depois de fechado, ele não poderá ser alterado.", "Fechar chamado")) {
            return;
        }
        try {
            nav.chamados().fecharChamado(atual);
            carregar();
            Estilo.mostrarSucesso(msg, "Chamado fechado.");
        } catch (IllegalStateException ex) {
            Estilo.mostrarErro(msg, ex.getMessage());
        }
    }

    public Chamado getAtual() { return atual; }
}
