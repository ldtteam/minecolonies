package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.coremod.colony.ColonyManager;
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

/**
 * Created by asie on 2/16/17.
 */
public class WhereAmICommand extends AbstractSingleCommand
{
    /**
     * Command description.
     */
    public static final String DESC = "whereami";

    /**
     * Position description string.
     */
    public static final String NONE_CLOSE = "You're not inside any colony, the closest colony is approx %.2f blocks away.";

    /**
     * Position description string.
     */
    public static final String INSIDE = "You're inside colony %s with id: %s, the colony center is approx %.2f blocks away.";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public WhereAmICommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (!(sender instanceof EntityPlayer))
        {
            Log.getLogger().info("In the console...");
            return;
        }

        final BlockPos playerPos = sender.getPosition();
        final IColony colony = ColonyManager.getClosestColony(server.getEntityWorld(), playerPos);
        final BlockPos center = colony.getCenter();
        final double distance = BlockPosUtil.getDistanceSquared(center, new BlockPos(playerPos.getX(), center.getY(), playerPos.getZ()));

        if (distance >= MathUtils.square(Configurations.Gameplay.workingRangeTownHall + (double) Configurations.Gameplay.townHallPadding))
        {
            sender.sendMessage(new TextComponentString(String.format(NONE_CLOSE, Math.sqrt(distance))));
            return;
        }

        final String colonyName = colony.getName();
        final String id = Integer.toString(colony.getID());

        sender.sendMessage(new TextComponentString(String.format(INSIDE, colonyName, id, Math.sqrt(distance))));
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
