# Revisão técnica — Projeto Cripto

## Resumo

A aplicação é um projeto acadêmico desenvolvido em Java Swing com PostgreSQL e JDBC. A revisão ampliou os módulos que anteriormente eram placeholders ou prévias: compra, venda, atualização manual de cotações e consulta ao extrato passaram a contar com fluxos implementados.

As operações que alteram saldos — depósito, saque, compra e venda — utilizam transações SQL para atualizar os saldos e registrar o histórico. Dessa forma, caso uma das etapas falhe, a operação pode ser revertida, evitando que apenas parte das alterações seja persistida.

## Alterações realizadas

### Persistência e modelo

- Adicionado `DAO/OperacaoDAO.java` para centralizar as operações financeiras, o gerenciamento de cotações e a consulta ao extrato.
- Adicionados `model/Cotacoes.java`, `model/ResultadoOperacao.java` e `model/Transacao.java`.
- Adicionado `database/schema.sql`, contendo a estrutura das tabelas de clientes, cotações e transações.
- Incluídas no script SQL conversões das colunas de saldo de `VARCHAR` para `NUMERIC` e tentativas de renomear colunas com grafia antiga (`SaldoBiticoin` e `SaldoEtherium`).
- Implementadas operações transacionais para compra, venda, depósito e saque. Se a atualização do saldo ou o registro do histórico falhar, a transação SQL é revertida.
- Incluídas validações de valores positivos, taxas entre 0 e 1, saldo suficiente e quantidades e cotações positivas.

### Compra

- A antiga prévia de compra foi substituída por uma operação que depende da confirmação do usuário.
- O valor em reais é debitado da carteira, e a quantidade de criptomoeda adquirida é calculada com base na cotação cadastrada e na taxa definida para a moeda.
- A quantidade adquirida e os valores da operação são registrados na tabela de transações.

### Venda

- Criada uma tela com seleção de moeda, entrada da quantidade, cotação e saldo disponível.
- A venda só é concluída se o usuário possuir a quantidade solicitada.
- A quantidade vendida é debitada da carteira de criptomoedas, e o valor líquido, após o desconto da taxa, é creditado no saldo em reais.

### Cotações

- Criada uma tela para cadastrar e atualizar manualmente as cotações de BTC, ETH e XRP.
- As cotações são globais para os usuários do simulador, e a interface informa essa característica antes de salvar as alterações.
- Os valores iniciais definidos no script SQL são exemplos didáticos, não cotações reais de mercado.

### Extrato

- Criada uma tabela de transações e uma tela para consultar as operações do usuário em ordem decrescente de data.
- A consulta apresenta até 500 operações e inclui depósitos, saques, compras e vendas.
- Os registros armazenam informações como tipo de operação, moeda, quantidade, cotação, valor bruto, taxa e valor líquido.

### Telas e navegação

- O menu passou a encaminhar o CPF do usuário autenticado para as telas de venda e extrato.
- As telas de venda, atualização de cotações e extrato passaram a ser construídas diretamente em Java.
- Os antigos arquivos `.form` dessas telas foram removidos por serem placeholders vazios ou não representarem as novas interfaces.

## Verificações executadas

- Os arquivos Java foram compilados com `javac`, utilizando Java 17 ou versão compatível.
- A compilação terminou sem erros, embora permaneçam avisos relacionados ao código Swing legado, principalmente `serialVersionUID`, inicialização de controladores nos construtores (`this-escape`) e uso de APIs de interface já existentes.
- Não foi possível executar o build Maven no ambiente da revisão porque o comando `mvn` não estava instalado.
- Não havia uma instância PostgreSQL disponível, e o comando `psql` também não estava instalado. Por isso, o script SQL e os fluxos completos da aplicação não puderam ser testados contra um banco de dados real nessa sessão.

**Importante:** a compilação dos arquivos Java não comprova, por si só, o funcionamento completo da aplicação. A integração com o banco e os fluxos de ponta a ponta ainda precisam ser validados localmente.

## Pontos pendentes e recomendações

### Alta prioridade antes de qualquer uso além de demonstração

1. **Senhas armazenadas em texto puro:** o cadastro e o login mantêm o comportamento legado e armazenam e comparam as senhas diretamente. Para qualquer uso além de uma demonstração acadêmica, é necessário implementar armazenamento seguro com hash e salt, utilizando uma solução adequada, como Argon2id ou bcrypt, e planejar a migração dos registros existentes.

2. **Controle de acesso à atualização de cotações:** não há distinção de papéis entre usuários comuns e administradores. Assim, qualquer usuário autenticado que consiga acessar a tela pode alterar as cotações globais. Caso essa ação deva ser restrita, é necessário implementar autorização por papel.

3. **Migração de banco legado:** o script `database/schema.sql` realiza uma migração mínima e precisa ser testado em uma cópia ou backup do banco existente antes de ser executado sobre os dados originais. CPFs duplicados podem impedir a criação do índice único, e valores não numéricos nas antigas colunas de saldo podem exigir correção manual.

### Melhorias técnicas recomendadas

- Adicionar testes automatizados para os cálculos financeiros e as regras de negócio.
- Implementar testes de integração dos DAOs com uma instância PostgreSQL de teste.
- Considerar o uso de `JPasswordField` nas telas de autenticação legadas.
- Separar melhor as regras de negócio das mensagens e dos diálogos Swing, pois os controladores ainda apresentam mensagens diretamente.
- Considerar a integração com uma fonte real de cotações somente se isso fizer parte do escopo do projeto.
- Revisar os avisos do código Swing legado e modernizar as telas antigas gradualmente, preservando os fluxos existentes e adicionando testes antes de alterações maiores.

## Como validar localmente

1. Faça backup do banco atual antes de executar qualquer migração.
2. Crie o banco `BancoCripto`, caso ainda não exista.
3. Execute o script SQL a partir da raiz do projeto:

   ```bash
   psql -U <usuario> -d BancoCripto -f database/schema.sql
   ```

4. Configure as variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`, conforme as instruções do `README.md`.
5. Execute a compilação com Maven:

   ```bash
   mvn clean compile
   ```

6. Execute a aplicação, caso o `pom.xml` esteja configurado com o plugin Maven Exec:

   ```bash
   mvn exec:java -Dexec.mainClass="projetocripto.ProjetoCripto"
   ```

7. Teste os fluxos de cadastro, login, depósito, compra, venda, saque, atualização de cotações e consulta ao extrato.
8. Verifique se uma operação que exceda o saldo disponível é rejeitada sem alterar os saldos ou registrar uma transação.
9. Teste também situações de erro, como valores inválidos, cotação inexistente e falhas de persistência, para verificar o comportamento das transações SQL.

## Conclusão

A revisão implementa os principais fluxos acadêmicos de compra, venda, atualização manual de cotações e consulta ao extrato, além de estruturar a persistência das operações financeiras.

A compilação Java foi verificada, mas a integração com PostgreSQL, a execução do script de criação e migração do banco e os testes completos ainda precisam ser realizados em um ambiente local configurado.

O Projeto Cripto continua sendo um simulador educacional. Antes de qualquer uso além de demonstração, é necessário tratar as limitações de segurança identificadas e validar os fluxos da aplicação com um banco de dados real.
