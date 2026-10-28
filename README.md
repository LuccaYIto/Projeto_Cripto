# Projeto Cripto

Este projeto foi desenvolvido como trabalho acadêmico e tem como objetivo simular uma plataforma simples para trabalhar com saldo em reais e criptomoedas. A aplicação possui uma interface gráfica feita em Java e utiliza um banco de dados PostgreSQL para armazenar as informações dos usuários e das movimentações.

O sistema trabalha com Bitcoin (BTC), Ethereum (ETH) e Ripple (XRP). Ele é apenas um simulador: não movimenta dinheiro real, não se conecta a corretoras e não busca cotações automaticamente na internet.

## Funcionalidades

| Funcionalidade | Estado | Comportamento |
|---|---|---|
| Cadastro e login | Implementados | Criam e autenticam usuários no PostgreSQL com o esquema esperado pelo projeto. |
| Consulta de saldo | Implementada | Exibe os saldos em reais, Bitcoin, Ethereum e Ripple. |
| Depósito simulado | Implementado | Credita reais e registra a operação no extrato na mesma transação SQL. |
| Saque simulado | Implementado | Verifica saldo suficiente, debita reais e registra a operação atomicamente. |
| Compra de cripto | Implementada | Debita reais, calcula a quantidade após a taxa, credita a moeda e registra a transação atomicamente. |
| Venda de cripto | Implementada | Verifica a quantidade disponível, debita a moeda, credita reais líquidos da taxa e registra a transação atomicamente. |
| Atualização de cotações | Implementada manualmente | Permite editar e salvar as cotações globais de BTC, ETH e XRP. Não consulta o mercado automaticamente. |
| Extrato | Implementado | Lista até 500 movimentações do usuário autenticado, da mais recente para a mais antiga. |

As taxas utilizadas vêm das classes `Bitcoin`, `Ethereum` e `Ripple`: compra de 2% para BTC e 1% para ETH/XRP; venda de 3% para BTC, 2% para ETH e 1% para XRP. Valores são aplicados como frações (por exemplo, `0.02` equivale a 2%).

## Tecnologias

- Java 17+
- Java Swing
- JDBC
- PostgreSQL
- Driver JDBC do PostgreSQL
- Maven

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
```

As telas de venda, atualização de cotações e extrato são construídas diretamente nos arquivos `.java`. Os antigos arquivos `.form` dessas três telas eram placeholders vazios e foram removidos para evitar que o formulário do NetBeans ficasse diferente da interface real.

## Pré-requisitos

1. JDK 17 ou superior.
2. PostgreSQL instalado e em execução.
3. Maven instalado (`mvn -version`).
4. Banco `BancoCripto` criado no PostgreSQL.

## Configurar o banco de dados

### 1. Criar o banco

No `psql` ou em um cliente SQL conectado a uma instância PostgreSQL, execute:

```sql
CREATE DATABASE "BancoCripto";
```

Se o banco já existir, pule esta etapa. Se você já possui dados, faça um backup antes de aplicar o script de esquema.

### 2. Criar ou atualizar as tabelas

Na raiz do projeto, execute:

```bash
psql -U postgres -d BancoCripto -f database/schema.sql
```

Substitua `postgres` pelo usuário do seu PostgreSQL, se necessário. O script prepara ou atualiza as colunas de saldo de `Cliente`, cria a tabela global `Cotacao` e cria a tabela `Transacao`. Também tenta importar uma cotação antiga válida de `public."Extrato"` quando a nova tabela ainda contém os valores iniciais de exemplo.

Se a tabela antiga `Cliente` contiver CPFs duplicados, a criação do índice único falhará. Nesse caso, faça backup e corrija os duplicados antes de continuar. Não apague dados para contornar o erro.

### 3. Configurar a conexão

O código não deve guardar credenciais dentro do repositório. Defina estas variáveis no terminal em que vai executar o programa.

**Windows PowerShell:**

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/BancoCripto"
$env:DB_USER = "seu_usuario_postgres"
$env:DB_PASSWORD = "sua_senha_postgres"
```

