package br.com.helpdesk.view;

import br.com.helpdesk.dao.AcessoDadosException;
import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.service.CadastroService;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

/**
 * Tela 1 – Login (RF01): e-mail corporativo e senha.
 */
public class PainelLogin extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private final CadastroService cadastros;
    private final Consumer<Pessoa> aoEntrar;
    private final CampoTexto campoEmail = new CampoTexto(20);
    private final JPasswordField campoSenha = new JPasswordField(20);
    private final JCheckBox mostrarSenha = new JCheckBox("Mostrar senha");
    private final JCheckBox manter = new JCheckBox("Manter e-mail preenchido");
    private final JLabel erro = Estilo.mensagem();
    private final Preferences prefs = Preferences.userNodeForPackage(PainelLogin.class);

    public PainelLogin(CadastroService cadastros, Consumer<Pessoa> aoEntrar) {
        this.cadastros = cadastros;
        this.aoEntrar = aoEntrar;
        setBackground(Estilo.FUNDO);
        setLayout(new GridBagLayout());

        CartaoPanel card = new CartaoPanel(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 28, 40));
        card.setPreferredSize(new Dimension(440, 520));

        JComponent logo = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override public Dimension getPreferredSize() { return new Dimension(80, 80); }
            @Override public Dimension getMaximumSize() { return new Dimension(80, 80); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Estilo.PRIMARIA);
                g2.fillRoundRect(0, 0, 79, 79, 18, 18);
                g2.setColor(Color_WHITE);
                g2.setFont(Estilo.GRANDE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("HD", (80 - fm.stringWidth("HD")) / 2, (80 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.getAccessibleContext().setAccessibleName("Logotipo Help Desk");

        JLabel titulo = new JLabel("Help Desk");
        titulo.setFont(Estilo.TITULO);
        titulo.setForeground(Estilo.TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Acesse com seu e-mail corporativo");
        sub.setFont(Estilo.FONTE);
        sub.setForeground(Estilo.TEXTO2);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        campoEmail.setDica("nome@empresa.com");
        JPanel pEmail = Layouts.fixarAltura(Estilo.comRotulo("E-mail *", campoEmail));

        campoSenha.setFont(Estilo.FONTE);
        campoSenha.setBorder(Estilo.bordaCampo());
        campoSenha.setPreferredSize(new Dimension(200, 38));
        JPanel pSenha = Layouts.fixarAltura(Estilo.comRotulo("Senha *", campoSenha));

        for (JCheckBox cb : new JCheckBox[]{mostrarSenha, manter}) {
            cb.setFont(Estilo.FONTE);
            cb.setOpaque(false);
            cb.setForeground(Estilo.TEXTO);
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        mostrarSenha.setMnemonic('M');
        manter.setMnemonic('E');
        mostrarSenha.addActionListener(e -> campoSenha.setEchoChar(mostrarSenha.isSelected() ? (char) 0 : '•'));
        campoSenha.setEchoChar('•');

        BotaoEstilo entrar = new BotaoEstilo("Entrar", BotaoEstilo.Tipo.PRIMARIO);
        entrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        entrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        entrar.setPreferredSize(new Dimension(200, 44));
        entrar.setMnemonic('N');
        entrar.addActionListener(e -> entrar());

        BotaoEstilo esqueci = new BotaoEstilo("Esqueci minha senha", BotaoEstilo.Tipo.SECUNDARIO);
        esqueci.setAlignmentX(Component.LEFT_ALIGNMENT);
        esqueci.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        esqueci.addActionListener(e -> Dialogos.info(this,
                "Para redefinir sua senha, procure o gestor de suporte.\n(Nas próximas etapas, o sistema enviará um link por e-mail.)"));

        erro.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(logo);
        card.add(Box.createVerticalStrut(14));
        card.add(titulo);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(pEmail);
        card.add(Box.createVerticalStrut(14));
        card.add(pSenha);
        card.add(Box.createVerticalStrut(6));
        card.add(mostrarSenha);
        card.add(manter);
        card.add(Box.createVerticalStrut(8));
        card.add(erro);
        card.add(Box.createVerticalStrut(8));
        card.add(entrar);
        card.add(Box.createVerticalStrut(10));
        card.add(esqueci);
        add(card);

        campoEmail.addActionListener(e -> campoSenha.requestFocusInWindow());
        campoSenha.addActionListener(e -> entrar());
    }

    private static final java.awt.Color Color_WHITE = java.awt.Color.WHITE;

    private void entrar() {
        Estilo.limpar(erro);
        String email = campoEmail.getText().trim();
        String senha = new String(campoSenha.getPassword());
        if (email.isEmpty()) {
            Estilo.mostrarErro(erro, "Informe o e-mail.");
            campoEmail.requestFocusInWindow();
            return;
        }
        if (senha.isEmpty()) {
            Estilo.mostrarErro(erro, "Informe a senha.");
            campoSenha.requestFocusInWindow();
            return;
        }
        Pessoa p;
        try {
            p = cadastros.autenticar(email, senha);
        } catch (AcessoDadosException ex) {
            Estilo.mostrarErro(erro, "Sem conexão com o banco de dados. Verifique se o MySQL está ligado.");
            return;
        }
        if (p == null) {
            Estilo.mostrarErro(erro, "E-mail ou senha incorretos. Confira e tente de novo.");
            campoSenha.setText("");
            campoSenha.requestFocusInWindow();
            return;
        }
        try {
            if (manter.isSelected()) {
                prefs.put("email", email);
            } else {
                prefs.remove("email");
            }
        } catch (RuntimeException ignorada) {
            // preferências indisponíveis: segue sem lembrar o e-mail
        }
        campoSenha.setText("");
        aoEntrar.accept(p);
    }

    @Override
    public void aoExibir() {
        Estilo.limpar(erro);
        campoSenha.setText("");
        mostrarSenha.setSelected(false);
        campoSenha.setEchoChar('•');
        String salvo = "";
        try {
            salvo = prefs.get("email", "");
        } catch (RuntimeException ignorada) {
            salvo = "";
        }
        campoEmail.setText(salvo);
        manter.setSelected(!salvo.isEmpty());
        (salvo.isEmpty() ? campoEmail : campoSenha).requestFocusInWindow();
    }
}
