
package model;

public class Extrato {
    
//    o codigo esta correto? (intenção era iniciar com os valores zerados)
    private String CPF;
    private String senha;
    private String dataHorario;
    private double cotacaoBitcoin;
    private double cotacaoEthereum;
    private double cotacaoRipple;

    public Extrato(String CPF, String senha, String dataHorario, double cotacaoBitcoin, double cotacaoEthereum, double cotacaoRipple) {
        this.CPF = CPF;
        this.senha = senha;
        this.dataHorario = dataHorario;
        this.cotacaoBitcoin = cotacaoBitcoin;
        this.cotacaoEthereum = cotacaoEthereum;
        this.cotacaoRipple = cotacaoRipple;
    }

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getDataHorario() {
        return dataHorario;
    }

    public void setDataHorario(String dataHorario) {
        this.dataHorario = dataHorario;
    }

    public double getCotacaoBitcoin() {
        return cotacaoBitcoin;
    }

    public void setCotacaoBitcoin(double cotacaoBitcoin) {
        this.cotacaoBitcoin = cotacaoBitcoin;
    }

    public double getCotacaoEthereum() {
        return cotacaoEthereum;
    }

    public void setCotacaoEthereum(double cotacaoEthereum) {
        this.cotacaoEthereum = cotacaoEthereum;
    }

    public double getCotacaoRipple() {
        return cotacaoRipple;
    }

    public void setCotacaoRipple(double cotacaoRipple) {
        this.cotacaoRipple = cotacaoRipple;
    }

    
    
    
    
    
    
}
//duvidas
//  --> linha 6
//  --> linha 21
