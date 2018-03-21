package com.minecolonies.coremod.commands.generalcommands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenu;
import com.minecolonies.coremod.commands.IActionCommand;
import com.minecolonies.coremod.entity.ai.mobs.util.MobEventsUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

/**
 * Created by Asher on 19/7/17.
 */
public class RaidAllNowCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final  String              DESC       = "raid-now";
    private static final TextComponentString SUCCESSFUL = new TextComponentString("Command Successful");

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public RaidAllNowCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public RaidAllNowCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId>";
    }

    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenu actionMenu) throws CommandException
    {
        executeShared(server, sender);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        executeShared(server, sender);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender) throws CommandException
    {
        if (sender instanceof EntityPlayer && !isPlayerOpped(sender))
        {
            sender.sendMessage(new TextComponentString("Must be OP to use command"));
            return;
        }

        for (final Colony colony : ColonyManager.getColonies())
        {
            MobEventsUtils.barbarianEvent(colony.getWorld(), colony);
        }

        sender.sendMessage(SUCCESSFUL);
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
