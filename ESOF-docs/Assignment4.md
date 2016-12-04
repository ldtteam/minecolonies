# Assignment 3 - Software Design #

![alt tag](resources/minecolonies.png)

## Introdução ##

## Testabilidade e Review do Software ##

### Controlabilidade ###

> "How easy it is to provide a program with the needed inputs, in terms of values, operations, and behaviors." - Introduction to Software Testing, page 36, Paul Ammann and Jeff Offputt

### Observabilidade ###

> "How easy it is to observe the behavior of a program in terms of its outputs, effects on the environment, and other hardware and software components." - Introduction to Software Testing, page 36, Paul Ammann and Jeff Offputt

O **MineColonies** utiliza as ferramentas disponibilizadas pelo SonarQube para controlo de qualidade do código, nomeadamente, execução de testes unitários sempre que há um build ou pull request do projeto. No SonarQube temos acesso à informação relacionada com estatísticas sobre o programa onde podemos obter: resultados, gráficos e os pacotes/ficheiros onde essas estatísticas foram aplicadas. A partir da página principal de estatísticas do [SonarQube para o MineColonies](http://home.kk-sc.de:9000/dashboard/index?id=com.minecolonies%3Aminecolonies%3Adevelop) temos acesso a determinada informação, sendo a mais relevante:

- Quality Gate - nível de qualidade atribuído pelos parâmetros do SonarQube
- Critical Issues - problemas no programa de nível crítico
- Bugs & Vulnerabilities - procura bugs e vulnerabilidades no código
- Code Smells - quantidade de code smells no programa
- Coverage - quantidade de linhas de código cobertas pelos testes unitários
- Duplications - quantidade de código duplicado em todo o projeto

Em suma, a observabilidade dos resultados dos testes é boa porque a partir da informação disponível acima é possível proceder de uma forma eficiente e direta à resolução ou detecção de eventuais problemas ou más implementações no programa. Contudo, a quantidade e cobertura de testes unitários também é um fator decisivo para a obtenção de algumas destas estatísticas, pelo que, este projeto está em falta, devido ao número reduzido de testes.

### Isolabilidade ###

> "The degree to which the component under test (CUT) can be tested in isolation." - [Software testability, Wikipedia](https://en.wikipedia.org/wiki/Software_testability)

### Separação de Responsabilidades ###

> "The degree to which the component under test has a single, well defined responsibility." - [Software testability, Wikipedia](https://en.wikipedia.org/wiki/Software_testability)

### Documentação e facilidade de leitura ###

> "The degree to which the component under test is documented or self-explaining." - [Software testability, Wikipedia](https://en.wikipedia.org/wiki/Software_testability)

Em geral, todo o projeto encontra-se documentado e o código é auto-explicativo. No entanto, existem algumas partes em que a documentação de uma determinada classe ou atributo poderia ser mais específica ou explicada, no caso de ser inexistente. Para além da documentação direta no código, também podemos encontrar um ficheiro README.md com mais explicações sobre o pacote em casos que seja necessário para complementar a documentação já existente, como é o caso do package colony.

Em termos de facilidade de leitura, devido à existência de um code style muito bem definido na [wiki](https://github.com/Minecolonies/minecolonies/wiki), a apresentação e organização do código é bem legível e homogénea. A existência da definição do code style a usar é fundamental para a legibilidade do código visto que existem muito colaboradores. Por vezes, a leitura pode ser um bocado cansativa devido à grande extensão de algumas classes, o que não se consegue evitar por ser, em si, um projeto grande e complexo.

### Heterogeneidade de tecnologias e métodos ###

> "The degree to which the use of diverse technologies requires to use diverse test methods and tools in parallel." - [Software testability, Wikipedia](https://en.wikipedia.org/wiki/Software_testability)

## Estatísticas de testes ##

## Bug Report ##

- Pesquisa de um bug

Na etapa de identificação de novos bugs, o grupo começou por instalar o Minecraft com a modificação Minecolonies, na expectativa de, utilizando a técnica dinâmica, conseguir encontrar um novo bug. Nesta fase o grupo contactou a equipa responsável para tentar testar algumas partes mais sensíveis e susceptíveis a problemas. No entanto, o esforço foi um pouco em vão, tendo em conta que a última release tinha apenas poucos dias, e as novas modificações estariam relacionadas com conteúdos mais avançados da modificação.Na nossa opinião, encontrar um bug num projeto tão maturo e complexo em termos de etapas de jogo, é uma tarefa bastante difícil e por vezes um mero acaso, pelo que pode demorar horas de jogo para ser encontrado. Dito isto, o grupo decidiu resolver um bug já reportado nas issues do projeto.

- Correção do [bug](https://github.com/Minecolonies/minecolonies/issues/241)

Com isto em mente, o grupo resolveu tratar de um bug já reportado anteriormente (issue #241). No minecraft existem uns blocos específicos que têm a funcionalidade de armazenar diversos itens e, na sua destruição, todos os itens que residem no interior desses mesmos devem cair ao chão. No minecolonies foram criados novos blocos que, para além de várias funcionalidades (como criar casas, recrutar trabalhadores etc.) também têm esta capacidade de servir de cofre ao utilizador. Na destruição destes blocos, todos os itens armazenados nestes blocos eram destruídos ao invés de cair ao chão. Após o tratamento do bug por parte do grupo, estes blocos têm já apresentam a funcionalidade pretendida.
A correção do bug começou pela identificação do método responsável pela destruição de blocos. Este método, “destroy()”, está definido na classe abstrata “AbstractBuilding”, e invoca um outro,”onDestroyed()” , que gere os eventos relacionados com a sua destruição. 
Ao longo da correção do bug, o grupo deparou-se com algumas dificuldades sendo a principal o desconhecimento do código base. Tendo em conta a extensão do projeto, teria sido impossível o reconhecimento do bug. O facto de já conhecermos o funcionamento geral do projeto (dados adquiridos ao longo do desenvolvimento da cadeira), facilitou a tarefa. Outra dificuldade encontrada foi o facto de o MineCraft Forge ter uma documentação muito pobre. O desconhecimento total do source code do Minecraft (não é open source), deixou-nos como única opção a análise de código puro da versão descompilada do MineCraft Forge.
Por fim, o bug foi resolvido utilizando dois métodos já implementados pelo minecraft: o primeiro que remove o bloco do mundo, criando os itens dentro dele armazenados em coordenadas ao acaso no espaço do antigo bloco, e outro que dá um update ao mundo para registar o evento.

- [Pull request](https://github.com/Minecolonies/minecolonies/pull/314)

Após a correção do bug, o grupo fez um pull request para o branch “develop” (branch que contém sempre a versão funcional da modificação). A pedido dos developers do projeto, foi criado um branch no projeto original (não no fork do grupo) com a correção do bug. O pull request foi depois realizado a partir desse branch. Este passo foi necessário para evitar conflitos com as verificações do Travis CI. Verificamos mais uma vez o cuidado intenso que é colocado neste projeto quando tivemos de fazer alterações ao código para corresponder a todos os parâmetros (tais como remover uma condição que iria dar sempre verdade, alterações de code style e ainda de documentação com o javadoc). Por fim o pull request passou no último parâmetro (já descrito na secção não sei o que), onde kostronor e raycoms aceitaram o nosso pull request.




## Bibliografia ##

## Contribuições ##

* [Inês Gomes](https://github.com/inesgomes) (up201405778@fe.up.pt) - X% - horas: X

* [Catarina Ramos](https://github.com/catramos96) (up201406219@fe.up.pt) - X% - horas: X

* [Mário Fernandes](https://github.com/MarioFernandes73) (up201201705@fe.up.pt) - X% - horas: X

* [Manuel Curral](https://github.com/Camolas)  (up201202445@fe.up.pt) - X% - horas: X
