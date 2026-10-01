package br.com.helpdesk.view;

import br.com.helpdesk.model.Prioridade;
import br.com.helpdesk.model.StatusChamado;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Cores, fontes e fábricas de componentes seguindo o guia de estilo da Etapa 2.
 * Todas as razões de contraste da paleta atendem à WCAG 2.1 AA.
 */
public final class Estilo {

    public static final Color PRIMARIA = new Color(0x1F4E9C);
    public static final Color PRIMARIA_ESCURA = new Color(0x163A73);
    public static final Color MENU = new Color(0x14233F);
    public static final Color FUNDO = new Color(0xF4F6FA);
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color TEXTO = new Color(0x1A1F2B);
    public static final Color TEXTO2 = new Color(0x4A5568);
    public static final Color BORDA = new Color(0x6B7686);
    public static final Color LINHA = new Color(0xD5DBE5);
    public static final Color PERIGO = new Color(0xB42318);
    public static final Color SUCESSO = new Color(0x1B6B3A);
    public static final Color FOCO = new Color(0x005FCC);
    public static final Color DESABILITADO = new Color(0xE6E9EE);
    public static final Color SELECAO = new Color(0xE3ECFA);

    public static final Font FONTE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font NEGRITO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font PEQUENA = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font PEQUENA_NEGRITO = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font SUBTITULO = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font GRANDE = new Font("Segoe UI", Font.BOLD, 28);

    private Estilo() { }

    /** Cores {texto, fundo} do status. */
    public static Color[] cores(StatusChamado s) {
        switch (s) {
            case ABERTO: return new Color[]{new Color(0x1F4E9C), new Color(0xE3ECFA)};
            case EM_ATENDIMENTO: return new Color[]{new Color(0x7A4A00), new Color(0xFFF0D1)};
            case AGUARDANDO_CLIENTE: return new Color[]{new Color(0x5B2A86), new Color(0xEFE4F8)};
            case RESOLVIDO: return new Color[]{new Color(0x1B6B3A), new Color(0xDDF3E4)};
            default: return new Color[]{new Color(0x3D4654), new Color(0xE6E9EE)};
        }
    }

    /** Cores {texto, fundo} da prioridade. */
    public static Color[] cores(Prioridade p) {
        switch (p) {
            case CRITICA: return new Color[]{new Color(0xA11D1D), new Color(0xFBE0E0)};
            case ALTA: return new Color[]{new Color(0x7A4A00), new Color(0xFFF0D1)};
            case MEDIA: return new Color[]{new Color(0x1F4E9C), new Color(0xE3ECFA)};
            default: return new Color[]{new Color(0x3D4654), new Color(0xE6E9EE)};
        }
    }

    public static String texto(StatusChamado s) {
        switch (s) {
            case ABERTO: return "Aberto";
            case EM_ATENDIMENTO: return "Em atendimento";
            case AGUARDANDO_CLIENTE: return "Aguardando cliente";
            case RESOLVIDO: return "Resolvido";
            default: return "Fechado";
        }
    }

    public static String texto(Prioridade p) {
        switch (p) {
            case CRITICA: return "Crítica";
            case ALTA: return "Alta";
            case MEDIA: return "Média";
            default: return "Baixa";
        }
    }

    // ---------- fábricas de componentes ----------

    public static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(NEGRITO);
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel apoio(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(PEQUENA);
        l.setForeground(TEXTO2);
        return l;
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(SUBTITULO);
        l.setForeground(TEXTO);
        return l;
    }

    /** Rótulo de mensagem (erro ou sucesso), sempre com ícone + texto (não depende só da cor). */
    public static JLabel mensagem() {
        JLabel l = new JLabel(" ");
        l.setFont(NEGRITO);
        l.setForeground(PERIGO);
        return l;
    }

    public static void mostrarErro(JLabel l, String texto) {
        l.setForeground(PERIGO);
        l.setText("⚠ " + texto);
    }

    public static void mostrarSucesso(JLabel l, String texto) {
        l.setForeground(SUCESSO);
        l.setText("✔ " + texto);
    }

    public static void limpar(JLabel l) {
        l.setText(" ");
    }

    public static Border bordaCampo() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA, 1), BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    public static void estilizar(JComboBox<?> combo) {
        combo.setFont(FONTE);
        combo.setBackground(SUPERFICIE);
        combo.setForeground(TEXTO);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 36));
    }

    /** Define o nome acessível lido por leitores de tela. */
    public static void nome(JComponent c, String nome) {
        c.getAccessibleContext().setAccessibleName(nome);
    }

    /** Rótulo + componente lado a lado na vertical, com setLabelFor. */
    public static JPanel comRotulo(String rotulo, JComponent campo) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = rotulo(rotulo);
        l.setLabelFor(campo);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(campo);
        nome(campo, rotulo.replace(" *", ""));
        return p;
    }

    public static JPanel linhaFlow(int align, int gap) {
        JPanel p = new JPanel(new FlowLayout(align, gap, 0));
        p.setOpaque(false);
        return p;
    }

    public static JScrollPane rolagem(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(LINHA));
        sp.getViewport().setBackground(SUPERFICIE);
        return sp;
    }

    // ---------- tabelas ----------

    public static DefaultTableModel modeloSomenteLeitura(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static void estilizar(JTable t) {
        t.setFont(FONTE);
        t.setForeground(TEXTO);
        t.setBackground(SUPERFICIE);
        t.setRowHeight(40);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(LINHA);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(SELECAO);
        t.setSelectionForeground(TEXTO);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 36));
        h.setDefaultRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = new JLabel(String.valueOf(v));
                l.setOpaque(true);
                l.setFont(NEGRITO);
                l.setForeground(TEXTO);
                l.setBackground(FUNDO);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, LINHA), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                return l;
            }
        });
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tb, v, sel, false, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(SwingConstants.LEFT);
                String s = String.valueOf(v);
                if (s.startsWith("⚠")) {
                    setForeground(PERIGO);
                    setFont(NEGRITO);
                } else {
                    setForeground(TEXTO);
                    setFont(FONTE);
                }
                return this;
            }
        });
    }
}
