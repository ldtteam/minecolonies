package com.minecolonies.coremod.commands.commandTypes;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.MessageUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

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


        if (!(context.getSource().getEntity() instanceof final Player sender))
        {
            return false;
        }

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            MessageUtils.format(COMMAND_COLONY_ID_NOT_FOUND, colonyID).sendTo(sender);
            return false;
        }

        // Check colony permissions
        return IMCCommand.isPlayerOped(sender) || colony.getPermissions().getRank(sender).isColonyManager();
    }
}
