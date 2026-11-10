# Lista 02 - Selenium

Suíte de testes automatizados para o Seu Barriga usando Selenium WebDriver,
Cucumber e JUnit 5.

## Pré-requisitos

- Java 11 ou superior
- Maven 3.9 ou superior
- Google Chrome ou Microsoft Edge instalado
- Acesso à aplicação <https://seubarriga.wcaquino.me>

O Selenium Manager obtém o driver do navegador automaticamente. O navegador
precisa estar instalado na máquina.

## Executar os testes

No PowerShell:

```powershell
mvn test
```

Antes de toda a suíte, um hook `@BeforeAll` cadastra um usuário com email e
senha únicos gerados para essa execução, e todos os cenários fazem login com
ele. Por isso cada cenário é independente e pode ser executado sozinho. As
credenciais ficam apenas em memória durante a execução e não precisam ser
configuradas.

Cada execução cria um novo usuário na aplicação; esses usuários não são
removidos automaticamente.

Por padrão, os testes usam Chrome em modo headless. Para exibir o navegador:

```powershell
$env:HEADLESS = "false"
mvn test
```

Para usar o Edge, configure `$env:BROWSER = "edge"`.

## Cenários cobertos

1. Impedir a criação de conta sem nome.
2. Impedir a criação de conta com nome duplicado.
3. Criar uma conta e uma movimentação para ela.
4. Consultar o resumo mensal filtrando pelo mês e ano atuais e conferir o filtro aplicado.
5. Editar o nome de uma conta.

Os cenários que criam dados usam nomes únicos para evitar conflitos entre
execuções. O relatório HTML do Cucumber é gerado em
`target/cucumber-report.html`.
