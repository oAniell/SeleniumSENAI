# language: pt

Funcionalidade: Cadastro e gerenciamento financeiro no Seu Barriga
  Como usuário do Seu Barriga
  Quero cadastrar um usuário e gerenciar minhas contas e movimentações
  Para acompanhar minhas finanças

  Cenário: Criar usuário para a execução dos testes
    Quando cadastro um novo usuário para os testes
    Então vejo a notificação "Usuário inserido com sucesso"

  Cenário: Não inserir uma conta sem nome
    Dado que acessei a aplicação e fiz login
    Quando navego para a criação de contas
    E tento salvar uma conta sem nome
    Então vejo a notificação "Informe o nome da conta"

  Cenário: Não inserir uma conta com nome duplicado
    Dado que acessei a aplicação e fiz login
    Quando navego para a criação de contas
    E crio uma conta com nome único
    E tento criar novamente a mesma conta
    Então vejo a notificação "Já existe uma conta com esse nome"

  Cenário: Criar uma conta e uma movimentação
    Dado que acessei a aplicação e fiz login
    Quando navego para a criação de contas
    E crio uma conta com nome único
    E crio uma movimentação para essa conta
    Então vejo a notificação "Movimentação adicionada com sucesso"

  Cenário: Buscar o resumo mensal pelo mês e ano atuais
    Dado que acessei a aplicação e fiz login
    Quando busco o resumo mensal do mês e ano atuais
    Então o resumo mensal é exibido

  Cenário: Editar o nome de uma conta
    Dado que acessei a aplicação e fiz login
    Quando navego para a criação de contas
    E crio uma conta com nome único
    E edito o nome da conta criada
    Então vejo a notificação "Conta alterada com sucesso"
