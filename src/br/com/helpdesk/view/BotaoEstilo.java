package br.com.helpdesk.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import javax.swing.JButton;

/**
 * Botão desenhado conforme o guia de estilo: primário, secundário ou de perigo,
 * com anel de foco visível (acessibilidade) e aparência própria para o estado desabilitado.
 */
public class BotaoEstilo extends JButton {

    private static final long serialVersionUID = 1L;

    public enum Tipo { PRIMARIO, SECUNDARIO, PERIGO }

    private Tipo tipo;

    public BotaoEstilo(String texto, Tipo tipo) {
        super(texto);
        this.tipo = tipo;
        setFont(Estilo.NEGRITO);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getAccessibleContext().setAccessibleName(texto);
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 40, 40);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        Color fundo;
        Color texto;
        Color borda;
        if (!isEnabled()) {
            fundo = Estilo.DESABILITADO; texto = Estilo.TEXTO2; borda = Estilo.LINHA;
        } else if (tipo == Tipo.PRIMARIO) {
            fundo = getModel().isPressed() ? Estilo.PRIMARIA_ESCURA : (getModel().isRollover() ? new Color(0x2A5DB0) : Estilo.PRIMARIA);
            texto = Color.WHITE; borda = fundo;
        } else if (tipo == Tipo.PERIGO) {
            fundo = getModel().isPressed() ? new Color(0x8E1B12) : Estilo.PERIGO;
            texto = Color.WHITE; borda = fundo;
        } else {
            fundo = getModel().isRollover() ? Estilo.SELECAO : Color.WHITE;
            texto = Estilo.PRIMARIA; borda = Estilo.PRIMARIA;
        }
        g2.setColor(fundo);
        g2.fillRoundRect(1, 1, w - 3, h - 3, 10, 10);
        g2.setColor(borda);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);
        if (isFocusOwner()) {
            g2.setColor(Estilo.FOCO);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(2, 2, w - 5, h - 5, 10, 10);
        }
        g2.setFont(getFont());
        g2.setColor(texto);
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
