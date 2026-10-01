#!/bin/bash
# Cria o repositório Git local e registra o projeto em commits organizados.
# Use no Git Bash (Windows) ou em qualquer terminal com bash, dentro da pasta do projeto:
#     bash criar_commits.sh
# Depois, conecte ao GitHub e envie (veja o documento de evidências):
#     git remote add origin https://github.com/kallymeire/help-desk-java.git
#     git push -u origin main
set -e
cd "$(dirname "$0")"

if [ -d .git ]; then
  echo "Já existe um repositório Git nesta pasta. Nada foi alterado."
  exit 1
fi

git init
git symbolic-ref HEAD refs/heads/main

# identificação do autor (pede apenas se ainda não estiver configurada)
if [ -z "$(git config user.name)" ]; then read -r -p "Seu nome para os commits: " NOME; git config user.name "$NOME"; fi
if [ -z "$(git config user.email)" ]; then read -r -p "Seu e-mail do GitHub: " EMAIL; git config user.email "$EMAIL"; fi

git add .gitignore build.xml manifest.mf nbproject
git commit -m "chore: estrutura inicial do projeto NetBeans e .gitignore"

git add README.md docs/telas
git commit -m "docs: adiciona README.md com especificações do sistema"

git add src/br/com/helpdesk/model src/br/com/helpdesk/util
git commit -m "feat(model): classes do domínio (Pessoa, Cliente, Tecnico, Gestor, Chamado, Interacao)"

git add sql config/banco.properties.exemplo
git commit -m "feat(banco): script MySQL com tabelas e dados iniciais"

git add src/br/com/helpdesk/dao
git commit -m "feat(dao): acesso a dados com JDBC (PessoaDAO e ChamadoDAO)"

git add src/br/com/helpdesk/service
git commit -m "feat(service): regras de negócio de chamados e cadastros"

git add src/br/com/helpdesk/view src/br/com/helpdesk/Main.java
git commit -m "feat(view): telas Swing, navegação e componentes de estilo"

git add docs lib executar.bat criar_commits.sh
git commit -m "docs: diagramas, síntese do projeto e instruções de execução"

echo
echo "Pronto! Histórico criado:"
git log --oneline --graph --decorate
echo
git status --short
