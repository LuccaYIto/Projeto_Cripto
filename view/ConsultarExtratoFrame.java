package view;

import controller.ControllerExtrato;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Tela de extrato de movimentações do cliente autenticado. */
public class ConsultarExtratoFrame extends JFrame {
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Data e hora", "Operação", "Moeda", "Quantidade", "Cotação (R$)",
                "Valor bruto (R$)", "Taxa", "Valor líquido (R$)"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabela = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Carregando movimentações...");
    private final ControllerExtrato controller;

    public ConsultarExtratoFrame(String cpfLogado) {
        super("Extrato de transações");
        controller = new ControllerExtrato(this, cpfLogado);
        initComponents();
        controller.carregar();
    }

    /** Mantém compatibilidade com código legado; sem CPF não haverá registros. */
    public ConsultarExtratoFrame() {
        this("");
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 12));
        header.add(new JLabel("Histórico de depósitos, saques, compras e vendas"), BorderLayout.WEST);
        JButton atualizar = new JButton("Atualizar");
        atualizar.addActionListener(evt -> controller.carregar());
        header.add(atualizar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        tabela.setAutoCreateRowSorter(true);
        tabela.setFillsViewportHeight(true);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 10, 12));
        footer.add(statusLabel, BorderLayout.CENTER);
        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(evt -> dispose());
        footer.add(fechar, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1100, 420));
        pack();
        setMinimumSize(new Dimension(900, 350));
        setLocationRelativeTo(null);
    }

    public DefaultTableModel getTableModel() { return tableModel; }
    public JLabel getStatusLabel() { return statusLabel; }
}
