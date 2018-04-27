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
import java.util.UUID;

/**
 * List all colonies.
 */
public class RefreshColonyCommand extends AbstractSingleCommand implements IActionCommand
{
    public static final  String DESC                                = "refresh";
    private static final String NO_COLONY_WITH_ID_FOUND_MESSAGE     = "Colony with ID %d not found.";
    private static final String NO_COLONY_FOUND_MESSAGE             = "Colony not found.";
    private static final String NO_COLONY_WITH_PLAYER_FOUND_MESSAGE = "Colony with mayor %s not found.";
    private static final String REFRESH                             = "Refresh successful!";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public RefreshColonyCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public RefreshColonyCommand(@NotNull final String... parents)
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        Colony colony = actionMenuState.getColonyForArgument("colony");
        if (null == colony)
        {
            final EntityPlayer player = actionMenuState.getPlayerForArgument("player");
            if (null != player)
            {
                IColony iColony = ColonyManager.getIColonyByOwner(server.getEntityWorld(), player);
                if (null == iColony)
                {
                    if (sender instanceof EntityPlayer)
                    {
                        final Entity senderEntity = sender.getCommandSenderEntity();
                        if (senderEntity != null)
                        {
                            final UUID mayorID = senderEntity.getUniqueID();
                            iColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID);
                        }
                    }
                }

                if (null != iColony)
                {
                    colony = ColonyManager.getColony(iColony.getID());
                }
            }
        }

        if (colony == null)
        {
            sender.sendMessage(new TextComponentString(NO_COLONY_FOUND_MESSAGE));
            return;
        }

        executeShared(server, sender, colony);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        final int colonyId;
        colonyId = getIthArgument(args, 0, -1);
        IColony tempColony = ColonyManager.getColony(colonyId);

        if (colonyId == -1 && args.length >= 1)
        {
            final EntityPlayer player = server.getEntityWorld().getPlayerEntityByName(args[0]);
            if (player != null)
            {
                tempColony = ColonyManager.getIColonyByOwner(server.getEntityWorld(), player);
            }
        }

        if (sender instanceof EntityPlayer)
        {
            final Entity senderEntity = sender.getCommandSenderEntity();
            if (senderEntity == null)
            {
                return;
            }

            final UUID mayorID = senderEntity.getUniqueID();
            if (tempColony == null)
            {
                tempColony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), mayorID);
            }
        }

        if (tempColony == null)
        {
            if (colonyId == -1 && args.length != 0)
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_WITH_PLAYER_FOUND_MESSAGE, args[0])));
            }
            else
            {
                sender.sendMessage(new TextComponentString(String.format(NO_COLONY_WITH_ID_FOUND_MESSAGE, colonyId)));
            }
            return;
        }

        final Colony colony = ColonyManager.getColony(tempColony.getID());
        if (colony == null)
        {
            sender.sendMessage(new TextComponentString(NO_COLONY_FOUND_MESSAGE));
            return;
        }

        executeShared(server, sender, colony);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final Colony colony)
    {
        if (sender instanceof EntityPlayer)
        {
            final EntityPlayer senderPlayer = (EntityPlayer) sender.getCommandSenderEntity();
            if (!canPlayerUseCommand(senderPlayer, Commands.REFRESH_COLONY, colony.getID()))
            {
                sender.sendMessage(new TextComponentString(NOT_PERMITTED));
                return;
            }
        }

        sender.sendMessage(new TextComponentString(REFRESH));
        colony.getPermissions().restoreOwnerIfNull();
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
