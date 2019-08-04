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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.CommandConstants.COLONY_X_NULL;
import static com.minecolonies.api.util.constant.CommandConstants.NO_COLONY_MESSAGE;

/**
 * List all colonies.
 */
public class SetHappinessLevelColonyCommand extends AbstractSingleCommand implements IActionCommand
{

    public static final  String DESC                                           = "shl";
    private static final String DELETE_COLONY_CONFIRM_DELETE_COMMAND_SUGGESTED = "/mc colony shl colony: %d level: %d";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public SetHappinessLevelColonyCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public SetHappinessLevelColonyCommand(@NotNull final String... parents)
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
        final Optional<Double> level = Optional.ofNullable(actionMenuState.getDoubleForArgument("level"));

        if (colony == null)
        {
            final String noColonyFoundMessage = String.format(NO_COLONY_MESSAGE);
            sender.sendMessage(new StringTextComponent(noColonyFoundMessage));
            return;
        }

        executeShared(server, sender, colony, level);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        final int colonyId;
        Optional<Double> level = Optional.empty();
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
                level = Optional.of(Double.parseDouble(args[1]));
            }
        }

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, server.getWorld(sender.getEntityWorld().provider.getDimension()));
        if (colony == null)
        {
            final String noColonyFoundMessage = String.format(COLONY_X_NULL, colonyId);
            sender.sendMessage(new StringTextComponent(noColonyFoundMessage));
            return;
        }

        executeShared(server, sender, colony, level);
    }

    private void executeShared(
            @NotNull final MinecraftServer server, @NotNull final ICommandSender sender, final IColony colony, final Optional<Double> level) throws CommandException
    {
        colony.getColonyHappinessManager().setLockedHappinessModifier(level);
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
