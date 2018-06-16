package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.CommandConstants.*;

/**
 * gives ability to change the colony owner.
 */
public class ChangeColonyOwnerCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final String DESC = "ownerchange";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public ChangeColonyOwnerCommand()
    {
        super();
    }

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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final Colony colony = actionMenuState.getColonyForArgument("colony");
        final EntityPlayer player = actionMenuState.getPlayerForArgument("player");
        executeShared(server, sender, colony, player);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (args.length < 2)
        {
            sender.sendMessage(new TextComponentString(NO_COLONY_OR_PLAYER));
            return;
        }

        int colonyId = getIthArgument(args, 0, -1);
        if (colonyId == -1)
        {
            final String playerName = args[0];
            final EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(playerName);

            final Entity senderEntity = sender.getCommandSenderEntity();

            if (senderEntity == null)
            {
                sender.sendMessage(new TextComponentString(NO_COLONY_OR_PLAYER));
                return;
            }
            else
            {
                if (playerName == null || playerName.isEmpty() || player == null)
                {
                    sender.sendMessage(new TextComponentString(NO_PLAYER));
                    return;
                }
                final IColony colony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), player.getUniqueID());

                if (colony == null)
                {
                    return;
                }

                colonyId = colony.getID();
            }
        }

        String playerName = null;
        if (args.length >= 2)
        {
            playerName = args[1];
        }

        final Colony colony = ColonyManager.getColony(colonyId);
        if (colony == null)
        {
            sender.sendMessage(new TextComponentString(String.format(COLONY_X_NULL, colonyId)));
            return;
        }

        if (playerName == null || playerName.isEmpty())
        {
            sender.sendMessage(new TextComponentString(NO_PLAYER));
            return;
        }

        final EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(playerName);

        executeShared(server, sender, colony, player);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, final Colony colony, final EntityPlayer player) throws CommandException
    {
        if (player == null)
        {
            sender.sendMessage(new TextComponentString(NO_PLAYER));
            return;
        }

        if (!isPlayerOpped(sender))
        {
            return;
        }

        if (ColonyManager.getIColonyByOwner(sender.getEntityWorld(), player) != null)
        {
            sender.sendMessage(new TextComponentString(String.format(HAS_A_COLONY, player.getName())));
            return;
        }

        colony.getPermissions().setOwner(player);

        sender.sendMessage(new TextComponentString(String.format(SUCCESS_MESSAGE_OWNERCHANGE, player.getName(), colony.getID())));
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
