package com.minecolonies.coremod.commands.generalcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class CheckForAutoDeletesCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final String DESC = "check";
    private static final String COMMAND_CHECK_FOR_AUTODELETES = "/mc check confirmDelete: true";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public CheckForAutoDeletesCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public CheckForAutoDeletesCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public boolean canRankUseCommand(@NotNull final Colony colony, @NotNull final EntityPlayer player)
    {
        return false;
    }

    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", false);
        executeShared(server, sender, confirmDelete);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        boolean confirmDelete = false;
        if (args.length == 2)
        {
            if ("confirmDelete:".equalsIgnoreCase(args[0]))
            {
                if ("true".equalsIgnoreCase(args[1]))
                {
                    confirmDelete = true;
                }
            }
        }
        executeShared(server, sender, confirmDelete);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, final boolean confirmDelete) throws CommandException
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

        final List<Colony> colonies = ColonyManager.getColonies();

        final List<Colony> coloniesToDelete = new ArrayList<>();

        for (int index = 0; colonies.size() - 1 >= index; index++)
        {
            final Colony colony = colonies.get(index);

            if (colony.canBeAutoDeleted() && Configurations.gameplay.autoDeleteColoniesInHours != 0
                  && colony.getLastContactInHours() >= Configurations.gameplay.autoDeleteColoniesInHours)
            {
                coloniesToDelete.add(colony);
            }
        }

        if (confirmDelete)
        {
            sender.sendMessage(new TextComponentString("Successful"));
            for (final Colony col : coloniesToDelete)
            {
                server.addScheduledTask(() -> ColonyManager.deleteColony(col.getID(), Configurations.gameplay.autoDestroyColonyBlocks));
            }
        }
        else
        {
            final ITextComponent deleteButton = new TextComponentString("[DELETE]").setStyle(new Style().setBold(true).setColor(TextFormatting.GOLD).setClickEvent(
              new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND_CHECK_FOR_AUTODELETES)
            ));
            sender.sendMessage(new TextComponentString("There are: " + coloniesToDelete.size() + " of a total of " + colonies.size() + " to delete."));
            sender.sendMessage(new TextComponentString("Click [DELETE] to confirm"));
            sender.sendMessage(deleteButton);
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
