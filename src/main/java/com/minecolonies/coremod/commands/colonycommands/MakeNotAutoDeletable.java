package com.minecolonies.coremod.commands.colonycommands;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionArgument;
import com.minecolonies.coremod.commands.IActionCommand;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class MakeNotAutoDeletable extends AbstractSingleCommand implements IActionCommand
{
    public static final  String DESC                       = "deletable";
    private static final String NO_COLONY_FOUND_MESSAGE_ID = "Colony with ID %d not found.";
    private static final String MARKED                     = "Marking succesful!";
    private static final String NOT_ENOUGH_ARGUMENTS       = "You must have 2 Arguments: <ColonyId> <true|false> ";

    private static final int NUMBER_OR_ARGS_REQUIRED = 2;

    public MakeNotAutoDeletable()
    {
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public MakeNotAutoDeletable(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <true|false>";
    }

    @Override
    public boolean canRankUseCommand(@NotNull final Colony colony, @NotNull final EntityPlayer player)
    {
        return false;
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final List<ActionArgument> actionArgumentList,
            @NotNull final Map<String, Object> argumentValueByActionArgumentNameMap) throws CommandException
    {
        Colony colony = null;
        final Object colonyObject = argumentValueByActionArgumentNameMap.get("colony");
        if (null != colonyObject)
        {
            colony = (Colony) colonyObject;
        }

        boolean canBeDeleted = false;
        final Object canBeDeletedObject = argumentValueByActionArgumentNameMap.get("canBeDeleted");
        if (null != canBeDeletedObject)
        {
            final Boolean canBeDeletedObjectBoolean = (Boolean) canBeDeletedObject;
            canBeDeleted = canBeDeletedObjectBoolean.booleanValue();
        }

        executeShared(server, sender, colony, canBeDeleted);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (args.length < NUMBER_OR_ARGS_REQUIRED)
        {
            sender.sendMessage(new TextComponentString(NOT_ENOUGH_ARGUMENTS));
            return;
        }

        final int colonyId;
        colonyId = Integer.parseInt(args[0]);
        final Colony colony = ColonyManager.getColony(colonyId);

        if (colony == null)
        {
            sender.sendMessage(new TextComponentString(String.format(NO_COLONY_FOUND_MESSAGE_ID, colonyId)));
            return;
        }

        final boolean canBeDeleted;
        canBeDeleted = Boolean.parseBoolean(args[1]);

        executeShared(server, sender, colony, canBeDeleted);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @Nullable final Colony colony, final boolean canBeDeleted)
    {
        if (sender instanceof EntityPlayer && !isPlayerOpped(sender))
        {
            sender.sendMessage(new TextComponentString("Must be OP to use command"));
            return;
        }
        else if (sender instanceof TileEntity)
        {
            return;
        }

        sender.sendMessage(new TextComponentString(MARKED));
        colony.setCanBeAutoDeleted(canBeDeleted);
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
