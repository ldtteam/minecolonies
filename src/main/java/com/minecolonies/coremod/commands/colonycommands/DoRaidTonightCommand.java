package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Command to set whether a colony will raid tonight.
 */
public class DoRaidTonightCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final  String              DESC                       = "raid-tonight";
    private static final StringTextComponent NO_COLONY_FOUND_MESSAGE = new StringTextComponent("No Colony found.");
    private static final StringTextComponent NO_ARGUMENTS               = new StringTextComponent("Please define a colony to raid tonight.");
    private static final StringTextComponent SUCCESSFUL                 = new StringTextComponent("Command Successful");

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public DoRaidTonightCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public DoRaidTonightCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final Colony colony = actionMenuState.getColonyForArgument("colony");
        if (colony == null)
        {
            sender.sendMessage(NO_COLONY_FOUND_MESSAGE);
            return;
        }

        executeShared(server, sender, colony);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        Colony colony = null;
        if (args.length != 0)
        {
            colony = ColonyManager.getColonyByWorld(Integer.parseInt(args[0]), server.getWorld(sender.getEntityWorld().provider.getDimension()));
            if (colony == null)
            {
                sender.sendMessage(NO_COLONY_FOUND_MESSAGE);
                return;
            }
        }
        else
        {
            sender.sendMessage(NO_ARGUMENTS);
            return;
        }

        executeShared(server, sender, colony);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @Nullable final Colony colony)
    {
        if (sender instanceof PlayerEntity && !isPlayerOpped(sender))
        {
            sender.sendMessage(new StringTextComponent("Must be OP to use command"));
            return;
        }

        colony.getRaiderManager().setWillRaidTonight(true);
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
