## Estrutura do projeto

```text
Projeto_Cripto_revisado/
├── DAO/
│   ├── ClienteDAO.java             # Cadastro e consultas legadas de cliente
│   ├── Conexao.java                # Configuração JDBC por variáveis de ambiente
│   └── OperacaoDAO.java            # Compra, venda, saldo, cotação e extrato
├── controller/                     # Validação e fluxo das telas
├── model/
│   ├── Cliente.java
│   ├── Cotacoes.java
│   ├── ResultadoOperacao.java
│   ├── Transacao.java
│   ├── Moedas.java
│   ├── Bitcoin.java
│   ├── Ethereum.java
│   └── Ripple.java
├── view/                           # Interfaces Swing
├── projetocripto/ProjetoCripto.java # Ponto de entrada
├── database/schema.sql              # Esquema e migração mínima do PostgreSQL
├── pom.xml
├── .gitignore
├── README.md
└── REVISAO_TECNICA.md

# Projeto Cripto

Este projeto foi desenvolvido como trabalho acadêmico e tem como objetivo simular uma plataforma simples para trabalhar com saldo em reais e criptomoedas. A aplicação possui uma interface gráfica feita em Java e utiliza um banco de dados PostgreSQL para armazenar as informações dos usuários e das movimentações.

O sistema trabalha com Bitcoin (BTC), Ethereum (ETH) e Ripple (XRP). Ele é apenas um simulador: não movimenta dinheiro real, não se conecta a corretoras e não busca cotações automaticamente na internet.

## Funcionalidades

| Funcionalidade | Estado | Comportamento |
|---|---|---|
| Cadastro e login | Implementados | Permitem cadastrar e autenticar usuários no PostgreSQL. |
| Consulta de saldo | Implementada | Exibe os saldos em reais, Bitcoin, Ethereum e Ripple. |
| Depósito simulado | Implementado | Credita reais e registra a operação no extrato na mesma transação SQL. |
| Saque simulado | Implementado | Verifica se há saldo suficiente, debita reais e registra a operação. |
| Compra de criptomoedas | Implementada | Debita reais, calcula a quantidade após a taxa, credita a moeda e registra a transação. |
| Venda de criptomoedas | Implementada | Verifica a quantidade disponível, debita a moeda e credita reais após o desconto da taxa. |
| Atualização de cotações | Implementada manualmente | Permite editar e salvar as cotações de BTC, ETH e XRP. |
| Extrato | Implementado | Lista as movimentações do usuário autenticado, da mais recente para a mais antiga, com limite de 500 registros. |

As taxas estão definidas nas classes `Bitcoin`, `Ethereum` e `Ripple`. Na compra, a taxa é de 2% para BTC e 1% para ETH e XRP. Na venda, é de 3% para BTC, 2% para ETH e 1% para XRP.

Os valores são representados como frações no código. Por exemplo, `0.02` corresponde a 2%.

## Tecnologias utilizadas

- Java 17 ou superior
- Java Swing para as interfaces gráficas
- JDBC para a conexão com o banco de dados
- PostgreSQL
- Driver JDBC do PostgreSQL
- Maven para gerenciamento e compilação do projeto

## Estrutura do projeto

```text
Projeto_Cripto/
├── DAO/
│   ├── ClienteDAO.java                        # Cadastro e consultas legadas de cliente
│   ├── Conexao.java                           # Configuração JDBC por variáveis de ambiente
│   └── OperacaoDAO.java                       # Compra, venda, saldo, cotação e extrato
├── controller/                               
│   └── Classes responsáveis pelo fluxo das telas                       # Validação e fluxo das telas 
├── model/
│   ├── Cliente.java
│   ├── Carteira.java
│   ├── Criptomoedas.java
│   ├── Cotacoes.java
│   ├── Extrato.java
│   ├── Moedas.java
│   ├── Reais.java
│   ├── ResultadoOperacao.java
│   ├── Tarifacao.java
│   ├── Transacao.java
│   ├── Bitcoin.java
│   ├── Ethereum.java
│   └── Ripple.java
├── projetocripto/                             
│   └── ProjetoCripto.java                     # Ponto de entrada
├── view/                                      
│   └── Interfaces gráficas                    # Interfaces Swing
├── database/
│   └── schema.sql                             # Esquema e migração mínima do PostgreSQL
├── pom.xml
├── .gitignore
├── README.md
└── REVISAO_TECNICA.md
```

A pasta `DAO` contém as classes responsáveis pelo acesso ao banco de dados. A pasta `controller` organiza as ações das telas, enquanto `model` reúne as classes que representam os dados e as regras do sistema.

A pasta `view` contém as interfaces gráficas, e `projetocripto` contém a classe principal da aplicação.

As telas de venda, atualização de cotações e extrato são construídas diretamente nos arquivos Java. Os arquivos `.form` dessas telas não são necessários quando as interfaces são criadas diretamente no código.

## Pré-requisitos

Antes de executar o projeto, é necessário ter instalado:

1. JDK 17 ou superior.
2. PostgreSQL.
3. Maven.
4. Um banco de dados chamado `BancoCripto`.

## Configuração do banco de dados

### 1. Criar o banco

No PostgreSQL, crie o banco de dados:

```sql
CREATE DATABASE "BancoCripto";
```

Se o banco já existir, não é necessário criá-lo novamente.

Caso já existam dados importantes, faça um backup antes de executar alterações na estrutura do banco.

### 2. Criar ou atualizar as tabelas

Na raiz do projeto, execute:

```bash
psql -U postgres -d BancoCripto -f database/schema.sql
```

Substitua `postgres` pelo nome do seu usuário do PostgreSQL, se necessário.

O script prepara as colunas de saldo da tabela `Cliente`, cria a tabela `Cotacao` para armazenar os preços das criptomoedas e cria a tabela `Transacao` para registrar as movimentações.

O script também tenta importar uma cotação antiga válida da tabela `public."Extrato"` quando a nova tabela ainda contém os valores iniciais de exemplo.

Se houver CPFs duplicados na tabela `Cliente`, a criação do índice único poderá falhar. Nesse caso, faça um backup e verifique os registros duplicados antes de continuar. Não apague dados sem verificar o que será afetado.

## Configuração da conexão

As credenciais do banco não devem ficar salvas diretamente no código ou no repositório. A conexão utiliza variáveis de ambiente.

### Windows (PowerShell)

Abra o terminal do VS Code e execute:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/BancoCripto"
$env:DB_USER = "seu_usuario_postgres"
$env:DB_PASSWORD = "sua_senha_postgres"
```

