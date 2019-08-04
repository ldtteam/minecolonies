package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.CommandConstants.*;
import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.DELETECOLONY;

/**
 * List all colonies.
 */
public class DeleteColonyCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final String DESC                                           = "delete";
    public static final String DELETE_COLONY_CONFIRM_DELETE_COMMAND_SUGGESTED = "/mc colony delete colony: %d canDestroy: %s confirmDelete: true";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public DeleteColonyCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public DeleteColonyCommand(@NotNull final String... parents)
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
    public boolean canRankUseCommand(@NotNull final IColony colony, @NotNull final PlayerEntity player)
    {
        return colony.getPermissions().getRank(player).equals(Rank.OWNER);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final IColony colony = actionMenuState.getColonyForArgument("colony");
        final boolean canDestroy = actionMenuState.getBooleanValueForArgument("canDestroy", true);
        final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", false);

        if (colony == null)
        {
            final String noColonyFoundMessage = String.format(NO_COLONY_MESSAGE);
            sender.sendMessage(new StringTextComponent(noColonyFoundMessage));
            return;
        }

        executeShared(server, sender, colony, canDestroy, confirmDelete);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        final int colonyId;
        boolean canDestroy = true;
        boolean confirmDelete = false;
        if (args.length == 0)
        {
            IColony colony = null;
            if (sender instanceof EntityPlayer)
            {
                colony = IColonyManager.getInstance().getIColonyByOwner(CompatibilityUtils.getWorldFromEntity((EntityPlayer) sender), (EntityPlayer) sender);
            }

            if (colony == null)
            {
                sender.sendMessage(new StringTextComponent(NO_COLONY_MESSAGE));
                return;
            }
            colonyId = colony.getID();
        }
        else
        {
            colonyId = getIthArgument(args, 0, -1);
            if (args.length > 1)
            {
                canDestroy = Boolean.parseBoolean(args[1]);
            }

            if (args.length > 2)
            {
                confirmDelete = Boolean.parseBoolean(args[2]);
            }
        }

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, server.getWorld(sender.getEntityWorld().provider.getDimension()));
        if (colony == null)
        {
            final String noColonyFoundMessage = String.format(COLONY_X_NULL, colonyId);
            sender.sendMessage(new StringTextComponent(noColonyFoundMessage));
            return;
        }

        executeShared(server, sender, colony, canDestroy, confirmDelete);
    }

    private void executeShared(
            @NotNull final MinecraftServer server, @NotNull final ICommandSender sender, final IColony colony, final boolean canDestroy,
            final boolean confirmDelete) throws CommandException
    {

        if (!confirmDelete)
        {
            final ITextComponent deleteButton = new TextComponentTranslation("tile.blockHutTownHall.deleteMessageLink")
                    .setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format(DELETE_COLONY_CONFIRM_DELETE_COMMAND_SUGGESTED,
                                            colony.getID(), canDestroy ? "true" : "false")
                            )));
            sender.sendMessage(new StringTextComponent("Click [DELETE] to confirm the deletion of colony: " + colony.getID()));
            sender.sendMessage(deleteButton);
            return;
        }


        final Entity senderEntity = sender.getCommandSenderEntity();

        if (senderEntity instanceof EntityPlayer)
        {
            final PlayerEntity player = (EntityPlayer) sender;
            if (!canPlayerUseCommand(player, DELETECOLONY, colony.getID()))
            {
                sender.sendMessage(new StringTextComponent(NOT_PERMITTED));
                return;
            }
        }
        final boolean shouldDestroy = canDestroy;
        // TODO: pass in sender and notify when the delete task finishes.
        server.addScheduledTask(() -> IColonyManager.getInstance().deleteColonyByWorld(colony.getID(), shouldDestroy, sender.getEntityWorld()));
        sender.sendMessage(new StringTextComponent(DELETE_COLONY_TASK_SCHEDULED));
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
