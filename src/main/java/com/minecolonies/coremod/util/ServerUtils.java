package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.permissions.ColonyPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
     * Returns the online PlayerEntity with the given UUID.
     *
     * @param world world the player is in
     * @param id    the player's UUID
     * @return the Player
     */
    @Nullable
    public static ColonyPlayer getPlayerFromUUID(@NotNull final Level world, @NotNull final UUID id)
    {
        for (int i = 0; i < world.players().size(); ++i)
        {
            if (id.equals((world.players().get(i)).getGameProfile().getId()))
            {
                return world.players().get(i);
            }
        }
        return null;
    }

    /**
     * Returns a list of online players whose UUID's match the ones provided.
     *
     * @param world the world the players are in.
     * @param ids   List of UUIDs
     * @return list of PlayerEntitys
     */
    @NotNull
    public static List<ColonyPlayer> getPlayersFromUUID(@Nullable final Level world, @NotNull final Collection<UUID> ids)
    {
        if (world == null)
        {
            return Collections.emptyList();
        }
        @NotNull final List<ColonyPlayer> players = new ArrayList<>();

        for (final Object o : world.players())
        {
            if (o instanceof ColonyPlayer)
            {
                @NotNull final ColonyPlayer player = (ColonyPlayer) o;
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
     * Returns a list of players from a list of {@link ColonyPlayer}.
     * <p>
     * The {@link ColonyPlayer} is a wrapper around a {@link UUID} of minecraft players. The List will simply be converted into an {@link PlayerEntity} type.
     * <p>
     * Uses {@link ServerUtils#getPlayerFromPermPlayer(ColonyPlayer, World)}.
     *
     * @param players The list of players to convert.
     * @param world   an instance of the world.
     * @return A list of {@link PlayerEntity}s
     */
    @NotNull
    public static List<ColonyPlayer> getPlayersFromPermPlayer(@NotNull final List<ColonyPlayer> players, @NotNull final Level world)
    {
        @NotNull final List<ColonyPlayer> playerList = new ArrayList<>();

        for (@NotNull final ColonyPlayer player : players)
        {
            playerList.add(ServerUtils.getPlayerFromPermPlayer(player, world));
        }

        return playerList;
    }

    /**
     * Retrieves a Player from {@link ColonyPlayer}.
     * <p>
     * Simply converts our type into the base type.
     * <p>
     * Passes this {@link ColonyPlayer#getID()} to {@link ServerUtils#getPlayerFromUUID(UUID, World)}.
     *
     * @param player The {@link ColonyPlayer} to convert
     * @param world  an instance of the world.
     * @return The {@link PlayerEntity} reference.
     */
    @Nullable
    public static ColonyPlayer getPlayerFromPermPlayer(@NotNull final ColonyPlayer player, @NotNull final Level world)
    {
        return ServerUtils.getPlayerFromUUID(player.getID(), world);
    }

    /**
     * Finds a player by his UUID
     * <p>
     * Found on <a href="http://jabelarminecraft.blogspot.de/p/minecraft-forge-172-finding-block.html">jabelarminecraft.</a>
     *
     * @param uuid  the uuid to search for
     * @param world an instance of the world.
     * @return The player the player if found or null
     */
    @Nullable
    public static ColonyPlayer getPlayerFromUUID(@Nullable final UUID uuid, @NotNull final Level world)
    {
        if (uuid == null)
        {
            return null;
        }
        final List<ServerPlayer> allPlayers = world.getServer().getPlayerList().getPlayers();
        for (@NotNull final ServerPlayer player : allPlayers)
        {
            if (player.getUUID().equals(uuid))
            {
                return player;
            }
        }
        return null;
    }
}
