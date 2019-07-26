package com.minecolonies.coremod.commands.colonycommands.requestsystem;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
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

public class RSResetAllCommand extends AbstractSingleCommand implements IActionCommand
{
    public static final  String DESC            = "rsResetAll";
    private static final String SUCCESS_MESSAGE = "After 1.618 Seconds it reinstantiated all colonies completely new.";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public RSResetAllCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public RSResetAllCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <(Optional)Player>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
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
        if (sender instanceof EntityPlayer)
        {
            final EntityPlayer player = (EntityPlayer) sender;
            if (!isPlayerOpped(player))
            {
                sender.sendMessage(new TextComponentString(NOT_PERMITTED));
                return;
            }
        }

        for (final IColony colony: IColonyManager.getInstance().getAllColonies())
        {
            colony.getRequestManager().reset();
        }
        sender.sendMessage(new TextComponentString(String.format(SUCCESS_MESSAGE)));
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
        return false;
    }
}
