# Assignment 3 - Software Design #

![alt tag](resources/minecolonies.png)

## Introdução ##

## Logical View ##

![alt tag](resources/Diagrama de Lógica.png)

A "logical view"  encontra-se integrada na primeira etapa das "software architecture views", que consiste num conjunto de design de ideias que servem de suporte para a elaboração de um sistema de software que encontre uma solução adequada ao problema detectado. A missão dela consiste em transmitir à maneira como o sistema de software se encontra estruturalmente organizado a nível de comunicação, computacional e comportamental.

No projecto do Minecolonies optou-se por representar esta vista através de um diagrama UML de caixas ou packages em que se mostram os componentes do sistema encadeados e os conectores que interligam uns aos outros entre si.
O projecto por nós escolhido é constituído por packages que chegam a 1 ou 2 camadas de packages. Porventura ocorre a excepção com o package relacionado com os actores do jogo tem uma cadeia de 4 packages que estão representados pelos símbolos de nesting sendo o entity o package "Base" (mais exterior) culminando esta herança de packages nas personagens do jogo: "Builder, Deliveryman,Farmer,Fisherman,Lumberjack" e "Miner".

Desta forma, optasse por dar mais ênfase as classes envolvente com o jogo propriamente dito e a sua lógica (componentes funcionais do modo de jogo) que são o entity e o colonies.
O diagrama aqui apresentado resulta dessa simplificação das dependências entre os packages, onde ocultou-se associações de packages menos relevantes para tornar o diagrama mais objectivo e familiar, devido a complexidade do sistema que estamos a lidar.

O "package Client" (utilizador) associa-se diretamente com o "Colony"  (ambiente do jogo) , com o "Entity" (agentes do jogo) e camadas subsequentes da generalizada, bem como os serviços "Network" de rede e o "Lib" (variedade constantes).

## Development View ##

![alt tag](resources/Diagrama de Componentes.png)

Sendo o MineColonies uma modificação de um outro jogo (Minecraft), existem muitas funcionalidades bases que terão que ser reutilizadas e outras alteradas de forma a conseguir enquadrar as novas funcionalidades. Por esta razão, no Development View, a componente Game Modification Minecolonies depende da componente Game Base Minecraft.

Dentro da componente Game Modification Minecolonies, a componente Manager faz o tratamento, tanto da parte do cliente, como da parte do servidor, da informação recebida/enviada através da componente de Network, que trata das ligações cliente-servidor via proxy. A informação é tratada conforme o disponibilizado pela componente Data. Esta última, caracteriza toda a nova informação implementada pelo mod e que, por sua vez, depende de Items que define os novos itens inseridos no jogo. Temos ainda a componente Achievements que representa os novos tipo de objetivos alcançados pelo jogador. Esta componente depende da informação na componente Data.
	
Dentro da componente Data, a componente Colony, que define a colónia e a sua gestão, tem dependências diretas com as componentes: Citizen, Permissions e Task. A primeira, traduz os cidadãos da colónia que podem ou não estar empregados, daí a sua dependência com a componente Job. Esta última, depende da componente Building, onde o trabalho tem lugar, e da componente Citizen, os empregados. A componente Building está relacionado com um Block específico dependendo do tipo de trabalho a executar. Permissions é a componente que permite à colónia definir os privilégios para cada jogador. Já a componente Task permite fazer a gestão de trabalhos na colónia e os seus cidadãos, incluindo a construção de novos edifícios, daí a dependência com Block.

## Deployment View ##

![alt tag](resources/Diagrama de Deployment.png)

De acordo com o diagrama, é possível observar que existem 3 elementos obrigatórios para a utilização do MineColonies: um computador, um sistema operativo compatível e uma versão do jogo Minecraft com a modificação MineColonies instalada. O computador deverá obedecer aos seguintes requisitos mínimos:

* CPU: Intel Pentium D or AMD Athlon 64 (K8) 2.6 GHz
* RAM: 2GB
* GPU (Integrated): Intel HD Graphics or AMD (formerly ATI) Radeon HD Graphics with OpenGL 2.1
* GPU (Discrete): Nvidia GeForce 9600 GT or AMD Radeon HD 2400 with OpenGL 3.1
* HDD: At least 200MB for Game Core and Other Files
* Java 6 Release 45

O sistema operativo terá de ter o jogo minecraft instalado que por sua vez terá de ter a modificação minecolonies. Esta modificação depende bastante da versão do minecraft no computador do utilizador por razões de compatibilidade.

## Process View ##

## Contribuições ##

* [Inês Gomes](https://github.com/inesgomes) (up201405778@fe.up.pt) - X% - horas: X

* [Catarina Ramos](https://github.com/catramos96) (up201406219@fe.up.pt) - X% - horas: X

* [Mário Fernandes](https://github.com/MarioFernandes73) (up201201705@fe.up.pt) - X% - horas: X

* [Manuel Curral](https://github.com/Camolas)  (up201202445@fe.up.pt) - X% - horas: X
