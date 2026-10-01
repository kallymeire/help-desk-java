package br.com.helpdesk.view;

import br.com.helpdesk.model.Chamado;
import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Prioridade;
import br.com.helpdesk.model.StatusChamado;
import br.com.helpdesk.service.ChamadoService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Tela 2 – Painel de chamados (RF07 e RF08): indicadores, filtros e tabela.
 */
public class PainelChamados extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private final Navegador nav;
    private final JLabel kAbertos = valor();
    private final JLabel kAtendimento = valor();
    private final JLabel kAtrasados = valor();
    private final JLabel kFechados = valor();
    private final CampoTexto busca = new CampoTexto(24);
    private final JComboBox<String> comboStatus = new JComboBox<>();
    private final JComboBox<String> comboPrioridade = new JComboBox<>();
    private final DefaultTableModel modelo = Estilo.modeloSomenteLeitura("Nº", "Título", "Cliente", "Prioridade", "Status", "Técnico", "Prazo");
    private final JTable tabela = new JTable(modelo);
    private final JLabel rodape = Estilo.apoio(" ");
    private List<Chamado> exibidos = new ArrayList<>();
    private boolean carregando = false;

    public PainelChamados(Navegador nav) {
        this.nav = nav;
        setBackground(Estilo.FUNDO);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel topo = new JPanel();
        topo.setOpaque(false);
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));

        JPanel kpis = new JPanel(new GridLayout(1, 4, 16, 0));
        kpis.setOpaque(false);
        kpis.add(kpi("Abertos", kAbertos, null));
        kpis.add(kpi("Em atendimento", kAtendimento, null));
        kpis.add(kpi("Atrasados", kAtrasados, "⚠ prazo vencido"));
        kpis.add(kpi("Fechados hoje", kFechados, null));
        kpis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        kpis.setPreferredSize(new Dimension(100, 96));
        topo.add(kpis);
        topo.add(Box.createVerticalStrut(16));

        // filtros
        CartaoPanel filtros = new CartaoPanel(new BorderLayout(12, 0));
        filtros.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        busca.setDica("Buscar por nº, título ou cliente");
        Estilo.nome(busca, "Buscar chamado por número, título ou cliente");
        busca.setPreferredSize(new Dimension(320, 38));
        String[] st = {"Todos os status", "Aberto", "Em atendimento", "Aguardando cliente", "Resolvido", "Fechado"};
        for (String s : st) comboStatus.addItem(s);
        String[] pr = {"Todas as prioridades", "Baixa", "Média", "Alta", "Crítica"};
        for (String s : pr) comboPrioridade.addItem(s);
        Estilo.estilizar(comboStatus);
        Estilo.estilizar(comboPrioridade);
        Estilo.nome(comboStatus, "Filtrar por status");
        Estilo.nome(comboPrioridade, "Filtrar por prioridade");
        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        esq.setOpaque(false);
        esq.add(busca);
        esq.add(comboStatus);
        esq.add(comboPrioridade);
        BotaoEstilo novo = new BotaoEstilo("+ Novo chamado", BotaoEstilo.Tipo.PRIMARIO);
        novo.setToolTipText("Abrir novo chamado (Ctrl+N)");
        novo.addActionListener(e -> nav.irPara("novo"));
        filtros.add(esq, BorderLayout.WEST);
        filtros.add(novo, BorderLayout.EAST);
        filtros.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        filtros.setPreferredSize(new Dimension(100, 66));
        topo.add(filtros);
        add(topo, BorderLayout.NORTH);

        // tabela
        Estilo.estilizar(tabela);
        Estilo.nome(tabela, "Lista de chamados");
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(230);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(210);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(130);
        TableCellRenderer etiquetas = new EtiquetaRenderer();
        tabela.getColumnModel().getColumn(3).setCellRenderer(etiquetas);
        tabela.getColumnModel().getColumn(4).setCellRenderer(etiquetas);
        tabela.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) abrirSelecionado();
            }
        });
        tabela.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "abrir");
        tabela.getActionMap().put("abrir", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { abrirSelecionado(); }
        });

        CartaoPanel cartaoTabela = new CartaoPanel(new BorderLayout(0, 8));
        cartaoTabela.setBorder(BorderFactory.createEmptyBorder(8, 8, 10, 8));
        cartaoTabela.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        JPanel pe = new JPanel(new BorderLayout());
        pe.setOpaque(false);
        pe.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        pe.add(rodape, BorderLayout.WEST);
        pe.add(Estilo.apoio("Dê duplo clique (ou Enter) em um chamado para atender · F5 atualiza"), BorderLayout.EAST);
        cartaoTabela.add(pe, BorderLayout.SOUTH);
        add(cartaoTabela, BorderLayout.CENTER);

        busca.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recarregar(); }
            @Override public void removeUpdate(DocumentEvent e) { recarregar(); }
            @Override public void changedUpdate(DocumentEvent e) { recarregar(); }
        });
        comboStatus.addActionListener(e -> recarregar());
        comboPrioridade.addActionListener(e -> recarregar());
    }

    private static JLabel valor() {
        JLabel l = new JLabel("0");
        l.setFont(Estilo.GRANDE);
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    private static CartaoPanel kpi(String titulo, JLabel valor, String nota) {
        CartaoPanel c = new CartaoPanel(new BorderLayout());
        c.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        JLabel t = Estilo.rotulo(titulo);
        t.setForeground(Estilo.TEXTO2);
        c.add(t, BorderLayout.NORTH);
        c.add(valor, BorderLayout.CENTER);
        if (nota != null) {
            JLabel n = new JLabel(nota);
            n.setFont(Estilo.PEQUENA_NEGRITO);
            n.setForeground(new java.awt.Color(0xA11D1D));
            c.add(n, BorderLayout.EAST);
        }
        Estilo.nome(valor, titulo);
        return c;
    }

    private Cliente restricao() {
        return nav.usuario() instanceof Cliente ? (Cliente) nav.usuario() : null;
    }

    private void abrirSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha >= 0 && linha < exibidos.size()) {
            nav.abrirAtendimento(exibidos.get(linha));
        }
    }

    private void recarregar() {
        if (carregando) return;
        StatusChamado s = comboStatus.getSelectedIndex() <= 0 ? null : StatusChamado.values()[comboStatus.getSelectedIndex() - 1];
        Prioridade p = comboPrioridade.getSelectedIndex() <= 0 ? null : Prioridade.values()[comboPrioridade.getSelectedIndex() - 1];
        ChamadoService svc = nav.chamados();
        exibidos = svc.buscar(busca.getText(), s, p, restricao());
        modelo.setRowCount(0);
        for (Chamado c : exibidos) {
            modelo.addRow(new Object[]{"#" + c.getId(), c.getTitulo(), c.getCliente().getNome(), c.getPrioridade(), c.getStatus(),
                c.getTecnico() == null ? "—" : c.getTecnico().getNome(), c.getPrazoResumo()});
        }
        rodape.setText(exibidos.size() == 1 ? "Mostrando 1 chamado" : "Mostrando " + exibidos.size() + " chamados");
        List<Chamado> base = restricao() == null ? svc.listarTodos() : svc.listarDoCliente(restricao());
        kAbertos.setText(String.valueOf(ChamadoService.contar(base, StatusChamado.ABERTO)));
        kAtendimento.setText(String.valueOf(ChamadoService.contar(base, StatusChamado.EM_ATENDIMENTO)));
        kAtrasados.setText(String.valueOf(ChamadoService.contarAtrasados(base)));
        kFechados.setText(String.valueOf(ChamadoService.contarFechadosHoje(base)));
    }

    @Override
    public void aoExibir() {
        carregando = true;
        busca.setText("");
        comboStatus.setSelectedIndex(0);
        comboPrioridade.setSelectedIndex(0);
        carregando = false;
        recarregar();
    }

    /** Recarrega mantendo os filtros atuais (usado pelo F5). */
    public void atualizar() {
        recarregar();
    }

    public JTable getTabela() { return tabela; }
    public int linhasExibidas() { return exibidos.size(); }

    /** Desenha status e prioridade como etiquetas (cor + texto). */
    private static class EtiquetaRenderer implements TableCellRenderer {
        private final JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        private final Etiqueta etiqueta = new Etiqueta();

        EtiquetaRenderer() {
            painel.add(etiqueta);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            painel.setBackground(sel ? Estilo.SELECAO : Estilo.SUPERFICIE);
            if (v instanceof StatusChamado) {
                etiqueta.definir(Estilo.texto((StatusChamado) v), Estilo.cores((StatusChamado) v));
            } else if (v instanceof Prioridade) {
                etiqueta.definir(Estilo.texto((Prioridade) v), Estilo.cores((Prioridade) v));
            }
            return painel;
        }
    }
}
