# Default JDK is the source
FROM openjdk:8u222-stretch

# Defining variables.
ARG min_ram=512M
ARG max_ram=3G
ARG forge_version
ARG mc_version
ARG forge_file_version

# Copying the build.properties file
COPY build.properties build.properties

# Create a working directory
WORKDIR /home/server
RUN echo Downloading https://files.minecraftforge.net/maven/net/minecraftforge/forge/$forge_file_version/forge-$forge_file_version-installer.jar
RUN curl https://files.minecraftforge.net/maven/net/minecraftforge/forge/$forge_file_version/forge-$forge_file_version-installer.jar > installer.jar
RUN java -jar installer.jar --installServer
RUN rm -rf mods
RUN ls
RUN echo eula=true >> eula.txt
RUN echo java -server -ms$min_ram -Xmx$max_ram -jar forge-$forge_file_version-universal.jar nogui >> start.sh
RUN chmod +x start.sh

# Vanilla data volumes
VOLUME /home/server/world

# Copy mod files
RUN mkdir /home/server/mods
COPY build/libs/* /home/server/mods
RUN ls /home/server/mods

# Expose ports
EXPOSE 25565

# Setting default run command
CMD /home/server/start.sh
