/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author uniflito
 */
public class Reais extends Moedas{
    
    public Reais(){
        super("Reais", 1.00, 0.01, 0.01);
    }
}

//-- Table: public.Cliente
//
//-- DROP TABLE IF EXISTS public."Cliente";
//
//CREATE TABLE IF NOT EXISTS public."Cliente"
//(
//    "Nome" character varying COLLATE pg_catalog."default" NOT NULL,
//    "CPF" character varying COLLATE pg_catalog."default" NOT NULL,
//    "Senha" character varying COLLATE pg_catalog."default" NOT NULL,
//    "SaldoReais" character varying COLLATE pg_catalog."default" NOT NULL,
//    "SaldoBiticoin" character varying COLLATE pg_catalog."default" NOT NULL,
//    "SaldoEtherium" character varying COLLATE pg_catalog."default" NOT NULL,
//    "SaldoRipple" character varying COLLATE pg_catalog."default" NOT NULL
//)
//
//TABLESPACE pg_default;
//
//ALTER TABLE IF EXISTS public."Cliente"
//    OWNER to postgres;
