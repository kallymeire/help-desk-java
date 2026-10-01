package br.com.helpdesk.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import javax.swing.JTextField;

/**
 * Campo de texto com borda do guia de estilo, dica (texto de exemplo) e anel de foco.
 */
public class CampoTexto extends JTextField {

    private static final long serialVersionUID = 1L;

    private String dica = "";

    public CampoTexto(int colunas) {
        super(colunas);
        setFont(Estilo.FONTE);
        setForeground(Estilo.TEXTO);
        setBorder(Estilo.bordaCampo());
        setCaretColor(Estilo.TEXTO);
        setDisabledTextColor(Estilo.TEXTO2);
    }

    public void setDica(String dica) {
        this.dica = dica;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, 38);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 38);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (getText().isEmpty() && !dica.isEmpty()) {
            g2.setColor(Estilo.TEXTO2);
            g2.setFont(getFont().deriveFont(java.awt.Font.ITALIC));
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(dica, getInsets().left, y);
        }
        if (isFocusOwner()) {
            g2.setColor(Estilo.FOCO);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
        }
        g2.dispose();
    }
}
