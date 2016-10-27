# Assignment 2 - Requirement Elicitation #

## Introdução ##


## Âmbito / Propósito ##


## Descrição ##

### Levantamento de requerimentos ###

### Problemas ###

### Técnicas ###

### Análise, negociação e validação de requerimentos ###


## Requerimentos específicos e funcionalidades ##

### Requerimentos funcionais ###

### Requerimentos não funcionais ###


## Diagrama de casos de usos ##

O modelo de casos de uso é uma sequência de transições num sistema visto da perspetiva de um ator.
Para o projeto *MineColonies*, criamos este modelo de casos de uso que descreve o sistema de manipulação dos objetos pelo ator que é o jogador. Este modelo não tem em consideração as possibilidades do jogo original Minecraft, mas sim as novas funcionalidades implementadas para o *MineColonies*.


// imagem do modelo //


Como é possível interpretar pela imagem, o jogador é o único ator deste sistema e tem controlo sob todas as ações. De seguida estão apresentadas duas descrições de dois casos de uso.

*Nome :* Retirar uma tarefa dum NPC.

*Atores :* Jogador.

*Objetivo :* retirar um NPC da sua tarefa atual.

*Referência para requerimentos :* !!duvida!!

*Pré condições :* 

* NPC deve estar empregado.
* A tarefa deve constar na lista de tarefas não concluídas do NPC

*Descrição :*

1. O jogador seleciona o NPC que deseja retirar a tarefa.
2. O NPC mostra uma janela para a confirmação.
3. O jogador confirma.
4. O NPC deixa essa tarefa

*Pós condições :*

1. O NPC avança para a próxima tarefa da lista.


*Nome :* Atualizar um edifício.

*Atores :* Jogador.

*Objetivo :* Atualizar um edifício de um NPC.

*Referência para requerimentos :*!!duvida!!

*Pré condições :*

* Deve existir um NPC do tipo Builder.
* Deve existir esse edifício.
* O nível do NPC associado a esse edifício deve ser superior ao nível do edifício.

*Descrição :*

1. O jogador seleciona o edifício que deseja evoluir.
2. O sistema mostra uma janela de escolha do NPC do tipo Builder que irá construir.
3. O jogador escolhe o NPC.
4. O sistema mostra uma janela de confirmação.

*Pós condições :*

1. É adicionada a tarefa “Upgrade Building x “ ao NPC do tipo Builder
2. O NPC evoluí o edifício.



## Modelo de domínio ##

O modelo de domínio é um modelo que representa um conjunto de classes conceptuais que descrevem as relações do jogo.


// imagem do modelo //


Como é possível interpretar da imagem, as classes principais são: jogador, citizen(NPC), Block Hut e Building. A classe Inventory é também importante pois é meio de relação entre o NPC e o jogador. O jogador pode, por exemplo, colocar materiais que o NPC necessite no seu inventário, e recolher os produtos do NPC. 
Como também pode ser interpretado pela imagem, cada personagem/block hut apenas tem um inventário, e um block hut está apenas relacionado a um citizen e a um building. 
Os achivements são apenas propriedade do Jogador, sem qualquer relação às restantes classes.

## Conclusão ##


## Contribuições ##

* [Inês Gomes](https://github.com/inesgomes) (up201405778@fe.up.pt)

* [Catarina Ramos](https://github.com/catramos96) (up201406219@fe.up.pt)

* [Mário Fernandes](https://github.com/MarioFernandes73) (up201201705@fe.up.pt) 

* [Manuel Curral](https://github.com/Camolas)  (up201202445@fe.up.pt)
