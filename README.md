[![CLA assistant](https://cla-assistant.io/readme/badge/Minecolonies/minecolonies)](https://cla-assistant.io/Minecolonies/minecolonies)
[![Build Status](https://teamcity.minecolonies.com/app/rest/builds/buildType:Minecolonies_Alpha/statusIcon)](http://teamcity.minecolonies.com/)
[![Quality Gate](https://sonar.minecolonies.com/api/badges/gate?key=Minecolonies%3Aversion%2F1.11&blinking=true)](https://sonar.minecolonies.com/overview?id=Minecolonies%3Aversion%2F1.11)
[![Comment Lines](https://sonar.minecolonies.com/api/badges/measure?key=Minecolonies%3Aversion%2F1.11&blinking=true&metric=comment_lines_density)](https://sonar.minecolonies.com/overview?id=Minecolonies%3Aversion%2F1.11)
[![Lines of Code](https://sonar.minecolonies.com/api/badges/measure?key=Minecolonies%3Aversion%2F1.11&blinking=true&metric=ncloc)](https://sonar.minecolonies.com/overview?id=Minecolonies%3Aversion%2F1.11)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3f8479027286436bbb6add73d309e054)](https://www.codacy.com/app/Minecolonies/minecolonies?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Minecolonies/minecolonies&amp;utm_campaign=Badge_Grade)
[![Stories in Ready](https://badge.waffle.io/Minecolonies/minecolonies.png?label=help%20wanted&title=Help%20Wanted)](http://waffle.io/Minecolonies/minecolonies)

![alt tag](resources/minecolonies.png)


### About the mod ###

**MineColonies** is an interactive Town building mod that allows you to create your own thriving Town within Minecraft. It depicts real-life scenarios by providing you with different craftable options to build your own Town and enhance your gaming experience. Featuring many NPC workers such as: Builders, Crafters, Farmers, Fishermen, Guards, Barracks, Miners, Smeltery, Baker, Cook/Restuarant, Deliveryman, Animal Herders and many more planned for development. As well as a specialized buildings such as: Warehouse, Citizen Huts and a Townhall. Minecolonies gives you the ability to create a colony as rich and unique as every player.

##### Website:
https://www.minecolonies.com/


For Developers
--


Compiling MineColonies
----

IMPORTANT: Please report any issues you have, there might be some problems with the documentation! Also make sure you know EXACTLY what you're doing! It's not any of our faults if your OS crashes, becomes corrupted, etc.

#### Setup Java
The Java JDK is used to compile MineColonies

1. Download and install the Java JDK 8.
    * [Windows](https://adoptopenjdk.net/). Choose OpenJDK 8 (LTS) version and HotSpot JVM, then click latest release button. After download is complete open file, accept licence agreement and in custom setup make sure that `Add to Path` and `Set JAVA_HOME` is set to `Entire feature will be installed on your local hard drive`. 
    Then choose install and wait for instalation to finish.
	* Linux: Installation methods for certain popular flavors of Linux are listed below.  If your distribution is not listed, follow the instructions specific to your package manager or install it manually [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
		* Gentoo: `emerge dev-java/oracle-jdk-bin`
		* Archlinux: `pacman -S jdk8-openjdk`
		* Ubuntu/Debian: `apt-get install openjdk-8-jdk`
		* Fedora: `yum install java-1.8.0-openjdk`
2. Open up your command line and run `javac`.  If it spews out a bunch of possible options and the usage, then you're good to go.  If not, either try the steps again.

##### Trouble shooting
If `javac` command does not work on Windows:
* Go to `Control Panel\System and Security\System`, and click on `Advanced System Settings` on the left-hand side.
* Click on `Environment Variables`.
* Under `System Variables`, click `New`.
* For `Variable Name`, input `JAVA_HOME`.
* For `Variable Value`, input something similar to `C:\Program Files\Java\jdk1.8.0_45` exactly as shown (or wherever your Java JDK installation is), and click `OK`.
* Scroll down to a variable named `Path`, and double-click on it.
* Append `;%JAVA_HOME%\bin` EXACTLY AS SHOWN and click `OK`.  Make sure the location is correct; double-check just to make sure.

#### Setup Gradle (Optional)
Gradle is used to execute the various build tasks when compiling MineColonies

1. Download and install Gradle.
	* [Windows/Mac download link](http://www.gradle.org/downloads).  You only need the binaries, but choose whatever flavor you want.
		* Unzip the package and put it wherever you want, eg `C:\Gradle`.
	* Linux: Installation methods for certain popular flavors of Linux are listed below.  If your distribution is not listed, follow the instructions specific to your package manager or install it manually [here](http://www.gradle.org/downloads).
		* Gentoo: `emerge dev-java/gradle-bin`
		* Archlinux: You'll have to install it from the [AUR](https://aur.archlinux.org/packages/gradle).
		* Ubuntu/Debian: `apt-get install gradle`
		* Fedora: Install Gradle manually from its website (see above), as Fedora ships a "broken" version of Gradle.  Use `yum install gradle` only if you know what you're doing.
2. Windows: Set environment variables for Gradle.
	* Go back to `Environment Variables` and then create a new system variable.
	* For `Variable Name`, input `GRADLE_HOME`.
	* For `Variable Value`, input something similar to `C:\Gradle-3.0` exactly as shown (or wherever your Gradle installation is), and click `Ok`.
	* Scroll down to `Path` again, and append `;%GRADLE_HOME%\bin` EXACTLY AS SHOWN and click `Ok`.  Once again, double-check the location.
3. Open up your command line and run `gradle`.  If it says "Welcome to Gradle [version].", then you're good to go.  If not, either try the steps again.

#### Setup Git
Git is used to clone MineColonies and update your local copy.

1. Download and install Git [here](http://git-scm.com/download/).
2. *Optional*: Download and install a Git GUI client, such as Gitkraken, SourceTree, Github for Windows/Mac, SmartGitHg, TortoiseGit, etc.  A nice list is available [here](http://git-scm.com/downloads/guis).

#### Setup MineColonies (Command-line)
This section assumes that you're using the command-line version of Git.

1. Open up your command line.
2. Navigate to a place where you want to download MineColonies source (eg `C:\Github\MineColonies\`) by executing `cd [folder location]`.  This location is known as `basefolder` from now on.
3. Execute `git clone https://github.com/Minecolonies/minecolonies.git`.  This will download MineColonies' source into `basefolder`.
4. Right now, you should have a directory that looks something like:

***
    basefolder
	\-MineColonies
		\-MineColonies' files (should have `build.gradle`)
***

#### Setup MineColonies (Gitkraken)
If you decide to go with a GUI client like Gitkraken:

1. Open Gitkraken
2. Click File -> Clone Repo
3. Select GitHub.com and choose a base folder to clone to.
4. Write Minecolonies in the Repository to clone and select the one by ldtteam.

![](https://i.imgur.com/jVTXyCJ.png)


5. Click Clone the repo.



#### Compile MineColonies (Command-line)
1. Execute `gradlew setupDecompWorkspace`. This sets up Forge and downloads the necessary libraries to build MineColonies.  This might take some time, be patient.
    * You will generally only have to do this once until the Forge version in `build.properties` changes.
2. Execute `gradlew build`. If you did everything right, `BUILD SUCCESSFUL` will be displayed after it finishes.  This should be relatively quick.
    * If you see `BUILD FAILED`, check the error output (it should be right around `BUILD FAILED`), fix everything (if possible), and try again.
3. Go to `basefolder\MineColonies\build\libs`.
    *  You should see a `.jar` file named `MineColonies-universal-null.jar`.
4. Copy the jar into your Minecraft mods folder, and you are done! (~/.minecraft/mods on Linux)
5. Alternatively, you can also run `./gradlew runClient` to start Minecraft with this jar.

#### Compile MineColonies (Intellij)
1. Right click the build.gradle file and select open with Intellij.
2. Select auto import and make sure a valid gradle and jvm is selected.

![](https://i.imgur.com/ewccjDZ.png)

3. This will load already most imports.
4. Click View -> Tool Windows -> Gradle
5. In the Gradle View go to Tasks -> forgegradle

![](https://i.imgur.com/34H45Tb.png)

6. Execute setupDecompWorkspace. This sets up Forge and downloads the necessary libraries to build MineColonies.  This might take some time, be patient.
7. Click the small refresh symbol in the upper left of the gradle view.
8. Execute genIntellijRuns and restart intellij.
9. You will see a Minecraft Client and Server startup configuration.
10. Execute it with your username as a program argument to have always the same name ingame.

![](https://i.imgur.com/vDvyNN5.png)

11. If it doesn't start and throw a lot of errors try another setupDecompWorkspace which often does wonders.
12. If you want to produce a running jar execute build in the build subfolder. Which will result in a runnable jar in basefolder\MineColonies\build\libs.

##### Trouble shooting
If gradle synchronization fails make sure:
1. `File -> Project structure -> Project -> Project SDK` is set to your installed JDK version.
2. `File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JVM` is set to your installed JDK version.

#### Updating Your Repository
In order to get the most up-to-date builds, you'll have to periodically update your local repository.

1. Open up your command line.
2. Navigate to `basefolder` in the console.
3. Make sure you have not made any changes to the local repository, or else there might be issues with Git.
	* If you have, try reverting them to the status that they were when you last updated your repository.
4. Execute `git pull version/1.12`.  This pulls all commits from the official repository that do not yet exist on your local repository and updates it (With Gitkraken just click the small pull arrow at the top).


#### Trouble shooting
- Sometimes gradle tasks fail because of missing memory, for that you can find system wide settings in the .gradle folder in your HOME directory (~/.gradle/gradle.properties or on Windows in C:\Users\username\.gradle\gradle.properties).
- Sometimes after a branch change if libraries can not be resolved running another setupDecompWorkspace or clicking the refresh button in the intellij gradle view solves many issues.

### Contributing
***
#### Submitting a PR
So you found a bug in our code?  Think you can make it more efficient?  Want to help in general?  Great!

1. If you haven't already, create a Github account.
2. Click the `Fork` icon located at the top-right.
3. Make the changes that you want to and commit them.
	* If you're making changes locally, you'll have to do `git commit -a` and `git push` in your command line. (or with gitkraken stage the changes, commit them and then push them first)
4. Click `Pull Request` in the middle.
5. Click `Click 'new pull request' to create a pull request for this comparison`, enter your PR's title, and create a detailed description telling us what you changed.
6. Click `Create pull request`, and wait for feedback!

#### Creating an Issue
MineColonies crashes every time?  Have a suggestion?  Found a bug?  Create an issue now!

1. Make sure your issue hasn't already been answered or fixed.  Also think about whether your issue is a valid one before submitting it.
2. Go to [the issues page](https://github.com/Minecolonies/minecolonies/issues).
3. Click `New Issue`
4. Fill in the form:
    * `Title`: Short summary of your issue
    * `Description`: A description of what your problem is, with additional info. What have you tried to fix it etc.
    * `Assignee`: (Optional) Assign someone to the issue.
    * `Attachments`: Add the latest.log from %appdata%/.minecraft/logs

5. Click `Submit New Issue`, and wait for feedback!


For Users
--

All our versions can be found downloadable at curse:

https://minecraft.curseforge.com/projects/minecolonies

If you want to chat with the developers and join our amazing community.

https://discord.minecolonies.com
