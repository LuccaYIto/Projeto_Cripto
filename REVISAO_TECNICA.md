# Revisão técnica — Projeto Cripto

## Resumo

A aplicação é um projeto acadêmico Java Swing com PostgreSQL/JDBC. A revisão ampliou os módulos que eram placeholders ou prévias: compra, venda, atualização manual de cotações e extrato agora possuem fluxo implementado. As operações que alteram saldo (depósito, saque, compra e venda) atualizam o saldo e registram o histórico dentro da mesma transação SQL.

## Alterações realizadas

### Persistência e modelo

- Adicionado `DAO/OperacaoDAO.java` para centralizar as operações financeiras, cotações e consulta do extrato.
- Adicionados `model/Cotacoes.java`, `model/ResultadoOperacao.java` e `model/Transacao.java`.
- Adicionado `database/schema.sql`, com tabelas de cliente, cotação global e transação; inclui conversão das colunas de saldo de `VARCHAR` para `NUMERIC` e tentativa de renomear duas colunas com grafia antiga (`SaldoBiticoin` e `SaldoEtherium`).
- A compra, venda, depósito e saque são transacionais. Se a atualização de saldo ou a gravação de histórico falhar, a operação SQL é revertida.
- Foram incluídas validações de valores positivos, taxa entre 0 e 1, saldo suficiente e quantidades/cotação positivas.

### Compra

- A antiga prévia de compra foi substituída por uma operação confirmada pelo usuário.
- O valor em reais é debitado, a quantidade de cripto é calculada com a cotação cadastrada e a taxa definida na classe da moeda é descontada do valor convertido.
- A quantidade adquirida e o valor da operação são registrados em `Transacao`.

### Venda

- Criada tela com seleção de moeda, entrada da quantidade, cotação e saldo disponível.
- A venda só é concluída se o usuário tiver a quantidade solicitada. A carteira cripto é debitada e o valor líquido, já descontada a taxa, é creditado em reais.

### Cotações

- Criada tela para carregar e salvar manualmente as cotações de BTC, ETH e XRP.
- As cotações são globais para os usuários do simulador; a interface informa isso antes de salvar.
- Os valores iniciais do script SQL são exemplos didáticos, não valores de mercado.

### Extrato

- Criada tabela de transações e tela para listar, em ordem decrescente de data, até 500 operações do usuário.
- Depósitos, saques, compras e vendas são registrados com tipo, moeda, quantidade, cotação, valor bruto, taxa e valor líquido.

### Telas e navegação

- O menu agora passa o CPF autenticado para venda e extrato.
- As telas de venda, atualização de cotações e extrato passaram a ser construídas diretamente no Java. Os antigos `.form` dessas telas eram placeholders vazios ou não representavam a nova interface e foram removidos.

## Verificações executadas

- Todos os arquivos Java foram compilados com `javac` usando Java 17 ou compatível.
- A compilação terminou sem erros. Permanecem avisos do código Swing legado, principalmente `serialVersionUID`, inicialização de controladores nos construtores (`this-escape`) e uso de APIs de interface já existentes.
- Não foi possível executar build Maven no ambiente desta revisão porque o comando `mvn` não está instalado.
- Não havia uma instância PostgreSQL disponível e não há `psql` instalado no ambiente; portanto, o script SQL e os fluxos ponta a ponta não puderam ser testados contra um banco real nesta sessão.

## Pontos que continuam pendentes ou exigem atenção

### Alta prioridade antes de uso além de demonstração

1. **Senhas em texto puro:** cadastro e login preservam o comportamento legado e ainda gravam/comparam a senha diretamente. Implementar hash com salt (Argon2id/bcrypt) e migrar os registros antes de qualquer uso real.
2. **Controle de acesso à atualização de cotações:** não existe papel de administrador; qualquer usuário autenticado que alcance a tela pode mudar as cotações globais. O projeto precisa de autorização por papel se essa ação não deve ser pública.
3. **Migração de banco legado:** `database/schema.sql` faz uma migração mínima, mas precisa ser executado primeiro em uma cópia/backup do banco existente. CPFs duplicados impedem a criação do índice único; valores não numéricos nas antigas colunas de saldo também exigem correção manual.

### Melhorias técnicas recomendadas

- Adicionar testes automatizados de cálculo e testes de integração do DAO com PostgreSQL temporário/de teste.
- Considerar autenticação baseada em hash e `JPasswordField` nas telas legadas.
- Separar melhor regras de negócio e mensagens Swing; os controladores ainda apresentam diálogos diretamente.
- Considerar uma fonte de cotações real apenas se isso fizer parte do escopo; a versão atual atualiza preços manualmente.
- Revisar os avisos Swing legados e modernizar telas antigas gradualmente, sem mudar os fluxos já estáveis sem testes.

## Como validar localmente

1. Faça backup do banco atual.
2. Crie o banco `BancoCripto`, se necessário.
3. Execute `psql -U <usuario> -d BancoCripto -f database/schema.sql`.
4. Defina `DB_URL`, `DB_USER` e `DB_PASSWORD`.
5. Execute `mvn clean compile` e `mvn exec:java -Dexec.mainClass="projetocripto.ProjetoCripto"`.
6. Teste cadastro, login, depósito, compra, venda, saque, atualização de cotações e extrato com um usuário de teste.
7. Confirme que uma operação acima do saldo disponível falha sem alterar os saldos ou registrar uma transação.

## Conclusão

A revisão completa a maior parte do fluxo acadêmico planejado e dá ao projeto uma estrutura de dados para cotações e histórico. A compilação Java está verificada, mas a validação de integração depende da execução do `database/schema.sql` e de testes no PostgreSQL local. O projeto continua sendo um simulador educacional, não uma aplicação financeira de produção.
