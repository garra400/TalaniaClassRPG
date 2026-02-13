# TalaniaClassRPG - UI e Comandos de Classe

## Visão Geral
Este mod agora implementa um sistema de RPG funcional, inspirado em Orbis_and_Dungeons e RPGLeveling, com interface de seleção de classe, comandos e integração total com o TalaniaCore.

---

## Comandos Disponíveis

- `/class select [--player <nome>]`  
  Abre a tela de seleção de classe para o jogador alvo (ou para si mesmo).
- `/class change <classe> [--player <nome>]`  
  Troca a classe do jogador diretamente.
- `/class reset [--player <nome>]`  
  Reseta a classe do jogador para nenhuma.
- `/class info [--player <nome>]`  
  Mostra a classe atual do jogador.

Todos os comandos suportam o argumento `--player` para uso administrativo.

---

## Tela de Seleção de Classe

- UI moderna, paginada, com até 4 classes por página.
- Mostra nome, descrição, forças e fraquezas da classe.
- Botões de navegação (próximo/anterior), confirmar e voltar.
- Totalmente traduzível (en/pt_br).
- Inspirada na UI do Orbis_and_Dungeons.

---

## Integração com TalaniaCore

- Utiliza o sistema de perfis para salvar a classe do jogador.
- Usa o sistema de tradução e eventos do core.
- UI construída com o wrapper de interface do TalaniaCore.

---

## Como Estender

- Para adicionar novas classes, basta adicionar no enum `ClassType` e configurar traduções.
- Para customizar a UI, edite o arquivo `class_selection.ui`.
- Para adicionar lógica extra, edite os métodos de callback em `ClassSelectionPage`.

---

## Referências
- [docs/UI_SYSTEM.md](../Orbis_and_Dungeons/docs/UI_SYSTEM.md) (base visual)
- [TalaniaCore/docs/API_REFERENCE.md](../TalaniaCore/docs/API_REFERENCE.md)

---

*Documentação gerada automaticamente por GitHub Copilot em 2026.*
