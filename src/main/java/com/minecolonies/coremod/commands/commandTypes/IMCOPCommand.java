package com.minecolonies.coremod.commands.commandTypes;

import com.minecolonies.api.util.MessageUtils;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_REQUIRES_OP;

/**
 * Interface for commands requiring OP rights to execute.
 */
public interface IMCOPCommand extends IMCCommand
{
    /**
     * Executes pre-checks before issuing the command. Checks for the senders type and OP rights.
     */
    @Override
    default boolean checkPreCondition(final CommandContext<CommandSource> context)
    {
        if (context.getSource().hasPermission(OP_PERM_LEVEL))
        {
            return true;
        }

        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof PlayerEntity))
        {
            return false;
        }

        if (!IMCCommand.isPlayerOped((PlayerEntity) sender))
        {
            MessageUtils.format(COMMAND_REQUIRES_OP).sendTo((PlayerEntity) sender);
            return false;
        }
        return true;
    }
}
