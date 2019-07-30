package com.minecolonies.coremod.commands.colonycommands.requestsystem;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.RSRESET;

public class RSResetCommand extends AbstractSingleCommand implements IActionCommand
{
    public static final  String DESC            = "reset";
    private static final String SUCCESS_MESSAGE = "After 1.618 Seconds it reinstantiated completely new.";
    private static final String COLONY_NULL     = "Couldn't find colony %d.";
    private static final String COLONY_NOT_FOUND = "Couldn't find colony.";
    private static final String NO_ARGUMENTS    = "Please define a colony";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public RSResetCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public RSResetCommand(@NotNull final String... parents)
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
        final IColony colony = actionMenuState.getColonyForArgument("colony");
        if (colony == null)
        {
            sender.sendMessage(new StringTextComponent(COLONY_NOT_FOUND));
            return;
        }

        executeShared(server, sender, colony);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (args.length == 0)
        {
            sender.sendMessage(new StringTextComponent(NO_ARGUMENTS));
            return;
        }

        final Entity senderEntity = sender.getCommandSenderEntity();

        int colonyId = getIthArgument(args, 0, -1);
        if (colonyId == -1 && senderEntity instanceof PlayerEntity)
        {
            final IColony colony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), ((EntityPlayer) sender).getUniqueID());
            if (colony == null)
            {
                sender.sendMessage(new StringTextComponent(COLONY_NULL));
                return;
            }
            colonyId = colony.getID();
        }

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, server.getWorld(sender.getEntityWorld().provider.getDimension()));

        if (colony == null)
        {
            sender.sendMessage(new StringTextComponent(String.format(COLONY_NULL, colonyId)));
            return;
        }

        executeShared(server, sender, colony);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final IColony colony) throws CommandException
    {
        final Entity senderEntity = sender.getCommandSenderEntity();

        if (senderEntity instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) sender;
            if (!canPlayerUseCommand(player, RSRESET, colony.getID()))
            {
                sender.sendMessage(new StringTextComponent(NOT_PERMITTED));
                return;
            }
        }

        colony.getRequestManager().reset();
        sender.sendMessage(new StringTextComponent(String.format(SUCCESS_MESSAGE, colony.getID())));
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
        return false;
    }
}
