package br.com.helpdesk;

import br.com.helpdesk.dao.AcessoDadosException;
import br.com.helpdesk.dao.ConexaoMySQL;
import br.com.helpdesk.view.Dialogos;
import br.com.helpdesk.view.Estilo;
import br.com.helpdesk.view.JanelaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada do Sistema de Chamados Help Desk (Projeto Integrador – Etapa 4).
 * Antes de abrir a tela de login, confere se o banco de dados MySQL está acessível.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarVisual();
            tratarErrosInesperados();
            try {
                ConexaoMySQL.testar();
            } catch (AcessoDadosException e) {
                Dialogos.erro(null, "Não foi possível conectar ao banco de dados MySQL.\n\n" + e.getMessage()
                        + "\n\nConfira:\n1) o MySQL está ligado;\n2) o script sql/helpdesk_banco.sql foi executado no MySQL Workbench;"
                        + "\n3) usuário e senha estão corretos em config/banco.properties;"
                        + "\n4) o driver MySQL Connector/J está nas bibliotecas do projeto.");
                System.exit(1);
                return;
            }
            new JanelaPrincipal().setVisible(true);
        });
    }

    /** Mostra uma mensagem amigável se o banco ficar indisponível durante o uso. */
    private static void tratarErrosInesperados() {
        Thread.setDefaultUncaughtExceptionHandler((thread, erro) -> {
            Throwable causa = erro;
            while (causa != null && !(causa instanceof AcessoDadosException)) {
                causa = causa.getCause();
            }
            final String mensagem = causa != null
                    ? "Não foi possível acessar o banco de dados.\nVerifique se o MySQL está ligado e tente novamente.\n\n" + causa.getMessage()
                    : "Ocorreu um erro inesperado: " + erro;
            erro.printStackTrace();
            SwingUtilities.invokeLater(() -> Dialogos.erro(null, mensagem));
        });
    }

    private static void configurarVisual() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // mantém o visual padrão do Java
        }
        UIManager.put("OptionPane.messageFont", Estilo.FONTE);
        UIManager.put("OptionPane.buttonFont", Estilo.NEGRITO);
        UIManager.put("ToolTip.font", Estilo.PEQUENA);
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("FileChooser.saveButtonText", "Salvar");
        UIManager.put("FileChooser.cancelButtonText", "Cancelar");
    }
}
