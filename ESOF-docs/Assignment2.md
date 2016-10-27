# Assignment 2 - Requirement Elicitation #

## Introdução ##


## Âmbito / Propósito ##


## Descrição ##

A engenharia de requerimentos passa por várias fases: levantamento de requerimentos, análise e negociação de requerimentos, especificação de requerimentos e validação de requerimentos. Neste relatório vamo-nos focar no levantamento de requerimentos.

### Levantamento de requerimentos ###

O principal objetivo do levantamento de requerimentos é a interação com os stakeholders de modo a descobrir os seus requerimentos. Para tal é necessário usar várias técnicas como inquéritos, questionários, observações sociais protótipos, entre outros. No caso do MineColonies, por ser um projeto open source e sem fins lucrativos, os seus contribuidores não estão propriamente interessados na utilização de técnicas mais profissionais para conhecer o seu público alvo. 
Os únicos meios são então os fóruns onde são analisadas críticas e sugestões deixadas pelos utilizadores, e os pull requests dos contribuidores.

### Problemas ###

Relativamente à modificação MineColonies, não existem grandes problemas relativos à comunicação entre utilizadores e programadores visto que a grande maior parte de sugestões já foram pensadas pelos programadores e já estão implementadas ou estão em numa lista à espera que alguem as comece.

> "Probably 95% of suggestions are things we have already thought about and are on the to-do list." - [Pmardle](https://github.com/pmardle)

Assim, são maioritariamente os programadores que regem o futuro da modificação.
Como neste projeto, é seguida a política “one man one branch”, acaba por não haver muitos dos problemas de comunicação em equipa. No entanto, pode não acontecer o mesmo com os utilizadores. Embora o interesse por parte dos responsáveis em satisfazer os pedidos da comunidade seja enorme, infelizmente, devido à falta de tempo e de programadores, por vezes são negadas algumas sugestões. Entre elas, estão updates a versões antigas da modificação. Por exemplo: o minecraft é regido por versões (sendo bastante popular a 1.7.10), por isso, jogadores que utilizem esta versão do Minecraft têm dificuldade em utilizar a modificação (apenas versões antigas do Minecolonies têm compatibilidade com a versão 1.7.10).

### Técnicas ###

A comunicação é feita via fórum pelos utilizadores, de maneira a que todos possam ver as sugestões já feitas, as implementações que estão a ser executadas e as implementações planeadas. Também é utilizado o github na divisão de tarefas, dividindo o trabalho em branches e apenas recebendo o código no repositório principal quando este está a funcionar sem problemas.
Uma das técnicas usadas pela equipa do Minecolonies é a prototipagem. Como a equipa necessita de ajuda para validar algumas implementações específicas, utilizam um sistema de “patreons”. Um “Patreon” é um utilizador que paga para receber benefícios sobre o jogo. O principal benefício neste jogo é o teste de protótipos lançados pela equipa. Um “Patreon” tem acesso a versões de teste da modificação, o que permite aos contribuidores identificar erros e receber feedbacks com muita antecedência. Desta maneira é possível poupar recursos na correcção de bugs e evitar problemas de implementações mal recebidas pela comunidade.

### Análise, negociação e validação de requerimentos ###

A decisão do que das próximas implementações é feita em função do que já está feito, do que a modificação necessita e também tendo em conta as opiniões da comunidade. Um exemplo disto será o caso dos guardas. Tendo esta funcionalidade recebido uma grande quantidade de pedidos devido à sua popularidade e necessidade por parte da comunidade, os programadores resolveram desenvolvê-la mais cedo e com várias alterações pedidas pela comunidade.


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

Após a investigação feita e o novo conhecimento adquirido relativo à engenharia de requerimentos, podemos dizer que a comunicação é algo fulcral para que um projeto tenha o seu devido sucesso.
Analisando de uma perspectiva generalista, qualquer tipo de trabalho feito em grupo sofre de problemas idênticos aos identificados neste trabalho e necessita de aplicar várias técnicas para combater esses mesmos. Dito isto, a engenharia de requerimentos e prototipagem não é apenas relativa a informática mas sim a todas as áreas de engenharia e não só; por exemplo, qualquer obra de engenharia civil necessita de uma só visão, uma divisão de tarefas clara e quando existe uma dúvida nunca se deverá assumir o que seja. 
Relativamente ao caso do minecolonies em específico, a equipa presente não tenha grandes problemas relacionado com este tópico e isto é graças às medidas que todos adotam para que não hajam falhas. A modificação em termos de código segue a política de “one branch one person”, os objetivos estabelecidos são claros, a comunicação é abundante e eficaz e ninguém assume o que seja, pois sabem perfeitamente que um erro detectado no final será muito mais problemático para resolver do que um identificado numa fase mais precoce.

## Contribuições ##

* [Inês Gomes](https://github.com/inesgomes) (up201405778@fe.up.pt)

* [Catarina Ramos](https://github.com/catramos96) (up201406219@fe.up.pt)

* [Mário Fernandes](https://github.com/MarioFernandes73) (up201201705@fe.up.pt) 

* [Manuel Curral](https://github.com/Camolas)  (up201202445@fe.up.pt)
