package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.util.TeleportToColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.HOMETP;

/**
 * this command is made to TP a player to their home colony.
 */
public class HomeTeleportCommand extends AbstractSingleCommand
{
    /**
     * The description.
     */
    public static final String DESC = "home";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public HomeTeleportCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "home";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String... args) throws CommandException
    {
        //see if player is allowed to use in the configs
        if (canCommandSenderUseCommand(HOMETP))
        {
            TeleportToColony.colonyTeleport(server, sender);
            return;
        }
        else
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString("This is not allowed on this server."));
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
            @NotNull final MinecraftServer server,
            @NotNull final ICommandSender sender,
            @NotNull final String[] args,
            final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull String[] args, int index)
    {
        return index == 0;
    }
}



