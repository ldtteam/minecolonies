# Eclipse additional set-up

Note: You will need an Eclipse with a newer ECJ (eclipse built-in compiler) in order to compile certain lamba statements.   Eclipse Luna did not work.

* execute "gradlew eclipse" after executing "gradlew setupDecompWorkspace".

* Edit .classpath and remove these lines:

```
	<classpathentry kind="lib" path="/home/minecraft/workspaces/luna-minecraft/minecolonies/build/classes/structures"/>
	<classpathentry kind="lib" path="/home/minecraft/workspaces/luna-minecraft/minecolonies/build/resources/structures"/>
	<classpathentry kind="lib" path="/home/minecraft/workspaces/luna-minecraft/minecolonies/build/classes/blockOut"/>
	<classpathentry kind="lib" path="/home/minecraft/workspaces/luna-minecraft/minecolonies/build/resources/blockOut"/>
```

* Install PMD

    Use this update site:

    http://www.acanda.ch/eclipse-pmd/release/latest

* Install checkstyle

    See checkstyle.MD.

* Configure logging 
