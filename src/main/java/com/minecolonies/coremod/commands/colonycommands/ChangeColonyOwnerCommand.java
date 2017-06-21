package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.IColony;
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

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.CHANGE_COLONY_OWNER;

/**
 * gives ability to change the colony owner.
 */
public class ChangeColonyOwnerCommand extends AbstractSingleCommand
{

    public static final  String       DESC            = "ownerchange";
    private static final String       SUCCESS_MESSAGE = "Succesfully switched Owner %s to colony %d";
    private static final String       COLONY_NULL     = "Couldn't find colony %d.";
    private static final String       NO_ARGUMENTS    = "Please define a colony and player";
    private static final String       NO_PLAYER       = "Can't find player to add";
    private static final String HAS_A_COLONY          = "Player %s has a colony already.";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public ChangeColonyOwnerCommand(@NotNull final String... parents)
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if(args.length < 2)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_ARGUMENTS));
            return;
        }

        if (!isPlayerOpped(sender))
        {
            return;
        }

        int colonyId = getIthArgument(args, 0, -1);
        if(colonyId == -1)
        {
            final String playerName = args[0];

            if(playerName == null || playerName.isEmpty())
            {
                sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_PLAYER));
                return;
            }
            final EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(playerName);
            final IColony colony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), player.getUniqueID());
            colonyId = colony.getID();
        }

        final Colony colony = ColonyManager.getColony(colonyId);

        if (colony == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(COLONY_NULL, colonyId)));
            return;
        }

        String playerName = null;
        if (args.length >= 2)
        {
            playerName = args[1];
        }

        if(playerName == null || playerName.isEmpty())
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_PLAYER));
            return;
        }

        final EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(playerName);
        if(player == null)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(NO_PLAYER));
            return;
        }

        if(ColonyManager.getIColonyByOwner(sender.getEntityWorld(), player) != null)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString(String.format(HAS_A_COLONY, playerName)));
            return;
        }

        colony.getPermissions().setOwner(player);

        sender.addChatMessage(new TextComponentString(String.format(SUCCESS_MESSAGE, playerName, colonyId)));
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
        return index == 0 || index == 1;
    }
}
