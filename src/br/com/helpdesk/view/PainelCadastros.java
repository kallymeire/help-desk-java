package br.com.helpdesk.view;

import br.com.helpdesk.model.Cliente;
import br.com.helpdesk.model.Pessoa;
import br.com.helpdesk.model.Tecnico;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Tela 5 – Cadastro de clientes e técnicos (RF02), com abas Clientes e Técnicos.
 */
public class PainelCadastros extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private final Navegador nav;
    private boolean modoClientes = true;
    private boolean carregando = false;
    private Pessoa editando;
    private List<? extends Pessoa> listaAtual = new ArrayList<Cliente>();

    private final BotaoEstilo abaClientes = new BotaoEstilo("Clientes", BotaoEstilo.Tipo.PRIMARIO);
    private final BotaoEstilo abaTecnicos = new BotaoEstilo("Técnicos", BotaoEstilo.Tipo.SECUNDARIO);
    private final CampoTexto busca = new CampoTexto(20);
    private final BotaoEstilo novo = new BotaoEstilo("+ Novo cliente", BotaoEstilo.Tipo.PRIMARIO);
    private DefaultTableModel modelo = Estilo.modeloSomenteLeitura("Nome", "E-mail", "Setor", "Telefone");
    private final JTable tabela = new JTable(modelo);
    private final JLabel tituloForm = Estilo.titulo("Dados do cliente");
    private final CampoTexto campoNome = new CampoTexto(20);
    private final CampoTexto campoEmail = new CampoTexto(20);
    private final CampoTexto campoSetor = new CampoTexto(10);
    private final CampoTexto campoTelefone = new CampoTexto(10);
    private final JComboBox<String> comboNivel = new JComboBox<>(new String[]{"N1", "N2", "N3"});
    private final CampoTexto campoEspecialidade = new CampoTexto(10);
    private JPanel linhaCliente;
    private JPanel linhaTecnico;
    private final JLabel msg = Estilo.mensagem();
    private final BotaoEstilo excluir = new BotaoEstilo("Excluir", BotaoEstilo.Tipo.PERIGO);
    private final BotaoEstilo redefinir = new BotaoEstilo("Redefinir senha", BotaoEstilo.Tipo.SECUNDARIO);

    public PainelCadastros(Navegador nav) {
        this.nav = nav;
        setBackground(Estilo.FUNDO);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel abas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        abas.setOpaque(false);
        abaClientes.setMnemonic('C');
        abaTecnicos.setMnemonic('T');
        abaClientes.setPreferredSize(new Dimension(150, 40));
        abaTecnicos.setPreferredSize(new Dimension(150, 40));
        abaClientes.addActionListener(e -> trocarModo(true));
        abaTecnicos.addActionListener(e -> trocarModo(false));
        abas.add(abaClientes);
        abas.add(abaTecnicos);
        add(abas, BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(16, 0));
        corpo.setOpaque(false);

        // lista
        CartaoPanel lista = new CartaoPanel(new BorderLayout(0, 10));
        lista.setBorder(BorderFactory.createEmptyBorder(14, 14, 12, 14));
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);
        busca.setDica("Buscar por nome ou e-mail");
        Estilo.nome(busca, "Buscar por nome ou e-mail");
        barra.add(busca, BorderLayout.CENTER);
        novo.addActionListener(e -> novoRegistro());
        barra.add(novo, BorderLayout.EAST);
        lista.add(barra, BorderLayout.NORTH);
        Estilo.estilizar(tabela);
        Estilo.nome(tabela, "Lista de cadastros");
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        corpo.add(lista, BorderLayout.CENTER);

        // formulário
        CartaoPanel form = new CartaoPanel(null);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(380, 100));
        tituloForm.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(tituloForm);
        form.add(Box.createVerticalStrut(12));
        form.add(alinhado(Estilo.comRotulo("Nome *", campoNome)));
        form.add(Box.createVerticalStrut(10));
        form.add(alinhado(Estilo.comRotulo("E-mail *", campoEmail)));
        form.add(Box.createVerticalStrut(10));
        JPanel r1 = new JPanel(new GridLayout(1, 2, 12, 0));
        r1.setOpaque(false);
        r1.add(Estilo.comRotulo("Setor", campoSetor));
        r1.add(Estilo.comRotulo("Telefone", campoTelefone));
        linhaCliente = alinhado(r1);
        form.add(linhaCliente);
        Estilo.estilizar(comboNivel);
        JPanel r2 = new JPanel(new GridLayout(1, 2, 12, 0));
        r2.setOpaque(false);
        r2.add(Estilo.comRotulo("Nível *", comboNivel));
        r2.add(Estilo.comRotulo("Especialidade", campoEspecialidade));
        linhaTecnico = alinhado(r2);
        form.add(linhaTecnico);
        form.add(Box.createVerticalStrut(8));
        JLabel dica = new JLabel("<html><body style='width:300px'>O e-mail não pode se repetir. Contas novas recebem a senha inicial 123456 (use Redefinir senha para voltar a ela).</body></html>");
        dica.setFont(Estilo.PEQUENA);
        dica.setForeground(Estilo.TEXTO2);
        dica.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(dica);
        form.add(Box.createVerticalStrut(10));
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(msg);
        form.add(Box.createVerticalGlue());
        BotaoEstilo salvar = new BotaoEstilo("Salvar", BotaoEstilo.Tipo.PRIMARIO);
        BotaoEstilo limpar = new BotaoEstilo("Novo", BotaoEstilo.Tipo.SECUNDARIO);
        salvar.setToolTipText("Salvar (Ctrl+S)");
        salvar.addActionListener(e -> salvar());
        limpar.addActionListener(e -> novoRegistro());
        excluir.addActionListener(e -> excluir());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botoes.setOpaque(false);
        botoes.add(salvar);
        botoes.add(limpar);
        botoes.add(excluir);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Layouts.fixarAltura(botoes));
        form.add(Box.createVerticalStrut(10));
        redefinir.setAlignmentX(Component.LEFT_ALIGNMENT);
        redefinir.setToolTipText("Volta a senha da pessoa selecionada para 123456");
        redefinir.addActionListener(e -> redefinirSenha());
        form.add(redefinir);
        corpo.add(form, BorderLayout.EAST);
        add(corpo, BorderLayout.CENTER);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !carregando) selecionar();
        });
        busca.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recarregarTabela(null); }
            @Override public void removeUpdate(DocumentEvent e) { recarregarTabela(null); }
            @Override public void changedUpdate(DocumentEvent e) { recarregarTabela(null); }
        });
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "salvar");
        getActionMap().put("salvar", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { salvar(); }
        });
        trocarModo(true);
    }

    private static JPanel alinhado(JComponent c) {
        JPanel p;
        if (c instanceof JPanel) {
            p = (JPanel) c;
        } else {
            p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.add(c);
        }
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return Layouts.fixarAltura(p);
    }

    private void trocarModo(boolean clientes) {
        modoClientes = clientes;
        abaClientes.setTipo(clientes ? BotaoEstilo.Tipo.PRIMARIO : BotaoEstilo.Tipo.SECUNDARIO);
        abaTecnicos.setTipo(clientes ? BotaoEstilo.Tipo.SECUNDARIO : BotaoEstilo.Tipo.PRIMARIO);
        modelo = clientes ? Estilo.modeloSomenteLeitura("Nome", "E-mail", "Setor", "Telefone")
                : Estilo.modeloSomenteLeitura("Nome", "E-mail", "Nível", "Especialidade");
        carregando = true;
        tabela.setModel(modelo);
        busca.setText("");
        carregando = false;
        novo.setText(clientes ? "+ Novo cliente" : "+ Novo técnico");
        tituloForm.setText(clientes ? "Dados do cliente" : "Dados do técnico");
        linhaCliente.setVisible(clientes);
        linhaTecnico.setVisible(!clientes);
        recarregarTabela(null);
        novoRegistro();
    }

    private void recarregarTabela(Pessoa selecionar) {
        String t = busca.getText().trim().toLowerCase();
        List<Pessoa> filtrada = new ArrayList<>();
        List<? extends Pessoa> base = modoClientes ? nav.cadastros().listarClientes() : nav.cadastros().listarTecnicos();
        for (Pessoa p : base) {
            if (t.isEmpty() || p.getNome().toLowerCase().contains(t) || p.getEmail().toLowerCase().contains(t)) filtrada.add(p);
        }
        listaAtual = filtrada;
        carregando = true;
        modelo.setRowCount(0);
        for (Pessoa p : filtrada) {
            if (p instanceof Cliente) {
                Cliente c = (Cliente) p;
                modelo.addRow(new Object[]{c.getNome(), c.getEmail(), c.getSetor(), c.getTelefone()});
            } else {
                Tecnico c = (Tecnico) p;
                modelo.addRow(new Object[]{c.getNome(), c.getEmail(), "N" + c.getNivel(), c.getEspecialidade()});
            }
        }
        carregando = false;
        int idx = selecionar == null ? -1 : filtrada.indexOf(selecionar);
        if (idx >= 0) tabela.setRowSelectionInterval(idx, idx);
        else tabela.clearSelection();
    }

    private void selecionar() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= listaAtual.size()) return;
        editando = listaAtual.get(linha);
        campoNome.setText(editando.getNome());
        campoEmail.setText(editando.getEmail());
        if (editando instanceof Cliente) {
            campoSetor.setText(((Cliente) editando).getSetor());
            campoTelefone.setText(((Cliente) editando).getTelefone());
        } else {
            comboNivel.setSelectedIndex(((Tecnico) editando).getNivel() - 1);
            campoEspecialidade.setText(((Tecnico) editando).getEspecialidade());
        }
        excluir.setEnabled(true);
        redefinir.setEnabled(true);
        Estilo.limpar(msg);
    }

    private void novoRegistro() {
        editando = null;
        carregando = true;
        tabela.clearSelection();
        carregando = false;
        campoNome.setText("");
        campoEmail.setText("");
        campoSetor.setText("");
        campoTelefone.setText("");
        campoEspecialidade.setText("");
        comboNivel.setSelectedIndex(0);
        excluir.setEnabled(false);
        redefinir.setEnabled(false);
        Estilo.limpar(msg);
        campoNome.requestFocusInWindow();
    }

    private void salvar() {
        try {
            Pessoa salvo;
            if (modoClientes) {
                salvo = nav.cadastros().salvarCliente((Cliente) editando, campoNome.getText(), campoEmail.getText(),
                        campoSetor.getText(), campoTelefone.getText());
            } else {
                salvo = nav.cadastros().salvarTecnico((Tecnico) editando, campoNome.getText(), campoEmail.getText(),
                        comboNivel.getSelectedIndex() + 1, campoEspecialidade.getText());
            }
            busca.setText("");
            recarregarTabela(salvo);
            editando = salvo;
            excluir.setEnabled(true);
            redefinir.setEnabled(true);
            Estilo.mostrarSucesso(msg, "Cadastro salvo.");
        } catch (IllegalArgumentException ex) {
            Estilo.mostrarErro(msg, ex.getMessage());
        }
    }

    private void redefinirSenha() {
        if (editando == null) return;
        if (!Dialogos.confirmar(this, "Voltar a senha de " + editando.getNome() + " para 123456?", "Redefinir")) return;
        nav.cadastros().redefinirSenha(editando);
        Estilo.mostrarSucesso(msg, "Senha redefinida para 123456.");
    }

    private void excluir() {
        if (editando == null) return;
        if (nav.cadastros().possuiVinculos(editando)) {
            Estilo.mostrarErro(msg, "Não é possível excluir: há chamados ligados a " + editando.getNome() + ".");
            return;
        }
        if (!Dialogos.confirmar(this, "Excluir o cadastro de " + editando.getNome() + "? Essa ação não pode ser desfeita.", "Excluir")) {
            return;
        }
        nav.cadastros().excluir(editando);
        recarregarTabela(null);
        novoRegistro();
        Estilo.mostrarSucesso(msg, "Cadastro excluído.");
    }

    @Override
    public void aoExibir() {
        trocarModo(modoClientes);
    }
}
