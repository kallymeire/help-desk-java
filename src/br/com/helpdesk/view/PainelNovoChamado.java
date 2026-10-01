package br.com.helpdesk.view;

import br.com.helpdesk.model.Chamado;
import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Prioridade;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Tela 3 – Abertura de chamado (RF03).
 */
public class PainelNovoChamado extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private final Navegador nav;
    private final JComboBox<Cliente> comboCliente = new JComboBox<>();
    private final CampoTexto campoContato = new CampoTexto(20);
    private final CampoTexto campoTitulo = new CampoTexto(30);
    private final JTextArea areaDescricao = new JTextArea();
    private final JRadioButton[] radios = new JRadioButton[Prioridade.values().length];
    private final JPanel[] cartoes = new JPanel[Prioridade.values().length];
    private final JLabel erroTitulo = Estilo.mensagem();
    private final JLabel erroDescricao = Estilo.mensagem();
    private final JLabel erroGeral = Estilo.mensagem();

    public PainelNovoChamado(Navegador nav) {
        this.nav = nav;
        setBackground(Estilo.FUNDO);
        setLayout(new BorderLayout(16, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        CartaoPanel form = new CartaoPanel(null);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel t = Estilo.titulo("Descreva o problema");
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(t);
        form.add(Box.createVerticalStrut(14));

        Estilo.estilizar(comboCliente);
        comboCliente.addActionListener(e -> atualizarContato());
        campoContato.setEditable(false);
        campoContato.setBackground(Estilo.FUNDO);
        JPanel linha1 = new JPanel(new GridLayout(1, 2, 16, 0));
        linha1.setOpaque(false);
        linha1.add(Estilo.comRotulo("Cliente *", comboCliente));
        linha1.add(Estilo.comRotulo("Setor / telefone", campoContato));
        linha1.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Layouts.fixarAltura(linha1));
        form.add(Box.createVerticalStrut(12));

        campoTitulo.setDica("Ex.: Computador sem acesso à rede");
        JPanel pTitulo = Estilo.comRotulo("Título *", campoTitulo);
        pTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Layouts.fixarAltura(pTitulo));
        erroTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(erroTitulo);
        form.add(Box.createVerticalStrut(6));

        JLabel lp = Estilo.rotulo("Prioridade *");
        lp.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lp);
        form.add(Box.createVerticalStrut(4));
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        Prioridade[] ps = Prioridade.values();
        for (int i = 0; i < ps.length; i++) {
            final int idx = i;
            JRadioButton rb = new JRadioButton(Estilo.texto(ps[i]));
            rb.setFont(Estilo.NEGRITO);
            rb.setForeground(Estilo.TEXTO);
            rb.setOpaque(false);
            Estilo.nome(rb, "Prioridade " + Estilo.texto(ps[i]) + ", prazo de " + ps[i].getPrazoHoras() + " horas");
            radios[i] = rb;
            grupo.add(rb);
            JPanel c = new JPanel(new BorderLayout());
            c.add(rb, BorderLayout.NORTH);
            JLabel prazo = Estilo.apoio("prazo " + ps[i].getPrazoHoras() + " h");
            prazo.setBorder(BorderFactory.createEmptyBorder(0, 26, 4, 0));
            c.add(prazo, BorderLayout.SOUTH);
            c.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { radios[idx].doClick(); }
            });
            rb.addItemListener(e -> destacar());
            cartoes[i] = c;
            cards.add(c);
        }
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Layouts.fixarAltura(cards));
        form.add(Box.createVerticalStrut(12));

        JScrollPane sp = Layouts.areaTexto(areaDescricao, 6);
        JPanel pDesc = Estilo.comRotulo("Descrição *", sp);
        Estilo.nome(areaDescricao, "Descrição do problema");
        pDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(pDesc);
        erroDescricao.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(erroDescricao);
        JLabel obrig = Estilo.apoio("* campos obrigatórios");
        obrig.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(obrig);
        form.add(Box.createVerticalStrut(8));
        erroGeral.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(erroGeral);

        BotaoEstilo cancelar = new BotaoEstilo("Cancelar", BotaoEstilo.Tipo.SECUNDARIO);
        BotaoEstilo abrir = new BotaoEstilo("Abrir chamado", BotaoEstilo.Tipo.PRIMARIO);
        cancelar.addActionListener(e -> nav.irPara("painel"));
        abrir.addActionListener(e -> abrir());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        botoes.setOpaque(false);
        botoes.add(cancelar);
        botoes.add(abrir);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Layouts.fixarAltura(botoes));
        add(form, BorderLayout.CENTER);

        // painel lateral de prazos
        CartaoPanel sla = new CartaoPanel(null);
        sla.setLayout(new BoxLayout(sla, BoxLayout.Y_AXIS));
        JLabel st = Estilo.titulo("Prazos de atendimento");
        st.setAlignmentX(Component.LEFT_ALIGNMENT);
        sla.add(st);
        sla.add(Box.createVerticalStrut(12));
        for (Prioridade p : ps) {
            JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            l.setOpaque(false);
            l.add(new Etiqueta(Estilo.texto(p), Estilo.cores(p)));
            l.add(Box.createHorizontalStrut(12));
            l.add(new JLabel("até " + p.getPrazoHoras() + " horas") {
                private static final long serialVersionUID = 1L;
                { setFont(Estilo.FONTE); setForeground(Estilo.TEXTO); }
            });
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            sla.add(Layouts.fixarAltura(l));
            sla.add(Box.createVerticalStrut(10));
        }
        JLabel nota = new JLabel("<html><body style='width:240px'>Chamados de prioridade alta ou crítica são atendidos por técnicos de nível 2 ou 3.</body></html>");
        nota.setFont(Estilo.PEQUENA);
        nota.setForeground(Estilo.TEXTO2);
        nota.setAlignmentX(Component.LEFT_ALIGNMENT);
        sla.add(nota);
        sla.add(Box.createVerticalGlue());
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setOpaque(false);
        lateral.add(sla, BorderLayout.NORTH);
        lateral.setPreferredSize(new Dimension(300, 100));
        add(lateral, BorderLayout.EAST);
    }

    private void destacar() {
        for (int i = 0; i < radios.length; i++) {
            boolean sel = radios[i].isSelected();
            cartoes[i].setBackground(sel ? Estilo.SELECAO : Estilo.SUPERFICIE);
            cartoes[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(sel ? Estilo.PRIMARIA : Estilo.BORDA, sel ? 2 : 1),
                    BorderFactory.createEmptyBorder(4, 4, 2, 4)));
        }
    }

    private void atualizarContato() {
        Cliente c = (Cliente) comboCliente.getSelectedItem();
        campoContato.setText(c == null ? "" : c.getSetor() + " · " + c.getTelefone());
    }

    private void abrir() {
        Estilo.limpar(erroTitulo);
        Estilo.limpar(erroDescricao);
        Estilo.limpar(erroGeral);
        boolean ok = true;
        if (campoTitulo.getText().trim().isEmpty()) {
            Estilo.mostrarErro(erroTitulo, "Informe o título do chamado.");
            campoTitulo.requestFocusInWindow();
            ok = false;
        }
        if (areaDescricao.getText().trim().isEmpty()) {
            Estilo.mostrarErro(erroDescricao, "Descreva o problema para que o técnico possa ajudar.");
            if (ok) areaDescricao.requestFocusInWindow();
            ok = false;
        }
        if (!ok) return;
        Prioridade prioridade = Prioridade.MEDIA;
        for (int i = 0; i < radios.length; i++) {
            if (radios[i].isSelected()) prioridade = Prioridade.values()[i];
        }
        try {
            Chamado c = nav.chamados().abrirChamado((Cliente) comboCliente.getSelectedItem(),
                    campoTitulo.getText(), areaDescricao.getText(), prioridade);
            Dialogos.info(this, "Chamado #" + c.getId() + " aberto com sucesso.\nPrazo limite de atendimento: "
                    + c.getPrazoLimite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")) + ".");
            nav.irPara("painel");
        } catch (IllegalArgumentException ex) {
            Estilo.mostrarErro(erroGeral, ex.getMessage());
        }
    }

    @Override
    public void aoExibir() {
        comboCliente.removeAllItems();
        for (Cliente c : nav.cadastros().listarClientes()) comboCliente.addItem(c);
        boolean ehCliente = nav.usuario() instanceof Cliente;
        if (ehCliente) comboCliente.setSelectedItem(nav.usuario());
        comboCliente.setEnabled(!ehCliente);
        atualizarContato();
        campoTitulo.setText("");
        areaDescricao.setText("");
        radios[Prioridade.MEDIA.ordinal()].setSelected(true);
        destacar();
        Estilo.limpar(erroTitulo);
        Estilo.limpar(erroDescricao);
        Estilo.limpar(erroGeral);
        campoTitulo.requestFocusInWindow();
    }
}
