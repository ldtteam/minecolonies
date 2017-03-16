package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.util.TeleportToColony;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.COLONYTP;

/**
 * this command is made to TP a player to a friends colony.
 */
public class ColonyTeleportCommand extends AbstractSingleCommand
{
    public static final  String DESC             = "colonytp";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ColonyTeleportCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "colonytp" + "<colID>";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String... args) throws CommandException
    {

        //see if player is allowed to use in the configs
        if (!canCommandSenderUseCommand(COLONYTP))
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString("This is not allowed on this server."));
            return;
        }
        else
        {
            //send the info to the Colony TP utils
            TeleportToColony.ColonyTeleport(server,sender,args[0]);
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions (
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



