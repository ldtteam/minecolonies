package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.mobs.util.MobEventsUtils;
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
 * Trigger a raid event at run
 */
public class DoRaidNowCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final  String              DESC                       = "raid";
    private static final StringTextComponent NO_COLONY_FOUND_MESSAGE = new StringTextComponent("No Colony found.");
    private static final StringTextComponent NO_ARGUMENTS               = new StringTextComponent("Please define a colony to raid.");
    private static final StringTextComponent SUCCESSFUL                 = new StringTextComponent("Command Successful");

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public DoRaidNowCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public DoRaidNowCommand(@NotNull final String... parents)
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
        final IColony colony = actionMenuState.getColonyForArgument("colony");
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
        IColony colony = null;
        if (args.length != 0)
        {
            colony = IColonyManager.getInstance().getColonyByWorld(Integer.parseInt(args[0]), server.getWorld(sender.getEntityWorld().world.getDimension().getType().getId()));
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

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @Nullable final IColony colony)
    {
        if (sender instanceof PlayerEntity && !isPlayerOpped(sender))
        {
            sender.sendMessage(new StringTextComponent("Must be OP to use command"));
            return;
        }

        MobEventsUtils.raiderEvent(colony.getWorld(), colony);
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
