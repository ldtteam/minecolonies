# Assignment 1 - Software Processes #


## Descrição do projeto ##

### Sobre o jogo ###

MineColonies é uma modificação do famoso jogo "Minecraft" da Mojang. Esta modificação permite a simulação de uma cidade através da adição de estruturas e trabalhadores NPC (Non Player Characters) que podem ser controlados pelo utilizador. Tal liberdade permite a construção desde pequenas aldeias eficientes especializadas num trabalhador até grandes e vibrantes metrópoles! As possibilidades são infinitas!

#### Funcionalidades dentro do jogo ####

A modificação permite ao utilizador gerar automaticamente navios, câmaras municipais, quintas, casas, entre outras estruturas e posteriormente associa-la a um trabalhador. Os trabalhadores, com os seus sistems de niveis e experiência, trabalham no seu ofício, e quanto maior for o seu nível, mais rápidos e eficientes eles são. Existe uma grande variedade de trabalhos, desde construtores, agricultores, mineiros até mesmo guardas da cidade ou carteiros.
Todas estas funcionalidades são feitas a partir do MineColonies, transformando assim o minecraft, um jogo no qual raramente se via outro humano, num jogo bastante diferente e mais emocionante!


## Processo de desenvolvimento ##

### Comunidade ###

A comunidade MineColonies é formada por utilizadores e programadores do jogo. Além da sua página oficial (www.minecolines.com) existe ainda um canal do youtube com vídeos oficiais do jogo. No site é possível receber as últimas novidades do jogo, aceder a fóruns, fazer download do jogo, seguir tutoriais e até inscrever-se como programador.
Os forúns podem ser de comunicação entre jogadores, jogadores e programadores ou só entre programadores. Os programadores são bastante responsivos e dinâmicos.

### Publicações ###

MineColonies não é um projeto de raíz. Depois de conversar com alguns membros da equipa (marvin e raycoms), descobrimos que existia um projeto anterior que foi descontinuado pelo programador. Esse projeto era apenas singleplayer e teria diversos bugs. Mais tarde, um dos integrantes do atual grupo (que não é programador) decidiu pegar no antigo projeto e procurar novos programadores para uma nova abordagem do projeto. Esta nova abordagem data de 27 de abril de 2014.

Desde então, foram lançadas algumas versões do jogo:

  * 9 maio de 2016 (Minecolonies Alpha)
  * 7 setembro de 2016 (Minecolonies Alpha v0.2)
  * 3 outubro 2016 (Minecolonies Alpha v0.4)
 
As novas versões do jogo incluem modo multiplayer, mais trabalhadores, menos bugs e ainda algumas alterações a nível visual. Foi também lançado na primeira semana de outubro um servidor oficial.

### Repositório ###

-> falar aqui sobre o nucleo da equipa (nem todos os contribuidores estão ativos)

O projeto MineColonies tem 13 contribuidores e está numa fase de grande evolução e bastante ativa. No último mês 8 autores fizeram mais de 400 commits, 389 ficheiros foram modificados e houveram 18171 adições e 12280 eliminações; 44 issues foram resolvidas e surgiram 12 novas.

### Fase do projeto ###

-> encontra-se em fase de evolução porque ... (ver alguns aspetos no relatório)

Neste momento a equipa está a focar o seu trabalho na correção de bugs e na preparação de novas etapas a médio prazo, tais como melhorarias de trabalhadores e novas implementações.

### Modelo de desenvolvimento ###

-> explicar o que é este modelo. Justificar com algumas coisas que os programadores responderam

No que diz respeito ao modelo de processo, este trabalho insere-se no "incremental development and delivery", visto que cada funcionalidade é um branch, cada commit é transformado num .jar e cada pull request é libertado ao público automaticamente.

### Estruturação ###

#### Equipa ####

Após fazermos algumas perguntas sobre o desenvolvimento do projeto aos programadores encarregues do minecolonies conseguimos concluir que a maior parte da comunicação é feita via Slack. O forum é utilizado especialmente para falar com a comunidade não programadora, isto é, para receber feedback dos jogadores sobre o projeto.
Relativamente ao trabalho em si, geralmente todos os contribuidores trabalham naquilo que querem e no que mais apreciam. Quando existe uma grande mudança no jogo base, por exemplo, mudanças de versões do minecraft, a fluidez do trabalho pode mudar, mas na maior parte dos casos, existe alguém responsável que expõem algumas coisas que são precisas fazer e, a partir daí, os contribuidores escolhem o que fazem.

#### Código ####

Relativamente ao código em si, é exigido aos contribuidores que sigam regras bastante específicas que vão até ao ponto de mencionar como devem colocar as chavetas em cada função. Embora sejam regras que possam parecer excessivas, num projeto open source é conveniente que todos tenham a mesma maneira de implementar código, visto que programadores podem entrar ou sair a qualquer momento. O código é estruturado em branches e também são usados testes unitários.
Existem 3 mantenedores que maioritariamente verificam o código submitido e após estar pronto, utilizam o SonarQube que irá comentar todos os possíveis problemas que encontrou. Finalmente quando todos os mantenedores verificarem o trabalho do SonarQube (e feito as devidas alterações) o trabalho do programador pode ser implementado.
Cada ponto a trabalhar geralmente é feito apenas por uma pessoa, embora possa haver ajuda, o minecolonies costuma ser um projeto em que se segue uma política de "one branch one person".

## Opiniões, Críticas e Alternativas ##
