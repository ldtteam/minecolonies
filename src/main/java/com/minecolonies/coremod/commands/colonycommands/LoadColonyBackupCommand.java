package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import com.minecolonies.coremod.util.BackUpHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * List all colonies.
 */
public class LoadColonyBackupCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final String DESC = "loadBackup";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public LoadColonyBackupCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public LoadColonyBackupCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId|OwnerName>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        /*
         * Do nothing no compat required.
         */
    }

    @Override
    public boolean canRankUseCommand(@NotNull final IColony colony, @NotNull final EntityPlayer player)
    {
        return colony.getPermissions().getRank(player).equals(Rank.OWNER);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        if (!isPlayerOpped(sender))
        {
            sender.sendMessage(new TextComponentString(NOT_PERMITTED));
            return;
        }

        final int colonyId = actionMenuState.getIntegerForArgument("colony");
        final int dimension = actionMenuState.getIntegerForArgument("dimension");
        server.addScheduledTask(() -> BackUpHelper.loadColonyBackup(colonyId, dimension, true));
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
      @NotNull final MinecraftServer server,
      @NotNull final ICommandSender sender,
      @NotNull final String[] args,
      @Nullable final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return index == 0
                 && args.length > 0
                 && !args[0].isEmpty()
                 && getIthArgument(args, 0, Integer.MAX_VALUE) == Integer.MAX_VALUE;
    }
}
