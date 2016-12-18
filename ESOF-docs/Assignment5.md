# Assignment 5 - Software Maintenance/Evolution #

![alt tag](resources/minecolonies.png)

## Introdução ##

(texto)

## Software Maintainability ##

(texto)

## Evolution Process ##

### Identificação ### 

Para a realização do último trabalho prático foi requisitada a implementação de uma nova feature no projeto selecionado. Dada a extensão do projeto e a sua organização, decidimos que a melhor abordagem seria a implementação de um feature dentro da lista das features já selecionadas pela equipa. Posto isto, decidimos escolher a feature [#6 - “Restrict lumberjack tool usage depending on his level“](https://github.com/Minecolonies/minecolonies/issues/6). Esta escolha baseou-se quer no interesse do tema como no grau de dificuldade encontrado. De seguida contactamos a equipa do **Minecolonies** para esclarecer algumas dúvidas e procurar um “ponto de partida”. A equipa mostrou-se, como sempre, disponível e sugeriu ainda a generalização do sistema de níveis para todos os *workers*.

> “That would be something doable, definitely It would be nice if this could be implemented for all workers in a general ai function” - [Marvin](https://github.com/Kostronor)

### Descrição ###

A feature a implementar tem como objetivo a implementação de um sistema de níveis, comum a todos os workers (*NPC’s*), que restringe as ferramentas usadas de acordo com o nível do *blockhut* (bloco que associa um trabalhador com a respetiva casa). A declaração de restrições foi nos especificada pelos *developers* como:

* *blockhut* nível 0: apenas ferramentas de nível 0 (sem encantamentos)
* *blockhut* nível 1: ferramentas de nível 0 ou 1 (sem encantamentos)
* *blockhut* nível 2: ferramentas até nível 2 (sem encantamentos)
* *blockhut* nível 3: ferramentas até nível 2 (permite encantamentos)
* *blockhut* nível 4: ferramentas até nível 3 (permite encantamentos)
* *blockhut* nível 5: qualquer tipo de ferramenta (inclui ferramentas extra **Minecraft**)

Os níveis das ferramentas são relativamente ao tipo de material, em que:

* nível 0: madeira e ouro;
* nível 1: pedra;
* nível 2: ferro;
* nível 3: diamante.

### Implementação ###

Para começar a implementação, auxiliamos-nos nas dicas dos *developers* que nos indicaram a classe [abstractEntityAiBasic9](https://github.com/Minecolonies/minecolonies/blob/develop/src/main/java/com/minecolonies/coremod/entity/ai/basic/AbstractEntityAIBasic.java) mais especificamente o método *holdEfficientTool()* que tem como objetivo a escolha do slot com a melhor ferramenta (adequado ao bloco) do inventório. No entanto, após uma análise mais profunda do código, chegamos à conclusão que a implementação não estaria apenas restrita a este método. 

Assim, foi necessário dividir a implementação em vários passos. Fazendo uma abordagem *bottom-up*, o primeiro método, e mais importante, a ser implementado foi:

				`verifyToolLevel(final ItemStack item, int level, final int hutLevel)`
			
Este método retorna verdadeiro ou falso consoante o **item** recebido respeita o sistema de níveis de acordo com o **level** do bloco com que irá interagir e o **hutLevel** desse *worker*.

Este método é invocado por duas vezes em duas ocasiões distintas:

			`hasToolLevel(final String tool, @NotNull final InventoryCitizen inventory, final int hutLevel)` 

					`getMostEfficientTool(@NotNull final Block target)`
				
O primeiro método, também criado pelo grupo, recebe o tipo de **tool** do bloco a interagir, o **inventory** do worker, e o seu **hutLevel**. O seu objetivo é retornar verdadeiro ou falso caso exista alguma **tool** no inventário do *worker* que se adeque ao bloco e respeite o sistema de níveis. Este método é por sua vez invocado nos seguintes métodos

						`checkForTool(@NotNull String tool)`
							
						`checkForPickaxe(final int minlevel)`
						
Ambos os métodos têm o mesmo objetivo: verificar se existe alguma ferramenta no inventário do *worker* que o possibilite de executar a sua tarefa. O primeiro método, engloba todas as ferramentas menos a picareta. O segundo método é um caso particular do primeiro, devido às restrições de nível mínimo desta ferramenta (regras do **Minecraft**). Estes métodos são invocados aquando o pedido por parte do jogador ao *NPC* que execute uma tarefa. Se retornar falso, o *worker* (*NPC*) não executará a sua tarefa enquanto não tiver a ferramenta adequada, pedindo-a ao jogador em intervalos de tempo regulares. Se retornar verdadeiro, executa a tarefa. O nosso método é invocado aquando a verificação da existência de ferramentas para executar a tarefa, limitando a escolha dependendo do sistema de níveis.

O segundo método (*getMostEfficientTool()*) já pertencia ao projeto, mas teve de ser ligeiramente alterado para suportar o sistema de níveis. Este método recebe um bloco **target**, e calcula qual a ferramenta adequada para a interação. De seguida percorre o inventário e escolhe (dentro das ferramentas adequadas) a que tem nível maior. Aqui entra o nosso método, pois limita a escolha da maior ferramenta, tendo em consideração o sistema de níveis implementado. Este método é usado quando o *worker* começa a sua tarefa. Embora já existisse a verificação dos níveis no *checkForTools()* (que é executado antes deste método), é necessária uma nova verificação, pois no *checkForTools()* apenas indica se existe pelo menos uma ferramenta válida, e neste método é necessário escolher qual.

Além destes métodos foram necessárias algumas alterações como:

* alteração do conteúdo das mensagens enviadas pelo NPC para o jogador via chat;
* criação do método `swapToolGrade(final int toolGrade)` para a escolha da ferramenta adequada a enviar na mensagem;
* criação de novas constantes;

Os ficheiros alterados podem ser observados na secção de [ficheiros alterados](https://github.com/Minecolonies/minecolonies/pull/352/files) do nosso *pull request*. 

### Resultado ###

(imagem + descrição)

### Dificuldades ###

(texto)

### Análise do impacto desta feature no projeto ###

(texto)

## Pull Request ##

(texto)

##Conclusão ##

(texto)

## Contribuições ##

* [Inês Gomes](https://github.com/inesgomes) (up201405778@fe.up.pt) - 25% - horas: 9

* [Catarina Ramos](https://github.com/catramos96) (up201406219@fe.up.pt) - 25% - horas: 9

* [Mário Fernandes](https://github.com/MarioFernandes73) (up201201705@fe.up.pt) - 25% - horas: 9

* [Manuel Curral](https://github.com/Camolas)  (up201202445@fe.up.pt) - 25% - horas: 9