### Linux e macOS

```bash
export DB_URL="jdbc:postgresql://localhost:5432/BancoCripto"
export DB_USER="seu_usuario_postgres"
export DB_PASSWORD="sua_senha_postgres"
```

A variável `DB_URL` é opcional, pois o projeto utiliza `jdbc:postgresql://localhost:5432/BancoCripto` como endereço padrão.

As variáveis `DB_USER` e `DB_PASSWORD` devem ser configuradas com as credenciais do PostgreSQL.

Também são aceitas as propriedades Java `db.url`, `db.user` e `db.password`, conforme a implementação da classe `DAO/Conexao.java`.

Se o projeto for executado no GitHub Codespaces, o PostgreSQL também deverá estar acessível a partir do ambiente remoto. Nesse caso, `localhost` representa o próprio ambiente do Codespace, e não o computador pessoal.

## Como compilar e executar

Na raiz do projeto, execute:

```bash
mvn clean compile
```

Esse comando compila os arquivos Java.

Para iniciar a aplicação utilizando o plugin Maven Exec configurado no projeto, execute:

```bash
mvn exec:java "-Dexec.mainClass=projetocripto.ProjetoCripto"
```

Também é possível importar o projeto no NetBeans ou no VS Code e executar a classe principal `projetocripto.ProjetoCripto`.

Para que as funcionalidades que utilizam o banco funcionem, o PostgreSQL precisa estar ativo, as tabelas devem ter sido criadas e as variáveis de conexão precisam estar configuradas corretamente.

## Fluxo sugerido para testar

Depois de configurar o banco, recomendo seguir estes passos:

1. Iniciar o projeto e cadastrar um usuário de teste.
2. Fazer login com o CPF e a senha cadastrados.
3. Realizar um depósito simulado para adicionar reais à carteira.
4. Acessar a tela de compra, conferir o saldo e as cotações e confirmar uma compra.
5. Acessar a tela de venda e vender uma quantidade que não ultrapasse o saldo de criptomoedas disponível.
6. Abrir a tela de atualização de cotações e alterar os preços utilizados nas próximas operações.
7. Consultar o extrato e verificar se as movimentações foram registradas.
8. Tentar realizar um saque ou uma venda com saldo insuficiente e verificar se a operação é recusada sem alterar os saldos.

## Regras de cálculo

**Compra de criptomoedas**

O valor informado é debitado da carteira em reais. A taxa de compra é descontada do valor convertido em criptomoeda, e a quantidade recebida é calculada com base na cotação utilizada no momento da operação.

**Venda de criptomoedas**

A quantidade vendida é debitada da carteira. O valor bruto é calculado multiplicando a quantidade pela cotação, e a taxa de venda é descontada antes de creditar os reais na conta.

**Registro das operações**

A atualização dos saldos e o registro da transação são realizados na mesma transação SQL. Se uma das etapas falhar, a operação deverá ser revertida.

**Precisão dos valores**

O banco utiliza campos `NUMERIC`, e os cálculos das novas operações utilizam `BigDecimal`. Os saldos em reais são arredondados para centavos, enquanto as quantidades de criptomoedas utilizam até oito casas decimais.

**Cotações**

Os preços são definidos manualmente e armazenados no banco. Eles não representam necessariamente as cotações atuais do mercado.

## Segurança e limitações

- As senhas ainda são armazenadas em texto puro, pois o fluxo original de autenticação foi mantido. Antes de utilizar o sistema além de uma demonstração acadêmica, é necessário implementar hash de senhas com salt, utilizando uma solução como Argon2id ou bcrypt.
- A tela legada de consulta de saldo solicita CPF e senha, enquanto as operações de compra, venda e extrato utilizam o CPF da sessão aberta.
- O sistema não possui níveis de acesso para administradores. Por isso, usuários que tenham acesso ao menu podem atualizar as cotações globais.
- Depósitos e saques apenas alteram saldos internos simulados. Não existe integração com bancos ou meios de pagamento.
- Não há integração com corretoras, carteiras blockchain ou APIs de mercado.
- Bancos de dados antigos que possuam estruturas diferentes das esperadas pelo projeto podem exigir ajustes no script SQL.
- A compilação pode ser verificada sem uma conexão ativa com o banco, mas os testes completos das funcionalidades dependem de um PostgreSQL configurado.

A compilação do projeto foi verificada com Java 17. Os testes integrados com PostgreSQL ainda precisam ser realizados em um ambiente configurado para a aplicação.
