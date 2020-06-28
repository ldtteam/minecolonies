package com.minecolonies.coremod.commands.commandTypes;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Rank;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Commands which may be used by officers and owners or OP only.
 */
public interface IMCColonyOfficerCommand extends IMCCommand
{
    /**
     * Executes pre-checks before issuing the command.
     * Checks for the senders type and OP rights.
     */
    @Override
    default boolean checkPreCondition(final CommandContext<CommandSource> context)
    {
        if (context.getSource().hasPermissionLevel(OP_PERM_LEVEL))
        {
            return true;
        }


        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof PlayerEntity))
        {
            return false;
        }

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getWorld().dimension.getType().getId());
        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.colonyidnotfound", colonyID);
            return false;
        }

        // Check colony permissions
        if (IMCCommand.isPlayerOped((PlayerEntity) sender) || colony.getPermissions().getRank((PlayerEntity) sender) == Rank.OFFICER
              || colony.getPermissions().getRank((PlayerEntity) sender) == Rank.OWNER)
        {
            return true;
        }

        return false;
    }
}
