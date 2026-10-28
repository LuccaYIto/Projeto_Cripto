
package model;

public class Cliente {
    
    private String nome;
    private String CPF;
    private String Senha;
    private double saldoReais;
    private double saldoBitcoin;
    private double saldoEthereum;
    private double saldoRipple;

    public Cliente() {
        
    }

    public Cliente(String CPF, double saldoReais) {
        this.CPF = CPF;
        this.saldoReais = saldoReais;
    }
    

    public Cliente(String nome, String CPF, String Senha, double saldoReais, double saldoBitcoin, double saldoEthereum, double saldoRipple) {
        this.nome = nome;
        this.CPF = CPF;
        this.Senha = Senha;
        this.saldoReais = saldoReais;
        this.saldoBitcoin = saldoBitcoin;
        this.saldoEthereum = saldoEthereum;
        this.saldoRipple = saldoRipple;
    }
    

    public Cliente(String nome, String CPF, String Senha) {
        this.nome = nome;
        this.CPF = CPF;
        this.Senha = Senha;
        this.saldoReais = 0.0;
        this.saldoBitcoin = 0.0;
        this.saldoEthereum = 0.0;
        this.saldoRipple = 0.0;
    }

    
    

    public String getNome() {
        return nome;
    }

    public String getCPF() {
        return CPF;
    }

    public String getSenha() {
        return Senha;
    }

    public double getSaldoReais() {
        return saldoReais;
    }

    public double getSaldoBitcoin() {
        return saldoBitcoin;
    }

    public double getSaldoEthereum() {
        return saldoEthereum;
    }

    public double getSaldoRipple() {
        return saldoRipple;
    }
    

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public void setSenha(String Senha) {
        this.Senha = Senha;
    }

    public void setSaldoReais(double saldoReais) {
        this.saldoReais = saldoReais;
    }

    public void setSaldoBitcoin(double saldoBitcoin) {
        this.saldoBitcoin = saldoBitcoin;
    }

    public void setSaldoEthereum(double saldoEthereum) {
        this.saldoEthereum = saldoEthereum;
    }

    public void setSaldoRipple(double saldoRipple) {
        this.saldoRipple = saldoRipple;
    }
    
    

    @Override
    public String toString() {
        // Nunca inclua a senha em logs ou mensagens de depuração.
        return "Cliente{" + "nome=" + nome + ", CPF=" + CPF
                + ", saldoReais=" + saldoReais + ", saldoBitcoin=" + saldoBitcoin
                + ", saldoEthereum=" + saldoEthereum + ", saldoRipple=" + saldoRipple + '}';
    }
    
    
    
}
