package br.com.helpdesk.view;

import br.com.helpdesk.model.Chamado;
import br.com.helpdesk.model.StatusChamado;
import br.com.helpdesk.model.Tecnico;
import br.com.helpdesk.service.ChamadoService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Tela 6 – Relatório de chamados (RF09): filtros, gráfico, resumo e exportação.
 */
public class PainelRelatorios extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Navegador nav;
    private final CampoTexto campoInicio = new CampoTexto(9);
    private final CampoTexto campoFim = new CampoTexto(9);
    private final JComboBox<Object> comboTecnico = new JComboBox<>();
    private final JComboBox<String> comboStatus = new JComboBox<>();
    private final JLabel msg = Estilo.mensagem();
    private final GraficoBarras grafico = new GraficoBarras();
    private final DefaultTableModel modelo = Estilo.modeloSomenteLeitura("Nº", "Título", "Técnico", "Status", "Aberto em");
    private final JTable tabela = new JTable(modelo);
    private final JLabel[] valoresResumo = new JLabel[8];
    private final String[] nomesResumo = {"Abertos", "Em atendimento", "Aguardando cliente", "Resolvidos", "Fechados",
        "Atrasados", "Tempo médio de atendimento", "Fechados dentro do prazo"};
    private List<Chamado> ultimo = new ArrayList<>();

    public PainelRelatorios(Navegador nav) {
        this.nav = nav;
        setBackground(Estilo.FUNDO);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        CartaoPanel filtros = new CartaoPanel(new BorderLayout());
        filtros.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        campos.setOpaque(false);
        campoInicio.setDica("dd/mm/aaaa");
        campoFim.setDica("dd/mm/aaaa");
        Estilo.estilizar(comboTecnico);
        Estilo.estilizar(comboStatus);
        comboTecnico.setPreferredSize(new Dimension(190, 36));
        comboStatus.setPreferredSize(new Dimension(170, 36));
        comboStatus.addItem("Todos");
        for (StatusChamado s : StatusChamado.values()) comboStatus.addItem(Estilo.texto(s));
        campos.add(Estilo.comRotulo("Período inicial", campoInicio));
        campos.add(Estilo.comRotulo("Período final", campoFim));
        campos.add(Estilo.comRotulo("Técnico", comboTecnico));
        campos.add(Estilo.comRotulo("Status", comboStatus));
        filtros.add(campos, BorderLayout.WEST);
        BotaoEstilo gerar = new BotaoEstilo("Gerar relatório", BotaoEstilo.Tipo.PRIMARIO);
        gerar.addActionListener(e -> gerar());
        JPanel pg = new JPanel(new BorderLayout());
        pg.setOpaque(false);
        pg.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
        pg.add(gerar, BorderLayout.CENTER);
        filtros.add(pg, BorderLayout.EAST);
        JPanel topo = new JPanel(new BorderLayout(0, 4));
        topo.setOpaque(false);
        topo.add(filtros, BorderLayout.CENTER);
        topo.add(msg, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        // esquerda: gráfico + tabela
        CartaoPanel esquerda = new CartaoPanel(new BorderLayout(0, 12));
        JPanel blocoGrafico = new JPanel(new BorderLayout(0, 8));
        blocoGrafico.setOpaque(false);
        blocoGrafico.add(Estilo.titulo("Chamados por técnico"), BorderLayout.NORTH);
        blocoGrafico.add(grafico, BorderLayout.CENTER);
        esquerda.add(blocoGrafico, BorderLayout.NORTH);
        JPanel blocoTabela = new JPanel(new BorderLayout(0, 8));
        blocoTabela.setOpaque(false);
        blocoTabela.add(Estilo.titulo("Chamados do período"), BorderLayout.NORTH);
        Estilo.estilizar(tabela);
        tabela.setRowHeight(32);
        Estilo.nome(tabela, "Chamados do período");
        blocoTabela.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        esquerda.add(blocoTabela, BorderLayout.CENTER);
        add(esquerda, BorderLayout.CENTER);

        // direita: resumo
        CartaoPanel direita = new CartaoPanel(null);
        direita.setLayout(new BoxLayout(direita, BoxLayout.Y_AXIS));
        direita.setPreferredSize(new Dimension(390, 100));
        JLabel tr = Estilo.titulo("Resumo");
        tr.setAlignmentX(Component.LEFT_ALIGNMENT);
        direita.add(tr);
        direita.add(Box.createVerticalStrut(8));
        for (int i = 0; i < nomesResumo.length; i++) {
            JPanel l = new JPanel(new BorderLayout());
            l.setOpaque(false);
            l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.LINHA));
            JLabel k = Estilo.apoio(nomesResumo[i]);
            k.setFont(Estilo.NEGRITO);
            valoresResumo[i] = new JLabel("0");
            valoresResumo[i].setFont(Estilo.NEGRITO);
            valoresResumo[i].setForeground(Estilo.TEXTO);
            l.add(k, BorderLayout.WEST);
            l.add(valoresResumo[i], BorderLayout.EAST);
            l.setPreferredSize(new Dimension(300, 40));
            l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            Estilo.nome(valoresResumo[i], nomesResumo[i]);
            direita.add(l);
        }
        direita.add(Box.createVerticalGlue());
        BotaoEstilo csv = new BotaoEstilo("Exportar CSV", BotaoEstilo.Tipo.SECUNDARIO);
        BotaoEstilo imprimir = new BotaoEstilo("Imprimir / PDF", BotaoEstilo.Tipo.SECUNDARIO);
        csv.addActionListener(e -> exportarCsv());
        imprimir.addActionListener(e -> imprimir());
        JPanel botoes = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        botoes.setOpaque(false);
        botoes.add(csv);
        botoes.add(imprimir);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        direita.add(Layouts.fixarAltura(botoes));
        add(direita, BorderLayout.EAST);
    }

    private void gerar() {
        Estilo.limpar(msg);
        LocalDate ini;
        LocalDate fim;
        try {
            ini = campoInicio.getText().trim().isEmpty() ? null : LocalDate.parse(campoInicio.getText().trim(), DATA);
            fim = campoFim.getText().trim().isEmpty() ? null : LocalDate.parse(campoFim.getText().trim(), DATA);
        } catch (DateTimeParseException ex) {
            Estilo.mostrarErro(msg, "Informe as datas no formato dd/mm/aaaa, por exemplo 29/09/2026.");
            return;
        }
        if (ini != null && fim != null && fim.isBefore(ini)) {
            Estilo.mostrarErro(msg, "A data final não pode ser anterior à data inicial.");
            return;
        }
        Object t = comboTecnico.getSelectedItem();
        Tecnico tec = t instanceof Tecnico ? (Tecnico) t : null;
        int si = comboStatus.getSelectedIndex();
        StatusChamado st = si <= 0 ? null : StatusChamado.values()[si - 1];
        ultimo = nav.chamados().filtrarRelatorio(ini, fim, tec, st);

        // gráfico
        List<String> nomes = new ArrayList<>();
        List<Integer> vals = new ArrayList<>();
        for (Tecnico x : nav.cadastros().listarTecnicos()) {
            if (tec != null && tec != x) continue;
            int n = 0;
            for (Chamado c : ultimo) if (x.equals(c.getTecnico())) n++;
            nomes.add(x.getNome());
            vals.add(n);
        }
        grafico.definir(nomes, vals);

        // tabela
        modelo.setRowCount(0);
        for (Chamado c : ultimo) {
            modelo.addRow(new Object[]{"#" + c.getId(), c.getTitulo(), c.getTecnico() == null ? "—" : c.getTecnico().getNome(),
                Estilo.texto(c.getStatus()), c.getDataAbertura().format(DATA_HORA)});
        }

        // resumo
        valoresResumo[0].setText(String.valueOf(ChamadoService.contar(ultimo, StatusChamado.ABERTO)));
        valoresResumo[1].setText(String.valueOf(ChamadoService.contar(ultimo, StatusChamado.EM_ATENDIMENTO)));
        valoresResumo[2].setText(String.valueOf(ChamadoService.contar(ultimo, StatusChamado.AGUARDANDO_CLIENTE)));
        valoresResumo[3].setText(String.valueOf(ChamadoService.contar(ultimo, StatusChamado.RESOLVIDO)));
        valoresResumo[4].setText(String.valueOf(ChamadoService.contar(ultimo, StatusChamado.FECHADO)));
        valoresResumo[5].setText(String.valueOf(ChamadoService.contarAtrasados(ultimo)));
        long fechados = 0;
        long noPrazo = 0;
        long minutos = 0;
        for (Chamado c : ultimo) {
            if (c.getStatus() == StatusChamado.FECHADO && c.getDataFechamento() != null) {
                fechados++;
                minutos += Duration.between(c.getDataAbertura(), c.getDataFechamento()).toMinutes();
                if (!c.getDataFechamento().isAfter(c.getPrazoLimite())) noPrazo++;
            }
        }
        if (fechados == 0) {
            valoresResumo[6].setText("—");
            valoresResumo[7].setText("—");
        } else {
            long media = minutos / fechados;
            valoresResumo[6].setText(media / 60 + " h " + media % 60 + " min");
            valoresResumo[7].setText(noPrazo * 100 / fechados + "%");
        }
        Estilo.mostrarSucesso(msg, ultimo.size() + (ultimo.size() == 1 ? " chamado encontrado." : " chamados encontrados."));
    }

    /** Monta o CSV (separado por ponto e vírgula, para abrir direto no Excel em português). */
    public String gerarCsv() {
        StringBuilder sb = new StringBuilder("Nº;Título;Cliente;Técnico;Prioridade;Status;Aberto em\n");
        for (Chamado c : ultimo) {
            sb.append(c.getId()).append(';').append(aspas(c.getTitulo())).append(';').append(aspas(c.getCliente().getNome())).append(';')
              .append(aspas(c.getTecnico() == null ? "" : c.getTecnico().getNome())).append(';')
              .append(Estilo.texto(c.getPrioridade())).append(';').append(Estilo.texto(c.getStatus())).append(';')
              .append(c.getDataAbertura().format(DATA_HORA)).append('\n');
        }
        return sb.toString();
    }

    private static String aspas(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private void exportarCsv() {
        if (ultimo.isEmpty()) {
            Estilo.mostrarErro(msg, "Gere o relatório antes de exportar.");
            return;
        }
        if (GraphicsEnvironment.isHeadless()) return;
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salvar relatório em CSV");
        fc.setSelectedFile(new File("relatorio_chamados.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] corpo = gerarCsv().getBytes(StandardCharsets.UTF_8);
            byte[] tudo = new byte[bom.length + corpo.length];
            System.arraycopy(bom, 0, tudo, 0, bom.length);
            System.arraycopy(corpo, 0, tudo, bom.length, corpo.length);
            Files.write(fc.getSelectedFile().toPath(), tudo);
            Estilo.mostrarSucesso(msg, "Arquivo salvo em " + fc.getSelectedFile().getName() + ".");
        } catch (IOException ex) {
            Estilo.mostrarErro(msg, "Não foi possível salvar o arquivo: " + ex.getMessage());
        }
    }

    private void imprimir() {
        if (ultimo.isEmpty()) {
            Estilo.mostrarErro(msg, "Gere o relatório antes de imprimir.");
            return;
        }
        if (GraphicsEnvironment.isHeadless()) return;
        try {
            tabela.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Relatório de chamados – Help Desk"), new MessageFormat("Página {0}"));
        } catch (PrinterException ex) {
            Estilo.mostrarErro(msg, "Não foi possível imprimir: " + ex.getMessage());
        }
    }

    @Override
    public void aoExibir() {
        comboTecnico.removeAllItems();
        comboTecnico.addItem("Todos");
        for (Tecnico t : nav.cadastros().listarTecnicos()) comboTecnico.addItem(t);
        LocalDate hoje = LocalDate.now();
        campoInicio.setText(hoje.minusDays(30).format(DATA));
        campoFim.setText(hoje.format(DATA));
        comboStatus.setSelectedIndex(0);
        gerar();
    }

    public List<Chamado> getUltimo() { return ultimo; }
}
