package model;

import java.math.BigDecimal;

/** Resultado de uma compra ou venda concluída no banco. */
public final class ResultadoOperacao {
    private final String moeda;
    private final BigDecimal quantidade;
    private final BigDecimal cotacao;
    private final BigDecimal valorBruto;
    private final BigDecimal taxa;
    private final BigDecimal valorLiquido;

    public ResultadoOperacao(String moeda, BigDecimal quantidade, BigDecimal cotacao,
            BigDecimal valorBruto, BigDecimal taxa, BigDecimal valorLiquido) {
        this.moeda = moeda;
        this.quantidade = quantidade;
        this.cotacao = cotacao;
        this.valorBruto = valorBruto;
        this.taxa = taxa;
        this.valorLiquido = valorLiquido;
    }

    public String getMoeda() { return moeda; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getCotacao() { return cotacao; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public BigDecimal getTaxa() { return taxa; }
    public BigDecimal getValorLiquido() { return valorLiquido; }
}
