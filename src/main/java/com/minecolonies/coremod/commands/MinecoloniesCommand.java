package com.minecolonies.coremod.commands;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.commands.colonycommands.HomeTeleportCommand;
import com.minecolonies.coremod.commands.generalcommands.BackupCommand;
import com.minecolonies.coremod.commands.generalcommands.RandomTeleportCommand;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Minecolonies root command.
 * <p>
 * Manages all sub commands.
 */
public class MinecoloniesCommand extends AbstractSplitCommand
{
    public static final  String             DESC              = "minecolonies";
    private static final Map<UUID, Instant> commandExecutions = new HashMap<>();

    private final ImmutableMap<String, ISubCommand> subCommands =
      new ImmutableMap.Builder<String, ISubCommand>()
        .put(ColoniesCommand.DESC, new ColoniesCommand(DESC))
        .put(ColonyCommand.DESC, new ColonyCommand(DESC))
        .put(CitizensCommand.DESC, new CitizensCommand(DESC))
        .put(RandomTeleportCommand.DESC, new RandomTeleportCommand(DESC))
        .put(BackupCommand.DESC, new BackupCommand(DESC))
        .put(HomeTeleportCommand.DESC, new HomeTeleportCommand(DESC))
        .build();

    /**
     * Initialize this command with the canon alias.
     */
    public MinecoloniesCommand()
    {
        super(DESC);
    }

    /**
     * Check if the player is able to execute a teleport command again.
     *
     * @param player the player executing.
     * @return true if should be able to.
     */
    public static boolean canExecuteCommand(@NotNull final EntityPlayer player)
    {
        if (Configurations.gameplay.teleportBuffer == 0)
        {
            return true;
        }

        cleanUpList();
        final boolean canTeleport = !commandExecutions.containsKey(player.getUniqueID());

        if (canTeleport)
        {
            commandExecutions.put(player.getUniqueID(), Instant.now());
        }
        return canTeleport;
    }

    /**
     * Clean up the commandExecutions list according to config details.
     */
    private static void cleanUpList()
    {
        final Map<UUID, Instant> mapCopy = new HashMap<>(commandExecutions);
        for (final Map.Entry<UUID, Instant> entry : mapCopy.entrySet())
        {
            if (Instant.now().isAfter(entry.getValue()) && (Instant.now().getEpochSecond() - entry.getValue().getEpochSecond()) > Configurations.gameplay.teleportBuffer)
            {
                commandExecutions.remove(entry.getKey());
            }
        }
    }

    @Override
    public Map<String, ISubCommand> getSubCommands()
    {
        return subCommands;
    }
}