**Linux/macOS:**

```bash
export DB_URL="jdbc:postgresql://localhost:5432/BancoCripto"
export DB_USER="seu_usuario_postgres"
export DB_PASSWORD="sua_senha_postgres"
```

`DB_URL` é opcional; o padrão é `jdbc:postgresql://localhost:5432/BancoCripto`. `DB_USER` e `DB_PASSWORD` são obrigatórias. Também são aceitas as propriedades Java `db.url`, `db.user` e `db.password`.

## Compilar e executar

Na raiz do projeto:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="projetocripto.ProjetoCripto"
```

No NetBeans, o projeto pode ser importado como projeto Maven existente. Algumas telas antigas continuam usando os arquivos `.form` originais; as três telas novas mencionadas acima são mantidas diretamente no código Java.

## Fluxo sugerido para testar

1. Configure o PostgreSQL, aplique `database/schema.sql` e defina as variáveis de conexão.
2. Inicie o projeto e cadastre um usuário de teste.
3. Entre com o CPF e a senha usados no cadastro.
4. Faça um depósito simulado para adicionar reais à carteira.
5. Abra **Comprar**, atualize a exibição de saldo/cotações, informe um valor e confirme uma compra.
6. Abra **Vender**, selecione a moeda comprada e venda uma quantidade que não ultrapasse o saldo da carteira.
7. Abra **Atualizar cotação** para alterar os valores usados nas próximas operações. Uma confirmação informa que os valores são globais para todos os usuários do simulador.
8. Abra **Consultar extrato** para verificar depósitos, saques, compras e vendas registrados.
9. Tente sacar mais reais ou vender mais criptomoedas do que possui; a operação deve ser recusada sem alterar os saldos.

## Regras de cálculo

- **Compra:** o valor bruto informado é debitado da carteira em reais. A taxa de compra é descontada do valor convertido em cripto. A quantidade é calculada usando a cotação global do momento da operação.
- **Venda:** a quantidade selecionada é debitada da carteira. O valor bruto é calculado por quantidade × cotação; a taxa de venda é descontada antes de creditar reais.
- **Atomicidade:** a atualização de saldo e a gravação no extrato ocorrem na mesma transação SQL. Se uma das etapas falhar, a movimentação é revertida.
- **Precisão:** o banco usa `NUMERIC`; os cálculos das novas operações usam `BigDecimal`. Saldos de reais são arredondados para centavos e quantidades de cripto para oito casas decimais.
- **Cotações:** são definidas manualmente e armazenadas globalmente. Não são cotações atuais ou garantidas de mercado.

## Segurança e limitações

- **Senhas ainda são armazenadas em texto puro**, pois a autenticação original foi mantida para não alterar o fluxo existente. Antes de qualquer uso além de demonstração, implemente hash de senha com salt (Argon2id ou bcrypt) e migre as contas existentes.
- A tela de consulta de saldo legada ainda solicita CPF e senha; as operações de compra, venda e extrato usam o CPF da sessão aberta.
- Qualquer usuário autenticado que acesse o menu pode atualizar as cotações globais, pois o projeto não possui papéis de administrador.
- Depósitos e saques apenas alteram saldos internos simulados; não há integração com meios de pagamento.
- Não existe integração com corretora, carteira blockchain nem API de mercado.
- A camada de cadastro/autenticação original usa senha em texto puro; não use senhas reais ou reutilizadas.
- O esquema é compatível com os nomes de coluna documentados no `database/schema.sql`; bancos legados muito diferentes podem exigir migração manual.
- A compilação Java pode ser verificada sem banco, mas as operações de ponta a ponta exigem PostgreSQL configurado e o script SQL aplicado.

## Licença

Nenhuma licença foi fornecida no material original. Escolha uma licença antes de autorizar formalmente terceiros a reutilizar o código.
