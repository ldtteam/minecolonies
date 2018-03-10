package com.minecolonies.coremod.commands.colonycommands;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionArgument;
import com.minecolonies.coremod.commands.IActionCommand;
import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.FakePlayer;

/**
 * gives ability to change the colony owner.
 */
public class ChangeColonyOwnerCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final  String DESC            = "ownerchange";
    private static final String SUCCESS_MESSAGE = "Succesfully switched Owner %s to colony %d";
    private static final String COLONY_NULL     = "Couldn't find colony %d.";
    private static final String NO_ARGUMENTS    = "Please define a colony and player";
    private static final String NO_PLAYER       = "Can't find player to add";
    private static final String HAS_A_COLONY    = "Player %s has a colony already.";

    public ChangeColonyOwnerCommand()
    {
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final List<ActionArgument> actionArgumentList,
            @NotNull final Map<String, Object> argumentValueByActionArgumentNameMap) throws CommandException
    {
        Colony colony = null;
        EntityPlayer player = null;
        for (final ActionArgument actionArgument : actionArgumentList)
        {
            final Object object = argumentValueByActionArgumentNameMap.get(actionArgument.getName());
            switch (actionArgument.getType()) {
                case Player:
                    player = (EntityPlayer) object;
                    break;
                case Colony:
                    colony = (Colony) object;
                    break;
                case Citizen:
                    break;
                case CoordinateX:
                case CoordinateY:
                case CoordinateZ:
                    break;
                default:
                    break;
            }
        }

        executeShared(server, sender, colony, player);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (args.length < 2)
        {
            sender.sendMessage(new TextComponentString(NO_ARGUMENTS));
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
                server.sendMessage(new TextComponentString(NO_ARGUMENTS));
                return;
            }
            else
            {
                if (playerName == null || playerName.isEmpty() || player == null)
                {
                    senderEntity.sendMessage(new TextComponentString(NO_PLAYER));
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
            sender.sendMessage(new TextComponentString(String.format(COLONY_NULL, colonyId)));
            return;
        }

        if (playerName == null || playerName.isEmpty())
        {
            sender.sendMessage(new TextComponentString(NO_PLAYER));
            return;
        }

        EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(playerName);

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

        sender.sendMessage(new TextComponentString(String.format(SUCCESS_MESSAGE, player.getName(), colony.getID())));
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
