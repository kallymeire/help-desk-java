package br.com.helpdesk.view;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

/**
 * Mensagens e confirmações em português (e seguras para rodar sem interface gráfica em testes).
 */
public final class Dialogos {

    private Dialogos() { }

    public static void info(Component pai, String mensagem) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[info] " + mensagem);
            return;
        }
        JOptionPane.showMessageDialog(pai, mensagem, "Help Desk", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void erro(Component pai, String mensagem) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[erro] " + mensagem);
            return;
        }
        JOptionPane.showMessageDialog(pai, mensagem, "Help Desk", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component pai, String mensagem, String botaoSim) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[confirmar] " + mensagem);
            return true;
        }
        Object[] opcoes = {botaoSim, "Cancelar"};
        int r = JOptionPane.showOptionDialog(pai, mensagem, "Confirmação", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[1]);
        return r == 0;
    }
}
