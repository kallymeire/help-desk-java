package br.com.helpdesk.view;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Gráfico de barras horizontais desenhado com Java 2D. O valor é escrito ao lado de cada barra
 * (a informação não depende só da cor ou do tamanho).
 */
public class GraficoBarras extends JPanel {

    private static final long serialVersionUID = 1L;

    private final List<String> nomes = new ArrayList<>();
    private final List<Integer> valores = new ArrayList<>();

    public GraficoBarras() {
        setOpaque(false);
    }

    public void definir(List<String> nomes, List<Integer> valores) {
        this.nomes.clear();
        this.valores.clear();
        this.nomes.addAll(nomes);
        this.valores.addAll(valores);
        StringBuilder sb = new StringBuilder("Chamados por técnico: ");
        for (int i = 0; i < nomes.size(); i++) {
            sb.append(nomes.get(i)).append(" ").append(valores.get(i)).append("; ");
        }
        getAccessibleContext().setAccessibleName(sb.toString());
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, Math.max(1, nomes.size()) * 40 + 8);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(Estilo.FONTE);
        FontMetrics fm = g2.getFontMetrics();
        if (nomes.isEmpty()) {
            g2.setColor(Estilo.TEXTO2);
            g2.drawString("Nenhum chamado no período.", 4, 24);
            g2.dispose();
            return;
        }
        int max = 1;
        for (int v : valores) max = Math.max(max, v);
        int xBarra = 150;
        int larguraMax = Math.max(50, getWidth() - xBarra - 60);
        for (int i = 0; i < nomes.size(); i++) {
            int y = 4 + i * 40;
            g2.setColor(Estilo.TEXTO);
            g2.drawString(nomes.get(i), 4, y + (32 - fm.getHeight()) / 2 + fm.getAscent());
            int w = valores.get(i) == 0 ? 3 : Math.max(6, valores.get(i) * larguraMax / max);
            g2.setColor(Estilo.PRIMARIA);
            g2.fillRoundRect(xBarra, y, w, 32, 6, 6);
            g2.setColor(Estilo.TEXTO);
            g2.setFont(Estilo.NEGRITO);
            g2.drawString(String.valueOf(valores.get(i)), xBarra + w + 8, y + (32 - fm.getHeight()) / 2 + fm.getAscent());
            g2.setFont(Estilo.FONTE);
        }
        g2.dispose();
    }
}
