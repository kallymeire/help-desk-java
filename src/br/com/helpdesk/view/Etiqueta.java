package br.com.helpdesk.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Etiqueta (badge) com texto e cor para status e prioridade.
 */
public class Etiqueta extends JPanel {

    private static final long serialVersionUID = 1L;

    private String texto = "";
    private Color corTexto = Estilo.TEXTO;
    private Color corFundo = Estilo.FUNDO;

    public Etiqueta() {
        setFont(Estilo.PEQUENA_NEGRITO);
        setOpaque(false);
    }

    public Etiqueta(String texto, Color[] cores) {
        this();
        definir(texto, cores);
    }

    public void definir(String texto, Color[] cores) {
        this.texto = texto;
        this.corTexto = cores[0];
        this.corFundo = cores[1];
        getAccessibleContext().setAccessibleName(texto);
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(texto) + 22, 24);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(corFundo);
        g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
        g2.setColor(corTexto);
        g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, (w - fm.stringWidth(texto)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
