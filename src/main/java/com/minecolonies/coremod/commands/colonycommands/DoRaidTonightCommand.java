package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
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
 * Command to set whether a colony will raid tonight.
 */
public class DoRaidTonightCommand extends AbstractSingleCommand
{

    public static final  String              DESC                       = "raid-tonight";
    private static final TextComponentString NO_COLONY_FOUND_MESSAGE_ID = new TextComponentString("No Colony found.");
    private static final TextComponentString NO_ARGUMENTS               = new TextComponentString("Please define a colony to raid tonight.");
    private static final TextComponentString SUCCESSFUL                 = new TextComponentString("Command Successful");

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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (sender instanceof EntityPlayer && !isPlayerOpped(sender))
        {
            sender.sendMessage(new TextComponentString("Must be OP to use command"));
            return;
        }

        if (args.length != 0)
        {
            final Colony colony = ColonyManager.getColony(Integer.parseInt(args[0]));
            if (colony == null)
            {
                sender.sendMessage(NO_COLONY_FOUND_MESSAGE_ID);
                return;
            }

            colony.getBarbManager().setWillRaidTonight(true);

            sender.sendMessage(SUCCESSFUL);
        }
        else
        {
            sender.sendMessage(NO_ARGUMENTS);
        }
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
