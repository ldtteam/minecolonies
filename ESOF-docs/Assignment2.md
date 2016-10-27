# Assignment 2 - Requirement Elicitation #

## Introdução ##


## Âmbito / Propósito ##


## Descrição ##

### Levantamento de requerimentos ###

### Problemas ###

### Técnicas ###

### Análise, negociação e validação de requerimentos ###


## Requerimentos específicos e funcionalidades ##

Minecolonies é uma modificação do Minecraft que se baseia na gestão de uma cidade através dos vários residentes e trabalhadores.Tendo o jogo base já as suas funcionalidades, este projeto proporciona aos utilizadores uma nova perspetiva oferecendo novas características, itens e regras, sob as do jogo base, de forma a contextualizar a ideia principal.

### Requerimentos funcionais ###

#### Estruturas ####

Para simular o comportamento de uma cidade foram implementados dois tipos de estruturas essenciais: a town hall e o supply ships. O jogo não deve iniciar sem a colocação destas estruturas.

##### Town Hall #####

O town hall representa o centro da área protegida em que se insere a cidade. No início, podemos posicionar a câmara onde quisermos, desde que não incida sobre outra cidade, mas uma vez posicionada não pode ser deslocada para outro sítio. O raio da cidade pode também ser definido.
Esta estrutura permite obter informações e fazer a gestão de residentes e trabalhadores (npcs), criar nova town hall (nova cidade), reparar, alterar o nome, nomes e permissões de acesso a outros jogadores conforme um rank, modificar o modo de contratação (manual/automático), entre outros.

##### Supply Ships #####

O Supply ship representa o posto de abastecimento da cidade bem como a primeira casa do jogador. Esta estrutura tem que ser posicionada sob uma grande porção de água e não podem ser colocadas mais estruturas deste tipo no mesmo mundo.

#### Npcs ####

Os NPCs do Minecolonies podem ser divididos em vários tipos: builder, farmer, fisherman, baker, lumberjack, miner, crafter, guard, hunter, blacksmith, stonemason, deliveryman e citizen. Cada NPC dos tipos acima referidos tem um conjunto de características e funcionalidades próprias, desempenhando, cada um, um papel único. O número de NPCs por cidade é ilimitado.

#### Crafts ####

Como complemento para o bom funcionamento das ideias a pôr em prática, foram adicionados novos objetos que podemos construir através da crafting table do jogo base. Os objetos novos são: supply ship, builders hut, citizens hut, miners hut,lumberjack hut, fishermans hut, build tool, scan tool e field. Estes objetos são essenciais para pôr em práticas as funcionalidades de determinados npcs e para gestão da cidade.

#### Achievements ####

A modificação também disponibiliza uma expansão e um melhoramento ao sistema de conquistas implementado no jogo base. Este sistema é uma adição ao vasto leque de novidades que minecolonies traz ao jogo que torna a experiência dos jogadores bastante mais agradável, cobrindo vários tópicos como o inicial de “construir um supply ship” ou o de “criar NPC’s”. Esta funcionalidade apenas serve para melhorar a qualidade de jogo, não tendo nenhuma recompensa para além de ter uma lista de proezas completadas, mas também tem a particularidade de servir de uma espécie de guia interativo para novos jogadores.

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
