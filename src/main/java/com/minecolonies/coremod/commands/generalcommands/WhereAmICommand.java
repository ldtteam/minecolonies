package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by asie on 2/16/17.
 */
public class WhereAmICommand extends AbstractSingleCommand implements IActionCommand
{
    /**
     * Command description.
     */
    public static final String DESC = "whereami";

    /**
     * Position description string.
     */
    public static final String NONE = "No colony close at all.";

    /**
     * Position description string.
     */
    public static final String NONE_CLOSE = "You're not inside any colony, the closest colony is approx %.2f blocks away.";

    /**
     * Position description string.
     */
    public static final String INSIDE = "You're inside colony %s with id: %s, the colony center is approx %.2f blocks away.";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public WhereAmICommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public WhereAmICommand(@NotNull final String... parents)
    {
        super(parents);
    }

    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        executeShared(server, sender);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        executeShared(server, sender);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender) throws CommandException
    {
        if (!(sender instanceof PlayerEntity))
        {
            Log.getLogger().info("In the console...");
            return;
        }

        final BlockPos playerPos = sender.getPosition();
        final IColony colony = ColonyManager.getClosestColony(server.getEntityWorld(), playerPos);

        if (colony == null)
        {
            sender.sendMessage(new StringTextComponent(NONE));
            return;
        }
        final BlockPos center = colony.getCenter();
        final double distance = BlockPosUtil.getDistanceSquared(center, new BlockPos(playerPos.getX(), center.getY(), playerPos.getZ()));

        if (!ColonyManager.isCoordinateInAnyColony(sender.getEntityWorld(), playerPos))
        {
            sender.sendMessage(new StringTextComponent(String.format(NONE_CLOSE, Math.sqrt(distance))));
            return;
        }

        final String colonyName = colony.getName();
        final String id = Integer.toString(colony.getID());

        sender.sendMessage(new StringTextComponent(String.format(INSIDE, colonyName, id, Math.sqrt(distance))));
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
