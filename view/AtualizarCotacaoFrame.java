package view;

import controller.ControllerAtualizarCotacao;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Tela para atualizar manualmente as cotações globais do simulador. */
public class AtualizarCotacaoFrame extends JFrame {
    private final JTextField bitcoinField = new JTextField(18);
    private final JTextField ethereumField = new JTextField(18);
    private final JTextField rippleField = new JTextField(18);
    private final JLabel statusLabel = new JLabel(" ");
    private final ControllerAtualizarCotacao controller;

    public AtualizarCotacaoFrame() {
        super("Atualizar cotações");
        this.controller = new ControllerAtualizarCotacao(this);
        initComponents();
        controller.carregarCotacoes();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel aviso = new JLabel("Cotações manuais de demonstração (valores em reais)");
        content.add(aviso, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 6, 8, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0;
        fields.add(new JLabel("Bitcoin (BTC): R$"), c);
        c.gridx = 1; fields.add(bitcoinField, c);
        c.gridx = 0; c.gridy = 1;
        fields.add(new JLabel("Ethereum (ETH): R$"), c);
        c.gridx = 1; fields.add(ethereumField, c);
        c.gridx = 0; c.gridy = 2;
        fields.add(new JLabel("Ripple (XRP): R$"), c);
        c.gridx = 1; fields.add(rippleField, c);
        content.add(fields, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(statusLabel, BorderLayout.NORTH);
        JPanel buttons = new JPanel();
        JButton carregar = new JButton("Recarregar");
        JButton salvar = new JButton("Salvar cotações");
        JButton fechar = new JButton("Fechar");
        carregar.addActionListener(evt -> controller.carregarCotacoes());
        salvar.addActionListener(evt -> controller.salvarCotacoes());
        fechar.addActionListener(evt -> dispose());
        buttons.add(carregar);
        buttons.add(salvar);
        buttons.add(fechar);
        bottom.add(buttons, BorderLayout.SOUTH);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    public JTextField getBitcoinField() { return bitcoinField; }
    public JTextField getEthereumField() { return ethereumField; }
    public JTextField getRippleField() { return rippleField; }
    public JLabel getStatusLabel() { return statusLabel; }
}
