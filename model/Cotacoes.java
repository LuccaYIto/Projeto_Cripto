package model;

import java.math.BigDecimal;

/** Cotações globais das criptomoedas suportadas pelo simulador. */
public final class Cotacoes {
    private final BigDecimal bitcoin;
    private final BigDecimal ethereum;
    private final BigDecimal ripple;

    public Cotacoes(BigDecimal bitcoin, BigDecimal ethereum, BigDecimal ripple) {
        this.bitcoin = validar(bitcoin, "Bitcoin");
        this.ethereum = validar(ethereum, "Ethereum");
        this.ripple = validar(ripple, "Ripple");
    }

    private static BigDecimal validar(BigDecimal valor, String moeda) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("A cotação de " + moeda + " deve ser maior que zero.");
        }
        return valor;
    }

    public BigDecimal getBitcoin() { return bitcoin; }
    public BigDecimal getEthereum() { return ethereum; }
    public BigDecimal getRipple() { return ripple; }
}
