[![CLA assistant](https://cla-assistant.io/readme/badge/Minecolonies/minecolonies)](https://cla-assistant.io/Minecolonies/minecolonies)
[![Build Status](https://teamcity.minecolonies.com/app/rest/builds/buildType:Minecolonies_Alpha/statusIcon)](http://teamcity.minecolonies.com/)
[![Quality Gate](https://sonar.minecolonies.com/api/badges/gate?key=Minecolonies%3Aversion%2F1.11&blinking=true)](https://sonar.minecolonies.com/overview?id=Minecolonies%3Aversion%2F1.11)
[![Comment Lines](https://sonar.minecolonies.com/api/badges/measure?key=Minecolonies%3Aversion%2F1.11&blinking=true&metric=comment_lines_density)](https://sonar.minecolonies.com/overview?id=Minecolonies%3Aversion%2F1.11)
[![Lines of Code](https://sonar.minecolonies.com/api/badges/measure?key=Minecolonies%3Aversion%2F1.11&blinking=true&metric=ncloc)](https://sonar.minecolonies.com/overview?id=Minecolonies%3Aversion%2F1.11)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3f8479027286436bbb6add73d309e054)](https://www.codacy.com/app/Minecolonies/minecolonies?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Minecolonies/minecolonies&amp;utm_campaign=Badge_Grade)
[![Stories in Ready](https://badge.waffle.io/Minecolonies/minecolonies.png?label=help%20wanted&title=Help%20Wanted)](http://waffle.io/Minecolonies/minecolonies)



![alt tag](resources/minecolonies.png)

### About the mod ###

   **MineColonies** is a mod for the famous game *Minecraft* from Mojang. This mod allows the user to generate many structures like ships, town halls, farms and even NPC's which are called workers. These workers can be assigned by the player to do various things ranging from building structures, gathering resources and even protecting existing buildings! Workers also have an experience and leveling system: the more they work, the more experience they'll get, allowing them to complete their tasks a lot faster.
    The freedom and convenience given by this mod allows anyone to build many different cities: they can be small, specialized villages or big and vibrant metropolis. The possibilities are endless!

##### Website:
https://www.minecolonies.com/


For Users
--


Compiling MineColonies
----

IMPORTANT: Please report any issues you have, there might be some problems with the documentation! Also make sure you know EXACTLY what you're doing! It's not any of our faults if your OS crashes, becomes corrupted, etc.

#### Setup Java
The Java JDK is used to compile MineColonies

1. Download and install the Java JDK 8.
    * [Windows/Mac download link](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).  Scroll down, accept the `Oracle Binary Code License Agreement for Java SE`, and download it (if you have a 64-bit OS, please download the 64-bit version).
	* Linux: Installation methods for certain popular flavors of Linux are listed below.  If your distribution is not listed, follow the instructions specific to your package manager or install it manually [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
		* Gentoo: `emerge dev-java/oracle-jdk-bin`
		* Archlinux: `pacman -S jdk8-openjdk`
		* Ubuntu/Debian: `apt-get install openjdk-8-jdk`
		* Fedora: `yum install java-1.8.0-openjdk`
2. Windows: Set environment variables for the JDK.
    * Go to `Control Panel\System and Security\System`, and click on `Advanced System Settings` on the left-hand side.
    * Click on `Environment Variables`.
    * Under `System Variables`, click `New`.
    * For `Variable Name`, input `JAVA_HOME`.
    * For `Variable Value`, input something similar to `C:\Program Files\Java\jdk1.8.0_45` exactly as shown (or wherever your Java JDK installation is), and click `OK`.
    * Scroll down to a variable named `Path`, and double-click on it.
    * Append `;%JAVA_HOME%\bin` EXACTLY AS SHOWN and click `OK`.  Make sure the location is correct; double-check just to make sure.
3. Open up your command line and run `javac`.  If it spews out a bunch of possible options and the usage, then you're good to go.  If not, either try the steps again.

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
2. *Optional*: Download and install a Git GUI client, such as SourceTree, Github for Windows/Mac, SmartGitHg, TortoiseGit, etc.  A nice list is available [here](http://git-scm.com/downloads/guis).

#### Setup MineColonies
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

#### Compile MineColonies
1. Execute `gradlew setupDecompWorkspace`. This sets up Forge and downloads the necessary libraries to build MineColonies.  This might take some time, be patient.
    * You will generally only have to do this once until the Forge version in `build.properties` changes.
2. Execute `gradlew build`. If you did everything right, `BUILD SUCCESSFUL` will be displayed after it finishes.  This should be relatively quick.
    * If you see `BUILD FAILED`, check the error output (it should be right around `BUILD FAILED`), fix everything (if possible), and try again.
3. Go to `basefolder\MineColonies\build\libs`.
    *  You should see a `.jar` file named `MineColonies-universal--0.0.#.jar`, where # is the `build_number` value in `build.properties`.
4. Copy the jar into your Minecraft mods folder, and you are done! (~/.minecraft/mods on Linux)
5. Alternatively, you can also run `./gradlew runClient` to start Minecraft with this jar.

#### Updating Your Repository
In order to get the most up-to-date builds, you'll have to periodically update your local repository.

1. Open up your command line.
2. Navigate to `basefolder` in the console.
3. Make sure you have not made any changes to the local repository, or else there might be issues with Git.
	* If you have, try reverting them to the status that they were when you last updated your repository.
4. Execute `git pull master`.  This pulls all commits from the official repository that do not yet exist on your local repository and updates it.

### Contributing
***
#### Submitting a PR
So you found a bug in our code?  Think you can make it more efficient?  Want to help in general?  Great!

1. If you haven't already, create a BitBucket account.
2. Click the `Fork` icon located at the top-right, when you click on the triple dots of this page (below your username).
3. Make the changes that you want to and commit them.
	* If you're making changes locally, you'll have to do `git commit -a` and `git push` in your command line.
4. Click `Pull Request` at the left of the triple dot.
5. Click `Click to create a pull request for this comparison`, enter your PR's title, and create a detailed description telling us what you changed.
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

~~NOTE: A video explaining how to is [here](http://youtu.be/2SscjHmLvss).~~

For Developers
--

PR's (pull requests) are required to get code onto the develop and master branches.

To do this, the easiest way is to download [sourcetree](http://www.sourcetreeapp.com/) or [gitkraken](https://www.gitkraken.com/). Install it, and add a repository (either clone, or select working directory)
Then, click on branch, and create a new one. You can push to your own branch! Make sure your branch is selected, before you push! [Example](http://gyazo.com/4b453a55a8baf59f573bb1c4636a5ca2) So it should be big black. Once you are satisfied, go to [github](https://github.com/Minecolonies/minecolonies/compare), and create a pull request.

