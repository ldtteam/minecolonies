package com.minecolonies.coremod.commands;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.commands.colonycommands.HomeTeleportCommand;
import com.minecolonies.coremod.commands.colonycommands.LoadColonyBackupCommand;
import com.minecolonies.coremod.commands.generalcommands.*;
import net.minecraft.entity.player.PlayerEntity;
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
        .put(DeleteCommand.DESC, new DeleteCommand(DESC))
        .put(ColonyCommand.DESC, new ColonyCommand(DESC))
        .put(CitizensCommand.DESC, new CitizensCommand(DESC))
        .put(RandomTeleportCommand.DESC, new RandomTeleportCommand(DESC))
        .put(BackupCommand.DESC, new BackupCommand(DESC))
        .put(HomeTeleportCommand.DESC, new HomeTeleportCommand(DESC))
        .put(LoadColonyBackupCommand.DESC, new LoadColonyBackupCommand(DESC))
        .put(RaidAllTonightCommand.DESC, new RaidAllTonightCommand(DESC))
        .put(RaidAllNowCommand.DESC, new RaidAllNowCommand(DESC))
        .put(CheckForAutoDeletesCommand.DESC, new CheckForAutoDeletesCommand(DESC))
        .put(WhoAmICommand.DESC, new WhoAmICommand(DESC))
        .put(WhereAmICommand.DESC, new WhereAmICommand(DESC))
        .put(LootGenCommand.DESC, new LootGenCommand(DESC))
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
    public static boolean canExecuteCommand(@NotNull final PlayerEntity player)
    {
        if (MineColonies.getConfig().getCommon().gameplay.teleportBuffer == 0 || AbstractSingleCommand.isPlayerOpped(player))
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
            if (Instant.now().isAfter(entry.get()) && (Instant.now().getEpochSecond() - entry.get().getEpochSecond()) > MineColonies.getConfig().getCommon().gameplay.teleportBuffer)
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
