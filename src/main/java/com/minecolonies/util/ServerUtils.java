package com.minecolonies.util;

import com.minecolonies.colony.permissions.Permissions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Utility for server related stuff.
 * <p>
 * Here you can query players on the server.
 *
 * @since 0.2
 */
public final class ServerUtils
{

    /**
     * Private constructor to hide the implicit public one.
     */
    private ServerUtils()
    {
    }

    /**
     * Returns the online EntityPlayer with the given UUID.
     *
     * @param world world the player is in
     * @param id    the player's UUID
     * @return the Player
     */
    @Nullable
    public static EntityPlayer getPlayerFromUUID(@NotNull World world, @NotNull UUID id)
    {
        for (int i = 0; i < world.playerEntities.size(); ++i)
        {
            if (id.equals((world.playerEntities.get(i)).getGameProfile().getId()))
            {
                return world.playerEntities.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a list of online players whose UUID's match the ones provided.
     *
     * @param world the world the players are in.
     * @param ids   List of UUIDs
     * @return list of EntityPlayers
     */
    @NotNull
    public static List<EntityPlayer> getPlayersFromUUID(@NotNull World world, @NotNull Collection<UUID> ids)
    {
        @NotNull final List<EntityPlayer> players = new ArrayList<>();

        for (final Object o : world.playerEntities)
        {
            if (o instanceof EntityPlayer)
            {
                @NotNull final EntityPlayer player = (EntityPlayer) o;
                if (ids.contains(player.getGameProfile().getId()))
                {
                    players.add(player);
                    if (players.size() == ids.size())
                    {
                        return players;
                    }
                }
            }
        }
        return players;
    }

    /**
     * Returns a list of players from a list of {@link Permissions.Player}.
     * <p>
     * The {@link Permissions.Player} is a wrapper around a {@link UUID} of minecraft players.
     * The List will simply be converted into an {@link EntityPlayer} type.
     * <p>
     * Uses {@link ServerUtils#getPlayerFromPermPlayer(Permissions.Player)}.
     *
     * @param players The list of players to convert.
     * @return A list of {@link EntityPlayer}s
     */
    @NotNull
    public static List<EntityPlayer> getPlayersFromPermPlayer(@NotNull List<Permissions.Player> players)
    {
        @NotNull final List<EntityPlayer> playerList = new ArrayList<>();

        for (@NotNull final Permissions.Player player : players)
        {
            playerList.add(ServerUtils.getPlayerFromPermPlayer(player));
        }

        return playerList;
    }

    /**
     * Retrieves a Player from {@link Permissions.Player}.
     * <p>
     * Simply converts our type into the base type.
     * <p>
     * Passes this {@link Permissions.Player#getID()} to {@link ServerUtils#getPlayerFromUUID(UUID)}.
     *
     * @param player The {@link Permissions.Player} to convert
     * @return The {@link EntityPlayer} reference.
     */
    @Nullable
    public static EntityPlayer getPlayerFromPermPlayer(@NotNull Permissions.Player player)
    {
        return ServerUtils.getPlayerFromUUID(player.getID());
    }

    /**
     * Finds a player by his UUID
     * <p>
     * Found on <a href="http://jabelarminecraft.blogspot.de/p/minecraft-forge-172-finding-block.html">jabelarminecraft.</a>
     *
     * @param uuid the uuid to search for
     * @return The player the player if found or null
     */
    @Nullable
    public static EntityPlayer getPlayerFromUUID(@Nullable UUID uuid)
    {
        if (uuid == null)
        {
            return null;
        }
        final List<EntityPlayerMP> allPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (@NotNull final EntityPlayerMP player : allPlayers)
        {
            if (player.getUniqueID().equals(uuid))
            {
                return player;
            }
        }
        return null;
    }
}
