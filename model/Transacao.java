package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro imutável apresentado na tela de extrato. */
public final class Transacao {
    private final LocalDateTime dataHora;
    private final String tipo;
    private final String moeda;
    private final BigDecimal quantidade;
    private final BigDecimal cotacao;
    private final BigDecimal valorBruto;
    private final BigDecimal taxa;
    private final BigDecimal valorLiquido;

    public Transacao(LocalDateTime dataHora, String tipo, String moeda, BigDecimal quantidade,
            BigDecimal cotacao, BigDecimal valorBruto, BigDecimal taxa, BigDecimal valorLiquido) {
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.moeda = moeda;
        this.quantidade = quantidade;
        this.cotacao = cotacao;
        this.valorBruto = valorBruto;
        this.taxa = taxa;
        this.valorLiquido = valorLiquido;
    }

    public LocalDateTime getDataHora() { return dataHora; }
    public String getTipo() { return tipo; }
    public String getMoeda() { return moeda; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getCotacao() { return cotacao; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public BigDecimal getTaxa() { return taxa; }
    public BigDecimal getValorLiquido() { return valorLiquido; }
}
