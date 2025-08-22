package com.minecolonies.core.commands.commandTypes;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.commands.arguments.ColonyIdArgument;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Commands which may be used by officers and owners or OP only.
 */
public interface IMCColonyOfficerCommand extends IMCCommand
{
    /**
     * Executes pre-checks before issuing the command. Checks for the senders type and OP rights.
     */
    @Override
    default boolean checkPreCondition(final CommandContext<CommandSourceStack> context)
    {
        if (context.getSource().hasPermission(OP_PERM_LEVEL))
        {
            return true;
        }


        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof Player))
        {
            return false;
        }

        final IColony colony;
        try
        {
            colony = ColonyIdArgument.getColony(context, COLONYID_ARG);
        }
        catch (CommandRuntimeException e)
        {
            return false;
        }

        // Check colony permissions
        return IMCCommand.isPlayerOped((Player) sender) || colony.getPermissions().getRank((Player) sender).isColonyManager();
    }
}
