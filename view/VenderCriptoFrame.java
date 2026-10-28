package view;

import controller.ControllerVenderCripto;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Tela de venda de criptomoedas já presentes na carteira do usuário autenticado. */
public class VenderCriptoFrame extends JFrame {
    private final JComboBox<String> moedaCombo = new JComboBox<>(new String[]{
        "Bitcoin (BTC)", "Ethereum (ETH)", "Ripple (XRP)"});
    private final JTextField quantidadeField = new JTextField(18);
    private final JLabel cotacaoLabel = new JLabel("Cotação: carregando...");
    private final JLabel saldoLabel = new JLabel("Saldo disponível: carregando...");
    private final ControllerVenderCripto controller;

    public VenderCriptoFrame(String cpfLogado) {
        super("Vender criptomoedas");
        controller = new ControllerVenderCripto(this, cpfLogado);
        initComponents();
        controller.atualizarResumo();
    }

    /** Construtor de compatibilidade para abrir a tela sem sessão; as operações exigem CPF válido. */
    public VenderCriptoFrame() {
        this("");
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20));
        content.add(new JLabel("Venda simulada com crédito do valor líquido em reais"), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 6, 8, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0;
        fields.add(new JLabel("Criptomoeda:"), c);
        c.gridx = 1; fields.add(moedaCombo, c);
        c.gridx = 0; c.gridy = 1;
        fields.add(new JLabel("Quantidade a vender:"), c);
        c.gridx = 1; fields.add(quantidadeField, c);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        fields.add(cotacaoLabel, c);
        c.gridy = 3; fields.add(saldoLabel, c);
        content.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton atualizar = new JButton("Atualizar saldo/cotação");
        JButton vender = new JButton("Vender");
        JButton fechar = new JButton("Fechar");
        atualizar.addActionListener(evt -> controller.atualizarResumo());
        vender.addActionListener(evt -> controller.vender());
        fechar.addActionListener(evt -> dispose());
        buttons.add(atualizar);
        buttons.add(vender);
        buttons.add(fechar);
        content.add(buttons, BorderLayout.SOUTH);

        moedaCombo.addActionListener(evt -> controller.atualizarResumo());
        setContentPane(content);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    public JComboBox<String> getMoedaCombo() { return moedaCombo; }
    public JTextField getQuantidadeField() { return quantidadeField; }
    public JLabel getCotacaoLabel() { return cotacaoLabel; }
    public JLabel getSaldoLabel() { return saldoLabel; }
}
