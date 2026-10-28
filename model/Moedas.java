/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author uniflito
 */
public class Moedas extends Carteira{
     private String nome;
     private double cotacao;
     private double taxaCompra;
     private double taxaVenda;

    public Moedas() {
    }

    public Moedas(String nome, double cotacao, double taxaCompra, double taxaVenda) {
        this.nome = nome;
        this.cotacao = cotacao;
        this.taxaCompra = taxaCompra;
        this.taxaVenda = taxaVenda;
    }

    public String getNome() {
        return nome;
    }

    public double getCotacao() {
        return cotacao;
    }

    public double getTaxaCompra() {
        return taxaCompra;
    }

    public double getTaxaVenda() {
        return taxaVenda;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCotacao(double cotacao) {
        this.cotacao = cotacao;
    }

    public void setTaxaCompra(double taxaCompra) {
        this.taxaCompra = taxaCompra;
    }

    public void setTaxaVenda(double taxaVenda) {
        this.taxaVenda = taxaVenda;
    }
    
    public void atualizarCotacao(){
        this.cotacao += this.cotacao ;
    }
}
