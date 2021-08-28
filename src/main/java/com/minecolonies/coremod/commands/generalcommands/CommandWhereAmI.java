package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

public class CommandWhereAmI implements IMCCommand
{
    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();

        final BlockPos playerPos = new BlockPos(sender.position());
        final IColony colony = IColonyManager.getInstance().getClosestColony(sender.getCommandSenderWorld(), playerPos);

        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage((Player) sender, "com.minecolonies.command.whereami.nocolony");
            return 0;
        }
        final BlockPos center = colony.getCenter();
        final double distance = BlockPosUtil.getDistance2D(center, new BlockPos(playerPos.getX(), center.getY(), playerPos.getZ()));

        if (!IColonyManager.getInstance().isCoordinateInAnyColony(sender.getCommandSenderWorld(), playerPos))
        {
            LanguageHandler.sendPlayerMessage((Player) sender, "com.minecolonies.command.whereami.colonyclose", distance);
            return 0;
        }

        final String colonyName = colony.getName();
        final String id = Integer.toString(colony.getID());

        LanguageHandler.sendPlayerMessage((Player) sender, "com.minecolonies.command.whereami.incolony", colonyName, id, distance);

        return 0;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "whereami";
    }
}
