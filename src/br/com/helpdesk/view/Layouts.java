package br.com.helpdesk.view;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Pequenos utilitários de layout usados pelas telas. */
final class Layouts {

    private Layouts() { }

    /** Impede que o BoxLayout estique o componente na vertical. */
    static <T extends JComponent> T fixarAltura(T c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    /** Área de texto com rolagem, borda padrão e anel de foco visível. */
    static JScrollPane areaTexto(final JTextArea area, int linhas) {
        area.setRows(linhas);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Estilo.FONTE);
        area.setForeground(Estilo.TEXTO);
        area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        final JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(Estilo.BORDA, 1));
        area.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                sp.setBorder(BorderFactory.createLineBorder(Estilo.FOCO, 3));
            }
            @Override public void focusLost(FocusEvent e) {
                sp.setBorder(BorderFactory.createLineBorder(Estilo.BORDA, 1));
            }
        });
        return sp;
    }
}
